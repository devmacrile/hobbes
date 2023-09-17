package com.hobbes;

import com.hobbes.backend.ControlFlowGraph;
import com.hobbes.backend.allocation.RegisterAllocatorType;
import com.hobbes.backend.mips.MIPSProgram;
import com.hobbes.graphviz.GraphVizControlFlowGraph;
import com.hobbes.ir.IRProgram;
import com.hobbes.graphviz.GraphVizListener;
import com.hobbes.lexer.LexerErrorListener;
import com.hobbes.semantic.SemanticError;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {

        System.out.println(
            """
            -----------------------------------------------------------------
            | For seeing life is but a motion of limbs,                     |
            | the beginning whereof is in some principal part within,       |
            | why may we not say that all automata have an artificial life? |
            | -- Thomas Hobbes, Leviathan                                   |
            -----------------------------------------------------------------
            """
        );

        String inputFile = null;
        boolean generateTokens = false;
        boolean generateTreeGraph = false;
        boolean generateSymbolTable = false;
        boolean generateIR = false;
        boolean generateCfgGraph = false;
        boolean generateLiveness = false;
        RegisterAllocatorType allocatorType = RegisterAllocatorType.NAIVE;
        int i = 0;
        while (i < args.length) {
            if (i < args.length - 1 && args[i].equals("-f"))
                inputFile = args[i + 1];
            if (args[i].equals("-s"))
                generateTokens = true;
            if (args[i].equals("-p"))
                generateTreeGraph = true;
            if (args[i].equals("-t"))
                generateSymbolTable = true;
            if (args[i].equals("-i"))
                generateIR = true;
            if (args[i].equals("-c"))
                generateCfgGraph = true;
            if (args[i].equals("-v"))
                generateLiveness = true;
            if (args[i].equals("-l"))
                allocatorType = RegisterAllocatorType.INTRABLOCK;
            if (args[i].equals("-g"))
                allocatorType = RegisterAllocatorType.BRIGGS;
            i += 1;
        }

        if (inputFile == null) {
            System.err.println("Input file required! Use -f flag.");
            System.exit(1);
        }

        String filenamePrefix = inputFile.substring(0, inputFile.lastIndexOf('.'));
        CharStream input = getCharStreamFromFile(inputFile);
        TigerLexer lexer = new TigerLexer(input);

        // check lexer for errors
        LexerErrorListener sel = new LexerErrorListener();
        lexer.addErrorListener(sel);
        lexer.getAllTokens();
        if (sel.isError()) {
            System.err.println("Lexer encountered an error!");
            System.exit(2);
        }
        lexer.reset();

        TigerParser parser = new TigerParser(new CommonTokenStream(lexer));
        parser.tiger_program();
        if (parser.getNumberOfSyntaxErrors() > 0){
            System.err.println("Parser encountered an error!");
            System.exit(3);
        }

        // reset state changes from error testing
        parser.reset();
        lexer.reset();

        // build symbol table
        SemanticVisitor semanticVisitor = new SemanticVisitor();
        semanticVisitor.visit(parser.tiger_program());
        SymbolTable symbolTable = semanticVisitor.getSymbolTable();

        if (semanticVisitor.errorCount() > 0) {
            for (SemanticError error : semanticVisitor.getErrors())
                System.err.println(error);
            System.exit(4);
        }

        // construct intermediate representation
        parser.reset();
        IRVisitor irVisitor = new IRVisitor(symbolTable.reset(), semanticVisitor);
        IRProgram irProgram = (IRProgram) irVisitor.visit(parser.tiger_program());

        // build control flow graph and perform liveness analysis
        ControlFlowGraph cfg = new ControlFlowGraph();
        cfg.build(irProgram);

        // reset state changes from compiler execution
        parser.reset();

        MIPSProgram mipsProgram = new MIPSProgram(irProgram, cfg, allocatorType);
        writeToFile(mipsProgram.generateAsm(), String.format("%s%s", filenamePrefix, mipsFileSuffix(allocatorType)));

        if (generateTokens) {
            String tokenFilename = filenamePrefix + ".tokens";
            System.out.println("Generating token file " + tokenFilename);
            List<String> tokenStrings = new ArrayList<>();
            for (Token t : lexer.getAllTokens())
                tokenStrings.add("<" + lexer.getVocabulary().getSymbolicName(t.getType()) + ", \"" + t.getText() + "\">\n");
            writeToFile(tokenStrings, tokenFilename);
            lexer.reset();
        }

        if (generateTreeGraph) {
            String graphFilename = filenamePrefix + ".tree.gv";
            System.out.println("Generating graph viz " + graphFilename);
            ParseTree parseTree = parser.tiger_program();
            ParseTreeWalker parseTreeWalker = new ParseTreeWalker();
            GraphVizListener listener = new GraphVizListener(lexer.getVocabulary(), parser.getRuleNames());
            parseTreeWalker.walk(listener, parseTree);
            writeToFile(listener.getGraphViz(), graphFilename);
        }

        if (generateSymbolTable) {
            String symbolTableFilename = filenamePrefix + ".st";
            writeToFile(symbolTable.format(), symbolTableFilename);
        }

        if (generateIR) {
            String irFilename = filenamePrefix + ".ir";
            writeToFile(irProgram.toString(), irFilename);
        }

        if (generateCfgGraph) {
            String cfgFilename = filenamePrefix + ".cfg.gv";
            System.out.println("Generating CFG graph viz " + cfgFilename);
            GraphVizControlFlowGraph cfgViz = new GraphVizControlFlowGraph(cfg);
            writeToFile(cfgViz.getGraphViz(), cfgFilename);
        }

        if (generateLiveness) {
            String livenessFilename = filenamePrefix + ".liveness";
            System.out.println("Generating variable liveness output: " + livenessFilename);
            writeToFile(mipsProgram.printLiveVariableSets(), livenessFilename);
        }

        System.exit(0);
    }

    private static CharStream getCharStreamFromFile(String filename) {
        CharStream charStream = null;
        try {
            charStream = CharStreams.fromPath(Paths.get(filename));
        } catch (IOException e) {
            System.err.println(filename + " could not be found!");
            System.exit(1);
        }
        return charStream;
    }

    private static void writeToFile(List<String> lines, String filename) throws IOException {
        writeToFile(String.join("", lines), filename);
    }

    private static void writeToFile(String s, String filename) throws IOException {
        FileWriter writer = new FileWriter(filename);
        writer.write(s);
        writer.close();
    }

    private static String mipsFileSuffix(RegisterAllocatorType allocatorType) {
        return switch (allocatorType) {
            case NAIVE -> ".naive.s";
            case INTRABLOCK -> ".ib.s";
            case BRIGGS -> ".briggs.s";
        };
    }
}
