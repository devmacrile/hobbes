package com.hobbes.ir;

import com.hobbes.symbol.PrimitiveTypes;
import com.hobbes.symbol.TypeSymbol;

public class IRConstant implements IRValue {

    private TypeSymbol type;
    private String value;

    public IRConstant(TypeSymbol type, String value) {
        this.type = type;
        this.value = value;
    }

    public TypeSymbol getType() {
        return this.type;
    }

    public String getReference() {
        return this.value;
    }

    public static IRConstant Zero() {
        return new IRConstant(PrimitiveTypes.TigerInt, "0");
    }

    public static IRConstant Zero(TypeSymbol type) {
        if (type.getBaseType().equals(PrimitiveTypes.TigerFloat))
            return new IRConstant(PrimitiveTypes.TigerFloat, "0.0");
        return Zero();
    }

    public static IRConstant One() {
        return new IRConstant(PrimitiveTypes.TigerInt, "1");
    }

    public static IRConstant One(TypeSymbol type) {
        if (type.getBaseType().equals(PrimitiveTypes.TigerFloat))
            return new IRConstant(PrimitiveTypes.TigerFloat, "1.0");
        return One();
    }

}
