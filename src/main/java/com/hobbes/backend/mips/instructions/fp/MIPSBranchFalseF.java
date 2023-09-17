package com.hobbes.backend.mips.instructions.fp;

import com.hobbes.backend.mips.MIPSLabel;
import com.hobbes.backend.mips.instructions.MIPSInstruction;

public class MIPSBranchFalseF implements MIPSInstruction {

    private MIPSLabel label;

    public MIPSBranchFalseF(MIPSLabel label) {
        this.label = label;
    }

    public String emit() {
        return String.format("bc1f %s", label.getName());
    }
}
