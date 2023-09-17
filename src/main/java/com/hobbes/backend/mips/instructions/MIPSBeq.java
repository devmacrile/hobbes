package com.hobbes.backend.mips.instructions;

import com.hobbes.backend.mips.MIPSLabel;
import com.hobbes.backend.mips.registers.MIPSRegister;

public class MIPSBeq extends MIPSBranch {

    public MIPSBeq(MIPSRegister left, MIPSRegister right, MIPSLabel label) {
        super(left, right, label);
    }
    public String getInstructionCode() {
        return "beq";
    }
}
