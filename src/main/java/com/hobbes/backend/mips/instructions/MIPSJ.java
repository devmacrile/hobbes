package com.hobbes.backend.mips.instructions;

import com.hobbes.backend.mips.MIPSLabel;

public class MIPSJ implements MIPSInstruction {

    private MIPSLabel label;

    public MIPSJ(MIPSLabel label) {
        this.label = label;
    }

    public String emit() {
        return String.format("j %s", label.getName());
    }
}
