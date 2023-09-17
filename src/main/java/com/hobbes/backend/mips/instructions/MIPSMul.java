package com.hobbes.backend.mips.instructions;

import com.hobbes.backend.mips.registers.MIPSRegister;

public class MIPSMul implements MIPSInstruction {

    private MIPSRegister destination;
    private MIPSRegister left;
    private MIPSRegister right;

    public MIPSMul(MIPSRegister destination, MIPSRegister left, MIPSRegister right) {
        this.destination = destination;
        this.left = left;
        this.right = right;
    }

    public String emit() {
        return String.format("mul $%s, $%s, $%s", destination.getName(), left.getName(), right.getName());
    }
}
