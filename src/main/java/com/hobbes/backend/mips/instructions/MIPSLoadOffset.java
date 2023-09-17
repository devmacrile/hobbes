package com.hobbes.backend.mips.instructions;

import com.hobbes.backend.mips.registers.MIPSRegister;
import com.hobbes.backend.mips.registers.MIPSRegisters;

public class MIPSLoadOffset implements MIPSInstruction {

    private MIPSRegister register;
    private int offset;
    private MIPSRegister offsetRegister;

    public MIPSLoadOffset(MIPSRegister register, int offset) {
        this.register = register;
        this.offset = offset;
        this.offsetRegister = MIPSRegisters.SP;
    }

    public MIPSLoadOffset(MIPSRegister register, int offset, MIPSRegister offsetRegister) {
        this.register = register;
        this.offset = offset;
        this.offsetRegister = offsetRegister;
    }

    public String emit() {
        return String.format("lw $%s, %d($%s)", register.getName(), offset, offsetRegister.getName());
    }
}
