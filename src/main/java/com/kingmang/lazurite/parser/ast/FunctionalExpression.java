package com.kingmang.lazurite.parser.ast;

import com.kingmang.lazurite.LZREx.LZRException;
import com.kingmang.lazurite.LZREx.VarExeption;
import com.kingmang.lazurite.core.*;
import com.kingmang.lazurite.runtime.LZR.LZRFunction;
import com.kingmang.lazurite.runtime.Value;
import com.kingmang.lazurite.runtime.Variables;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public final class FunctionalExpression extends InterruptableNode implements Expression, Statement {
    
    public final Expression functionExpr;
    public final List<Expression> arguments;
    
    public FunctionalExpression(Expression functionExpr) {
        this.functionExpr = functionExpr;
        arguments = new ArrayList<>();
    }
    
    public void addArgument(Expression arg) {
        arguments.add(arg);
    }
    
    @Override
    public void execute() {
        eval();
    }
    
    @Override
    public Value eval() {
        super.interruptionCheck();
        final int size = arguments.size();
        final Value[] values = new Value[size];
        for (int i = 0; i < size; i++) {
            values[i] = arguments.get(i).eval();
        }
        final Function f = consumeFunction(functionExpr);
        CallStack.enter(functionExpr.toString(), f);
        try {
            final Value result = f.execute(values);
            CallStack.exit();
            return result;
        } catch (LZRException ex) {
            throw new RuntimeException(ex.getMessage() + " in function " + functionExpr, ex);
        }
    }
    
    private Function consumeFunction(Expression expr) {
        try {
            final Value value = expr.eval();
            if (value.type() == Types.FUNCTION) {
                return ((LZRFunction) value).getValue();
            }
            return getFunction(value.asString());
        } catch (VarExeption ex) {
            return getFunction(ex.getVariable());
        }
    }
    
    private Function getFunction(String key) {
        if (KEYWORD.isExists(key)) {
            return KEYWORD.get(key);
        }
        if (Variables.isExists(key)) {
            final Value variable = Variables.get(key);
            if (variable.type() == Types.FUNCTION) {
                return ((LZRFunction)variable).getValue();
            }
        }
        throw new LZRException("UnknownFunctionException ","Unknown function "+ key);
    }
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <R, T> R accept(ResultVisitor<R, T> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (functionExpr instanceof ValueExpression && ((ValueExpression)functionExpr).value.type() == Types.STRING) {
            sb.append(((ValueExpression)functionExpr).value.asString()).append('(');
        } else {
            sb.append(functionExpr).append('(');
        }
        final Iterator<Expression> it = arguments.iterator();
        if (it.hasNext()) {
            sb.append(it.next());
            while (it.hasNext()) {
                sb.append(", ").append(it.next());
            }
        }
        sb.append(')');
        return sb.toString();
    }
}
