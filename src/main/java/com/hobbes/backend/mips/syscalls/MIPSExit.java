package com.hobbes.backend.mips.syscalls;

import com.hobbes.backend.mips.MIPSConstants;
import com.hobbes.backend.mips.instructions.MIPSInstruction;
import com.hobbes.backend.mips.instructions.MIPSJr;
import com.hobbes.backend.mips.instructions.MIPSLoadI;
import com.hobbes.backend.mips.instructions.MIPSSyscall;
import com.hobbes.backend.mips.registers.MIPSRegisters;

import java.util.Arrays;
import java.util.List;

public class MIPSExit extends MIPSSystemCall {

    private final String name = "exit";

    public MIPSExit() {
        super();
    }

    @Override
    public String getName() {
        return name;
    }

    protected List<MIPSInstruction> instructions() {
        return Arrays.asList(
                new MIPSLoadI(MIPSRegisters.V0, MIPSConstants.EXIT),
                new MIPSSyscall(),
                new MIPSJr()
        );
    }
}
