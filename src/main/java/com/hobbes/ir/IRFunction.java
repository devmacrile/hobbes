package com.hobbes.ir;

import com.hobbes.symbol.NullTypeSymbol;
import com.hobbes.symbol.PrimitiveTypes;
import com.hobbes.symbol.TypeSymbol;
import com.hobbes.util.StringLineBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class IRFunction {

    private static final String MAIN = "main";
    private String name;
    private TypeSymbol returnType;
    private List<IRVariable> params;
    private IRBlock body;

    public IRFunction(String name, TypeSymbol returnType, List<IRVariable> params) {
        this.name = name;
        this.returnType = returnType;
        this.params = params;
        this.body = new IRBlock();
        body.addStatement(new IRLocationIdentifier(name));
    }

    public void addInitializationStatements(List<IRStatement> initializationStatements) {
        body.prependStatements(initializationStatements);
    }

    public String getName() {
        return this.name;
    }

    public IRBlock getBody() {
        return this.body;
    }

    public List<IRVariable> getLocalVariables() {
        return this.body.getVariables();
    }

    public List<IRVariable> getParams() {
        return this.params;
    }

    public void append(IRBlock block) {
        body.append(block);
    }

    public boolean isMain() {
        return name.equals(MAIN);
    }

    public String toString() {
        String intNames = String.join(", ", getIntNames());
        String floatNames = String.join(", ", getFloatNames());
        StringLineBuilder sb = new StringLineBuilder();
        sb.append("start_function ").appendLine(name);
        sb.append("  ").appendLine(getSignature());
        sb.append("  int-list: ").appendLine(intNames);
        sb.append("  float-list: ").appendLine(floatNames);
        for (IRStatement statement : body.getStatements()) {
            sb.append("  ").appendLine(statement.emit());
        }
        sb.append("end_function ").append(name);
        return sb.toString();
    }

    private String getSignature() {
        if (returnType.equals(new NullTypeSymbol()))
            return String.format("void %s(%s)", name, paramSignature());
        return String.format("%s %s(%s)", returnType.getName(), name, paramSignature());
    }

    private String paramSignature() {
        return params.stream()
                .map(IRVariable::toString)
                .collect(Collectors.joining(", "));
    }

    private List<String> getIntNames() {
        List<String> names = getVariableNames(PrimitiveTypes.TigerInt);
        names.addAll(getParameterNames(PrimitiveTypes.TigerInt));
        return names;
    }

    private List<String> getFloatNames() {
        List<String> names = getVariableNames(PrimitiveTypes.TigerFloat);
        names.addAll(getParameterNames(PrimitiveTypes.TigerFloat));
        return names;
    }

    private List<String> getParameterNames(TypeSymbol type) {
        return getIRVariableNames(params, type);
    }

    private List<String> getVariableNames(TypeSymbol type) {
        return getIRVariableNames(body.getVariables(), type);
    }

    private List<String> getIRVariableNames(List<IRVariable> variableList, TypeSymbol type) {
        return variableList
                .stream()
                .filter(var -> var.getType().getBaseType().equals(type))
                .map(IRVariable::printName)
                .collect(Collectors.toList());
    }
}
