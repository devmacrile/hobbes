package com.hobbes.backend.mips.instructions.fp;

import com.hobbes.backend.mips.instructions.MIPSInstruction;
import com.hobbes.backend.mips.registers.MIPSRegister;

public class MIPSIntToFloat implements MIPSInstruction {

    private MIPSRegister destination;
    private MIPSRegister source;

    public MIPSIntToFloat(MIPSRegister destination, MIPSRegister source) {
        this.destination = destination;
        this.source = source;
    }

    public String emit() {
        return String.format("cvt.s.w $%s, $%s", destination.getName(), source.getName());
    }
}
