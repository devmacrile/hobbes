package com.hobbes.symbol;

public class VariableSymbol extends BaseSymbol {

    private TypeSymbol type;
    private final boolean isStatic;

    public VariableSymbol(String name, TypeSymbol type) {
        super(name);
        this.type = type;
        this.isStatic = false;
    }
    public VariableSymbol(String name, TypeSymbol type, boolean isStatic) {
        super(name);
        this.type = type;
        this.isStatic = isStatic;
    }

    public TypeSymbol getType() {
        return this.type;
    }

    public void setType(TypeSymbol type) {
        this.type = type;
    }

    @Override
    public String toString() {
        String storageClass = "";
        // TODO remove magic strings
        if (isStatic) {
            storageClass += "static";
        } else {
            storageClass += "var";
        }
        return String.format("%s, %s, %s", getName(), storageClass, type.getName());
    }
}
