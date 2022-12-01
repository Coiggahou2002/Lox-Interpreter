package com.coiggahou.lox;

public class AstPrinter implements Expr.Visitor<String>{
    String print(Expr expr) {
        return expr.accept(this);
    }

    /**
     * parenthesize a list of expressions with a description at the head
     * @param desc
     * @param exprs
     * @return the parenthesized string
     */
    private String parenthesize(String desc, Expr... exprs) {

        StringBuilder sb = new StringBuilder();

        // add the opening ( and the description
        sb.append("(").append(desc);

        // recusively add all the expression string
        for (Expr expr : exprs) {
            sb.append(" ");
            sb.append(expr.accept(this));
        }

        // add the closing )
        sb.append(")");

        return sb.toString();
    }

    @Override
    public String visitBinaryExpr(Expr.BinaryExpr expr) {
        return parenthesize(
                expr.operator.lexeme,
                expr.left,
                expr.right
        );
    }

    @Override
    public String visitUnaryExpr(Expr.UnaryExpr expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.GroupingExpr expr) {
        return parenthesize("group", expr.expression);
    }

    /**
     * Literal Expression is terminator,
     * so we won't parenthesize it
     */
    @Override
    public String visitLiteralExpr(Expr.LiteralExpr expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    // just for test
    public static void main(String[] args) {
        Expr expression = new Expr.BinaryExpr(
                new Expr.UnaryExpr(
                            new Token(TokenType.MINUS, "-", null, 1),
                            new Expr.LiteralExpr(123)
                        ),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.GroupingExpr(
                            new Expr.LiteralExpr(45.67)
                )
        );
        System.out.println(new AstPrinter().print(expression));
    }
}
