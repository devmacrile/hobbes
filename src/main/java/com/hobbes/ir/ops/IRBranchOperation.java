package com.hobbes.ir.ops;

import com.hobbes.ir.IRLocationIdentifier;
import com.hobbes.ir.IRValue;

import java.util.Arrays;
import java.util.List;

public abstract class IRBranchOperation extends IRControlFlowOperation {

    private IRValue left;
    private IRValue right;
    private IRLocationIdentifier to;

    public IRBranchOperation(IRValue left, IRValue right, IRLocationIdentifier to) {
        super(to);
        this.to = to;
        this.left = left;
        this.right = right;
    }

    abstract protected String getOpCode();

    public String emit() {
        return String.format("  %s, %s, %s, %s", getOpCode(), left.getReference(), right.getReference(), to.getName());
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

    public IRLocationIdentifier getLabel() {
        return this.to;
    }
}
