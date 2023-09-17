package com.hobbes.backend.mips;

import com.hobbes.backend.mips.instructions.*;
import com.hobbes.backend.mips.instructions.fp.MIPSLoadOffsetF;
import com.hobbes.backend.mips.instructions.fp.MIPSStoreOffsetF;
import com.hobbes.backend.mips.registers.MIPSGRegister;
import com.hobbes.backend.mips.registers.MIPSRegister;
import com.hobbes.backend.mips.registers.MIPSRegisters;
import com.hobbes.ir.IRFunction;
import com.hobbes.util.StringLineBuilder;

import java.util.ArrayList;
import java.util.List;

public class MIPSFunction {

    private IRFunction function;
    private StackFrame stackFrame;
    private List<MIPSInstruction> body;
    private List<MIPSRegister> allocatedRegisters;

    public MIPSFunction(IRFunction function, StackFrame stackFrame, List<MIPSInstruction> body, List<MIPSRegister> allocatedRegisters) {
        this.function = function;
        this.stackFrame = stackFrame;
        this.body = body;
        this.allocatedRegisters = allocatedRegisters;
    }

    public static MIPSLabel getEpilogueLabel(String functionName) {
        return new MIPSLabel(String.format("_%s_epilogue", functionName));
    }

    public String toString() {
        StringLineBuilder slb = new StringLineBuilder();
        slb.appendLine(String.format("%s:", function.getName()));
        slb.appendLine("# prologue", "  ");
        for (MIPSInstruction instruction : prologue())
            slb.appendLine(instruction.emit(), "  ");
        slb.appendLine("").appendLine("# body", "  ");
        for (MIPSInstruction instruction : body) {
            if (instruction instanceof MIPSLabel label) {
                if (!label.getName().equals(function.getName()))
                    slb.appendLine(instruction.emit());
            } else {
                slb.appendLine(instruction.emit(), "  ");
            }
        }
        slb.appendLine("").appendLine("# epilogue", "  ");
        for (MIPSInstruction instruction : epilogue())
            if (instruction instanceof MIPSLabel label)
                slb.appendLine(instruction.emit());
            else
                slb.appendLine(instruction.emit(), "  ");
        return slb.toString();
    }

    private List<MIPSInstruction> prologue() {
        // Create room on the stack for the new call frame by decrementing the stack pointer.
        // Store the return address register $ra to the stack
        // Store any used $s registers to the stack
        List<MIPSInstruction> instructions = new ArrayList<>();
        instructions.add(new MIPSAddIU(MIPSRegisters.SP, MIPSRegisters.SP, -stackFrame.getSize()));
        instructions.add(new MIPSStoreOffset(MIPSRegisters.RA, stackFrame.getRegisterOffset(MIPSRegisters.RA)));
        if (!function.isMain()) {
            for (MIPSRegister register : allocatedRegisters) {
                if (register.isSave()) {
                    if (register instanceof MIPSGRegister)
                        instructions.add(new MIPSStoreOffset(register, stackFrame.getRegisterOffset(register)));
                    else
                        instructions.add(new MIPSStoreOffsetF(register, stackFrame.getRegisterOffset(register)));
                }
            }
        }
        return instructions;
    }

    private List<MIPSInstruction> epilogue() {
        // Load any saved $s registers from stack back into register
        // Load the return address from stack back into $ra
        // Delete the call frame by incrementing the stack pointer
        List<MIPSInstruction> instructions = new ArrayList<>();
        instructions.add(getEpilogueLabel(function.getName()));
        if (!function.isMain()) {
            for (MIPSRegister register : allocatedRegisters) {
                if (register.isSave()) {
                    if (register instanceof MIPSGRegister)
                        instructions.add(new MIPSLoadOffset(register, stackFrame.getRegisterOffset(register)));
                    else
                        instructions.add(new MIPSLoadOffsetF(register, stackFrame.getRegisterOffset(register)));
                }
            }
        }
        instructions.add(new MIPSLoadOffset(MIPSRegisters.RA, stackFrame.getRegisterOffset(MIPSRegisters.RA)));
        instructions.add(new MIPSAddIU(MIPSRegisters.SP, MIPSRegisters.SP, stackFrame.getSize()));
        instructions.add(new MIPSJr());
        return instructions;
    }
}
