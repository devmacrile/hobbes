package com.hobbes.ir;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IRBlock {

    private IRValue currentIdentifier;
    private List<IRStatement> statements;
    private List<IRVariable> variables;

    public IRBlock() {
        this.currentIdentifier = null;
        this.statements = new ArrayList<>();
        this.variables = new ArrayList<>();
    }

    public IRValue getCurrentIdentifier() {
        return this.currentIdentifier;
    }
    public void setCurrentIdentifier(IRValue identifier) {
        this.currentIdentifier = identifier;
    }

    public List<IRStatement> getStatements() {
        return statements;
    }

    public List<IRVariable> getVariables() {
        return variables;
    }

    public List<String> getVariableNames() {
        return variables.stream().map(IRVariable::getName).collect(Collectors.toList());
    }

    public void prependStatements(List<IRStatement> irStatements) {
        if (statements.isEmpty())
            statements = irStatements;
        else
            statements.addAll(1, irStatements);
    }
    public void addStatement(IRStatement statement) {
        //if (statement instanceof IROperation)
        //    if (((IROperation) statement).getTempVariable().isPresent())
        //        currentIdentifier = ((IROperation) statement).getTempVariable().get().getName();
        statements.add(statement);
    }

    public void addVariable(IRVariable variable) {
        currentIdentifier = variable;
        variables.add(variable);
    }

    public void append(IRBlock other) {
        if (other.getCurrentIdentifier() != null)
            currentIdentifier = other.getCurrentIdentifier();
        statements.addAll(other.getStatements());
        variables.addAll(other.getVariables());
    }

    public boolean isEmpty() {
        return statements.isEmpty() && variables.isEmpty();
    }
}
