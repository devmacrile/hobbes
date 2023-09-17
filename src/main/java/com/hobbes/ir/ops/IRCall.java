package com.hobbes.ir.ops;

import com.hobbes.ir.IRFunction;
import com.hobbes.ir.IROpCodes;
import com.hobbes.ir.IRValue;
import com.hobbes.ir.IRVariable;
import com.hobbes.symbol.FunctionSymbol;
import com.hobbes.symbol.ParameterSymbol;
import com.hobbes.symbol.TypeSymbol;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IRCall extends IROperation {

    private FunctionSymbol function;
    private List<IRValue> args;

    public IRCall(FunctionSymbol function, List<IRValue> args) {
        this.function = function;
        this.args = args;
    }

    public int argCount() {
        return args.size();
    }

    public List<IRValue> getArguments() {
        return this.args;
    }

    public String getFunctionName() {
        return this.function.getName();
    }

    public List<TypeSymbol> getParameterTypes() {
        return function.getParameters().stream().map(ParameterSymbol::getType).collect(Collectors.toList());
    }

    @Override
    public List<IRValue> getOperands() {
        return getArguments();
    }

    public String emit() {
        return String.format("  %s, %s, %s", IROpCodes.Call, function.getName(), String.join(", ", argStrings()));
    }

    private List<String> argStrings() {
        return args.stream().map(IRValue::getReference).collect(Collectors.toList());
    }
}
