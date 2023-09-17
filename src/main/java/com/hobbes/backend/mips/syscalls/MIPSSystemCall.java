package com.hobbes.backend.mips.syscalls;

import com.hobbes.backend.mips.MIPSLabel;
import com.hobbes.backend.mips.instructions.MIPSInstruction;
import com.hobbes.util.StringLineBuilder;

import java.util.List;

public abstract class MIPSSystemCall {

    public MIPSSystemCall() {}
    protected abstract String getName();
    protected abstract List<MIPSInstruction> instructions();
    public String emit() {
        StringLineBuilder slb = new StringLineBuilder();
        MIPSInstruction label = new MIPSLabel(getName());
        slb.appendLine(label.emit());
        for (MIPSInstruction instruction : instructions()) {
            if (instruction instanceof MIPSLabel)
                slb.appendLine(instruction.emit());
            else
                slb.appendLine(instruction.emit(), "  ");
        }
        return slb.toString();
    }
}
