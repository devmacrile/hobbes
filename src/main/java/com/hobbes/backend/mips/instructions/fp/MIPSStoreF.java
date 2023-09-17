package com.hobbes.backend.mips.instructions.fp;

import com.hobbes.backend.mips.MIPSLabel;
import com.hobbes.backend.mips.instructions.MIPSInstruction;
import com.hobbes.backend.mips.registers.MIPSRegister;

public class MIPSStoreF implements MIPSInstruction {

    private MIPSRegister register;
    private MIPSLabel label;

    public MIPSStoreF(MIPSRegister register, MIPSLabel label) {
        this.register = register;
        this.label = label;
    }

    public String emit() {
        return String.format("s.s $%s, %s", register.getName(), label.getName());
    }
}
