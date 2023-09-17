package com.hobbes.ir.ops;

import com.hobbes.ir.IROpCodes;
import com.hobbes.ir.IRValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class IRReturn extends IROperation {

    private Optional<IRValue> returnValue;

    public IRReturn() {
        this.returnValue = Optional.empty();
    }

    public IRReturn(IRValue returnValue) {
        this.returnValue = Optional.of(returnValue);
    }

    @Override
    public List<IRValue> getOperands() {
        return returnValue.map(Collections::singletonList).orElseGet(ArrayList::new);
    }

    public boolean hasReturnValue() {
        return returnValue.isPresent();
    }

    public IRValue getReturnValue() {
        return returnValue.get();
    }

    public String emit() {
        return String.format("  %s, %s, ,", IROpCodes.Return, returnValue.isPresent() ? returnValue.get().getReference() : "");
    }
}
