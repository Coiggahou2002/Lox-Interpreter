package com.coiggahou.lox;

import java.util.List;

abstract class Stmt {

    abstract <R> R accept(Visitor<R> visitor);

    interface Visitor<R> {
        R visitExpressionStmt(ExpressionStmt stmt);
        R visitPrintStmt(PrintStmt stmt);
        R visitDeclarationStmt(DeclarationStmt stmt);
        R visitBlockStmt(BlockStmt stmt);
    }



    static class ExpressionStmt extends Stmt {
        final Expr expr;

        ExpressionStmt(Expr expr) {
            this.expr = expr;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    /**
     * `print xxx;`
     */
    static class PrintStmt extends Stmt {
        final Expr expr;

        PrintStmt(Expr expr) {
            this.expr = expr;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }


    /**
     * `var a = expression;`
     */
    static class DeclarationStmt extends Stmt {
        final Token name;
        final Expr initializer;

        DeclarationStmt(Token name) {
            this.name = name;
            this.initializer = null;
        }

        DeclarationStmt(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitDeclarationStmt(this);
        }
    }

    static class BlockStmt extends Stmt {
        final List<Stmt> declarations;

        BlockStmt(List<Stmt> declarations) {
            this.declarations = declarations;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }
}
