package com.hobbes.backend.mips.instructions;

import com.hobbes.backend.mips.registers.MIPSRegister;

public class MIPSMove implements MIPSInstruction {

    private MIPSRegister destination;
    private MIPSRegister source;

    public MIPSMove(MIPSRegister destination, MIPSRegister source) {
        this.destination = destination;
        this.source = source;
    }

    public String emit() {
        return String.format("move $%s, $%s", destination.getName(), source.getName());
    }
}
