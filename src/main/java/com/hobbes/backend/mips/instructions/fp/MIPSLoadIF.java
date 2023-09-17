package com.hobbes.backend.mips.instructions.fp;

import com.hobbes.backend.mips.instructions.MIPSInstruction;
import com.hobbes.backend.mips.registers.MIPSRegister;

public class MIPSLoadIF implements MIPSInstruction {

    private MIPSRegister register;
    private float value;

    public MIPSLoadIF(MIPSRegister register, float value) {
        this.register = register;
        this.value = value;
    }

    public String emit() {
        return String.format("li.s $%s, %f", register.getName(), value);
    }
}
