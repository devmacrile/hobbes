package com.hobbes.ir.ops;

import com.hobbes.ir.IRConstant;
import com.hobbes.ir.IROpCodes;
import com.hobbes.ir.IRValue;
import com.hobbes.ir.IRVariable;
import com.hobbes.symbol.PrimitiveTypes;

public class IRAdd extends IRBinaryOperation {
    public IRAdd(IRValue left, IRValue right, IRVariable out) {
        super(left, right, out);
    }
    protected String getOpCode() {
        return IROpCodes.Add;
    }
    public static IRAdd inc(IRVariable variable) {
        return new IRAdd(variable, new IRConstant(PrimitiveTypes.TigerInt, "1"), variable);
    }
}
