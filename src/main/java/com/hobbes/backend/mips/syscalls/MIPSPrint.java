package com.hobbes.backend.mips.syscalls;

import com.hobbes.backend.mips.MIPSConstants;
import com.hobbes.backend.mips.instructions.*;
import com.hobbes.backend.mips.registers.MIPSRegisters;

import java.util.Arrays;
import java.util.List;

public class MIPSPrint extends MIPSSystemCall {

    private String name;
    private int code;

    public MIPSPrint(String name, int code) {
        super();
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    protected List<MIPSInstruction> instructions() {
        return Arrays.asList(
                new MIPSLoadI(MIPSRegisters.V0, code),
                new MIPSSyscall(),
                new MIPSLoadI(MIPSRegisters.V0, MIPSConstants.PRINTC),
                new MIPSLoadI(MIPSRegisters.A0, MIPSConstants.ASCII_NEWLINE),
                new MIPSSyscall(),
                new MIPSJr()
        );
    }
}
