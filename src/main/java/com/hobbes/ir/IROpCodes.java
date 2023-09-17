package com.hobbes.ir;

public class IROpCodes {

    // assignment
    public static final String Assign = "assign";

    // binary operations
    public static final String Add = "add";
    public static final String Sub = "sub";
    public static final String Mult = "mult";
    public static final String Div = "div";
    public static final String And = "and";
    public static final String Or = "or";

    // jump
    public static final String Goto = "goto";

    // branch
    public static final String BranchEqual = "breq";
    public static final String BranchNotEqual = "brneq";
    public static final String BranchLess = "brlt";
    public static final String BranchGreat = "brgt";
    public static final String BranchLessEq = "brleq";
    public static final String BranchGreatEq = "brgeq";

    // return
    public static final String Return = "return";

    // function calls
    public static final String Call = "call";
    public static final String CallR = "callr";

    // array operations
    public static final String ArrayStore = "array_store";
    public static final String ArrayLoad = "array_load";
}
