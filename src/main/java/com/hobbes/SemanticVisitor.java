package com.hobbes;

import com.hobbes.semantic.SemanticError;
import com.hobbes.semantic.SemanticErrorType;
import com.hobbes.semantic.SymbolRedefinitionException;
import com.hobbes.symbol.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class SemanticVisitor extends TigerBaseVisitor<Object> {

    private SymbolTable symbolTable;
    protected List<SemanticError> errors;

    public SemanticVisitor() {
        super();
        this.symbolTable = new SymbolTable();
        this.errors = new ArrayList<>();
    }

    public SymbolTable getSymbolTable() {
        return this.symbolTable;
    }

    public List<SemanticError> getErrors() {
        return this.errors;
    }

    public int errorCount() {
        return this.errors.size();
    }

    /*
    Overrides of visit functions from TigerBaseVisitor.
    There is some mixing of return type strategies going on.  In general:
      => returning List<SemanticError> means this override is primarily for defs
      => returning TypeSymbol means this override is primarily for checking refs
     */
    @Override
    public Object visitType_declaration(TigerParser.Type_declarationContext ctx) {
        TypeSymbol type = new TypeSymbol(ctx.ID().getText());
        if (ctx.type().base_type() != null) {
            Optional<Symbol> optBaseType = symbolTable.lookup(ctx.type().base_type().getText());
            if (optBaseType.isPresent())
                type.setBaseType(((TypeSymbol) optBaseType.get()).getBaseType());
            else
                errors.add(SemanticError.create(type, ctx, SemanticErrorType.SymbolUndeclaredError));
        } else {
            Optional<Symbol> optType = symbolTable.lookup(ctx.type().getText());
            optType.ifPresent(symbol -> type.setBaseType(((TypeSymbol) symbol).getBaseType()));
        }
        if (ctx.type().ARRAY() != null) {
            int arrayLength = Integer.parseInt(ctx.type().INTLIT().getText());
            type.setArrayLength(arrayLength);
        }
        try {
            symbolTable.insert(type);
        } catch (SymbolRedefinitionException e) {
            errors.add(SemanticError.create(type, ctx, SemanticErrorType.SymbolRedeclarationError));
        }
        return visitChildren(ctx);
    }

    @Override
    public List<SemanticError> visitVar_declaration(TigerParser.Var_declarationContext ctx) {
        boolean isStatic = ctx.storage_class().STATIC() != null;
        if (symbolTable.isCurrentGlobal() && !isStatic)
            errors.add(new SemanticError(ctx.getText(), ctx, SemanticErrorType.StorageClassError));
        else if (!symbolTable.isCurrentGlobal() && isStatic)
            errors.add(new SemanticError(ctx.getText(), ctx, SemanticErrorType.StorageClassError));
        TypeSymbol type = (TypeSymbol) symbolTable.lookup(ctx.type().getText()).orElse(new NullTypeSymbol());
        TigerParser.Id_listContext lc = ctx.id_list();
        if (type.getClass().equals(NullTypeSymbol.class))
            errors.add(new SemanticError(ctx.getText(), lc, SemanticErrorType.SymbolUndeclaredError));
        String variableName = lc.ID().getText();
        while (lc != null) {
            variableName = lc.ID().getText();
            VariableSymbol variableSymbol = new VariableSymbol(variableName, type, isStatic);
            try {
                symbolTable.insert(variableSymbol);
            } catch (SymbolRedefinitionException e) {
                errors.add(SemanticError.create(variableSymbol, ctx, SemanticErrorType.SymbolRedeclarationError));
            }
            lc = lc.id_list();
        }
        if (ctx.optional_init().ASSIGN() != null) {
            TypeSymbol initType = (TypeSymbol) visit(ctx.optional_init().const_());
            if ((!type.isArray() && !initType.canCast(type)) ||
                    (type.isArray() && !type.getBaseType().canCast(initType)))
                errors.add(new SemanticError(variableName, ctx, SemanticErrorType.AssignmentTypeMismatch));
        }
        return getErrors();
    }

    @Override
    public List<SemanticError> visitFunct(TigerParser.FunctContext ctx) {
        List<ParameterSymbol> parameters = getParameters(ctx.param_list());
        TypeSymbol retType = new NullTypeSymbol();
        FunctionSymbol fs = new FunctionSymbol(ctx.ID().getSymbol().getText(), retType, parameters);
        if (ctx.ret_type().type() != null) {
            Optional<Symbol> optReturnType = symbolTable.lookup(ctx.ret_type().type().getText());
            if (optReturnType.isPresent())
                fs.setReturnType((TypeSymbol) optReturnType.get());
            else
                errors.add(SemanticError.create(fs, ctx, SemanticErrorType.SymbolUndeclaredError));
        }
        try {
            symbolTable.insert(fs);
        } catch (SymbolRedefinitionException e) {
            errors.add(SemanticError.create(fs, ctx, SemanticErrorType.SymbolRedeclarationError));
        }
        symbolTable.InitializeScope(fs);
        for (ParameterSymbol param : parameters) {
            try {
                symbolTable.insert(param);
            } catch (SymbolRedefinitionException e) {
                errors.add(SemanticError.create(param, ctx, SemanticErrorType.SymbolRedeclarationError));
            }
        }
        visitChildren(ctx);
        symbolTable.FinalizeScope();
        return getErrors();
    }

    @Override
    public TypeSymbol visitConst(TigerParser.ConstContext ctx) {
        if (ctx.INTLIT() != null)
            return PrimitiveTypes.TigerInt;
        return PrimitiveTypes.TigerFloat;
    }

    @Override
    public Object visitExpr_list(TigerParser.Expr_listContext ctx) {
        List<TypeSymbol> exprTypes = new ArrayList<>();
        if (ctx.expr() != null) {
            exprTypes.add((TypeSymbol) visit(ctx.expr()));
            exprTypes.addAll((List<TypeSymbol>) visit(ctx.expr_list_tail()));
        }
        return exprTypes;
    }

    @Override
    public Object visitExpr_list_tail(TigerParser.Expr_list_tailContext ctx) {
        List<TypeSymbol> exprTypes = new ArrayList<>();
        if (ctx.expr() != null) {
            exprTypes.add((TypeSymbol) visit(ctx.expr()));
            exprTypes.addAll((List<TypeSymbol>) visit(ctx.expr_list_tail()));
        }
        return exprTypes;
    }

    @Override
    public Object visitExpr(TigerParser.ExprContext ctx) {
        TypeSymbol type = new NullTypeSymbol();

        // CONST
        if (ctx.const_() != null) {
            type = (TypeSymbol) visit(ctx.const_());
        }

        // VALUE
        if (ctx.value() != null) {
            type = (TypeSymbol) visit(ctx.value());
        }

        if (ctx.AND() != null || ctx.OR() != null) {
            TypeSymbol left = (TypeSymbol) visit(ctx.getChild(0));
            TypeSymbol right = (TypeSymbol) visit(ctx.getChild(2));
            if (!left.equals(PrimitiveTypes.TigerInt))
                errors.add(new SemanticError(ctx.getChild(0).getText(), ctx, SemanticErrorType.NonIntegerLogicalPredicate));
            if (!right.equals(PrimitiveTypes.TigerInt))
                errors.add(new SemanticError(ctx.getChild(2).getText(), ctx, SemanticErrorType.NonIntegerLogicalPredicate));
            type = PrimitiveTypes.TigerInt;
        }

        if (ctx.comparator_operators() != null) {
            if (ctx.expr(0).comparator_operators() != null)
                errors.add(new SemanticError(ctx.getText(), ctx, SemanticErrorType.ComparatorAssociativityError));
            TypeSymbol left = (TypeSymbol) visit(ctx.getChild(0));
            TypeSymbol right = (TypeSymbol) visit(ctx.getChild(2));
            if (left.isArray() || right.isArray())
                errors.add(new SemanticError(ctx.getText(), ctx, SemanticErrorType.InvalidBinaryOperatorType));
            else if (!left.equals(right))
                errors.add(new SemanticError(ctx.getText(), ctx, SemanticErrorType.ComparatorTypeMismatch));
            type = PrimitiveTypes.TigerInt;
        }

        if (ctx.plus_minus_operators() != null || ctx.mult_div_operators() != null) {
            TypeSymbol left = (TypeSymbol) visit(ctx.getChild(0));
            TypeSymbol right = (TypeSymbol) visit(ctx.getChild(2));
            if (left.isArray() || right.isArray())
                errors.add(new SemanticError(ctx.getText(), ctx, SemanticErrorType.InvalidBinaryOperatorType));
            else
                type = left.resolve(right);
        }

        if (ctx.POW() != null) {
            TypeSymbol left = (TypeSymbol) visit(ctx.getChild(0));
            TypeSymbol right = (TypeSymbol) visit(ctx.getChild(2));
            if (left.isArray())
                errors.add(new SemanticError(ctx.POW().getText(), ctx, SemanticErrorType.InvalidBinaryOperatorType));
            if (!right.equals(PrimitiveTypes.TigerInt))
                errors.add(new SemanticError(ctx.POW().getText(), ctx, SemanticErrorType.NonIntegerExponent));
            if (!left.isArray())
                type = left;
        }

        if (ctx.OPENPAREN() != null)
            type = (TypeSymbol) visit(ctx.getChild(1));

        return type;
    }

    @Override
    public TypeSymbol visitStat(TigerParser.StatContext ctx) {

        if (ctx.ID() != null && ctx.OPENPAREN() != null)
            analyzeFunctCall(ctx);

        if (ctx.IF() != null) {
            TypeSymbol ifType = (TypeSymbol) visit(ctx.getChild(1));
            if (!ifType.equals(PrimitiveTypes.TigerInt))
                errors.add(new SemanticError(ctx.getChild(1).getText(), ctx, SemanticErrorType.NonIntegerControlExpression));
            visit(ctx.getChild(3));
            if (ctx.ELSE() != null)
                visit(ctx.getChild(5));
        }

        if (ctx.WHILE() != null) {
            symbolTable.getCurrentScope().enterLoop();
            TypeSymbol condType = (TypeSymbol) visit(ctx.getChild(1));
            if (!condType.equals(PrimitiveTypes.TigerInt))
                errors.add(new SemanticError(ctx.getChild(1).getText(), ctx, SemanticErrorType.NonIntegerControlExpression));
            visit(ctx.getChild(3));
            symbolTable.getCurrentScope().exitLoop();
        }

        if (ctx.FOR() != null) {
            symbolTable.getCurrentScope().enterLoop();
            Optional<Symbol> optSymbol = symbolTable.lookup(ctx.ID().getText());
            if (optSymbol.isPresent()) {
                VariableSymbol var = (VariableSymbol) optSymbol.get();
                TypeSymbol fromType = (TypeSymbol) visit(ctx.getChild(3));
                TypeSymbol toType = (TypeSymbol) visit(ctx.getChild(5));
                if (!var.getType().equals(PrimitiveTypes.TigerInt))
                    errors.add(new SemanticError(ctx.ID().getText(), ctx, SemanticErrorType.NonIntegerForLoopVariable));
                if (!fromType.equals(PrimitiveTypes.TigerInt))
                    errors.add(new SemanticError(ctx.getChild(3).getText(), ctx, SemanticErrorType.NonIntegerForLoopBound));
                if (!toType.equals(PrimitiveTypes.TigerInt))
                    errors.add(new SemanticError(ctx.getChild(5).getText(), ctx, SemanticErrorType.NonIntegerForLoopBound));
            } else {
                errors.add(new SemanticError(ctx.ID().getText(), ctx, SemanticErrorType.SymbolUndeclaredError));
            }
            visit(ctx.getChild(7));
            symbolTable.getCurrentScope().exitLoop();
        }

        if (ctx.ASSIGN() != null && ctx.FOR() == null) {
            TypeSymbol valueType = (TypeSymbol) visit(ctx.getChild(0));
            TypeSymbol exprType = (TypeSymbol) visit(ctx.getChild(2));
            if (!exprType.canCast(valueType))
                errors.add(new SemanticError(ctx.value().getText(), ctx, SemanticErrorType.AssignmentTypeMismatch));
        }

        if (ctx.BREAK() != null) {
            if (!symbolTable.getCurrentScope().isInLoop())
                errors.add(new SemanticError(ctx.getText(), ctx, SemanticErrorType.BreakOutsideOfLoop));
        }

        if (ctx.RETURN() != null) {
            if (!symbolTable.isCurrentFunctionScope()) {
                errors.add(new SemanticError(ctx.getText(), ctx, SemanticErrorType.ReturnOutsideOfFunction));
            } else {
                if (ctx.optreturn().expr() != null) {
                    TypeSymbol definedReturnType = symbolTable.getBoundingFunction().getReturnType();
                    if (definedReturnType.equals(new NullTypeSymbol())) {
                        errors.add(new SemanticError(ctx.getText(), ctx, SemanticErrorType.ReturnTypeMismatch));
                    } else {
                        TypeSymbol returnExprType = (TypeSymbol) visit(ctx.optreturn().expr());
                        if (!returnExprType.canCast(definedReturnType)) {
                            errors.add(new SemanticError(ctx.getText(), ctx, SemanticErrorType.ReturnTypeMismatch));
                        }
                    }
                }
            }
        }

        if (ctx.LET() != null) {
            symbolTable.InitializeScope();
            visitChildren(ctx);
            symbolTable.FinalizeScope();
        }

        return new NullTypeSymbol();
    }

    @Override
    public Object visitValue(TigerParser.ValueContext ctx) {
        TypeSymbol type = new NullTypeSymbol();
        Optional<Symbol> optVar = symbolTable.lookup(ctx.ID().getText());
        if (optVar.isPresent()) {
            VariableSymbol symbol = (VariableSymbol) optVar.get();
            type = symbol.getType();
        } else {
            errors.add(new SemanticError(ctx.ID().getText(), ctx, SemanticErrorType.SymbolUndeclaredError));
        }
        if (!type.isArray() && ctx.value_tail().OPENBRACK() != null)
            errors.add(new SemanticError(ctx.ID().getText(), ctx, SemanticErrorType.NonArrayAccess));
        visitChildren(ctx);
        if (ctx.value_tail().OPENBRACK() != null)
            return type.getBaseType();
        return type;
    }

    @Override
    public TypeSymbol visitValue_tail(TigerParser.Value_tailContext ctx) {
        if (ctx.OPENBRACK() != null) {
            TypeSymbol type = (TypeSymbol) visit(ctx.getChild(1));
            if (type.isArray() || !type.getBaseType().equals(PrimitiveTypes.TigerInt))
                errors.add(new SemanticError(ctx.getChild(1).getText(), ctx, SemanticErrorType.NonIntegerIndex));
            return type;
        }
        return new NullTypeSymbol();
    }

    /*
    Private helpers that have control over their return types.
     */

    private List<ParameterSymbol> getParameters(TigerParser.Param_listContext ctx) {
        if (ctx.param() == null)
            return new ArrayList<>();
        List<ParameterSymbol> params = new ArrayList<>();
        params.add(getParameter(ctx.param()));
        params.addAll(getParameters(ctx.param_list_tail()));
        return params;
    }

    private List<ParameterSymbol> getParameters(TigerParser.Param_list_tailContext ctx) {
        if (ctx.param() == null)
            return new ArrayList<>();
        List<ParameterSymbol> params = new ArrayList<>();
        params.add(getParameter(ctx.param()));
        params.addAll(getParameters(ctx.param_list_tail()));
        return params;
    }

    private ParameterSymbol getParameter(TigerParser.ParamContext ctx) {
        String paramName = ctx.ID().getSymbol().getText();
        TypeSymbol paramType = new NullTypeSymbol();
        ParameterSymbol param = new ParameterSymbol(paramName, paramType);
        Optional<Symbol> optType = symbolTable.lookup(ctx.type().getText());
        if (optType.isPresent()) {
            paramType = (TypeSymbol) optType.get();
            param.setType(paramType);
            if (paramType.isArray())
                errors.add(new SemanticError(ctx.getText(), ctx, SemanticErrorType.InvalidFunctionParameterType));
        } else {
            errors.add(SemanticError.create(param, ctx, SemanticErrorType.SymbolUndeclaredError));
        }
        return param;
    }

    private void analyzeFunctCall(TigerParser.StatContext ctx) {
        /*
        Semantic checks:
          > Function is declared in scope
          > Correct number of arguments
          > Each argument is correct type
         */
        String functionName = ctx.ID().getText();
        // TODO need to make sure what we find is a function
        // => more argument for separating out symbol types
        Optional<Symbol> optSymbol = symbolTable.lookup(functionName);
        if (optSymbol.isPresent()) {
            FunctionSymbol fs = (FunctionSymbol) optSymbol.get();
            if (fs.numParameters() > 0) {
                List<TypeSymbol> argTypes = (List<TypeSymbol>) visit(ctx.getChild(3));
                if (argTypes.size() != fs.numParameters()) {
                    errors.add(SemanticError.create(fs, ctx, SemanticErrorType.MissingFunctionCallArguments));
                } else {
                    // TODO could have both be semantic errors but kiss for now (viz. know arrays are same size)
                    for (int i = 0; i < argTypes.size(); i++) {
                        if (!argTypes.get(i).canCast(fs.getParameters().get(i).getType())) {
                            errors.add(new SemanticError(ctx.ID().getText(), ctx, SemanticErrorType.ParameterTypeMismatch));
                            break;
                        }
                    }
                }
            }
        } else {
            errors.add(new SemanticError(functionName, ctx, SemanticErrorType.SymbolUndeclaredError));
        }
    }
}