package com.hobbes.backend.mips.syscalls;

import com.hobbes.backend.mips.MIPSLabel;
import com.hobbes.backend.mips.instructions.*;
import com.hobbes.backend.mips.registers.MIPSRegisters;

import java.util.Arrays;
import java.util.List;

public class MIPSNot extends MIPSSystemCall {

    private final String name = "_not";

    public MIPSNot() {
        super();
    }

    public String getName() {
        return name;
    }

    protected List<MIPSInstruction> instructions() {
        MIPSLabel label = new MIPSLabel(String.format("%s_label", name));
        return Arrays.asList(
                new MIPSBeq(MIPSRegisters.A0, MIPSRegisters.ZERO, label),
                new MIPSMove(MIPSRegisters.V0, MIPSRegisters.ZERO),
                new MIPSJr(),
                label,
                new MIPSLoadI(MIPSRegisters.V0, 1),
                new MIPSJr()
        );
    }
}
