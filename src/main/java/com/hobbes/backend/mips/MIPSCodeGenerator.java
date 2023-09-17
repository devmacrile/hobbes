package com.hobbes.backend.mips;

import com.hobbes.backend.allocation.RegisterAllocator;
import com.hobbes.backend.allocation.TempAllocation;
import com.hobbes.backend.mips.instructions.*;
import com.hobbes.backend.mips.instructions.fp.*;
import com.hobbes.backend.mips.registers.MIPSRegister;
import com.hobbes.backend.mips.registers.MIPSRegisters;
import com.hobbes.ir.*;
import com.hobbes.ir.ops.*;
import com.hobbes.symbol.PrimitiveTypes;
import com.hobbes.symbol.TypeSymbol;

import java.util.*;

public class MIPSCodeGenerator {

    private MIPSData staticData;
    private RegisterAllocator allocator;
    private IRFunction function;
    private StackFrame stackFrame;

    public MIPSCodeGenerator(MIPSData staticData, RegisterAllocator allocator, IRFunction function, StackFrame stackFrame) {
        this.staticData = staticData;
        this.allocator = allocator;
        this.function = function;
        this.stackFrame = stackFrame;
    }

    public List<MIPSInstruction> generate(List<IRStatement> statements) {

        List<MIPSInstruction> instructions = new ArrayList<>();

        // we will load all of our allocated registers at once up front
        // so if our first statement is a label (esp. in block by block generation)
        // we want the load statements to be within the label scope
        int startingIndex = (statements.get(0) instanceof IRLocationIdentifier) ? 1 : 0;
        if (startingIndex == 1)
            instructions.addAll(translate(statements.get(0), new HashMap<>()));

        // load any variables allocated to registers by our allocator
        for (IRValue value : allocator.getAllocatedValues())
            instructions.add(getLoadInstruction(allocator.getAllocatedRegister(value), value));

        for (int statementIndex = startingIndex; statementIndex < statements.size(); statementIndex++) {

            IRStatement statement = statements.get(statementIndex);

            if (statementIndex == statements.size() - 1)
                // if our last statement is control flow, we want to store our allocated
                // registers back to memory prior to the statement
                if (shouldStorePreemptively(statement))
                    for (IRValue value : getDefinedVariables(statements))
                        if (allocator.isAllocated(value))
                            instructions.add(getStoreInstruction(allocator.getAllocatedRegister(value), (IRVariable) value));

            Map<IRValue, MIPSRegister> registerMap = new HashMap<>();
            Map<IRVariable, MIPSRegister> storeBacks = new HashMap<>();
            Map<IRVariable, MIPSRegister> loadBacks = new HashMap<>();

            if (statement instanceof IROperation operation) {
                // get operand registers
                for (int i = 0; i < operation.getOperands().size(); i++) {
                    IRValue value = operation.getOperands().get(i);
                    if (noRegisterNeeded(operation, value))
                        continue;
                    if (allocator.isAllocated(value))
                        registerMap.put(value, allocator.getAllocatedRegister(value));
                    else {
                        TempAllocation tempAllocation = allocator.assignTempRegister(value);
                        if (tempAllocation.isSpill()) {
                            // insert spill code
                            IRVariable owner = (IRVariable) tempAllocation.getOwner().get();
                            instructions.add(getStoreInstruction(tempAllocation.getRegister(), owner));
                            loadBacks.put(owner, tempAllocation.getRegister());
                        }
                        instructions.add(getLoadInstruction(tempAllocation.getRegister(), value));
                        registerMap.put(value, tempAllocation.getRegister());
                    }

                    // may need extra instructions to move any ints that
                    // are used in float statements into float registers
                    if ((!isCallStatement(statement) && isFloatOperation(statement) && !isFloat(value) && !isFloatArrayIndex(statement, value)) ||
                            callArgumentDoesNotMatchType(statement, value, i)) {
                        TempAllocation tempAllocation = allocator.assignTempFRegister(value);
                        instructions.add(new MIPSMtc1F(registerMap.get(value), tempAllocation.getRegister()));
                        instructions.add(new MIPSIntToFloat(tempAllocation.getRegister(), tempAllocation.getRegister()));
                        registerMap.put(value, tempAllocation.getRegister());
                    }
                }

                // get target register
                Optional<IRVariable> optDefined = operation.getDestinationVariable();
                if (!isStore(statement) && optDefined.isPresent()) {
                    IRVariable defined = optDefined.get();
                    if (allocator.isAllocated(defined))
                        registerMap.put(defined, allocator.getAllocatedRegister(defined));
                    else if (!allocator.isTempAllocated(defined)) {
                        TempAllocation tempAllocation = allocator.assignTempRegister(defined);
                        if (tempAllocation.isSpill()) {
                            // insert spill code
                            IRVariable owner = (IRVariable) tempAllocation.getOwner().get();
                            instructions.add(getStoreInstruction(tempAllocation.getRegister(), owner));
                            loadBacks.put(owner, tempAllocation.getRegister());
                        }
                        registerMap.put(defined, tempAllocation.getRegister());
                    }
                    if (allocator.isTempAllocated(defined))
                        storeBacks.put(defined, allocator.getTempAllocatedRegister(defined));
                }
            }

            // translate instruction
            if (statement instanceof IROperation)
                instructions.add(new MIPSComment(statement.emit()));
            instructions.addAll(translate(statement, registerMap));

            // process any 'storebacks' (viz. reg back to memory), 'loadbacks' (memory back to assigned register)
            for (IRVariable variable : storeBacks.keySet())
                instructions.add(getStoreInstruction(storeBacks.get(variable), variable));
            for (IRVariable variable : loadBacks.keySet())
                instructions.add(getLoadInstruction(loadBacks.get(variable), variable));

            instructions.add(new MIPSEmpty());
            allocator.clearTempAllocations();
        }

        // if last statement is not control flow, then we have not yet
        // stored our registers back to memory and need to do so here
        if (!shouldStorePreemptively(statements.get(statements.size() - 1)))
            for (IRValue value : getDefinedVariables(statements))
                if (allocator.isAllocated(value))
                    instructions.add(getStoreInstruction(allocator.getAllocatedRegister(value), (IRVariable) value));
        return instructions;
    }

    private List<MIPSInstruction> translate(IRStatement statement, Map<IRValue, MIPSRegister> registerMap) {
        if (statement instanceof IRBinaryOperation binaryOperation) {
            return Collections.singletonList(translateBinaryOperation(binaryOperation, registerMap));
        }
        if (statement instanceof IRBranchOperation branchOperation) {
            if (isFloatOperation(branchOperation))
                return translateFloatBranchOperation(branchOperation, registerMap);
            return translateBranchOperation(branchOperation, registerMap);
        }
        if (statement instanceof IRArrayAssign arrayAssign) {
            return translateArrayInitialization(arrayAssign, registerMap);
        }
        if (statement instanceof IRArrayLoad arrayLoad) {
            return translateArrayLoad(arrayLoad, registerMap);
        }
        if (statement instanceof IRArrayStore arrayStore) {
            return translateArrayStore(arrayStore, registerMap);
        }
        if (statement instanceof IRAssign assign) {
            if (isArray(assign.getRight()))
                return translateArrayToArrayAssignment(assign, registerMap);
            MIPSRegister destination = registerMap.get(assign.getDestinationVariable().get());
            if (assign.getRight() instanceof IRConstant) {
                if (isFloatOperation(statement))
                    return Collections.singletonList(new MIPSLoadIF(destination, Float.parseFloat(assign.getRight().getReference())));
                return Collections.singletonList(new MIPSLoadI(destination, Integer.parseInt(assign.getRight().getReference())));
            }
            MIPSRegister source = registerMap.get(assign.getRight());
            if (isFloatOperation(statement))
                return Collections.singletonList(new MIPSMoveF(destination, source));
            return Collections.singletonList(new MIPSMove(destination, source));
        }
        if (statement instanceof IRCall) {
            return translateCallOperation((IRCall) statement, registerMap);
        }
        if (statement instanceof IRCallR) {
            return translateCallROperation((IRCallR) statement, registerMap);
        }
        if (statement instanceof IRJump jump) {
            return Collections.singletonList(new MIPSJ(new MIPSLabel(jump.getLabel().getName())));
        }
        if (statement instanceof IRReturn irReturn) {
            List<MIPSInstruction> instructions = new ArrayList<>();
            if (irReturn.hasReturnValue()) {
                if (isFloatOperation(statement))
                    instructions.add(new MIPSMoveF(MIPSRegisters.F0, registerMap.get(irReturn.getReturnValue())));
                else
                    instructions.add(new MIPSMove(MIPSRegisters.V0, registerMap.get(irReturn.getReturnValue())));
            }
            instructions.add(new MIPSJ(MIPSFunction.getEpilogueLabel(function.getName())));
            return instructions;
        }
        if (statement instanceof IRLocationIdentifier location)
            return Collections.singletonList(new MIPSLabel(location.getName()));
        return new ArrayList<>();
    }

    private MIPSInstruction translateBinaryOperation(IRBinaryOperation binaryOperation, Map<IRValue, MIPSRegister> registerMap) {
        assert binaryOperation.getDestinationVariable().isPresent();
        MIPSRegister destination = registerMap.get(binaryOperation.getDestinationVariable().get());
        MIPSRegister left = registerMap.get(binaryOperation.getLeft());
        if (binaryOperation instanceof IRAdd add) {
            if (isFloatOperation(binaryOperation)) {
                return new MIPSAddF(destination, left, registerMap.get(add.getRight()));
            } else {
                assert !(add.getLeft() instanceof IRConstant);
                if (add.getRight() instanceof IRConstant)
                    return new MIPSAddI(destination, left, (IRConstant) add.getRight());
                else
                    return new MIPSAdd(destination, left, registerMap.get(add.getRight()));
            }
        }
        MIPSRegister right = registerMap.get(binaryOperation.getRight());
        if (binaryOperation instanceof IRSub) {
            if (isFloatOperation(binaryOperation))
                return new MIPSSubF(destination, left, right);
            return new MIPSSub(destination, left, right);
        }
        if (binaryOperation instanceof IRMult) {
            if (isFloatOperation(binaryOperation))
                return new MIPSMulF(destination, left, right);
            return new MIPSMul(destination, left, right);
        }
        if (binaryOperation instanceof IRDiv) {
            if (isFloatOperation(binaryOperation))
                return new MIPSDivF(destination, left, right);
            return new MIPSDiv(destination, left, right);
        }
        if (binaryOperation instanceof IRAnd)
            return new MIPSAnd(destination, left, right);
        if (binaryOperation instanceof IROr)
            return new MIPSOr(destination, left, right);
        // Panic!
        assert false;
        return null;
    }

    private List<MIPSInstruction> translateBranchOperation(IRBranchOperation branchOperation, Map<IRValue, MIPSRegister> registerMap) {
        List<MIPSInstruction> instructions = new ArrayList<>();
        MIPSRegister left = registerMap.get(branchOperation.getLeft());
        MIPSRegister right = registerMap.get(branchOperation.getRight());
        MIPSLabel label = new MIPSLabel(branchOperation.getLabel().getName());
        if (branchOperation instanceof IRBranchEqual)
            instructions.add(new MIPSBeq(left, right, label));
        if (branchOperation instanceof IRBranchNotEqual)
            instructions.add(new MIPSBne(left, right, label));
        if (branchOperation instanceof IRBranchLess)
            instructions.add(new MIPSBlt(left, right, label));
        if (branchOperation instanceof IRBranchLessEqual)
            instructions.add(new MIPSBle(left, right, label));
        if (branchOperation instanceof IRBranchGreat)
            instructions.add(new MIPSBgt(left, right, label));
        if (branchOperation instanceof IRBranchGreatEqual)
            instructions.add(new MIPSBge(left, right, label));
        return instructions;
    }

    private List<MIPSInstruction> translateFloatBranchOperation(IRBranchOperation branchOperation, Map<IRValue, MIPSRegister> registerMap) {
        MIPSRegister left = registerMap.get(branchOperation.getLeft());
        MIPSRegister right = registerMap.get(branchOperation.getRight());
        MIPSLabel label = new MIPSLabel(branchOperation.getLabel().getName());
        if (branchOperation instanceof IRBranchEqual)
            return Arrays.asList(
                    new MIPSCompareEqualF(left, right),
                    new MIPSBranchTrueF(label)
            );
        if (branchOperation instanceof IRBranchNotEqual)
            return Arrays.asList(
                    new MIPSCompareEqualF(left, right),
                    new MIPSBranchFalseF(label)
            );
        if (branchOperation instanceof IRBranchLess)
            return Arrays.asList(
                    new MIPSCompareLessF(left, right),
                    new MIPSBranchTrueF(label)
            );
        if (branchOperation instanceof IRBranchLessEqual)
            return Arrays.asList(
                    new MIPSCompareLessEqualF(left, right),
                    new MIPSBranchTrueF(label)
            );
        if (branchOperation instanceof IRBranchGreat)
            return Arrays.asList(
                    new MIPSCompareLessEqualF(left, right),
                    new MIPSBranchFalseF(label)
            );
        if (branchOperation instanceof IRBranchGreatEqual)
            return Arrays.asList(
                    new MIPSCompareLessF(left, right),
                    new MIPSBranchFalseF(label)
            );
        return new ArrayList<>();
    }

    private List<MIPSInstruction> translateArrayInitialization(IRArrayAssign arrayAssign, Map<IRValue, MIPSRegister> registerMap) {
        List<MIPSInstruction> instructions = new ArrayList<>();
        for (int i = 0; i < arrayAssign.getArrayLength(); i++) {
            int arrayOffset;
            MIPSRegister baseRegister;
            int indexOffset = i * MIPSConstants.WORD_SIZE;
            if (isStatic(arrayAssign.getArray())) {
                arrayOffset = staticData.getGpOffset(arrayAssign.getArray()) + indexOffset;
                baseRegister = MIPSRegisters.GP;
            } else {
                arrayOffset = stackFrame.getVariableOffset(arrayAssign.getArray()) + indexOffset;
                baseRegister = MIPSRegisters.SP;
            }
            if (isFloat(arrayAssign.getArray()))
                instructions.add(new MIPSStoreOffsetF(registerMap.get(arrayAssign.getValue()), arrayOffset, baseRegister));
            else
                instructions.add(new MIPSStoreOffset(registerMap.get(arrayAssign.getValue()), arrayOffset, baseRegister));
        }
        return instructions;
    }

    private List<MIPSInstruction> translateArrayToArrayAssignment(IRAssign assign, Map<IRValue, MIPSRegister> registerMap) {
        List<MIPSInstruction> instructions = new ArrayList<>();
        IRVariable leftArray = assign.getDestinationVariable().get();
        IRVariable rightArray = (IRVariable) assign.getRight();
        for (int i = 0; i < leftArray.getType().getArrayLength(); i++) {
            int offset;
            MIPSRegister baseRegister;
            int indexOffset = i * MIPSConstants.WORD_SIZE;
            // load element of right array into register
            if (isStatic(rightArray)) {
                offset = staticData.getGpOffset(rightArray) + indexOffset;
                baseRegister = MIPSRegisters.GP;
            } else {
                offset = stackFrame.getVariableOffset(rightArray) + indexOffset;
                baseRegister = MIPSRegisters.SP;
            }
            if (isFloat(rightArray))
                instructions.add(new MIPSLoadOffsetF(registerMap.get(rightArray), offset, baseRegister));
            else
                instructions.add(new MIPSLoadOffset(registerMap.get(rightArray), offset, baseRegister));
            // store register into element of left array
            if (isStatic(leftArray)) {
                offset = staticData.getGpOffset(leftArray) + indexOffset;
                baseRegister = MIPSRegisters.GP;
            } else {
                offset = stackFrame.getVariableOffset(leftArray) + indexOffset;
                baseRegister = MIPSRegisters.SP;
            }
            if (isFloat(leftArray))
                instructions.add(new MIPSStoreOffsetF(registerMap.get(rightArray), offset, baseRegister));
            else
                instructions.add(new MIPSStoreOffset(registerMap.get(rightArray), offset, baseRegister));
        }
        return instructions;
    }


    private List<MIPSInstruction> translateArrayLoad(IRArrayLoad arrayLoad, Map<IRValue, MIPSRegister> registerMap) {
        if (arrayLoad.getArrayIndex() instanceof IRVariable)
            return translateArrayLoadVariableIndex(arrayLoad, registerMap);
        if (isStatic(arrayLoad.getArray())) {
            int offset = staticData.getGpOffset(arrayLoad.getArray()) + Integer.parseInt(arrayLoad.getArrayIndex().getReference()) * MIPSConstants.WORD_SIZE;
            if (isFloatOperation(arrayLoad))
                return Collections.singletonList(new MIPSLoadOffsetF(registerMap.get(arrayLoad.getDestinationVariable().get()), offset, MIPSRegisters.GP));
            return Collections.singletonList(new MIPSLoadOffset(registerMap.get(arrayLoad.getDestinationVariable().get()), offset, MIPSRegisters.GP));
        } else {
            int offset = stackFrame.getVariableOffset(arrayLoad.getArray()) + Integer.parseInt(arrayLoad.getArrayIndex().getReference()) * MIPSConstants.WORD_SIZE;
            if (isFloatOperation(arrayLoad))
                return Collections.singletonList(new MIPSLoadOffsetF(registerMap.get(arrayLoad.getDestinationVariable().get()), offset));
            return Collections.singletonList(new MIPSLoadOffset(registerMap.get(arrayLoad.getDestinationVariable().get()), offset));
        }
    }

    private List<MIPSInstruction> translateArrayLoadVariableIndex(IRArrayLoad arrayLoad, Map<IRValue, MIPSRegister> registerMap) {
        List<MIPSInstruction> instructions = new ArrayList<>();
        TempAllocation indexTemp = allocator.assignTempRegister(arrayLoad.getArrayIndex());
        if (indexTemp.isSpill())
            instructions.add(getStoreInstruction(indexTemp.getRegister(), (IRVariable) indexTemp.getOwner().get()));
        instructions.add(new MIPSMove(indexTemp.getRegister(), registerMap.get(arrayLoad.getArrayIndex())));
        instructions.add(new MIPSSll(indexTemp.getRegister(), indexTemp.getRegister(), 2));  // log2(MIPSConstants.WORD_SIZE)
        if (isStatic(arrayLoad.getArray())) {
            instructions.add(new MIPSAddI(indexTemp.getRegister(), indexTemp.getRegister(), new IRConstant(PrimitiveTypes.TigerInt, String.valueOf(staticData.getGpOffset(arrayLoad.getArray())))));
            instructions.add(new MIPSAdd(indexTemp.getRegister(), indexTemp.getRegister(), MIPSRegisters.GP));
        } else {
            instructions.add(new MIPSAddI(indexTemp.getRegister(), indexTemp.getRegister(), new IRConstant(PrimitiveTypes.TigerInt, String.valueOf(stackFrame.getVariableOffset(arrayLoad.getArray())))));
            instructions.add(new MIPSAdd(indexTemp.getRegister(), indexTemp.getRegister(), MIPSRegisters.SP));
        }
        IRVariable destination = arrayLoad.getDestinationVariable().get();
        if (isFloat(destination))
            instructions.add(new MIPSLoadOffsetF(registerMap.get(destination), 0, indexTemp.getRegister()));
        else
            instructions.add(new MIPSLoadOffset(registerMap.get(destination), 0, indexTemp.getRegister()));
        if (indexTemp.isSpill())
            instructions.add(getLoadInstruction(indexTemp.getRegister(), indexTemp.getOwner().get()));
        return instructions;
    }

    private List<MIPSInstruction> translateArrayStore(IRArrayStore arrayStore, Map<IRValue, MIPSRegister> registerMap) {
        if (arrayStore.getArrayIndex() instanceof IRVariable)
            return translateArrayStoreVariableIndex(arrayStore, registerMap);
        if (isStatic(arrayStore.getArray())) {
            int offset = staticData.getGpOffset(arrayStore.getArray()) + Integer.parseInt(arrayStore.getArrayIndex().getReference()) * MIPSConstants.WORD_SIZE;
            if (isFloatOperation(arrayStore))
                return Collections.singletonList(new MIPSStoreOffsetF(registerMap.get(arrayStore.getSource()), offset, MIPSRegisters.GP));
            return Collections.singletonList(new MIPSStoreOffset(registerMap.get(arrayStore.getSource()), offset, MIPSRegisters.GP));
        } else {
            int offset = stackFrame.getVariableOffset(arrayStore.getArray()) + Integer.parseInt(arrayStore.getArrayIndex().getReference()) * MIPSConstants.WORD_SIZE;
            if (isFloatOperation(arrayStore))
                return Collections.singletonList(new MIPSStoreOffsetF(registerMap.get(arrayStore.getSource()), offset));
            return Collections.singletonList(new MIPSStoreOffset(registerMap.get(arrayStore.getSource()), offset));
        }
    }

    private List<MIPSInstruction> translateArrayStoreVariableIndex(IRArrayStore arrayStore, Map<IRValue, MIPSRegister> registerMap) {
        List<MIPSInstruction> instructions = new ArrayList<>();
        TempAllocation indexTemp = allocator.assignTempRegister(arrayStore.getArrayIndex());
        if (indexTemp.isSpill())
            instructions.add(getStoreInstruction(indexTemp.getRegister(), (IRVariable) indexTemp.getOwner().get()));
        instructions.add(new MIPSMove(indexTemp.getRegister(), registerMap.get(arrayStore.getArrayIndex())));
        instructions.add(new MIPSSll(indexTemp.getRegister(), indexTemp.getRegister(), 2));  // log2(MIPSConstants.WORD_SIZE)
        if (isStatic(arrayStore.getArray())) {
            instructions.add(new MIPSAddI(indexTemp.getRegister(), indexTemp.getRegister(), new IRConstant(PrimitiveTypes.TigerInt, String.valueOf(staticData.getGpOffset(arrayStore.getArray())))));
            instructions.add(new MIPSAdd(indexTemp.getRegister(), indexTemp.getRegister(), MIPSRegisters.GP));
        } else {
            instructions.add(new MIPSAddI(indexTemp.getRegister(), indexTemp.getRegister(), new IRConstant(PrimitiveTypes.TigerInt, String.valueOf(stackFrame.getVariableOffset(arrayStore.getArray())))));
            instructions.add(new MIPSAdd(indexTemp.getRegister(), indexTemp.getRegister(), MIPSRegisters.SP));
        }
        IRVariable destination = arrayStore.getDestinationVariable().get();
        if (isFloat(destination))
            instructions.add(new MIPSStoreOffsetF(registerMap.get(arrayStore.getSource()), 0, indexTemp.getRegister()));
        else
            instructions.add(new MIPSStoreOffset(registerMap.get(arrayStore.getSource()), 0, indexTemp.getRegister()));
        if (indexTemp.isSpill())
            instructions.add(getLoadInstruction(indexTemp.getRegister(), indexTemp.getOwner().get()));
        return instructions;
    }

    private List<MIPSInstruction> translateCallOperation(IRCall callOperation, Map<IRValue, MIPSRegister> registerMap) {
        List<MIPSInstruction> instructions = new ArrayList<>();
        instructions.addAll(setArguments(callOperation.getParameterTypes(), callOperation.getArguments(), registerMap));
        instructions.add(new MIPSJal(callOperation.getFunctionName()));
        instructions.addAll(restoreTempRegisters());
        return instructions;
    }

    private List<MIPSInstruction> translateCallROperation(IRCallR callrOperation, Map<IRValue, MIPSRegister> registerMap) {
        List<MIPSInstruction> instructions = new ArrayList<>(saveTempRegisters());
        instructions.addAll(setArguments(callrOperation.getParameterTypes(), callrOperation.getArguments(), registerMap));
        instructions.add(new MIPSJal(callrOperation.getFunctionName()));
        instructions.addAll(restoreTempRegisters());
        if (isFloat(callrOperation.getDestinationVariable().get()))
            instructions.add(new MIPSMoveF(registerMap.get(callrOperation.getDestinationVariable().get()), MIPSRegisters.F0));
        else
            instructions.add(new MIPSMove(registerMap.get(callrOperation.getDestinationVariable().get()), MIPSRegisters.V0));
        return instructions;
    }

    private List<MIPSInstruction> setArguments(List<TypeSymbol> parameterTypes, List<IRValue> args, Map<IRValue, MIPSRegister> registerMap) {
        List<MIPSInstruction> instructions = new ArrayList<>();
        int intArgs = 0;
        int floatArgs = 0;
        for (int i = 0; i < args.size(); i++) {
            IRValue arg = args.get(i);
            TypeSymbol parameterType = parameterTypes.get(i);
            // first four args passed via $a0-$a3 registers, rest via stack
            if (isFloat(parameterType) && floatArgs < MIPSRegisters.floatFunctionArgumentRegisters.size())
                instructions.add(new MIPSMoveF(MIPSRegisters.floatFunctionArgumentRegisters.get(floatArgs), registerMap.get(arg)));
            else if (!isFloat(parameterType) && intArgs < MIPSRegisters.functionArgumentRegisters.size())
               instructions.add(new MIPSMove(MIPSRegisters.functionArgumentRegisters.get(intArgs), registerMap.get(arg)));

            // store param (even registered ones, for simplicity) to stack
            int offset = (intArgs + floatArgs) * MIPSConstants.WORD_SIZE;
            if (isFloat(parameterType))
                instructions.add(new MIPSStoreOffsetF(registerMap.get(arg), offset));
            else
                instructions.add(new MIPSStoreOffset(registerMap.get(arg), offset));

            if (isFloat(parameterType))
                floatArgs++;
            else
                intArgs++;
        }
        return instructions;
    }


    /*
    |--------------------------------------------|
    |             Private utilities              |
    |--------------------------------------------|
     */

    private MIPSInstruction getLoadInstruction(MIPSRegister register, IRValue value) {
        if (value instanceof IRVariable variable) {
            if (isStatic(variable)) {
                if (isFloat(value))
                    return new MIPSLoadOffsetF(register, staticData.getGpOffset(variable), MIPSRegisters.GP);
                else
                    return new MIPSLoadOffset(register, staticData.getGpOffset(variable), MIPSRegisters.GP);
            } else {
                if (isFloat(value))
                    return new MIPSLoadOffsetF(register, stackFrame.getVariableOffset(variable));
                else
                    return new MIPSLoadOffset(register, stackFrame.getVariableOffset(variable));
            }
        } else {
            if (isFloat(value))
                return new MIPSLoadIF(register, Float.parseFloat(value.getReference()));
            else
                return new MIPSLoadI(register, Integer.parseInt(value.getReference()));
        }
    }

    private MIPSInstruction getStoreInstruction(MIPSRegister register, IRVariable variable) {
        if (isStatic(variable)) {
            if (isFloat(variable))
                return new MIPSStoreOffsetF(register, staticData.getGpOffset(variable), MIPSRegisters.GP);
            else
                return new MIPSStoreOffset(register, staticData.getGpOffset(variable), MIPSRegisters.GP);
        } else {
            if (isFloat(variable))
                return new MIPSStoreOffsetF(register, stackFrame.getVariableOffset(variable));
            else
                return new MIPSStoreOffset(register, stackFrame.getVariableOffset(variable));
        }
    }

    private List<MIPSInstruction> saveTempRegisters() {
        List<MIPSInstruction> instructions = new ArrayList<>();
        for (IRValue value : allocator.getAllocatedValues()) {
            if (value instanceof IRVariable variable) {
                MIPSRegister register = allocator.getAllocatedRegister(variable);
                if (!register.isSave())
                    instructions.add(getStoreInstruction(register, variable));
            }
        }
        return instructions;
    }

    private List<MIPSInstruction> restoreTempRegisters() {
        List<MIPSInstruction> instructions = new ArrayList<>();
        for (IRValue value : allocator.getAllocatedValues()) {
            if (value instanceof IRVariable variable) {
                MIPSRegister register = allocator.getAllocatedRegister(variable);
                if (!register.isSave())
                    instructions.add(getLoadInstruction(register, variable));
            }
        }
        return instructions;
    }
    private List<MIPSInstruction> restoreGlobalValues() {
        // hack that should be fixed -- any global data we want restored after a function
        // call we will restore ourselves; this is for the case where the calling function
        // mutates a global value which we have saved in a register
        // to handle this properly we should
        List<MIPSInstruction> instructions = new ArrayList<>();
        instructions.add(new MIPSComment(" restore globals"));
        for (IRValue value : allocator.getAllocatedValues())
            if (value instanceof IRVariable variable)
                if (isStatic(variable)) {
                    if (isFloat(value))
                        instructions.add(new MIPSLoadOffsetF(allocator.getAllocatedRegister(value), staticData.getGpOffset(variable), MIPSRegisters.GP));
                    else
                        instructions.add(new MIPSLoadOffset(allocator.getAllocatedRegister(value), staticData.getGpOffset(variable), MIPSRegisters.GP));
                }
        return instructions;
    }

    private Set<IRValue> getDefinedVariables(List<IRStatement> statements) {
        Set<IRValue> defined = new HashSet<>();
        for (IRStatement statement : statements) {
            if (statement instanceof IROperation operation)
                if (operation.getDestinationVariable().isPresent())
                    defined.add(operation.getDestinationVariable().get());

        }
        return defined;
    }

    private boolean isFloatOperation(IRStatement statement) {
        boolean isFloat = false;
        if (statement instanceof IROperation operation) {
            Optional<IRVariable> optDestination = operation.getDestinationVariable();
            if (optDestination.isPresent()) {
                if (isFloat(optDestination.get()))
                    isFloat = true;
            }
            if (!isFloat) {
                for (IRValue operand : operation.getOperands()) {
                    if (isFloat(operand)) {
                        isFloat = true;
                        break;
                    }
                }
            }
        }
        return isFloat;
    }

    private boolean isStore(IRStatement statement) {
        return (statement instanceof IRArrayStore) ||
                (statement instanceof IRArrayAssign) ||
                (statement instanceof IRAssign && isArray(((IRAssign) statement).getRight()));
    }

    private boolean isFloat(IRValue value) {
        return value.getType().getBaseType().equals(PrimitiveTypes.TigerFloat);
    }

    private boolean isFloat(TypeSymbol type) {
        return type.getBaseType().equals(PrimitiveTypes.TigerFloat);
    }

    private boolean isStatic(IRVariable variable) {
        return staticData.isStatic(variable);
    }

    private boolean isArray(IRValue value) {
        return value.getType().isArray();
    }

    private boolean isFloatArrayIndex(IRStatement statement, IRValue value) {
        if (statement instanceof IRArrayLoad arrayLoad)
            return value.equals(arrayLoad.getArrayIndex());
        if (statement instanceof IRArrayStore arrayStore)
            return value.equals(arrayStore.getArrayIndex());
        return false;
    }

    private boolean noRegisterNeeded(IROperation operation, IRValue value) {
        // TODO this be super ugly; instead of working on general getOperands()
        // we should create specific statement "prologues" for each operation type
        if (operation instanceof IRAdd &&
                value instanceof IRConstant &&
                value.equals(((IRAdd) operation).getRight()) &&
                ((IRAdd) operation).getRight() instanceof IRConstant &&
                !isFloatOperation(operation))
            return true;
        if (operation instanceof IRAssign && ((IRAssign) operation).getRight() instanceof IRConstant)
            return true;
        return operation instanceof IRArrayLoad && value.getType().isArray();
    }

    private boolean isCallStatement(IRStatement statement) {
        return (statement instanceof IRCall) || (statement instanceof IRCallR);
    }
    private boolean callArgumentDoesNotMatchType(IRStatement statement, IRValue value, int paramIndex) {
        if (isCallStatement(statement)) {
            List<TypeSymbol> argTypes = (statement instanceof IRCall) ? ((IRCall) statement).getParameterTypes() : ((IRCallR) statement).getParameterTypes();
            return !isFloat(value) && argTypes.get(paramIndex).equals(PrimitiveTypes.TigerFloat);
        }
        return false;
    }

    private boolean shouldStorePreemptively(IRStatement statement) {
        return statement instanceof IRControlFlowOperation || statement instanceof IRReturn;
    }
}
