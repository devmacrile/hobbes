package com.hobbes.ir.ops;

import com.hobbes.ir.IRConstant;
import com.hobbes.ir.IROpCodes;
import com.hobbes.ir.IRValue;
import com.hobbes.ir.IRVariable;
import com.hobbes.symbol.PrimitiveTypes;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class IRAssign extends IROperation {

    private IRVariable left;
    private IRValue right;

    public IRAssign(IRVariable left, IRValue right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public Optional<IRVariable> getDestinationVariable() {
        return Optional.of(left);
    }

    @Override
    public List<IRValue> getOperands() {
        return Collections.singletonList(right);
    }

    public IRValue getRight() {
        return right;
    }

    public static IRAssign zero(String variableName) {
        return new IRAssign(new IRVariable(variableName, PrimitiveTypes.TigerInt), IRConstant.Zero());
    }

    public static IRAssign one(String variableName) {
        return new IRAssign(new IRVariable(variableName, PrimitiveTypes.TigerInt), IRConstant.One());
    }

    public String emit() {
        return String.format("  %s, %s, %s, ", IROpCodes.Assign, left.getName(), right.getReference());
    }
}
