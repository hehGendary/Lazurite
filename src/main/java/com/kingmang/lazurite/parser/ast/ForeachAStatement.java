package com.kingmang.lazurite.parser.ast;

import com.kingmang.lazurite.LZREx.LZRException;
import com.kingmang.lazurite.core.*;
import com.kingmang.lazurite.runtime.*;
import com.kingmang.lazurite.runtime.LZR.LZRArray;
import com.kingmang.lazurite.runtime.LZR.LZRMap;
import com.kingmang.lazurite.runtime.LZR.LZRString;


import java.util.Map;


public final class ForeachAStatement extends InterruptableNode implements Statement {
    
    public final String variable;
    public final Expression container;
    public final Statement body;

    public ForeachAStatement(String variable, Expression container, Statement body) {
        this.variable = variable;
        this.container = container;
        this.body = body;
    }

    @Override
    public void execute() {
        super.interruptionCheck();
        final Value previousVariableValue = Variables.isExists(variable) ? Variables.get(variable) : null;

        final Value containerValue = container.eval();
        switch (containerValue.type()) {
            case Types.STRING:
                iterateString(containerValue.asString());
                break;
            case Types.ARRAY:
                iterateArray((LZRArray) containerValue);
                break;
            case Types.MAP:
                iterateMap((LZRMap) containerValue);
                break;
            default:
                throw new LZRException("TypeExeption","Cannot iterate " + Types.typeToString(containerValue.type()));
        }

        // Restore variables
        if (previousVariableValue != null) {
            Variables.set(variable, previousVariableValue);
        } else {
            Variables.remove(variable);
        }
    }

    private void iterateString(String str) {
        for (char ch : str.toCharArray()) {
            Variables.set(variable, new LZRString(String.valueOf(ch)));
            try {
                body.execute();
            } catch (BreakStatement bs) {
                break;
            } catch (ContinueStatement cs) {
                // continue;
            }
        }
    }

    private void iterateArray(LZRArray containerValue) {
        for (Value value : containerValue) {
            Variables.set(variable, value);
            try {
                body.execute();
            } catch (BreakStatement bs) {
                break;
            } catch (ContinueStatement cs) {
                // continue;
            }
        }
    }

    private void iterateMap(LZRMap containerValue) {
        for (Map.Entry<Value, Value> entry : containerValue) {
            Variables.set(variable, new LZRArray(new Value[] {
                    entry.getKey(),
                    entry.getValue()
            }));
            try {
                body.execute();
            } catch (BreakStatement bs) {
                break;
            } catch (ContinueStatement cs) {
                // continue;
            }
        }
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
        return String.format("for %s : %s %s", variable, container, body);
    }
}
