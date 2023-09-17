package com.hobbes.backend.mips.instructions;

import com.hobbes.backend.mips.registers.MIPSRegister;
import com.hobbes.ir.IRConstant;

public class MIPSAddIU implements MIPSInstruction {

    private MIPSRegister destination;
    private MIPSRegister left;
    private int right;

    public MIPSAddIU(MIPSRegister destination, MIPSRegister left, int right) {
        this.destination = destination;
        this.left = left;
        this.right = right;
    }

    public String emit() {
        return String.format("addiu $%s, $%s, %s", destination.getName(), left.getName(), right);
    }
}
