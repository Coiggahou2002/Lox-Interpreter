package com.coiggahou.lox;
abstract class Expr {

    /**
     * an expression
     * 1. accept a visitor
     * 2. choose one of the visit method
     * 3. pass itself as an argument to the method
     * 4. let the visitor execute the specified method
     */
    abstract <R> R accept(Visitor<R> visitor);

    /**
     * any implemented visitor to visit an expression
     * should implement the following 5 methods
     * @param <R> is the type of the methods' return value
     */
    interface Visitor<R> {
        R visitBinaryExpr(BinaryExpr expr);
        R visitUnaryExpr(UnaryExpr expr);
        R visitGroupingExpr(GroupingExpr expr);
        R visitLiteralExpr(LiteralExpr expr);
        R visitVarExpr(VarExpr expr);
        R visitAssignExpr(AssignExpr expr);
        R visitLogicExpr(LogicExpr expr);
    }

    static class BinaryExpr extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;
        BinaryExpr(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    static class UnaryExpr extends Expr {
        final Token operator;
        final Expr right;
        UnaryExpr(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    static class GroupingExpr extends Expr {
        final Expr expression;
        GroupingExpr(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    static class LiteralExpr extends Expr {
        final Object value;
        LiteralExpr(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    /**
     * a VarExpr should be evaluated as
     * the RValue of a variable
     * which means the value of a variable
     */
    static class VarExpr extends Expr {
        final Token identifier;
        VarExpr(Token identifier) {
            this.identifier = identifier;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarExpr(this);
        }
    }

    static class AssignExpr extends Expr {
        final Token assignee;
        final Expr assigner;

        AssignExpr(Token assignee, Expr assigner) {
            this.assignee = assignee;
            this.assigner = assigner;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    static class LogicExpr extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        LogicExpr(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicExpr(this);
        }
    }

}
