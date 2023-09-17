package com.hobbes.symbol;

import com.hobbes.Scope;

public interface Symbol {
    String getName();
    Scope getScope();
    void setScope(Scope scope);
    String toString();
}
