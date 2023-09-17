package com.hobbes.backend.mips.registers;

public class MIPSGRegister implements MIPSRegister {

    private String name;
    private boolean isSave;

    public MIPSGRegister(String name) {
        this.name = name;
        this.isSave = name.startsWith("s") || name.equals("ra");
    }

    public String getName() {
        return this.name;
    }

    public MIPSRegisterType getType() {
        return MIPSRegisterType.GENERAL_PURPOSE;
    }

    public boolean isSave() {
        return this.isSave;
    }

    public String toString() {
        return String.format("$%s", name);
    }
}
