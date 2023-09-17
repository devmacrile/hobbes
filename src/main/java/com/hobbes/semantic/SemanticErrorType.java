package com.hobbes.semantic;

public enum SemanticErrorType {
    AssignmentTypeMismatch,
    BreakOutsideOfLoop,
    ComparatorTypeMismatch,
    ComparatorAssociativityError,
    InvalidBinaryOperatorType,
    InvalidFunctionParameterType,
    MissingFunctionCallArguments,
    NonArrayAccess,
    NonIntegerExponent,
    NonIntegerIndex,
    NonIntegerControlExpression,
    NonIntegerForLoopBound,
    NonIntegerForLoopVariable,
    NonIntegerLogicalPredicate,
    ParameterTypeMismatch,
    ReturnOutsideOfFunction,
    ReturnTypeMismatch,
    StorageClassError,
    SymbolRedeclarationError,
    SymbolUndeclaredError
}
