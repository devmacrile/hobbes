package com.hobbes.backend.allocation;

import com.hobbes.backend.mips.MIPSData;
import com.hobbes.ir.*;
import com.hobbes.ir.ops.IROperation;

import java.util.*;
import java.util.stream.Collectors;

public class LocalIntraBlockAllocator extends BaseRegisterAllocator {

    private IRFunction function;
    private List<IRStatement> statements;
    private List<IRValue> spillVariables;

    public LocalIntraBlockAllocator(IRFunction function, List<IRStatement> statements, MIPSData staticData) {
        super(function, staticData);
        this.function = function;
        this.statements = statements;
    }

    public void run() {
        Map<IRValue, Integer> useCounts = new HashMap<>();
        for (IRStatement statement : statements) {
            if (statement instanceof IROperation operation)
                for (IRValue operand : operation.getOperands())
                    useCounts.merge(operand, 1, Integer::sum);
        }
        List<IRValue> sortedByCount =
                useCounts.entrySet().stream()
                        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
        for (IRValue value : sortedByCount) {
            if (value.getType().isArray() || value instanceof IRConstant)
                continue;
            allocateRegister(value);
        }
    }

    public List<IRVariable> getSpillVariables() {
        // TODO if we can compute exact spill sets, then can
        // limit the stack frame size, but kiss for now
        return function.getLocalVariables();
    }
}
