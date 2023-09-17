package com.hobbes.ir;

import com.hobbes.symbol.PrimitiveTypes;
import com.hobbes.symbol.TypeSymbol;

import java.util.Objects;

public class IRVariable implements IRValue {

    private final String name;
    private TypeSymbol type;
    private boolean isStatic;

    public IRVariable(String name, TypeSymbol type) {
        this.name = name;
        this.type = type;
        this.isStatic = false;
    }

    public IRVariable(String name, TypeSymbol type, boolean isStatic) {
        this.name = name;
        this.type = type;
        this.isStatic = isStatic;
    }

    public static IRVariable getDummy() {
        return new IRVariable("", PrimitiveTypes.TigerInt);
    }

    public String getName() {
        return this.name;
    }

    public TypeSymbol getType() {
        return this.type;
    }

    public String getReference() {
        return getName();
    }

    public boolean isStatic() {
        return this.isStatic;
    }

    public void setStatic() {
        this.isStatic = true;
    }

    public String printName() {
        if (type.isArray())
            return String.format("%s[%s]", name, type.getArrayLength());
        return name;
    }
    public String toString() {
        return String.format("%s %s", type.getName(), name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IRVariable v = (IRVariable) o;
        return name.equals(v.getName()) &&
                type.equals(v.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }
}
