package com.hobbes.backend.mips.instructions;

import com.hobbes.backend.mips.registers.MIPSRegister;
import com.hobbes.backend.mips.registers.MIPSRegisters;

public class MIPSStoreOffset implements MIPSInstruction {

    private MIPSRegister register;
    private int offset;
    private MIPSRegister offsetRegister;

    public MIPSStoreOffset(MIPSRegister register, int offset) {
        this.register = register;
        this.offset = offset;
        this.offsetRegister = MIPSRegisters.SP;
    }

    public MIPSStoreOffset(MIPSRegister register, int offset, MIPSRegister offsetRegister) {
        this.register = register;
        this.offset = offset;
        this.offsetRegister = offsetRegister;
    }

    public String emit() {
        return String.format("sw $%s, %d($%s)", register.getName(), offset, offsetRegister.getName());
    }
}
