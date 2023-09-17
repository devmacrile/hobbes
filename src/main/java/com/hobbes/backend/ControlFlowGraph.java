package com.hobbes.backend;

import com.hobbes.ir.IRBlock;
import com.hobbes.ir.IRFunction;
import com.hobbes.ir.IRProgram;

import java.util.*;

public class ControlFlowGraph {

    private Map<IRFunction, BasicBlock> funcRootBlocks;

    public ControlFlowGraph() {
        this.funcRootBlocks = new HashMap<>();
    }

    public void build(IRProgram ir) {
        for (IRFunction function : ir.getFunctions()) {
            Map<Integer, BasicBlock> basicBlockCache = new HashMap<>();
            BasicBlock rootBlock = new BasicBlock(function.getName(), basicBlockCache, 0);
            IRBlock functionBody = function.getBody();
            rootBlock.build(functionBody.getStatements());
            funcRootBlocks.put(function, rootBlock);
        }
    }

    public List<BasicBlock> getRootBlocks() {
        return new ArrayList<>(funcRootBlocks.values());
    }

    public BasicBlock getRootBasicBlock(IRFunction function) {
        return funcRootBlocks.get(function);
    }
}
