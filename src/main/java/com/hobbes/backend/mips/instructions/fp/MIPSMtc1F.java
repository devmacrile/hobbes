package com.hobbes.backend.mips.instructions.fp;

import com.hobbes.backend.mips.instructions.MIPSInstruction;
import com.hobbes.backend.mips.registers.MIPSRegister;

public class MIPSMtc1F implements MIPSInstruction {

    private MIPSRegister destination;
    private MIPSRegister source;

    public MIPSMtc1F(MIPSRegister destination, MIPSRegister source) {
        this.destination = destination;
        this.source = source;
    }

    public String emit() {
        return String.format("mtc1 $%s, $%s", destination.getName(), source.getName());
    }
}
