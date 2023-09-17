package com.hobbes.backend.mips.instructions.fp;

import com.hobbes.backend.mips.MIPSLabel;
import com.hobbes.backend.mips.instructions.MIPSInstruction;

public class MIPSBranchTrueF implements MIPSInstruction {

    private MIPSLabel label;

    public MIPSBranchTrueF(MIPSLabel label) {
        this.label = label;
    }

    public String emit() {
        return String.format("bc1t %s", label.getName());
    }
}
