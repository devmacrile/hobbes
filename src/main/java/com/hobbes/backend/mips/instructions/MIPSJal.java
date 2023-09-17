package com.hobbes.backend.mips.instructions;

import com.hobbes.backend.mips.MIPSConstants;
import com.hobbes.backend.mips.syscalls.MIPSNot;

public class MIPSJal implements MIPSInstruction {

    private String locationName;

    public MIPSJal(String locationName) {
        this.locationName = locationName.equals(MIPSConstants.RESERVED_NOT) ? (new MIPSNot()).getName() : locationName;
    }

    public String emit() {
        return String.format("jal %s", locationName);
    }
}
