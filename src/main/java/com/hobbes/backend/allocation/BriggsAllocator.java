package com.hobbes.backend.allocation;

import com.hobbes.backend.liveness.InterferenceGraph;
import com.hobbes.backend.liveness.Web;
import com.hobbes.backend.mips.MIPSData;
import com.hobbes.backend.mips.registers.MIPSRegister;
import com.hobbes.ir.IRFunction;
import com.hobbes.ir.IRVariable;
import com.hobbes.symbol.PrimitiveTypes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class BriggsAllocator extends BaseRegisterAllocator {

    private IRFunction function;
    private InterferenceGraph interferenceGraph;

    public BriggsAllocator(IRFunction function, MIPSData staticData, InterferenceGraph interferenceGraph) {
        super(function, staticData);
        this.interferenceGraph = interferenceGraph;
        this.function = function;
    }

    public void run() {
        InterferenceGraph graphClone = interferenceGraph.clone();
        Stack<Web> stack = new Stack<>();
        boolean changes = true;
        // (1) first, add nodes to the coloring stack that
        // have degree < the number of registers
        while (graphClone.getNodes().size() > 0) {
            // (1) first, add nodes to the coloring stack that
            // have degree < the number of registers
            while (changes) {
                changes = false;
                for (Web web : interferenceGraph.getNodes()) {
                    if (!graphClone.hasNode(web))
                        continue;
                    if (web.getType().equals(PrimitiveTypes.TigerInt)) {
                        if (graphClone.getDegree(web) < getNumberOfAllocatableRegisters()) {
                            stack.push(web);
                            graphClone.deleteNode(web);
                            changes = true;
                        }
                    } else if (graphClone.getDegree(web) < getNumberOfAllocatableFRegisters()) {
                        stack.push(web);
                        graphClone.deleteNode(web);
                        changes = true;
                    }
                }
            }
            // (2) then when we no longer can, add a node of minimum spill cost
            if (graphClone.getNodes().size() > 0) {
                Web minCostNode = null;
                int minCost = Integer.MAX_VALUE;
                for (Web node : graphClone.getNodes())
                    if (node.getSpillCost() < minCost)
                        minCostNode = node;
                stack.push(minCostNode);
                graphClone.deleteNode(minCostNode);
            }
        }

        while (!stack.empty()) {
            Web web = stack.pop();
            Set<MIPSRegister> neighborRegisters = new HashSet<>();
            for (Web neighbor : interferenceGraph.getNeighbors(web))
                neighborRegisters.add(allocations.get(neighbor.getVariable()));
            allocateRegister(web.getVariable(), neighborRegisters);
        }
    }

    public List<IRVariable> getSpillVariables() {
        // this just informs the size of the stack frame atm
        // ideally we compute this exactly, but kiss for now
        return function.getLocalVariables();
    }
}
