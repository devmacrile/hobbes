package com.hobbes.backend.mips.instructions;

public class MIPSEmpty implements MIPSInstruction {

    public MIPSEmpty() {}

    public String emit() {
        return "";
    }
}
