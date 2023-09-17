package com.hobbes.ir.ops;

import com.hobbes.ir.IRValue;
import com.hobbes.ir.IRVariable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class IRBinaryOperation extends IROperation {

    private IRValue left;
    private IRValue right;
    private IRVariable out;

    int loopDepth;

    public IRBinaryOperation(IRValue left, IRValue right, IRVariable out) {
        this.left = left;
        this.right = right;
        this.out = out;
    }

    abstract protected String getOpCode();

    public int getLoopDepth() {
        return loopDepth;
    }

    @Override
    public Optional<IRVariable> getTempVariable() {
        return Optional.of(out);
    }

    public String emit() {
        return String.format("  %s, %s, %s, %s", getOpCode(), out.getName(), left.getReference(), right.getReference());
    }

    @Override
    public Optional<IRVariable> getDestinationVariable() {
        return Optional.of(out);
    }

    @Override
    public List<IRValue> getOperands() {
        return Arrays.asList(left, right);
    }

    public IRValue getLeft() {
        return this.left;
    }

    public IRValue getRight() {
        return this.right;
    }
}
