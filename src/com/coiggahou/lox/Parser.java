package com.coiggahou.lox;

import java.util.List;

import static com.coiggahou.lox.TokenType.*;


class Parser {

    /**
     * the input of the parse
     * a sequence of tokens
     */
    private final List<Token> tokens;

    private static class ParseError extends RuntimeException{}

    /**
     * index of the next token waiting to be parsed
     */
    private int current = 0;

    /**
     * aims to see if we reach EOF
     * which means we have no token left to parse
     * instead of saying if current == tokens.size()
     */
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    /**
     * look at the current token
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * look at the previous token
     */
    private Token previous() {
        if (current == 0) {
            return tokens.get(0);
        }
        return tokens.get(current - 1);
    }

    /**
     * consume the current token and returns it
     */
    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    /**
     * look at the current token
     * and see if it is of the TokenType we want
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        return peek().type == type;
    }

    /**
     * if the current token matches any of the [...types],
     * return true
     */
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    /**
     * try to match a token of specified type
     * catch and throw error during matching with specified message
     */
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            return new Expr.UnaryExpr(previous(), unary());
        }
        return primary();
    }

    private Expr primary() {
        if (match(TRUE)) { return new Expr.LiteralExpr(true); }
        if (match(FALSE)) { return new Expr.LiteralExpr(false); }
        if (match(NIL)) { return new Expr.LiteralExpr(null); };

        if (match(NUMBER, STRING)) {
            return new Expr.LiteralExpr(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "expected ')' after expression");
            return expr;
        }
        throw error(peek(), "Expect expression.");
    }


    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }
}
