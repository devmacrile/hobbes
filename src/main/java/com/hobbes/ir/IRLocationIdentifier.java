package com.hobbes.ir;

import java.util.Optional;

public class IRLocationIdentifier implements IRStatement {

    private String name;

    public IRLocationIdentifier(String blockName) {
        this.name = blockName;
    }

    public String getName() {
        return name;
    }

    public String emit() {
        return String.format("%s:", name);
    }
}
