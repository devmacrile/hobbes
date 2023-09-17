package com.hobbes.backend.mips.instructions;

import com.hobbes.backend.mips.registers.MIPSRegister;

public class MIPSDiv implements MIPSInstruction {

    private MIPSRegister destination;
    private MIPSRegister left;
    private MIPSRegister right;

    public MIPSDiv(MIPSRegister destination, MIPSRegister left, MIPSRegister right) {
        this.destination = destination;
        this.left = left;
        this.right = right;
    }

    public String emit() {
        return String.format("div $%s, $%s, $%s", destination.getName(), left.getName(), right.getName());
    }
}
