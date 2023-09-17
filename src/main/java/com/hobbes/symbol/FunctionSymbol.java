package com.hobbes.symbol;

import java.util.List;

public class FunctionSymbol extends BaseSymbol {

    private TypeSymbol returnType;
    private final List<ParameterSymbol> parameters;

    public FunctionSymbol(String name, List<ParameterSymbol> parameters) {
        super(name);
        this.returnType = new NullTypeSymbol();
        this.parameters = parameters;
    }
    public FunctionSymbol(String name, TypeSymbol returnType, List<ParameterSymbol> parameters) {
        super(name);
        this.returnType = returnType;
        this.parameters = parameters;
    }

    public TypeSymbol getReturnType() {
        return this.returnType;
    }
    public void setReturnType(TypeSymbol returnType) {
        this.returnType = returnType;
    }

    public List<ParameterSymbol> getParameters() {
        return this.parameters;
    }

    public int numParameters() {
        return this.parameters.size();
    }

    @Override
    public String toString() {
        return String.format("%s, func, %s", getName(), returnType.getName());
    }
}
