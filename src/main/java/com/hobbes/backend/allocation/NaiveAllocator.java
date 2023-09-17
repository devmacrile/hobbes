package com.hobbes.backend.allocation;

import com.hobbes.backend.mips.MIPSData;
import com.hobbes.ir.IRFunction;
import com.hobbes.ir.IRVariable;

import java.util.List;

public class NaiveAllocator extends BaseRegisterAllocator {

    private IRFunction function;

    public NaiveAllocator(IRFunction function, MIPSData staticData) {
        super(function, staticData);
        this.function = function;
    }

    public void run() { }

    public List<IRVariable> getSpillVariables() {
        return function.getLocalVariables();
    }
}
