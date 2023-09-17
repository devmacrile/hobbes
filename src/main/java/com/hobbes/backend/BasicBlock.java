package com.hobbes.backend;

import com.hobbes.ir.IRLocationIdentifier;
import com.hobbes.ir.IRStatement;
import com.hobbes.ir.ops.IRBranchOperation;
import com.hobbes.ir.ops.IRControlFlowOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BasicBlock {

    private int leaderIndex;
    private String blockId;
    private String functionName;
    private Map<Integer, BasicBlock> cache;
    private List<IRStatement> statements;
    private List<BasicBlock> predecessors;
    private List<BasicBlock> successors;

    public BasicBlock(String functionName, Map<Integer, BasicBlock> basicBlockCache, int leaderIndex) {
        this.leaderIndex = leaderIndex;
        this.blockId = UUID.randomUUID().toString();
        this.functionName = functionName;
        this.cache = basicBlockCache;
        this.statements = new ArrayList<>();
        this.predecessors = new ArrayList<>();
        this.successors = new ArrayList<>();
    }

    public int getLeaderIndex() {
        return this.leaderIndex;
    }

    public void build(List<IRStatement> irStatements) {
        if (irStatements == null || irStatements.isEmpty()) {
            return;
        }
        this.cache.put(leaderIndex, this);
        this.statements.add(irStatements.get(leaderIndex));
        for (int i = leaderIndex + 1; i < irStatements.size(); i++) {
            IRStatement statement = irStatements.get(i);
            if (statement instanceof IRLocationIdentifier) {
                int locationIndex = findTarget(irStatements, (IRLocationIdentifier) statement);
                if (this.cache.containsKey(locationIndex))
                    this.successors.add(this.cache.get(locationIndex));
                else
                    buildAndAddSuccessor(irStatements, locationIndex);
                return;
            }
            this.statements.add(statement);
            if (statement instanceof IRControlFlowOperation cfo) {
                IRLocationIdentifier target = cfo.getBlockIdentifier().get();
                int targetIndex = findTarget(irStatements, target);
                assert targetIndex != -1;
                if (this.cache.containsKey(targetIndex))
                    this.successors.add(this.cache.get(targetIndex));
                else
                    buildAndAddSuccessor(irStatements, targetIndex);
                if (cfo instanceof IRBranchOperation)
                    if (this.cache.containsKey(i + 1))
                        this.successors.add(this.cache.get(i + 1));
                    else
                        // if branch, want following as well as target as successor
                        buildAndAddSuccessor(irStatements, i + 1);
                return;
            }
        }
    }

    public String getBlockId() {
        return this.blockId;
    }

    public String getFunctionName() {
        return this.functionName;
    }

    public List<BasicBlock> getSuccessors() {
        return this.successors;
    }

    public List<IRStatement> getStatements() {
        return this.statements;
    }

    public void addStatement(IRStatement statement) {
        this.statements.add(statement);
    }

    private int findTarget(List<IRStatement> irStatements, IRLocationIdentifier target) {
        for (int i = 0; i < irStatements.size(); i++) {
            IRStatement statement = irStatements.get(i);
            if (statement instanceof IRLocationIdentifier locationIdentifier) {
                if (locationIdentifier.getName().equals(target.getName()))
                    return i;
            }
        }
        return -1;
    }

    private void build(List<IRStatement> irStatements, BasicBlock parent) {
        this.predecessors.add(parent);
        build(irStatements);
    }

    private void buildAndAddSuccessor(List<IRStatement> irStatements, int leaderIndex) {
        BasicBlock block = new BasicBlock(this.functionName, this.cache, leaderIndex);
        block.build(irStatements, this);
        this.successors.add(block);
    }
}
