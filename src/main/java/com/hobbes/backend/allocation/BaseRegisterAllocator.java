package com.hobbes.backend.allocation;

import com.hobbes.backend.mips.MIPSData;
import com.hobbes.backend.mips.registers.MIPSRegister;
import com.hobbes.backend.mips.registers.MIPSRegisters;
import com.hobbes.ir.IRFunction;
import com.hobbes.ir.IRValue;
import com.hobbes.ir.IRVariable;
import com.hobbes.symbol.PrimitiveTypes;

import java.util.*;

import static com.hobbes.backend.mips.registers.MIPSRegisters.*;

public abstract class BaseRegisterAllocator implements RegisterAllocator {

    private IRFunction function;
    private MIPSData staticData;
    // cheat for the moment and keep several registers
    // free for temp allocation to avoid spill logic
    private final List<MIPSRegister> TempPool = Arrays.asList(T0, T1, T2, T3);
    private final List<MIPSRegister> FloatTempPool = Arrays.asList(F4, F5, F6, F7);
    protected Map<IRValue, MIPSRegister> allocations;
    protected Map<IRValue, MIPSRegister> tempAllocations;

    public BaseRegisterAllocator(IRFunction function, MIPSData staticData) {
        this.function = function;
        this.staticData = staticData;
        this.allocations = new HashMap<>();
        this.tempAllocations = new HashMap<>();

        /*
        int intArgs = 0;
        int floatArgs = 0;
        for (IRVariable param : function.getParams()) {
            // first four int args passed via $a0-$a3 registers
            // first four float args passed via $f12-f15 registers
            if (isFloat(param) && floatArgs < floatFunctionArgumentRegisters.size())
                allocations.put(param, MIPSRegisters.floatFunctionArgumentRegisters.get(floatArgs));
            else if (!isFloat(param) && intArgs < functionArgumentRegisters.size())
                allocations.put(param, functionArgumentRegisters.get(intArgs));
            if (isFloat(param))
                floatArgs++;
            else
                intArgs++;
        }
        */
    }

    // abstract methods to be implemented
    public abstract void run();
    public abstract List<IRVariable> getSpillVariables();


    public List<IRValue> getAllocatedValues() {
        return new ArrayList<>(allocations.keySet());
    }

    public List<MIPSRegister> getAllocatedRegisters() {
        return new ArrayList<>(allocations.values());
    }

    public boolean isAllocated(IRValue value) {
        return allocations.containsKey(value);
    }

    public MIPSRegister getAllocatedRegister(IRValue value) {
        assert allocations.containsKey(value);
        return allocations.get(value);
    }

    public boolean isTempAllocated(IRValue value) {
        return tempAllocations.containsKey(value);
    }

    public MIPSRegister getTempAllocatedRegister(IRValue value) {
        assert tempAllocations.containsKey(value);
        return tempAllocations.get(value);
    }

    public TempAllocation assignTempRegister(IRValue value) {
        assert !tempAllocations.containsKey(value);
        assert !allocations.containsKey(value);
        if (isFloat(value))
            return assignTempFRegister(value);
        return assignTempRegister(value, MIPSRegisters.generalPurposeTempRegisters, MIPSRegisters.generalPurposeSaveRegisters);
    }

    public TempAllocation assignTempFRegister(IRValue value) {
        return assignTempRegister(value, MIPSRegisters.floatTempRegisters, MIPSRegisters.floatSaveRegisters);
    }

    public TempAllocation assignTempFRegister() {
        return assignTempRegister(IRVariable.getDummy(), MIPSRegisters.floatTempRegisters, MIPSRegisters.floatSaveRegisters);
    }

    public boolean isTempAssigned(MIPSRegister register) {
        return tempAllocations.containsValue(register);
    }

    public void clearTempAllocations() {
        this.tempAllocations.clear();
    }

    protected int getNumberOfAllocatableRegisters() {
        return generalPurposeSaveRegisters.size() + generalPurposeTempRegisters.size() - TempPool.size();
    }

    protected int getNumberOfAllocatableFRegisters() {
        // note: -4 from the temp pool
        return floatSaveRegisters.size() + floatTempRegisters.size() - FloatTempPool.size();
    }

    protected boolean allocateRegister(IRValue value) {
        if (isFloat(value))
            return allocateRegister(value, MIPSRegisters.floatSaveRegisters, MIPSRegisters.floatTempRegisters, new HashSet<>());
        return allocateRegister(value, MIPSRegisters.generalPurposeSaveRegisters, MIPSRegisters.generalPurposeTempRegisters, new HashSet<>());
    }

    protected boolean allocateRegister(IRValue value, Set<MIPSRegister> blacklist) {
        if (isFloat(value))
            return allocateRegister(value, MIPSRegisters.floatSaveRegisters, MIPSRegisters.floatTempRegisters, blacklist);
        return allocateRegister(value, MIPSRegisters.generalPurposeSaveRegisters, MIPSRegisters.generalPurposeTempRegisters, blacklist);
    }

    private boolean allocateRegister(IRValue value, List<MIPSRegister> save, List<MIPSRegister> temp, Set<MIPSRegister> blacklist) {
        if (staticData.isStatic((IRVariable) value))
            return false;
        // prioritize temp registers
        for (MIPSRegister register : temp)
            if (!blacklist.contains(register) && isFree(register) && !isTempReserved(register)) {
                allocations.put(value, register);
                return true;
            }
        // only allocate local variables to save registers
        // to ease the reasoning about function calls
        if (!staticData.isStatic((IRVariable) value))
            for (MIPSRegister register : save)
                if (!blacklist.contains(register) && isFree(register)) {
                    allocations.put(value, register);
                    return true;
                }
        return false;
    }

    private boolean isFree(MIPSRegister register) {
        return !allocations.containsValue(register) && !tempAllocations.containsValue(register);
    }

    private TempAllocation assignTempRegister(IRValue value, List<MIPSRegister> temp, List<MIPSRegister> save) {
        // prioritize free registers, and temp over save
        for (MIPSRegister register : temp)
            if (isFree(register)) {
                tempAllocations.put(value, register);
                return new TempAllocation(register, value);
            }
        for (MIPSRegister register : save)
            if (isFree(register)) {
                tempAllocations.put(value, register);
                return new TempAllocation(register, value);
            }

        // if none available, choose one that is allocated but not temp allocated
        for (MIPSRegister register : temp)
            if (!isTempAssigned(register)) {
                tempAllocations.put(value, register);
                return new TempAllocation(register, value);
            }
        // Panic!
        assert false;
        return null;
    }

    private IRValue getOwner(MIPSRegister register) {
        for (IRValue value : allocations.keySet()) {
            if (allocations.get(value).equals(register))
                return value;
        }
        return null;
    }

    private boolean isFloat(IRValue value) {
        return value.getType().getBaseType().equals(PrimitiveTypes.TigerFloat);
    }

    private boolean isTempReserved(MIPSRegister register) {
        return FloatTempPool.contains(register) || TempPool.contains(register);
    }
}
