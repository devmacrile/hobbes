package com.hobbes.backend.mips.instructions;

import com.hobbes.backend.mips.registers.MIPSRegister;

public class MIPSLoadI implements MIPSInstruction {

    private MIPSRegister register;
    private int value;

    public MIPSLoadI(MIPSRegister register, int value) {
        this.register = register;
        this.value = value;
    }

    public String emit() {
        return String.format("li $%s, %d", register.getName(), value);
    }
}
