package com.hobbes.backend.mips.instructions;

public class MIPSComment implements MIPSInstruction {

    private String comment;

    public MIPSComment(String comment) {
        this.comment = comment;
    }

    public String emit() {
        return String.format("#%s", comment);
    }
}
