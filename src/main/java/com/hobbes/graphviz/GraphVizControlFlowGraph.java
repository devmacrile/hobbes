package com.hobbes.graphviz;

import com.hobbes.backend.BasicBlock;
import com.hobbes.backend.ControlFlowGraph;
import com.hobbes.ir.IRStatement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GraphVizControlFlowGraph {

    private ControlFlowGraph cfg;
    private Set<String> cfgWalkCache;

    public GraphVizControlFlowGraph(ControlFlowGraph cfg) {
        this.cfg = cfg;
        this.cfgWalkCache = new HashSet<>();
    }

    public String getGraphViz() {
        return String.format("digraph D \n{\n  %s\n\n}", subgraph());
    }

    private String subgraph() {
        List<String> clusters = new ArrayList<>();
        int clusterNumber = 0;
        for (BasicBlock block : cfg.getRootBlocks()) {
            clusters.add(String.format("subgraph cluster_%d \n  {\n    node [shape=box]\n    %s\n    label=\"%s\"\n  }", clusterNumber, basicBlock(block), block.getFunctionName()));
            clusterNumber++;
        }
        return String.join("\n", clusters);
    }

    private String basicBlock(BasicBlock block) {
        List<String> edgeStrings = new ArrayList<>();
        List<String> succStrings = new ArrayList<>();
        for (BasicBlock succ : block.getSuccessors()) {
            if (!cfgWalkCache.contains(blockIdentifier(succ))) {
                cfgWalkCache.add(blockIdentifier(succ));
                succStrings.add(basicBlock(succ));
            }
            edgeStrings.add(String.format("%s -> %s", blockIdentifier(block), blockIdentifier(succ)));
        }
        List<String> emittedStatements =
                block.getStatements()
                        .stream()
                        .map(IRStatement::emit)
                        .collect(Collectors.toList());
        String succString = String.join("\n", succStrings);
        String edgeString = String.join("\n", edgeStrings);
        String statementString = String.join("\\n", emittedStatements);
        return String.format("%s [label=\"%s\"]\n%s\n%s\n", blockIdentifier(block), statementString, succString, edgeString);
    }

    private String blockIdentifier(BasicBlock block) {
        return "block" + block.getBlockId().replace("-", "");
    }
}
