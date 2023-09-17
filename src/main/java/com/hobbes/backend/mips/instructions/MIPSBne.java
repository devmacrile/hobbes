package com.hobbes.backend.mips.instructions;

import com.hobbes.backend.mips.MIPSLabel;
import com.hobbes.backend.mips.registers.MIPSRegister;

public class MIPSBne extends MIPSBranch {

    public MIPSBne(MIPSRegister left, MIPSRegister right, MIPSLabel label) {
        super(left, right, label);
    }
    public String getInstructionCode() {
        return "bne";
    }
}
