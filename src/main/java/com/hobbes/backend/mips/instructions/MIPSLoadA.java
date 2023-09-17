package com.hobbes.backend.mips.instructions;

import com.hobbes.backend.mips.MIPSLabel;
import com.hobbes.backend.mips.registers.MIPSRegister;

public class MIPSLoadA implements MIPSInstruction {

    private MIPSRegister destination;
    private MIPSLabel label;

    public MIPSLoadA(MIPSRegister destination, MIPSLabel label) {
        this.destination = destination;
        this.label = label;
    }

    public String emit() {
        return String.format("la $%s, %s", destination.getName(), label.getName());
    }
}
