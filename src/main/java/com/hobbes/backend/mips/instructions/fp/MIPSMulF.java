package com.hobbes.backend.mips.instructions.fp;

import com.hobbes.backend.mips.instructions.MIPSInstruction;
import com.hobbes.backend.mips.registers.MIPSRegister;

public class MIPSMulF implements MIPSInstruction {

    private MIPSRegister destination;
    private MIPSRegister left;
    private MIPSRegister right;

    public MIPSMulF(MIPSRegister destination, MIPSRegister left, MIPSRegister right) {
        this.destination = destination;
        this.left = left;
        this.right = right;
    }

    public String emit() {
        return String.format("mul.s $%s, $%s, $%s", destination.getName(), left.getName(), right.getName());
    }
}
