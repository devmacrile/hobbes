package com.hobbes.backend.mips.instructions.fp;

import com.hobbes.backend.mips.instructions.MIPSInstruction;
import com.hobbes.backend.mips.registers.MIPSRegister;
import com.hobbes.backend.mips.registers.MIPSRegisters;

public class MIPSStoreOffsetF implements MIPSInstruction {

    private MIPSRegister register;
    private int offset;
    private MIPSRegister offsetRegister;

    public MIPSStoreOffsetF(MIPSRegister register, int offset) {
        this.register = register;
        this.offset = offset;
        this.offsetRegister = MIPSRegisters.SP;
    }

    public MIPSStoreOffsetF(MIPSRegister register, int offset, MIPSRegister offsetRegister) {
        this.register = register;
        this.offset = offset;
        this.offsetRegister = offsetRegister;
    }

    public String emit() {
        return String.format("s.s $%s, %d($%s)", register.getName(), offset, offsetRegister.getName());
    }
}
