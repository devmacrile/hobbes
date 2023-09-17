package com.hobbes.backend.allocation;

import com.hobbes.backend.mips.registers.MIPSRegister;
import com.hobbes.ir.IRConstant;
import com.hobbes.ir.IRValue;

import java.util.Optional;

public class TempAllocation {

    private final MIPSRegister register;
    private final IRValue renter;
    private IRValue owner;
    private boolean isSpill;

    public TempAllocation(MIPSRegister register, IRValue renter, IRValue owner) {
        this.register = register;
        this.renter = renter;
        this.owner = owner;
    }

    public TempAllocation(MIPSRegister register, IRValue renter) {
        this.register = register;
        this.renter = renter;
        this.owner = null;
    }

    public MIPSRegister getRegister() {
        return this.register;
    }

    public IRValue getRenter() {
        return this.renter;
    }

    public Optional<IRValue> getOwner() {
        if (owner == null)
            return Optional.empty();
        return Optional.of(owner);
    }

    public boolean isSpill() {
        return owner != null && !(owner instanceof IRConstant);
    }
}
