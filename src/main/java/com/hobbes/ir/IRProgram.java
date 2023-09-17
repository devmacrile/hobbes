package com.hobbes.ir;

import com.hobbes.symbol.PrimitiveTypes;
import com.hobbes.symbol.TypeSymbol;
import com.hobbes.util.StringLineBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IRProgram {

    private String name;
    private Set<IRVariable> staticVariables;
    private List<IRFunction> functions;

    public IRProgram(String name) {
        this.name = name;
        this.staticVariables = new HashSet<>();
        this.functions = new ArrayList<>();
    }

    public void addVariable(IRVariable variable) {
        this.staticVariables.add(variable);
    }

    public void addVariables(List<IRVariable> variables) {
        for (IRVariable variable : variables) {
            variable.setStatic();
            this.staticVariables.add(variable);
        }
    }

    public void addFunction(IRFunction function) {
        this.functions.add(function);
    }

    public void addFunctions(List<IRFunction> funcList) {
        this.functions.addAll(funcList);
    }

    public List<IRFunction> getFunctions() {
        return this.functions;
    }

    public Set<IRVariable> getStaticVariables() {
        return this.staticVariables;
    }

    public IRFunction getMain() {
        for (IRFunction function : this.functions) {
            if (function.isMain())
                return function;
        }
        assert false;
        return null;
    }

    public List<IRFunction> getNonMainFunctions() {
        return this.functions.stream().filter(f -> !f.isMain()).collect(Collectors.toList());
    }

    public String toString() {
        String staticInts = String.join(", ", getStaticIntNames());
        String staticFloats = String.join(", ", getStaticFloatNames());
        StringLineBuilder sb = new StringLineBuilder();
        sb.append("start_program ").appendLine(name);
        sb.append("  static-int-list: ").appendLine(staticInts);
        sb.append("  static-float-list: ").appendLine(staticFloats);
        for (IRFunction function : functions) {
            sb.appendLine("");
            sb.appendLine(function.toString());
        }
        sb.appendLine("");
        sb.append("end_program ").append(name);
        return sb.toString();
    }

    private List<String> getStaticIntNames() {
        return getStaticVariableNames(PrimitiveTypes.TigerInt);
    }

    private List<String> getStaticFloatNames() {
        return getStaticVariableNames(PrimitiveTypes.TigerFloat);
    }

    private List<String> getStaticVariableNames(TypeSymbol type) {
        return staticVariables
                .stream()
                .filter(var -> var.getType().getBaseType().equals(type))
                .map(IRVariable::printName)
                .collect(Collectors.toList());
    }

}
