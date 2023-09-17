package com.hobbes.backend.mips.registers;

public interface MIPSRegister {
    String getName();
    MIPSRegisterType getType();
    boolean isSave();
}
