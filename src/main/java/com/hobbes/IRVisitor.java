package com.hobbes;

import com.hobbes.ir.*;
import com.hobbes.ir.ops.*;
import com.hobbes.symbol.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@SuppressWarnings("unchecked")
public class IRVisitor extends TigerBaseVisitor<Object> {

    private SymbolTable symbolTable;
    private SemanticVisitor semanticVisitor;
    private IRNamer namer;
    private Stack<IRLocationIdentifier> loopEndStack;

    public IRVisitor(SymbolTable symbolTable, SemanticVisitor semanticVisitor) {
        this.symbolTable = symbolTable;
        this.semanticVisitor = semanticVisitor;
        this.namer = new IRNamer();
        this.loopEndStack = new Stack<>();
    }

    /*
    -----------------------------------------------
    --------------- Public visitors ---------------
    -----------------------------------------------
    */
    @Override
    public IRProgram visitTiger_program(TigerParser.Tiger_programContext ctx) {
        IRProgram program = new IRProgram(ctx.ID().getText());
        IRBlock initBlock = (IRBlock) visit(ctx.declaration_segment());
        List<IRFunction> functions = (List<IRFunction>) visit(ctx.funct_list());
        for (IRFunction function : functions) {
            if (function.isMain())
                function.addInitializationStatements(initBlock.getStatements());
        }
        program.addVariables(initBlock.getVariables());
        program.addFunctions(functions);
        return program;
    }

    @Override
    public IRBlock visitDeclaration_segment(TigerParser.Declaration_segmentContext ctx) {
        // we do not care about type declarations as they are already in the
        // symbol table and the IR code needs to be created directly anyhow
        return (IRBlock) visit(ctx.var_declaration_list());
    }

    @Override
    public IRBlock visitVar_declaration_list(TigerParser.Var_declaration_listContext ctx) {
        IRBlock block = new IRBlock();
        if (ctx.var_declaration() != null) {
            block.append((IRBlock) visit(ctx.var_declaration()));
            block.append((IRBlock) visit(ctx.var_declaration_list()));
        }
        return block;
    }

    @Override
    public IRBlock visitVar_declaration(TigerParser.Var_declarationContext ctx) {
        IRBlock block = new IRBlock();
        TypeSymbol type = (TypeSymbol) symbolTable.lookup(ctx.type().getText()).get();
        TigerParser.Id_listContext lc = ctx.id_list();
        while (lc != null) {
            String name = namer.mangle(lc.ID().getText(), symbolTable.getDeclaredScope(lc.ID().getText()));
            IRVariable variable = new IRVariable(name, type);
            block.addVariable(variable);
            if (ctx.optional_init().ASSIGN() != null) {
                String initValue = ctx.optional_init().const_().getText();
                if (type.isArray())
                    block.addStatement(new IRArrayAssign(variable, new IRConstant(type, initValue), type.getArrayLength()));
                else
                    block.addStatement(new IRAssign(variable, new IRConstant(type, initValue)));
            }
            lc = lc.id_list();
        }
        return block;
    }

    @Override
    public List<IRFunction> visitFunct_list(TigerParser.Funct_listContext ctx) {
        List<IRFunction> functions = new ArrayList<>();
        if (ctx.funct() != null) {
            functions.add((IRFunction) visit(ctx.funct()));
            functions.addAll((List<IRFunction>) visit(ctx.funct_list()));
        }
        return functions;
    }

    @Override
    public IRFunction visitFunct(TigerParser.FunctContext ctx) {
        String functionName = ctx.ID().getText();
        TypeSymbol returnType = new NullTypeSymbol();
        if (ctx.ret_type().type() != null)
            returnType = (TypeSymbol) symbolTable.lookup(ctx.ret_type().type().getText()).get();
        symbolTable.enterScope();
        List<IRVariable> params = (List<IRVariable>) visit(ctx.param_list());
        IRBlock block = (IRBlock) visit(ctx.stat_seq());
        if (ctx.ret_type().type() == null)
            block.addStatement(new IRReturn());
        IRFunction function = new IRFunction(functionName, returnType, params);
        function.append(block);
        symbolTable.exitScope();
        return function;
    }

    @Override
    public List<IRVariable> visitParam_list(TigerParser.Param_listContext ctx) {
        if (ctx.param() == null)
            return new ArrayList<>();
        List<IRVariable> params = new ArrayList<>();
        params.add((IRVariable) visit(ctx.param()));
        params.addAll((List<IRVariable>) visit(ctx.param_list_tail()));
        return params;
    }

    @Override
    public List<IRVariable> visitParam_list_tail(TigerParser.Param_list_tailContext ctx) {
        if (ctx.param() == null)
            return new ArrayList<>();
        List<IRVariable> params = new ArrayList<>();
        params.add((IRVariable) visit(ctx.param()));
        params.addAll((List<IRVariable>) visit(ctx.param_list_tail()));
        return params;
    }

    @Override
    public IRVariable visitParam(TigerParser.ParamContext ctx) {
        String name = ctx.ID().getSymbol().getText();
        String irName = namer.mangle(name, symbolTable.getDeclaredScope(name));
        TypeSymbol type = (TypeSymbol) symbolTable.lookup(ctx.type().getText()).get();
        return new IRVariable(irName, type);
    }

    @Override
    public List<IRBlock> visitExpr_list(TigerParser.Expr_listContext ctx) {
        List<IRBlock> blocks = new ArrayList<>();
        if (ctx.expr() != null) {
            blocks.add((IRBlock) visit(ctx.expr()));
            blocks.addAll((List<IRBlock>) visit(ctx.expr_list_tail()));
        }
        return blocks;
    }

    @Override
    public  List<IRBlock> visitExpr_list_tail(TigerParser.Expr_list_tailContext ctx) {
        List<IRBlock> blocks = new ArrayList<>();
        if (ctx.expr() != null) {
            blocks.add((IRBlock) visit(ctx.expr()));
            blocks.addAll((List<IRBlock>) visit(ctx.expr_list_tail()));
        }
        return blocks;
    }

    @Override
    public IRBlock visitExpr(TigerParser.ExprContext ctx) {

        IRBlock block = new IRBlock();

        // CONST
        if (ctx.const_() != null) {
            block.append((IRBlock) visit(ctx.const_()));
        }

        if (ctx.value() != null) {
            block.append((IRBlock) visit(ctx.value()));
        }

        // AND/OR
        if (ctx.AND() != null || ctx.OR() != null) {
            block.append((IRBlock) visit(ctx.getChild(0)));
            IRValue left = block.getCurrentIdentifier();
            block.append((IRBlock) visit(ctx.getChild(2)));
            IRValue right = block.getCurrentIdentifier();
            IRVariable tempVariable = new IRVariable(namer.generateTempName(), PrimitiveTypes.TigerInt);
            if (ctx.AND() != null)
                block.addStatement(new IRAnd(left, right, tempVariable));
            else
                block.addStatement(new IROr(left, right, tempVariable));
            block.addVariable(tempVariable);
        }

        if (ctx.comparator_operators() != null) {
            block.append((IRBlock) visit(ctx.getChild(0)));
            IRValue left = block.getCurrentIdentifier();
            block.append((IRBlock) visit(ctx.getChild(2)));
            IRValue right = block.getCurrentIdentifier();

            // create a jump location for the branch operator
            IRLocationIdentifier loc = new IRLocationIdentifier(namer.generateLocationName());

            // generate a temp variable and assign to 0
            IRVariable tempVariable = new IRVariable(namer.generateTempName(), PrimitiveTypes.TigerInt);
            block.addStatement(IRAssign.zero(tempVariable.getName()));
            block.addVariable(tempVariable);

            if (ctx.comparator_operators().EQUAL() != null)
                block.addStatement(new IRBranchNotEqual(left, right, loc));
            else if (ctx.comparator_operators().NEQUAL() != null)
                block.addStatement(new IRBranchEqual(left, right, loc));
            else if (ctx.comparator_operators().LESS() != null)
                block.addStatement(new IRBranchGreatEqual(left, right, loc));
            else if(ctx.comparator_operators().GREAT() != null)
                block.addStatement(new IRBranchLessEqual(left, right, loc));
            else if (ctx.comparator_operators().LESSEQ() != null)
                block.addStatement(new IRBranchGreat(left, right, loc));
            else
                block.addStatement(new IRBranchLess(left, right, loc));
            block.addStatement(IRAssign.one(tempVariable.getName()));
            block.addStatement(loc);
        }

        if (ctx.plus_minus_operators() != null || ctx.mult_div_operators() != null) {
            TypeSymbol leftType = (TypeSymbol) semanticVisitor.visit(ctx.getChild(0));
            TypeSymbol rightType = (TypeSymbol) semanticVisitor.visit(ctx.getChild(2));
            block.append((IRBlock) visit(ctx.getChild(0)));
            IRValue left = block.getCurrentIdentifier();
            block.append((IRBlock) visit(ctx.getChild(2)));
            IRValue right = block.getCurrentIdentifier();
            IRVariable tempVariable = new IRVariable(namer.generateTempName(), leftType.resolve(rightType));
            if (ctx.plus_minus_operators() != null) {
                if (ctx.plus_minus_operators().PLUS() != null)
                    block.addStatement(new IRAdd(left, right, tempVariable));
                else if (ctx.plus_minus_operators().MINUS() != null)
                    block.addStatement(new IRSub(left, right, tempVariable));
            } else {
                if (ctx.mult_div_operators().MULT() != null)
                    block.addStatement(new IRMult(left, right, tempVariable));
                else
                    block.addStatement(new IRDiv(left, right, tempVariable));
            }
            block.addVariable(tempVariable);
        }

        if (ctx.POW() != null) {
            // expr POW expr
            // implement using iterative multiplicaton
            TypeSymbol type = (TypeSymbol) semanticVisitor.visit(ctx.getChild(0));

            IRLocationIdentifier powStart = new IRLocationIdentifier(namer.generateLocationName());
            IRLocationIdentifier powEnd = new IRLocationIdentifier(namer.generateLocationName());
            IRLocationIdentifier pow0 = new IRLocationIdentifier(namer.generateLocationName());

            // get base/exponent identifiers from expressions
            block.append((IRBlock) visit(ctx.expr(0)));
            IRValue base = block.getCurrentIdentifier();
            block.append((IRBlock) visit(ctx.expr(1)));
            IRVariable exponent = new IRVariable(namer.generateTempName(), PrimitiveTypes.TigerInt);
            block.addStatement(new IRAssign(exponent, block.getCurrentIdentifier()));
            block.addVariable(exponent);

            // define and initialize accum variable for storing results
            IRVariable accum = new IRVariable(namer.generateTempName(), type);
            block.addStatement(new IRAssign(accum, base));
            block.addVariable(accum);

            // check if exponent is 0 => just want to return 1
            block.addStatement(new IRBranchEqual(exponent, IRConstant.Zero(), pow0));

            block.addStatement(powStart);

            // check condition and perform multiplication
            block.addStatement(new IRBranchLessEqual(exponent, IRConstant.One(), powEnd));
            block.addStatement(new IRMult(accum, base, accum));
            block.addStatement(IRSub.dec(exponent));
            block.addStatement(new IRJump(powStart));

            // simply set accum to 1 if our exponent was 0
            block.addStatement(pow0);
            block.addStatement(new IRAssign(accum, IRConstant.One(base.getType())));

            block.addStatement(powEnd);
            block.setCurrentIdentifier(accum);
        }

        if (ctx.OPENPAREN() != null) {
            block.append((IRBlock) visit(ctx.getChild(1)));
        }

        return block;
    }

    @Override
    public IRBlock visitStat_seq(TigerParser.Stat_seqContext ctx) {
        if (ctx.stat() == null)
            return new IRBlock();
        IRBlock block = new IRBlock();
        block.append((IRBlock) visit(ctx.stat()));
        if (ctx.stat_seq() != null)
            block.append((IRBlock) visit(ctx.stat_seq()));
        return block;
    }

    @Override
    public IRBlock visitStat(TigerParser.StatContext ctx) {

        // used for name mangling
        // careful with interactions with enterScope()/exitScope()
        IRBlock block = new IRBlock();

        // function call
        if (ctx.ID() != null && ctx.OPENPAREN() != null) {
            String functionName = ctx.ID().getText();
            FunctionSymbol function = (FunctionSymbol) symbolTable.lookup(functionName).get();
            List<IRBlock> exprBlocks = ((List<IRBlock>) visit(ctx.expr_list()));
            List<IRValue> args = new ArrayList<>();
            for (IRBlock exprBlock : exprBlocks) {
                args.add(exprBlock.getCurrentIdentifier());
                block.append(exprBlock);
            }
            if (ctx.optprefix().ASSIGN() != null) {
                TypeSymbol returnType = function.getReturnType();
                if (ctx.optprefix().value().value_tail().OPENBRACK() != null) {
                    // lhs is an array access e.g. arr[1] := funct(a, b);
                    // => (1) callr into temp
                    IRVariable callDestination = new IRVariable(namer.generateTempName(), returnType);
                    block.addStatement(new IRCallR(function, callDestination, args));
                    block.addVariable(callDestination);

                    // => (2) array_store from temp
                    int scopeId = symbolTable.getDeclaredScope(ctx.optprefix().value().ID().getText());
                    String arrayName = namer.mangle(ctx.optprefix().value().ID().getText(), scopeId);
                    IRVariable array = new IRVariable(arrayName, getType(arrayName));
                    block.append((IRBlock) visit(ctx.value().value_tail().expr()));
                    block.addStatement(new IRArrayStore(array, block.getCurrentIdentifier(), callDestination));
                } else {
                    // lhs is regular value
                    int scopeId = symbolTable.getDeclaredScope(ctx.optprefix().value().ID().getText());
                    String destinationName = namer.mangle(ctx.optprefix().value().ID().getText(), scopeId);
                    IRVariable callDestination = new IRVariable(destinationName, returnType);
                    block.addStatement(new IRCallR(function, callDestination, args));
                }
            } else
                block.addStatement(new IRCall(function, args));
        }

        if (ctx.IF() != null) {
            // if or if-else still visit the if expr and get name
            block.append((IRBlock) visit(ctx.getChild(1)));
            IRValue ifExprName = block.getCurrentIdentifier();
            if (ctx.ELSE() == null) {
                IRLocationIdentifier afterIf = new IRLocationIdentifier(namer.generateLocationName());
                block.addStatement(IRBranchEqual.cond(ifExprName, afterIf));
                block.append((IRBlock) visit(ctx.getChild(3)));
                block.addStatement(afterIf);
            } else {
                IRLocationIdentifier elseBlock = new IRLocationIdentifier(namer.generateLocationName());
                IRLocationIdentifier afterElse = new IRLocationIdentifier(namer.generateLocationName());
                block.addStatement(IRBranchEqual.cond(ifExprName, elseBlock));
                block.append((IRBlock) visit(ctx.getChild(3)));
                block.addStatement(new IRJump(afterElse));
                block.addStatement(elseBlock);
                block.append((IRBlock) visit(ctx.getChild(5)));
                block.addStatement(afterElse);
            }
        }

        if (isLoop(ctx)) {
            // create jump target locations for start/end
            IRLocationIdentifier loopStart = new IRLocationIdentifier(namer.generateLocationName());
            IRLocationIdentifier loopEnd = new IRLocationIdentifier(namer.generateLocationName());

            loopEndStack.push(loopEnd);

            if (ctx.WHILE() != null) {
                // WHILE expr DO stat_seq ENDDO SEMICOLON |

                // start loop
                block.addStatement(loopStart);

                // get identifier for loop variable
                block.append((IRBlock) visit(ctx.getChild(1)));
                IRValue condition = block.getCurrentIdentifier();

                // check if loop expression false; goto loopEnd if so
                block.addStatement(IRBranchEqual.cond(condition, loopEnd));

                // visit inner loop statements
                block.append((IRBlock) visit(ctx.getChild(3)));

                // return to start of loop
                block.addStatement(new IRJump(loopStart));

                block.addStatement(loopEnd);
            }

            if (ctx.FOR() != null) {
                // FOR ID ASSIGN expr TO expr DO stat_seq ENDDO SEMICOLON

                // get the identifier for our loop condition variable
                int scopeId = symbolTable.getDeclaredScope(ctx.ID().getText());
                IRVariable loopVariable = new IRVariable(namer.mangle(ctx.ID().getText(), scopeId), PrimitiveTypes.TigerInt);
                block.append((IRBlock) visit(ctx.getChild(3)));

                // get the identifier for our initial loop variable value
                // and initialize our loop variable
                IRValue loopInit = block.getCurrentIdentifier();
                block.addStatement(new IRAssign(loopVariable, loopInit));

                block.addStatement(loopStart);

                // get the loop termination identifier
                block.append((IRBlock) visit(ctx.getChild(5)));
                IRValue loopTerm = block.getCurrentIdentifier();

                // check if our loop var is greater than loop
                // termination value and goto loop end if so
                block.addStatement(new IRBranchGreat(loopVariable, loopTerm, loopEnd));

                // visit loop inner statements
                block.append((IRBlock) visit(ctx.getChild(7)));

                // increment our index
                block.addStatement(IRAdd.inc(loopVariable));
                block.addStatement(new IRJump(loopStart));

                block.addStatement(loopEnd);
            }

            loopEndStack.pop();
        }

        if (ctx.ASSIGN() != null && ctx.FOR() == null) {
            // value ASSIGN expr SEMICOLON |
            if (ctx.value().value_tail().OPENBRACK() != null) {
                // array load
                int scopeId = symbolTable.getDeclaredScope(ctx.value().ID().getText());
                String arrayName = ctx.value().ID().getText();
                IRVariable array = new IRVariable(namer.mangle(arrayName, scopeId), getType(arrayName));
                block.append((IRBlock) visit(ctx.value().value_tail().expr()));
                IRValue arrayIndex = block.getCurrentIdentifier();
                block.append((IRBlock) visit(ctx.getChild(2)));
                block.addStatement(new IRArrayStore(array, arrayIndex, block.getCurrentIdentifier()));
            } else {
                block.append((IRBlock) visit(ctx.getChild(0)));
                IRVariable left = (IRVariable) block.getCurrentIdentifier();
                block.append((IRBlock) visit(ctx.getChild(2)));
                block.addStatement(new IRAssign(left, block.getCurrentIdentifier()));
            }
        }

        if (ctx.BREAK() != null) {
            assert(!loopEndStack.isEmpty());
            block.addStatement(new IRJump(loopEndStack.peek()));
        }

        if (ctx.RETURN() != null) {
            if (ctx.optreturn().expr() != null) {
                block.append((IRBlock) visit(ctx.optreturn().expr()));
                block.addStatement(new IRReturn(block.getCurrentIdentifier()));
            } else
                block.addStatement(new IRReturn());
        }

        if (ctx.LET() != null) {
            symbolTable.enterScope();
            block.append((IRBlock) visit(ctx.declaration_segment()));
            block.append((IRBlock) visit(ctx.getChild(3)));
            symbolTable.exitScope();
        }

        return block;
    }

    @Override
    public IRBlock visitValue(TigerParser.ValueContext ctx) {
        IRBlock block = new IRBlock();
        if (ctx.value_tail().OPENBRACK() != null) {
            // array access
            block.append((IRBlock) visit(ctx.value_tail().expr()));
            String arrayName = ctx.ID().getText();
            IRValue arrayIndex = block.getCurrentIdentifier();
            TypeSymbol arrayType = getType(arrayName);
            IRVariable destination = new IRVariable(namer.generateTempName(), arrayType.getBaseType());
            IRVariable sourceArray = new IRVariable(namer.mangle(arrayName, symbolTable.getDeclaredScope(arrayName)), arrayType);
            block.addStatement(new IRArrayLoad(sourceArray, arrayIndex, destination));
            block.addVariable(destination);
        } else {
            TypeSymbol type = (TypeSymbol) semanticVisitor.visitValue(ctx);
            String valueName = namer.mangle(ctx.ID().getText(), symbolTable.getDeclaredScope(ctx.ID().getText()));
            block.setCurrentIdentifier(new IRVariable(valueName, type));
        }
        return block;
    }

    @Override
    public IRBlock visitConst(TigerParser.ConstContext ctx) {
        IRBlock block = new IRBlock();
        if (ctx.INTLIT() != null)
            block.setCurrentIdentifier(new IRConstant(PrimitiveTypes.TigerInt, ctx.INTLIT().getText()));
        else
            block.setCurrentIdentifier(new IRConstant(PrimitiveTypes.TigerFloat, ctx.FLOATLIT().getText()));
        return block;
    }


    /*
    -----------------------------------------------
    ------------------- Private -------------------
    -----------------------------------------------
     */

    private boolean isLoop(TigerParser.StatContext ctx) {
        return (ctx.FOR() != null || ctx.WHILE() != null);
    }

    private TypeSymbol getType(String variableName) {
        VariableSymbol variable = (VariableSymbol) symbolTable.lookup(variableName).get();
        return variable.getType();
    }
}
