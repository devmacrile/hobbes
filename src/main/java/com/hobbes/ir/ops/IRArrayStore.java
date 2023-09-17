package com.hobbes.ir.ops;

import com.hobbes.ir.IROpCodes;
import com.hobbes.ir.IRValue;
import com.hobbes.ir.IRVariable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class IRArrayStore extends IROperation {

    private IRVariable array;
    private IRValue arrayIndex;
    private IRValue source;

    public IRArrayStore(IRVariable array, IRValue arrayIndex, IRValue source) {
        this.array = array;
        this.arrayIndex = arrayIndex;
        this.source = source;
    }

    @Override
    public Optional<IRVariable> getDestinationVariable() {
        return Optional.of(array);
    }

    @Override
    public List<IRValue> getOperands() {
        return Arrays.asList(source, arrayIndex);
    }

    public IRVariable getArray() {
        return array;
    }

    public IRValue getArrayIndex() {
        return arrayIndex;
    }

    public IRValue getSource() {
        return source;
    }

    public String emit() {
        return String.format("  %s, %s, %s, %s", IROpCodes.ArrayStore, array.getName(), arrayIndex.getReference(), source.getReference());
    }
}
