package com.hobbes.backend.mips;

import com.hobbes.backend.BasicBlock;
import com.hobbes.backend.ControlFlowGraph;
import com.hobbes.backend.liveness.ProgramPoint;
import com.hobbes.backend.liveness.LivenessAnalyzer;
import com.hobbes.backend.allocation.*;
import com.hobbes.backend.liveness.InterferenceGraph;
import com.hobbes.backend.mips.instructions.MIPSComment;
import com.hobbes.backend.mips.instructions.MIPSInstruction;
import com.hobbes.backend.mips.registers.MIPSRegister;
import com.hobbes.backend.mips.syscalls.MIPSExit;
import com.hobbes.backend.mips.syscalls.MIPSNot;
import com.hobbes.backend.mips.syscalls.MIPSPrint;
import com.hobbes.backend.mips.syscalls.MIPSSystemCall;
import com.hobbes.ir.IRFunction;
import com.hobbes.ir.IRProgram;
import com.hobbes.util.StringLineBuilder;

import java.util.*;

public class MIPSProgram {

    private IRProgram irProgram;
    private MIPSData dataSection;
    private ControlFlowGraph cfg;
    private Map<IRFunction, List<ProgramPoint>> liveness;
    private RegisterAllocatorType allocatorType;
    private final MIPSSystemCall printi = new MIPSPrint("printi", MIPSConstants.PRINTI);
    private final MIPSSystemCall printf = new MIPSPrint("printf", MIPSConstants.PRINTF);
    private final MIPSSystemCall exit = new MIPSExit();
    private final MIPSSystemCall not = new MIPSNot();

    public MIPSProgram(IRProgram irProgram, ControlFlowGraph cfg, RegisterAllocatorType allocatorType) {
        this.irProgram = irProgram;
        this.dataSection = new MIPSData(irProgram.getStaticVariables());
        this.cfg = cfg;
        this.liveness = new HashMap<>();
        this.allocatorType = allocatorType;
    }

    public String generateAsm() {
        StringLineBuilder slb = new StringLineBuilder();
        slb.appendLine(dataSection.emit());
        slb.appendLine(textSection());
        return slb.toString();
    }

    public String printLiveVariableSets() {
        StringLineBuilder slb = new StringLineBuilder();
        for (IRFunction function : irProgram.getFunctions()) {
            if (!liveness.containsKey(function))
                runLivenessAnalysis(function);
            slb.appendLine("");
            slb.appendLine(function.getName());
            List<ProgramPoint> programPoints = liveness.get(function);
            int maxStatementLength = 32;
            for (ProgramPoint pp : programPoints)
                if (pp.getStatement().emit().length() > maxStatementLength)
                    maxStatementLength = pp.getStatement().emit().length();
            for (int lineNumber = 0; lineNumber < programPoints.size(); lineNumber++) {
                slb.appendLine(String.format("%-6d  %s", lineNumber, programPoints.get(lineNumber).toString(maxStatementLength)));
            }
        }
        return slb.toString();
    }

    private String textSection() {
        StringLineBuilder slb = new StringLineBuilder();
        slb.appendLine(".text");
        slb.appendLine(".globl main");
        slb.appendLine(generateFunctionAsm(irProgram.getMain()));
        for (IRFunction function : irProgram.getNonMainFunctions())
            slb.appendLine(generateFunctionAsm(function));
        slb.appendLine(printi.emit());
        slb.appendLine(printf.emit());
        slb.appendLine(exit.emit());
        slb.appendLine(not.emit());
        return slb.toString();
    }

    private String generateFunctionAsm(IRFunction function) {
        List<ProgramPoint> inOutSets = runLivenessAnalysis(function);
        InterferenceGraph interferenceGraph = new InterferenceGraph(inOutSets);
        interferenceGraph.build();
        return switch (allocatorType) {
            case NAIVE -> generateGlobalAsm(function, new NaiveAllocator(function, dataSection));
            case INTRABLOCK -> generateLocalAsm(function);
            case BRIGGS -> generateGlobalAsm(function, new BriggsAllocator(function, dataSection, interferenceGraph));
        };
    }

    private String generateGlobalAsm(IRFunction function, RegisterAllocator allocator) {
        allocator.run();
        StackFrame stackFrame = new StackFrame(function, allocator.getSpillVariables());
        MIPSCodeGenerator asmGenerator = new MIPSCodeGenerator(dataSection, allocator, function, stackFrame);
        List<MIPSInstruction> asm = asmGenerator.generate(function.getBody().getStatements());
        return (new MIPSFunction(function, stackFrame, asm, allocator.getAllocatedRegisters())).toString();
    }

    private String generateLocalAsm(IRFunction function) {
        Map<Integer, List<MIPSInstruction>> blockAsm = new HashMap<>();
        Set<MIPSRegister> allocatedRegisters = new HashSet<>();
        StackFrame stackFrame = new StackFrame(function, function.getLocalVariables());
        Queue<BasicBlock> q = new LinkedList<>(
                Collections.singletonList(cfg.getRootBasicBlock(function))
        );
        while (!q.isEmpty()) {
            BasicBlock block = q.remove();
            if (!blockAsm.containsKey(block.getLeaderIndex())) {
                RegisterAllocator allocator = new LocalIntraBlockAllocator(function, block.getStatements(), dataSection);
                allocator.run();
                allocatedRegisters.addAll(allocator.getAllocatedRegisters());
                MIPSCodeGenerator asmGenerator = new MIPSCodeGenerator(dataSection, allocator, function, stackFrame);
                blockAsm.put(block.getLeaderIndex(), asmGenerator.generate(block.getStatements()));
                for (BasicBlock succ : block.getSuccessors()) {
                    if (!blockAsm.containsKey(succ.getLeaderIndex()))
                        q.add(succ);
                }
            }
        }
        // block tree-walk not necessarily in order, so get properly ordered instructions
        List<MIPSInstruction> asm = new ArrayList<>();
        for (int i = 0; i < function.getBody().getStatements().size(); i++) {
            if (blockAsm.containsKey(i)) {
                asm.add(new MIPSComment(String.format("Block %d", i)));
                asm.addAll(blockAsm.get(i));
            }
        }
        return (new MIPSFunction(function, stackFrame, asm, new ArrayList<>(allocatedRegisters))).toString();
    }

    private List<ProgramPoint> runLivenessAnalysis(IRFunction function) {
        if (liveness.containsKey(function))
            return liveness.get(function);
        LivenessAnalyzer analyzer = new LivenessAnalyzer(function, cfg.getRootBasicBlock(function));
        analyzer.run();
        liveness.put(function, analyzer.getProgramPoints());
        return liveness.get(function);
    }
}
