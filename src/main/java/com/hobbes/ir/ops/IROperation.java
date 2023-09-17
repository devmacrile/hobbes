package com.hobbes.ir.ops;

import com.hobbes.ir.IRLocationIdentifier;
import com.hobbes.ir.IRValue;
import com.hobbes.ir.IRStatement;
import com.hobbes.ir.IRVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class IROperation implements IRStatement {

    public Optional<IRVariable> getTempVariable() {
        return Optional.empty();
    }

    public Optional<IRLocationIdentifier> getBlockIdentifier() {
        return Optional.empty();
    }

    public Optional<IRVariable> getDestinationVariable() {
        return Optional.empty();
    }
    public List<IRValue> getOperands() {
        return new ArrayList<>();
    }
}
