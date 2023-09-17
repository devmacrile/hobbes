package com.hobbes.ir.ops;

import com.hobbes.ir.IRLocationIdentifier;
import com.hobbes.ir.IROpCodes;

import java.util.Optional;

public class IRJump extends IRControlFlowOperation {

    IRLocationIdentifier to;

    public IRJump(IRLocationIdentifier to) {
        super(to);
        this.to = to;
    }

    public IRLocationIdentifier getLabel() {
        return to;
    }

    public String emit() {
        return String.format("  %s, %s, ,", IROpCodes.Goto, to.getName());
    }

}
