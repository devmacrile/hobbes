package com.hobbes.backend.mips.registers;

public class MIPSFRegister implements MIPSRegister {

    private String name;
    private int registerNumber;
    private boolean isSave;

    public MIPSFRegister(int registerNumber) {
        this.registerNumber = registerNumber;
        this.name = String.format("f%d", registerNumber);
        this.isSave = registerNumber >= 20;
    }

    public String getName() {
        return this.name;
    }

    public MIPSRegisterType getType() {
        return MIPSRegisterType.FLOAT;
    }

    public boolean isSave() {
        return this.isSave;
    }

    public String toString() {
        return String.format("$%s", name);
    }
}
