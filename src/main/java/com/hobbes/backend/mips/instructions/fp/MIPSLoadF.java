package com.hobbes.backend.mips.instructions.fp;

import com.hobbes.backend.mips.MIPSLabel;
import com.hobbes.backend.mips.instructions.MIPSInstruction;
import com.hobbes.backend.mips.registers.MIPSRegister;

public class MIPSLoadF implements MIPSInstruction {

    private MIPSRegister register;
    private MIPSLabel label;

    public MIPSLoadF(MIPSRegister register, MIPSLabel label) {
        this.register = register;
        this.label = label;
    }

    public String emit() {
        return String.format("l.s $%s, %s", register.getName(), label.getName());
    }
}
