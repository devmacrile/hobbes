package com.hobbes.backend.liveness;

import com.hobbes.backend.BasicBlock;
import com.hobbes.ir.IRFunction;
import com.hobbes.ir.IRStatement;
import com.hobbes.ir.IRValue;
import com.hobbes.ir.IRVariable;
import com.hobbes.ir.ops.IROperation;

import java.util.*;

public class LivenessAnalyzer {

    private IRFunction function;
    private BasicBlock rootBlock;
    private List<ProgramPoint> programPoints;

    public LivenessAnalyzer(IRFunction function, BasicBlock rootBlock) {
        this.function = function;
        this.rootBlock = rootBlock;
        this.programPoints = new ArrayList<>();
    }

    public List<ProgramPoint> getProgramPoints() {
        return programPoints;
    }

    public void run() {
        boolean changes = true;
        Map<Integer, List<ProgramPoint>> sets = new HashMap<>();
        List<BasicBlock> blocks = getBlocks();
        for (BasicBlock block : blocks)
            sets.put(block.getLeaderIndex(), initialize(block));
        while (changes) {
            changes = false;
            for (BasicBlock block : blocks) {
                changes = updateSets(block, sets);
            }
        }
        // block tree-walk not necessarily in order, so get properly ordered instructions
        for (int i = 0; i < function.getBody().getStatements().size(); i++) {
            if (sets.containsKey(i)) {
                programPoints.addAll(sets.get(i));
            }
        }
    }

    private boolean updateSets(BasicBlock block, Map<Integer, List<ProgramPoint>> sets) {
        boolean changes = false;
        List<ProgramPoint> blockSets = sets.get(block.getLeaderIndex());
        // union out set of last statement with in sets of successors
        for (BasicBlock succ : block.getSuccessors()) {
            List<ProgramPoint> succPoints = sets.get(succ.getLeaderIndex());
            for (IRVariable succInVariable : succPoints.get(0).getInSet())
                if (!blockSets.get(blockSets.size() - 1).getOutSet().contains(succInVariable)) {
                    changes = true;
                    blockSets.get(blockSets.size() - 1).addToOutSet(succInVariable);
                }
        }
        // iterate through statements backwards, updating according to
        // In[I] = (out[I] - def[i]) U use[I]
        for (int i = block.getStatements().size() - 1; i >= 0; --i) {
            IRStatement statement = block.getStatements().get(i);
            ProgramPoint set = blockSets.get(i);
            // if we are not the last program point (updated in above logic)
            // then we want our out set to be set to our successor in set
            if (i < block.getStatements().size() - 1) {
                Set<IRVariable> ogOutSet = new HashSet<>(set.getOutSet());
                set.getOutSet().clear();
                set.getOutSet().addAll(blockSets.get(i + 1).getInSet());
                if (!set.getOutSet().equals(ogOutSet))
                    changes = true;
            }
            Set<IRVariable> originalInSet = new HashSet<>(set.getInSet());
            Set<IRVariable> useSet = new HashSet<>();
            set.getInSet().clear();
            set.getInSet().addAll(set.getOutSet());
            if (statement instanceof IROperation operation) {
                for (IRValue value : operation.getOperands())
                    if (value instanceof IRVariable var)
                        useSet.add(var);
                // if after setting in = out, in contains the def var => remove it
                if (operation.getDestinationVariable().isPresent() && set.getInSet().contains(operation.getDestinationVariable().get()))
                    set.removeFromInSet(operation.getDestinationVariable().get());
            }
            set.getInSet().addAll(useSet);
            if (!set.getInSet().equals(originalInSet))
                changes = true;
        }
        return changes;
    }

    private List<ProgramPoint> initialize(BasicBlock block) {
        List<ProgramPoint> blockSets = new ArrayList<>();
        for (IRStatement statement : block.getStatements())
            blockSets.add(new ProgramPoint(statement));
        return blockSets;
    }

    private List<BasicBlock> getBlocks() {
        List<BasicBlock> blocks = new ArrayList<>();
        Queue<BasicBlock> q = new LinkedList<>(Collections.singletonList(rootBlock));
        while (!q.isEmpty()) {
            BasicBlock block = q.remove();
            blocks.add(block);
            for (BasicBlock succ : block.getSuccessors()) {
                if (!blocks.contains(succ)) {
                    blocks.add(succ);
                    if (!q.contains(succ))
                        q.add(succ);
                }
            }
        }
        return blocks;
    }
}
