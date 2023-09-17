package com.hobbes.symbol;

public class TypeSymbol extends BaseSymbol {

    private TypeSymbol baseType;
    private int arrayLength;
    private boolean isDerived;

    public TypeSymbol(String name) {
        super(name);
        this.baseType = this;
        this.arrayLength = 0;
        this.isDerived = false;
    }

    public TypeSymbol(String name, TypeSymbol baseType) {
        super(name);
        this.baseType = baseType;
        this.arrayLength = 0;
        this.isDerived = true;
    }

    public boolean isDerived() {
        return this.isDerived;
    }

    public boolean isArray() {
        return this.arrayLength > 0;
    }

    public TypeSymbol getBaseType() {
        return this.baseType;
    }

    public void setBaseType(TypeSymbol type) {
        this.baseType = type;
    }

    public int getArrayLength() {
        return this.arrayLength;
    }

    public void setArrayLength(int arrayLength) {
        this.arrayLength = arrayLength;
    }

    public TypeSymbol resolve(TypeSymbol other) {
        if (baseType.equals(PrimitiveTypes.TigerInt) && other.getBaseType().equals(PrimitiveTypes.TigerInt))
            return PrimitiveTypes.TigerInt;
        return PrimitiveTypes.TigerFloat;
    }

    public boolean canCast(TypeSymbol that) {
        // equal method covers case where arrays are equal
        if (this.equals(that))
            return true;
        // direct derived type from base type
        if (!isArray() && !that.isArray() && this.equals(that.getBaseType()) || that.equals(this.getBaseType()))
            return true;
        // float can be cast to int
        if (!isArray() && this.equals(PrimitiveTypes.TigerInt) || this.baseType.equals(PrimitiveTypes.TigerInt))
            return !that.isArray() && that.equals(PrimitiveTypes.TigerFloat) || that.getBaseType().equals(PrimitiveTypes.TigerFloat);
        return false;
    }

    public boolean equals(TypeSymbol other) {
        if (isArray()) {
            return other.isArray() && other.getArrayLength() == arrayLength && other.getBaseType().equals(baseType);
        }
        if (isDerived()) {
            return baseType.equals(other.baseType);
        }
        return getName().equals(other.getName());
    }

    @Override
    public String toString() {
        String suffix = "";
        if (arrayLength > 0) {
            suffix += ", " + Integer.toString(arrayLength);
        }
        return String.format("%s, type, %s%s", getName(), baseType.getName(), suffix);
    }
}
