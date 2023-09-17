package com.hobbes.backend.mips.instructions.fp;

import com.hobbes.backend.mips.instructions.MIPSInstruction;
import com.hobbes.backend.mips.registers.MIPSRegister;

public class MIPSMoveF implements MIPSInstruction {

    private MIPSRegister destination;
    private MIPSRegister source;

    public MIPSMoveF(MIPSRegister destination, MIPSRegister source) {
        this.destination = destination;
        this.source = source;
    }

    public String emit() {
        return String.format("mov.s $%s, $%s", destination.getName(), source.getName());
    }
}
