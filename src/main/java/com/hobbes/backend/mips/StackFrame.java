package com.hobbes.backend.mips;

import com.hobbes.backend.mips.registers.MIPSRegister;
import com.hobbes.backend.mips.registers.MIPSRegisters;
import com.hobbes.ir.IRFunction;
import com.hobbes.ir.IRStatement;
import com.hobbes.ir.IRVariable;
import com.hobbes.ir.ops.IRCall;
import com.hobbes.ir.ops.IRCallR;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StackFrame {

    private IRFunction function;
    private List<IRVariable> spillVariables;
    private Map<IRVariable, Integer> variableOffsets;
    private int size;

    public StackFrame(IRFunction function, List<IRVariable> spillVariables) {
        this.function = function;
        this.spillVariables = spillVariables;
        this.variableOffsets = new HashMap<>();

        // compute total frame size bottom up s.t. spill variable offsets
        // are simply the running total
        int totalSize = MIPSConstants.WORD_SIZE * getNumberOfCallArgumentSlots();
        for (IRVariable variable : spillVariables) {
            variableOffsets.put(variable, totalSize);
            if (variable.getType().isArray())
                totalSize += MIPSConstants.WORD_SIZE * variable.getType().getArrayLength();
            else
                totalSize += MIPSConstants.WORD_SIZE;
        }
        totalSize += MIPSConstants.WORD_SIZE * (8 + 11);  // $s0-$s7, $f20-f30
        totalSize += MIPSConstants.WORD_SIZE;      // $ra
        this.size = totalSize;

        // set function parameter offsets, which are located in the
        // stack frame of the calling function!
        int paramNum = 0;
        for (IRVariable param : function.getParams()) {
            variableOffsets.put(param, this.size + paramNum * MIPSConstants.WORD_SIZE);
            paramNum++;
        }
    }

    public int getSize() {
        return this.size;
    }

    public int getVariableOffset(IRVariable variable) {
        assert variableOffsets.containsKey(variable);
        return variableOffsets.get(variable);
    }

    public int getRegisterOffset(MIPSRegister register) {
        assert register.isSave() && (register.getName().equals("ra") || register.getName().startsWith("s"));
        if (register.equals(MIPSRegisters.RA))
            return size - MIPSConstants.WORD_SIZE;
        if (register.equals(MIPSRegisters.S0))
            return size - 2 * MIPSConstants.WORD_SIZE;
        if (register.equals(MIPSRegisters.S1))
            return size - 3 * MIPSConstants.WORD_SIZE;
        if (register.equals(MIPSRegisters.S2))
            return size - 4 * MIPSConstants.WORD_SIZE;
        if (register.equals(MIPSRegisters.S3))
            return size - 5 * MIPSConstants.WORD_SIZE;
        if (register.equals(MIPSRegisters.S4))
            return size - 6 * MIPSConstants.WORD_SIZE;
        if (register.equals(MIPSRegisters.S5))
            return size - 7 * MIPSConstants.WORD_SIZE;
        if (register.equals(MIPSRegisters.S6))
            return size - 8 * MIPSConstants.WORD_SIZE;
        if (register.equals(MIPSRegisters.S7))
            return size - 9 * MIPSConstants.WORD_SIZE;
        if (register.equals(MIPSRegisters.F20))
            return size - 10 * MIPSConstants.WORD_SIZE;
        if (register.equals(MIPSRegisters.F21))
            return size - 11 * MIPSConstants.WORD_SIZE;
        if (register.equals(MIPSRegisters.F22))
            return size - 12 * MIPSConstants.WORD_SIZE;
        if (register.equals(MIPSRegisters.F23))
            return size - 13 * MIPSConstants.WORD_SIZE;
        if (register.equals(MIPSRegisters.F24))
            return size - 14 * MIPSConstants.WORD_SIZE;
        if (register.equals(MIPSRegisters.F25))
            return size - 15 * MIPSConstants.WORD_SIZE;
        if (register.equals(MIPSRegisters.F26))
            return size - 16 * MIPSConstants.WORD_SIZE;
        if (register.equals(MIPSRegisters.F27))
            return size - 17 * MIPSConstants.WORD_SIZE;
        if (register.equals(MIPSRegisters.F28))
            return size - 18 * MIPSConstants.WORD_SIZE;
        if (register.equals(MIPSRegisters.F29))
            return size - 19 * MIPSConstants.WORD_SIZE;
        if (register.equals(MIPSRegisters.F30))
            return size - 20 * MIPSConstants.WORD_SIZE;
        return -1;
    }

    private int getNumberOfCallArgumentSlots() {
        int slots = 4;  // minimum is 4 for $a0-$a3
        for (IRStatement statement : function.getBody().getStatements()) {
            if (statement instanceof IRCall call)
                if (call.argCount() > slots)
                    slots = call.argCount();
            if (statement instanceof IRCallR callr)
                if (callr.argCount() > slots)
                    slots = callr.argCount();
        }
        return slots;
    }
}
