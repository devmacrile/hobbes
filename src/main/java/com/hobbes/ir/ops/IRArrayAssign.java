package com.hobbes.ir.ops;

import com.hobbes.ir.IRConstant;
import com.hobbes.ir.IROpCodes;
import com.hobbes.ir.IRValue;
import com.hobbes.ir.IRVariable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class IRArrayAssign extends IROperation {

    private IRVariable array;
    private IRConstant value;
    private int arrayLength;

    public IRArrayAssign(IRVariable array, IRConstant value, int arrayLength) {
        this.array = array;
        this.value = value;
        this.arrayLength = arrayLength;
    }

    @Override
    public Optional<IRVariable> getDestinationVariable() {
        return Optional.of(array);
    }

    @Override
    public List<IRValue> getOperands() {
        return Collections.singletonList(value);
    }

    public IRVariable getArray() {
        return array;
    }

    public int getArrayLength() {
        return arrayLength;
    }

    public IRConstant getValue() {
        return value;
    }

    public String emit() {
        return String.format("  %s, %s, %d, %s", IROpCodes.Assign, array.getName(), arrayLength, value.getReference());
    }
}
