package com.hobbes.backend.mips.instructions;

import com.hobbes.backend.mips.registers.MIPSRegister;
import com.hobbes.ir.IRConstant;

public class MIPSAddI implements MIPSInstruction {
    private MIPSRegister destination;
    private MIPSRegister left;
    private IRConstant right;

    public MIPSAddI(MIPSRegister destination, MIPSRegister left, IRConstant right) {
        this.destination = destination;
        this.left = left;
        this.right = right;
    }

    public String emit() {
        return String.format("addi $%s, $%s, %s", destination.getName(), left.getName(), right.getReference());
    }
}
