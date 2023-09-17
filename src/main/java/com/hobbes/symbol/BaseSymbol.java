package com.hobbes.symbol;

import com.hobbes.Scope;

public class BaseSymbol implements Symbol {

    private final String name;
    private Scope scope;

    public BaseSymbol(String name) {
        this.name = name;
        this.scope = null;
    }

    public BaseSymbol(String name, Scope scope) {
        this.name = name;
        this.scope = scope;
    }

    public String getName() {
        return this.name;
    }

    public Scope getScope() {
        return this.scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
}
