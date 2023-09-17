package com.hobbes.backend.mips;

import com.hobbes.backend.mips.instructions.MIPSInstruction;

public class MIPSLabel implements MIPSInstruction {

    private String name;

    public MIPSLabel(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String emit() {
        return String.format("%s:", name);
    }
}
