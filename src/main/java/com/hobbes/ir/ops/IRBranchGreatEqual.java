package com.hobbes.ir.ops;

import com.hobbes.ir.IRLocationIdentifier;
import com.hobbes.ir.IROpCodes;
import com.hobbes.ir.IRValue;

public class IRBranchGreatEqual extends IRBranchOperation {
    public IRBranchGreatEqual(IRValue left, IRValue right, IRLocationIdentifier to) {
        super(left, right, to);
    }
    protected String getOpCode() {
        return IROpCodes.BranchGreatEq;
    }
}
