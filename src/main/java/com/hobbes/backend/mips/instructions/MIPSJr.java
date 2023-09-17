package com.hobbes.backend.mips.instructions;

import com.hobbes.backend.mips.registers.MIPSRegisters;

public class MIPSJr implements MIPSInstruction {

    public MIPSJr() { }

    public String emit() {
        return String.format("jr $%s", MIPSRegisters.RA.getName());
    }
}
