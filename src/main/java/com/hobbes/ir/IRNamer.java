package com.hobbes.ir;

public class IRNamer {
    private final String Sep = "_";
    private int nextTempId;
    private int nextLocationId;

    public IRNamer() {
        this.nextTempId = 1;
        this.nextLocationId = 0;
    }

    public String mangle(String name, int scopeId) {
        return String.format("%s%d%s%s", Sep, scopeId, Sep, name);
    }

    public String demangle(String mangledName) {
        return mangledName.substring(mangledName.lastIndexOf(Sep)+1);
    }

    public String generateTempName() {
        String name = String.format("_t%d", nextTempId);
        nextTempId++;
        return name;
    }

    public String generateLocationName() {
        String name = String.format("_L%d", nextLocationId);
        nextLocationId++;
        return name;
    }
}
