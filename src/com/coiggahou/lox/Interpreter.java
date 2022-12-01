package com.coiggahou.lox;

public class Interpreter implements Expr.Visitor<Object> {

    void interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        }
        catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

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
                checkNumberOprands(expr.operator, l, r);
                return (double)l - (double)r;
            }
            case STAR -> {
                checkNumberOprands(expr.operator, l, r);
                return (double)l * (double)r;
            }
            case SLASH -> {
                checkNumberOprands(expr.operator, l, r);
                return (double)l / (double)r;
            }
            case GREATER -> {
                checkNumberOprands(expr.operator, l, r);
                return (double)l > (double)r;
            }
            case GREATER_EQUAL -> {
                checkNumberOprands(expr.operator, l, r);
                return (double)l >= (double)r;
            }
            case LESS -> {
                checkNumberOprands(expr.operator, l, r);
                return (double)l < (double)r;
            }
            case LESS_EQUAL -> {
                checkNumberOprands(expr.operator, l, r);
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
                checkNumberOprand(expr.operator, r);
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

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }


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
     * method who determines whether two objects are equal
     * __NO IMPLICIT CONVERSIONS__ during comparison
     * @param a
     * @param b
     * @return
     */
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    private boolean checkNumberOprand(Token operator, Object oprand) {
        if (oprand instanceof Double) return true;
        throw new RuntimeError(operator, "Oprand must be a number.");
    }
    private boolean checkNumberOprands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return true;
        throw new RuntimeError(operator, "Oprand must be a number.");
    }
}
