package com.hobbes.backend.allocation;

import com.hobbes.backend.mips.registers.MIPSRegister;
import com.hobbes.ir.IRValue;
import com.hobbes.ir.IRVariable;

import java.util.List;

public interface RegisterAllocator {
    void run();
    List<IRValue> getAllocatedValues();
    List<MIPSRegister> getAllocatedRegisters();
    List<IRVariable> getSpillVariables();
    boolean isAllocated(IRValue value);
    MIPSRegister getAllocatedRegister(IRValue value);
    boolean isTempAllocated(IRValue value);
    MIPSRegister getTempAllocatedRegister(IRValue value);
    TempAllocation assignTempRegister(IRValue value);
    TempAllocation assignTempFRegister(IRValue value);
    TempAllocation assignTempFRegister();
    void clearTempAllocations();
}
