package com.hobbes.semantic;

import com.hobbes.symbol.Symbol;
import org.antlr.v4.runtime.ParserRuleContext;

public class SemanticError {

    private String symbolName;
    private int lineNumber;
    private int columnNumber;
    private SemanticErrorType errorType;

    public SemanticError(String symbolName, ParserRuleContext ctx, SemanticErrorType errorType) {
        this.symbolName = symbolName;
        this.lineNumber = ctx.getStart().getLine();
        this.columnNumber = ctx.getStart().getCharPositionInLine();
        this.errorType = errorType;
    }

    public static SemanticError create(Symbol symbol, ParserRuleContext ctx, SemanticErrorType errorType) {
        return new SemanticError(symbol.getName(), ctx, errorType);
    }

    public String toString() {
        return String.format("line %d:%d %s", lineNumber, columnNumber, getDescription());
    }

    private String getDescription() {
        return switch (errorType) {
            case AssignmentTypeMismatch -> String.format("Type mismatch during assignment of '%s'", symbolName);
            case BreakOutsideOfLoop -> String.format("Encountered '%s' outside of loop context.", symbolName);
            case ComparatorTypeMismatch -> String.format("Type mismatch in comparison expression '%s'.", symbolName);
            case ComparatorAssociativityError -> String.format("Comparison operators do not associate: '%s'.", symbolName);
            case InvalidBinaryOperatorType -> String.format("Invalid type used with binary operator '%s'", symbolName);
            case InvalidFunctionParameterType -> String.format("Invalid parameter type for function: '%s'.", symbolName);
            case MissingFunctionCallArguments -> String.format("Missing arguments in call to '%s'.", symbolName);
            case NonArrayAccess -> String.format("Access attempted on non-array type: '%s'.", symbolName);
            case NonIntegerExponent -> String.format("Non-integer exponent used in '%s'.", symbolName);
            case NonIntegerIndex -> String.format("Non-integer index '%s'.", symbolName);
            case NonIntegerLogicalPredicate -> String.format("Non-integer type used as logical predicate '%s'.", symbolName);
            case NonIntegerControlExpression -> String.format("Non-integer type used as control statement expression: '%s'.", symbolName);
            case NonIntegerForLoopBound -> String.format("Non-integer type used as for-loop bound: '%s'.", symbolName);
            case NonIntegerForLoopVariable -> String.format("Non-integer type used as for-loop variable: '%s'", symbolName);
            case ParameterTypeMismatch -> String.format("Argument in call to '%s' has incorrect type.", symbolName);
            case ReturnOutsideOfFunction -> String.format("Return statement '%s' outside of function scope.", symbolName);
            case ReturnTypeMismatch -> String.format("Return type does not match function signature: '%s'", symbolName);
            case StorageClassError -> String.format("Incorrect storage class (var or static) for scope: '%s'.", symbolName);
            case SymbolRedeclarationError -> String.format("Symbol '%s' already defined in scope.", symbolName);
            case SymbolUndeclaredError -> String.format("Undefined symbol involved in declaration of '%s'.", symbolName);
        };
    }
}
