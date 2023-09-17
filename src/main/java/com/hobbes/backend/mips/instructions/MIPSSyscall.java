package com.hobbes.backend.mips.instructions;

public class MIPSSyscall implements MIPSInstruction {

    public MIPSSyscall() {}

    public String emit() {
        return "syscall";
    }
}
