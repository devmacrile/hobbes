package com.hobbes.backend.liveness;

import com.hobbes.ir.IRStatement;
import com.hobbes.ir.IRVariable;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ProgramPoint {

    IRStatement statement;
    private Set<IRVariable> inSet;
    private Set<IRVariable> outSet;

    public ProgramPoint(IRStatement statement) {
        this.statement = statement;
        this.inSet = new HashSet<>();
        this.outSet = new HashSet<>();
    }

    public IRStatement getStatement() {
        return this.statement;
    }

    public Set<IRVariable> getInSet() {
        return this.inSet;
    }

    public void addToInSet(IRVariable variable) {
        inSet.add(variable);
    }

    public void removeFromInSet(IRVariable variable) {
        inSet.remove(variable);
    }

    public Set<IRVariable> getOutSet() {
        return this.outSet;
    }

    public void addToOutSet(IRVariable variable) {
        outSet.add(variable);
    }

    public void removeFromOutSet(IRVariable variable) {
        outSet.remove(variable);
    }

    public String toString(int maxStatementSize) {
        String inSetString = inSet.stream().map(IRVariable::getName).collect(Collectors.joining(","));
        String outSetString = outSet.stream().map(IRVariable::getName).collect(Collectors.joining(","));
        return String.format("%-" + maxStatementSize + "s in = { %s }, out = { %s }", statement.emit().trim(), inSetString , outSetString);
    }
}
