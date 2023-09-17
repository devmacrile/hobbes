package com.hobbes.backend.mips;

import com.hobbes.ir.IRProgram;
import com.hobbes.ir.IRVariable;
import com.hobbes.symbol.PrimitiveTypes;
import com.hobbes.util.StringLineBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MIPSData {

    private Set<IRVariable> staticVariables;
    private Map<IRVariable, Integer> gpOffsets;

    public MIPSData(Set<IRVariable> staticVariables) {
        this.staticVariables = staticVariables;
        this.gpOffsets = new HashMap<>();
        int offset = 0;
        for (IRVariable variable : staticVariables) {
            gpOffsets.put(variable, offset);
            if (variable.getType().isArray())
                offset += variable.getType().getArrayLength() * MIPSConstants.WORD_SIZE;
            else
                offset += MIPSConstants.WORD_SIZE;
        }
    }

    public int getGpOffset(IRVariable variable) {
        return gpOffsets.get(variable);
    }

    public boolean isStatic(IRVariable variable) {
        return staticVariables.contains(variable);
    }

    public String emit() {
        StringLineBuilder slb = new StringLineBuilder();
        slb.appendLine(".data");
        for (IRVariable variable : staticVariables) {
            if (variable.getType().isArray()) {
                slb.appendLine(String.format("%s: .space %d", variable.getName(), variable.getType().getArrayLength() * MIPSConstants.WORD_SIZE));
            } else if (variable.getType().getBaseType().equals(PrimitiveTypes.TigerInt)) {
                slb.appendLine(String.format("%s: .word 0", variable.getName()));
            } else {
                slb.appendLine(String.format("%s: .float 0.0", variable.getName()));
            }
        }
        return slb.toString();
    }
}
