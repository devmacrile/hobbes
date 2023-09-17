package com.hobbes.ir.ops;

import com.hobbes.ir.IROpCodes;
import com.hobbes.ir.IRValue;
import com.hobbes.ir.IRVariable;

public class IRDiv extends IRBinaryOperation {
    public IRDiv(IRValue left, IRValue right, IRVariable out) {
        super(left, right, out);
    }
    protected String getOpCode() {
        return IROpCodes.Div;
    }
}
