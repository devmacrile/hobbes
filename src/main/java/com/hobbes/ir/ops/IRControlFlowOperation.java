package com.hobbes.ir.ops;

import com.hobbes.ir.IRLocationIdentifier;

import java.util.Optional;

public abstract class IRControlFlowOperation extends IROperation {

    private IRLocationIdentifier to;

    public IRControlFlowOperation(IRLocationIdentifier to) {
        this.to = to;
    }

    public IRLocationIdentifier getTarget() {
        return to;
    }

    @Override
    public Optional<IRLocationIdentifier> getBlockIdentifier() {
        return Optional.of(to);
    }

}
