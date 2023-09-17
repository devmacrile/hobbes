package com.hobbes.backend.mips.instructions.fp;

import com.hobbes.backend.mips.instructions.MIPSInstruction;
import com.hobbes.backend.mips.registers.MIPSRegister;

public class MIPSSll implements MIPSInstruction {

    private MIPSRegister destination;
    private MIPSRegister source;
    private int bits;

    public MIPSSll(MIPSRegister destination, MIPSRegister source, int bits) {
        this.destination = destination;
        this.source = source;
        this.bits = bits;
    }

    public String emit() {
        return String.format("sll $%s, $%s, %d", destination.getName(), source.getName(), bits);
    }
}
