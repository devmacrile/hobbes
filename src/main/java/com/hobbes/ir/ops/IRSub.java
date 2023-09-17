package com.hobbes.ir.ops;

import com.hobbes.ir.IRConstant;
import com.hobbes.ir.IROpCodes;
import com.hobbes.ir.IRValue;
import com.hobbes.ir.IRVariable;
import com.hobbes.symbol.PrimitiveTypes;

public class IRSub extends IRBinaryOperation {
    public IRSub(IRValue left, IRValue right, IRVariable out) {
        super(left, right, out);
    }
    protected String getOpCode() {
        return IROpCodes.Sub;
    }
    public static IRSub dec(IRVariable variable) {
        return new IRSub(variable, new IRConstant(PrimitiveTypes.TigerInt, "1"), variable);
    }
}
