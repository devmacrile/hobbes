package com.hobbes.backend.mips.instructions;

import com.hobbes.backend.mips.MIPSLabel;
import com.hobbes.backend.mips.registers.MIPSRegister;

public abstract class MIPSBranch implements MIPSInstruction {

    private MIPSRegister left;
    private MIPSRegister right;
    private MIPSLabel label;

    public MIPSBranch(MIPSRegister left, MIPSRegister right, MIPSLabel label) {
        this.left = left;
        this.right = right;
        this.label = label;
    }

    public abstract String getInstructionCode();

    public String emit() {
        return String.format("%s $%s, $%s, %s", getInstructionCode(), left.getName(), right.getName(), label.getName());
    }

}
