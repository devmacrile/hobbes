package com.hobbes;

import com.hobbes.symbol.FunctionSymbol;
import com.hobbes.symbol.ParameterSymbol;
import com.hobbes.symbol.PrimitiveTypes;
import com.hobbes.symbol.Symbol;

import java.util.*;

public class Scope {

    private final int id;
    private final int level;
    private final Scope parent;

    private FunctionSymbol boundingFunction;
    private final List<Scope> children;

    private int childWalkPosition;
    private final Map<String, Symbol> symbols;

    private int loopDepth;

    public Scope(int id, int level) {
        this.id = id;
        this.level = level;
        this.parent = null;
        this.boundingFunction = null;
        this.children = new ArrayList<>();
        this.childWalkPosition = 0;
        this.symbols = new HashMap<>();
        this.loopDepth = 0;
    }

    public Scope(int id, Scope parent) {
        this.id = id;
        this.level = parent.getLevel() + 1;
        this.parent = parent;
        this.boundingFunction = parent.getBoundingFunction();
        this.children = new ArrayList<>();
        this.childWalkPosition = 0;
        this.symbols = new HashMap<>();
        this.loopDepth = parent.getLoopDepth();
    }

    public Scope(int id, Scope parent, FunctionSymbol boundingFunction) {
        this.id = id;
        this.level = parent.getLevel() + 1;
        this.parent = parent;
        this.boundingFunction = boundingFunction;
        this.children = new ArrayList<>();
        this.childWalkPosition = 0;
        this.symbols = new HashMap<>();
        this.loopDepth = parent.getLoopDepth();
    }

    public static Scope GlobalScope() {
        Scope global = new Scope(0, 0);
        List<ParameterSymbol> iParam = Arrays.asList(new ParameterSymbol("i", PrimitiveTypes.TigerInt));
        List<ParameterSymbol> fParam = Arrays.asList(new ParameterSymbol("f", PrimitiveTypes.TigerFloat));
        global.addSymbol(new FunctionSymbol("printi", iParam));
        global.addSymbol(new FunctionSymbol("printf", fParam));
        global.addSymbol(new FunctionSymbol("not", PrimitiveTypes.TigerInt, iParam));
        global.addSymbol(new FunctionSymbol("exit", iParam));
        global.addSymbol(PrimitiveTypes.TigerInt);
        global.addSymbol(PrimitiveTypes.TigerFloat);
        return global;
    }

    public int getId() {
        return this.id;
    }
    public int getLevel() {
        return this.level;
    }

    public Scope getParent() {
        return this.parent;
    }

    public boolean isFunctionScope() {
        return this.boundingFunction != null;
    }

    public FunctionSymbol getBoundingFunction() {
        return this.boundingFunction;
    }

    public List<Scope> getChildren() {
        return this.children;
    }

    public void addChild(Scope child) {
        this.children.add(child);
    }

    public Scope getNextChild() {
        Scope scope = this.children.get(this.childWalkPosition);
        this.childWalkPosition += 1;
        if (this.childWalkPosition == this.children.size())
            this.childWalkPosition = 0;
        return scope;
    }

    public boolean isSymbolNameDeclared(String symbolName) {
        return this.symbols.containsKey(symbolName);
    }

    public void addSymbol(Symbol symbol) {
        // check for conflicts
        this.symbols.put(symbol.getName(), symbol);
    }

    public Symbol getSymbol(String symbolName) {
        return this.symbols.get(symbolName);
    }

    public List<Symbol> getSymbols() {
        return new ArrayList<>(symbols.values());
    }

    public List<Scope> getScopes() {
        List<Scope> scopes = new ArrayList<>();
        scopes.add(this);
        for (Scope child : children) {
            scopes.addAll(child.getScopes());
        }
        return scopes;
    }

    protected int getLoopDepth() {
        return this.loopDepth;
    }

    public boolean isInLoop() {
        return this.loopDepth > 0;
    }
    public void enterLoop() {
        this.loopDepth += 1;
    }

    public void exitLoop() {
        this.loopDepth -= 1;
    }
}
