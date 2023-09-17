package com.hobbes;

import com.hobbes.semantic.SymbolRedefinitionException;
import com.hobbes.symbol.FunctionSymbol;
import com.hobbes.symbol.Symbol;
import com.hobbes.symbol.TypeSymbol;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SymbolTable {

    private final Scope global;
    private Scope current;
    private int nextScopeId;

    public SymbolTable() {
        this.global = Scope.GlobalScope();
        this.current = this.global;
        this.nextScopeId = this.global.getId() + 1;
    }

    public void InitializeScope() {
        Scope scope = new Scope(nextScopeId, this.current);
        this.current.addChild(scope);
        this.current = scope;
        nextScopeId++;
    }

    public void InitializeScope(FunctionSymbol boundingFunction) {
        Scope scope= new Scope(nextScopeId, this.current, boundingFunction);
        this.current.addChild(scope);
        this.current = scope;
        nextScopeId++;
    }

    public void FinalizeScope() {
        // add a paranoid check
        if (!isCurrentGlobal())
            this.current = this.current.getParent();
    }

    public SymbolTable reset() {
        this.current = global;
        return this;
    }

    public void enterScope() {
        this.current = this.current.getNextChild();
    }

    public void exitScope() {
        if (!isCurrentGlobal())
            this.current = this.current.getParent();
    }

    public Scope getCurrentScope() {
        return this.current;
    }

    public boolean isCurrentGlobal() {
        return this.current.equals(this.global);
    }

    public boolean isCurrentFunctionScope() {
        return this.current.isFunctionScope();
    }

    public FunctionSymbol getBoundingFunction() {
        return this.current.getBoundingFunction();
    }

    public Optional<Symbol> lookup(String symbolName) {
        Scope ctx = this.current;
        while (!ctx.isSymbolNameDeclared(symbolName) && ctx.getParent() != null) {
            ctx = ctx.getParent();
        }
        if (!ctx.isSymbolNameDeclared(symbolName)) {
            return Optional.empty();
        }
        return Optional.ofNullable(ctx.getSymbol(symbolName));
    }

    public void insert(Symbol symbol) throws SymbolRedefinitionException {
        if (current.isSymbolNameDeclared(symbol.getName())) {
            throw new SymbolRedefinitionException(String.format("Symbol '%s' already exists in current scope", symbol.getName()));
        }
        current.addSymbol(symbol);
    }

    public int getDeclaredScope(String symbolName) {
        Scope ctx = this.current;
        while (!ctx.isSymbolNameDeclared(symbolName) && ctx.getParent() != null) {
            ctx = ctx.getParent();
        }
        return ctx.getId();
    }

    public String format() {
        List<Scope> scopes = global.getScopes();
        StringBuilder builder = new StringBuilder();
        for (Scope scope : scopes) {
            String headerPrefix = String.join("", Collections.nCopies(scope.getLevel(), "  "));
            String prefix = String.join("", Collections.nCopies(scope.getLevel() + 1, "  "));
            builder.append(headerPrefix).append("scope ").append(scope.getId()).append(":\n");
            for (Symbol symbol : scope.getSymbols()) {
                if (!symbol.getClass().equals(TypeSymbol.class))
                    builder.append(prefix).append(symbol).append("\n");
            }
        }
        return builder.toString();
    }
}
