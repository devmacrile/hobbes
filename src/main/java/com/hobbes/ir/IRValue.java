package com.hobbes.ir;

import com.hobbes.symbol.TypeSymbol;

public interface IRValue {
    TypeSymbol getType();
    String getReference();
}
