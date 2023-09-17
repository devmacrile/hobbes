package com.hobbes.ir.ops;

import com.hobbes.ir.IRConstant;
import com.hobbes.ir.IRLocationIdentifier;
import com.hobbes.ir.IROpCodes;
import com.hobbes.ir.IRValue;
import com.hobbes.symbol.PrimitiveTypes;

public class IRBranchEqual extends IRBranchOperation {
    public IRBranchEqual(IRValue left, IRValue right, IRLocationIdentifier to) {
        super(left, right, to);
    }
    protected String getOpCode() {
        return IROpCodes.BranchEqual;
    }
    public static IRBranchEqual cond(IRValue left, IRLocationIdentifier to) {
        return new IRBranchEqual(left, new IRConstant(PrimitiveTypes.TigerInt, "0"), to);
    }
}
