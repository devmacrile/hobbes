package com.hobbes.graphviz;

import com.hobbes.TigerBaseListener;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

public class GraphVizListener extends TigerBaseListener {

    private List<String> nodes;
    private List<String> edges;
    private final Vocabulary vocab;
    private final String[] ruleNames;

    public GraphVizListener(Vocabulary vocab, String[] ruleNames) {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.vocab = vocab;
        this.ruleNames = ruleNames;
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        nodes.add(formatNode(node));
        if (node.getParent() != null) {
            String parentIdentifier = getContextIdentifier((RuleContext) node.getParent());
            edges.add(formatEdge(parentIdentifier, getNodeIdentifier(node)));
        }
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        nodes.add(formatRuleContext(ctx));
        if (ctx.getParent() != null) {
            edges.add(formatEdge(getContextIdentifier(ctx.getParent()), getContextIdentifier(ctx)));
        }
    }

    public String getGraphViz() {
        String nodeString = String.join("\n  ", nodes);
        String edgeString = String.join("\n  ", edges);
        return String.format("digraph D \n{\n  %s\n\n  %s\n}", nodeString, edgeString);
    }

    private String getNodeIdentifier(TerminalNode node) {
        String name = vocab.getSymbolicName(node.getSymbol().getType());
        return name + Integer.toString(node.hashCode());
    }

    private String getContextIdentifier(RuleContext ctx) {
        return getRule(ctx) + Integer.toString(ctx.hashCode());
    }

    private String getRule(RuleContext ctx) {
        return ruleNames[ctx.getPayload().getRuleIndex()];
    }

    private String getSymbol(TerminalNode node) {
        return vocab.getSymbolicName(node.getSymbol().getType());
    }

    private String formatNode(TerminalNode node) {
        return formatVizNode(getNodeIdentifier(node), getSymbol(node) + ":" + node.getText());
    }

    private String formatRuleContext(RuleContext ctx) {
        return formatVizNode(getContextIdentifier(ctx), getRule(ctx));
    }

    private String formatEdge(String parentId, String childId) {
        return parentId + " -> " + childId + ";";
    }

    private String formatVizNode(String name, String label) {
        return name + " [label=\"" + label + "\"];";
    }
}
