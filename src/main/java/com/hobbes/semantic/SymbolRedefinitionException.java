package com.hobbes.semantic;

public class SymbolRedefinitionException extends Exception {
    public SymbolRedefinitionException(String errorMessage) {
        super(errorMessage);
    }
}
