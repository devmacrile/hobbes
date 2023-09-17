package com.hobbes.backend.liveness;

import com.hobbes.ir.IRVariable;

import java.util.*;

public class InterferenceGraph {

    private List<ProgramPoint> programPoints;
    private List<Web> nodes;
    private Map<Web, List<Web>> edges;

    public InterferenceGraph(List<ProgramPoint> programPoints) {
        this.programPoints = programPoints;
        this.nodes = new ArrayList<>();
        this.edges = new HashMap<>();
    }

    private InterferenceGraph(List<ProgramPoint> programPoints, List<Web> nodes, Map<Web, List<Web>> edges) {
        this.programPoints = new ArrayList<>(programPoints);
        this.nodes = new ArrayList<>(nodes);
        this.edges = new HashMap<>(edges);
    }

    public InterferenceGraph clone() {
        return new InterferenceGraph(this.programPoints, this.nodes, this.edges);
    }

    public void build() {
        Set<IRVariable> allVariables = new HashSet<>();
        for (ProgramPoint pp : programPoints) {
            allVariables.addAll(pp.getInSet());
            allVariables.addAll(pp.getOutSet());
        }
        for (IRVariable variable : allVariables) {
            int i = 0;
            Web web = new Web(variable);
            while (i < programPoints.size()) {
                while (i < programPoints.size() && !isLive(variable, i))
                    i += 1;
                int start = i;
                while (i < programPoints.size() && isLive(variable, i))
                    i += 1;
                web.addLiveRange(new LiveRange(start, i));
            }
            nodes.add(web);
        }
        for (Web web : nodes) {
            edges.put(web, new ArrayList<>());
            for (Web other : nodes) {
                if (web.equals(other))
                    continue;
                if (web.interferes(other))
                    edges.get(web).add(other);
            }
        }
    }

    public boolean hasNode(Web node) {
        return nodes.contains(node);
    }

    public void deleteNode(Web node) {
        nodes.remove(node);
        edges.remove(node);
        for (Web web : edges.keySet())
            edges.get(web).remove(node);
    }

    public List<Web> getNodes() {
        return nodes;
    }

    public List<Web> getNeighbors(Web web) {
        return edges.getOrDefault(web, new ArrayList<>());
    }

    public int getDegree(Web node) {
        if (!edges.containsKey(node))
            return 0;
        return edges.get(node).size();
    }

    private boolean isLive(IRVariable variable, int ppNumber) {
        return programPoints.get(ppNumber).getInSet().contains(variable) ||
                programPoints.get(ppNumber).getOutSet().contains(variable);
    }
}
