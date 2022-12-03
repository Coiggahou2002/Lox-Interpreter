package com.coiggahou.lox;

import com.coiggahou.lox.error.RuntimeError;

import java.util.List;

public class Interpreter implements Expr.Visitor<Object>,
                                    Stmt.Visitor<Void>{

    private final Environment environment = new Environment();


    /**
     * this method determines what is truthy in Lox
     * and also what is not
     *
     * temporarily we simply assume that
     * everything is truthy except `nil` and `false`
     */
    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    /**
     * method that determines whether two objects are equal
     * __NO IMPLICIT CONVERSIONS__ during comparison
     */
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    /**
     * @throws RuntimeError with operator info if operand is not a number
     */
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    /**
     * @throws RuntimeError with operator info
     *          whenever anyone of the two is not a number
     */
    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Both operands must be numbers.");
    }

    /**
     * toString() in Lox
     */
    private String stringify(Object object) {
        if (object == null) return "nil";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    @Override
    public Object visitBinaryExpr(Expr.BinaryExpr expr) {
        Object l = evaluate(expr.left);
        Object r = evaluate(expr.right);
        switch (expr.operator.type) {
            case PLUS -> {
                if (l instanceof String && r instanceof String) {
                    return (String)l + (String)r;
                }
                // If one side of the `+` is String
                // while the other side is not,
                // automatically cast the non-String side to String
                // then do concatenation
                if (l instanceof String) {
                    return (String)l + stringify(r);
                }
                if (r instanceof String) {
                    return stringify(l) + (String)r;
                }
                if (l instanceof Double && r instanceof Double) {
                    return (double)l + (double)r;
                }
                throw new RuntimeError(expr.operator, "Oprands must be two numbers or two strings.");
            }
            case MINUS -> {
                checkNumberOperands(expr.operator, l, r);
                return (double)l - (double)r;
            }
            case STAR -> {
                checkNumberOperands(expr.operator, l, r);
                return (double)l * (double)r;
            }
            case SLASH -> {
                checkNumberOperands(expr.operator, l, r);
                return (double)l / (double)r;
            }
            case GREATER -> {
                checkNumberOperands(expr.operator, l, r);
                return (double)l > (double)r;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, l, r);
                return (double)l >= (double)r;
            }
            case LESS -> {
                checkNumberOperands(expr.operator, l, r);
                return (double)l < (double)r;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expr.operator, l, r);
                return (double)l <= (double)r;
            }
            case EQUAL_EQUAL -> {
                return isEqual(l, r);
            }
            case BANG_EQUAL -> {
                return !isEqual(l, r);
            }

        }
        return null;
    }

    @Override
    public Object visitUnaryExpr(Expr.UnaryExpr expr) {
        Object r = evaluate(expr.right);
        switch (expr.operator.type) {
            case BANG -> {
                return !isTruthy(r);
            }
            case MINUS -> {
                checkNumberOperand(expr.operator, r);
                return -(double)r;
            }
        }
        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.GroupingExpr expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.LiteralExpr expr) {
        return expr.value;
    }

    /**
     * what to do when we meet an identifier
     * (like the way we use a variable by its name usually)
     */
    @Override
    public Object visitVarExpr(Expr.VarExpr expr) {
        return environment.get(expr.identifier);
    }


    /**
     * An assign statement usually returns the assigned value
     *
     * e.g. In C or Cpp, we sometimes see
     *
     *          while((line = readLine()) != null) { // ... }
     *
     *      the assignment (line = readLine()) returns the assigned value
     */
    @Override
    public Object visitAssignExpr(Expr.AssignExpr expr) {
        return environment.assign(expr.assignee, evaluate(expr.assigner));
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }


    /**
     * an expression statement is sth like `1+2;`
     * we only need to evaluate it to make sure that
     * every sub-expression in it is evaluated
     * but without returning anything
     * @return NOTHING
     */
    @Override
    public Void visitExpressionStmt(Stmt.ExpressionStmt stmt) {
        evaluate(stmt.expr);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.PrintStmt stmt) {
        System.out.println(stringify(evaluate(stmt.expr)));
        return null;
    }

    /**
     * Whenever our client use "var" to declare a new variable,
     * we need to add a record to our variable table
     */
    @Override
    public Void visitDeclarationStmt(Stmt.DeclarationStmt stmt) {
        String varName = stmt.name.lexeme;
        // if a variable is defined but not assigned,
        // we set its value to `nil` (aka. null in Java)
        Object initializeValue = null;
        if (stmt.initializer != null) {
            initializeValue = evaluate(stmt.initializer);
        }
        environment.define(varName, initializeValue);
        return null;
    }


    private void execute(Stmt statement) {
        statement.accept(this);
    }


    void interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        }
        catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        }
        catch (RuntimeError error){
            Lox.runtimeError(error);
        }

    }


}
