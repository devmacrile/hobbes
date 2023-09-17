package com.hobbes.ir.ops;

import com.hobbes.ir.IROpCodes;
import com.hobbes.ir.IRValue;
import com.hobbes.ir.IRVariable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class IRArrayLoad extends IROperation {

    private IRVariable array;
    private IRValue arrayIndex;
    private IRVariable destination;

    public IRArrayLoad(IRVariable array, IRValue arrayIndex, IRVariable destination) {
        this.array = array;
        this.arrayIndex = arrayIndex;
        this.destination = destination;
    }

    @Override
    public Optional<IRVariable> getDestinationVariable() {
        return Optional.of(destination);
    }

    @Override
    public List<IRValue> getOperands() {
        return Arrays.asList(array, arrayIndex);
    }

    public IRVariable getArray() {
        return array;
    }

    public IRValue getArrayIndex() {
        return arrayIndex;
    }

    public String emit() {
        return String.format("  %s, %s, %s, %s", IROpCodes.ArrayLoad, destination.getName(), array.getName(), arrayIndex.getReference());
    }
}
