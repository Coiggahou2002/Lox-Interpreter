package com.coiggahou.lox;

import java.util.ArrayList;
import java.util.List;

import static com.coiggahou.lox.TokenType.*;


class Parser {

    private boolean allowREPLSingleExpression = false;
    private boolean foundREPLSingleExpression = false;


    /**
     * the input of the parse
     * a sequence of tokens
     */
    private final List<Token> tokens;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private static class ParseError extends RuntimeException {
    }

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

    /**
     * the method aims to skip over the current matching process for a declaration
     * when sth unexpected occur
     * and continue to parse the next possible declaration
     */
    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;
            switch (peek().type) {
                case CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> {
                    return;
                }
            }
            advance();
        }
    }

    /**
     * declaration -> variableDeclaration | statement
     */
    private Stmt declaration() {
        try {
            if (match(VAR)) {
                return variableDeclaration();
            }
            return statement();
        } catch (ParseError error) {
            // if error occur when parsing the current declaration,
            // use synchronize() to skip over it
            // and begin the parsing process for the next possible declaration
            synchronize();
            return null;
        }
    }

    /**
     * variableDeclaration -> "var" IDENTIFIER ("=" expr)? ";"
     */
    private Stmt variableDeclaration() {
        Token identifier = consume(IDENTIFIER, "expect variable name");
        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }
        consume(SEMICOLON, "expect ';' after variable declaration");
        return new Stmt.DeclarationStmt(identifier, initializer);
    }

    /**
     * stmt -> printStmt | exprStmt | ifStmt | block
     * printStmt -> "print" expr ";"
     * exprStmt  -> expr ";"
     * ifStmt    -> "if" "(" expression ")" statement ("else" statement)?
     * block     -> "{" declaration* "}"
     */
    private Stmt statement() {
        if (match(PRINT)) {
            return printStatement();
        }
        if (match(LEFT_BRACE)) {
            return block();
        }
        if (match(IF)) {
            return ifStatement();
        }
        if (match(WHILE)) {
            return whileStatement();
        }
        return expressionStatement();
    }

    private Stmt printStatement() {
        Expr exprToPrint = expression();
        consume(SEMICOLON, "expect ';' after print statement");
        return new Stmt.PrintStmt(exprToPrint);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();

        // if we are doing single-expr-evaluate of REPL,
        // we are not going to consume the SEMICOLON here
        // to avoid any error to be thrown
        if (allowREPLSingleExpression && isAtEnd()) {
            foundREPLSingleExpression = true;
        }
        else {
            consume(SEMICOLON, "expect ';' after expression");
        }
        return new Stmt.ExpressionStmt(expr);
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "expect '(' after 'if'");
        Expr condition = expression();
        consume(RIGHT_PAREN, "expect ')' after 'if' condition expression");
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.IfStmt(condition, thenBranch, elseBranch);
    }

    /**
     * whileStmt -> "while" "(" expression ")" statement
     */
    private Stmt whileStatement() {
        consume(LEFT_PAREN, "expect '(' after 'while'");
        Expr cond = expression();
        consume(RIGHT_PAREN, "expect ')' after while conditional expression");
        Stmt body = statement();
        return new Stmt.WhileStmt(cond, body);
    }

    private Stmt block() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(RIGHT_BRACE, "expect '}' at the end of a block");
        return new Stmt.BlockStmt(statements);
    }

    /**
     * expr -> equality
     */
    private Expr expression() {
        return assignment();
    }

    /**
     * assignment -> IDENTIFIER "=" assignment | logic_or
     *
     * the SELECT sets of these two production has an intersecting part { IDENTIFIER }
     *
     * In fact, IDENTIFIER in the former production is an LValue,
     * and IDENTIFIER in the latter is an RValue,
     * but the parser doesn't know whether it is an LValue or an RValue until it meets the "="
     *
     * It is easy to make wrong choice here
     * We have to do sth to determine which production we choose to continue
     *
     * We have two ways to solve the problem:
     * 1. look ahead for one more token, if we see "=", we know the ID is an LValue
     * 2. try to match logic_or() first without looking ahead for one more token
     */
    private Expr assignment() {
        // try to match logic_or() first
        Expr expr = logic_or();

        // if we didn't see "=" here, we ends here.
        if (match(EQUAL)) {
            // if we did see "=",
            // it means that the equality() we match before is actually a IDENTIFIER,
            // or to say, a VarExpr
            Token equal = previous();
            Expr assigner = assignment();
            if (expr instanceof Expr.VarExpr) {
                Token assignee = ((Expr.VarExpr) expr).identifier;
                return new Expr.AssignExpr(assignee, assigner);
            }
            error(equal, "Invalid assignment target.");
        }
        return expr;
    }

    /**
     * logic_or -> logic_and ("or" logic_and)*
     */
    private Expr logic_or() {
        Expr expr = logic_and();
        while (match(OR)) {
            Token operator = previous();
            Expr right = logic_and();
            expr = new Expr.LogicExpr(expr, operator, right);
        }
        return expr;
    }

    /**
     * logic_and -> equality ("and" equality)*
     */
    private Expr logic_and() {
        Expr expr = equality();
        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.LogicExpr(expr, operator, right);
        }
        return expr;
    }

    /**
     * equality -> expr (("!="|"==") expr)*
     */
    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    /**
     * comparison -> term ((">"|">="|"<"|"<=") term)*
     */
    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    /**
     * term -> factor (("-"|"+") factor)*
     */
    private Expr term() {
        Expr expr = factor();
        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    /**
     * factor -> unary (("*"|"/") unary)*
     */
    private Expr factor() {
        Expr expr = unary();
        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    /**
     * unary -> ("!"|"-") unary | primary
     */
    private Expr unary() {
        if (match(BANG, MINUS)) {
            return new Expr.UnaryExpr(previous(), unary());
        }
        return primary();
    }

    /**
     * primary -> "true" | "false" | "nil" | NUMBER | STRING | "("expr")" | IDENTIFIER
     */
    private Expr primary() {
        if (match(TRUE)) {
            return new Expr.LiteralExpr(true);
        }
        if (match(FALSE)) {
            return new Expr.LiteralExpr(false);
        }
        if (match(NIL)) {
            return new Expr.LiteralExpr(null);
        }

        if (match(NUMBER, STRING)) {
            return new Expr.LiteralExpr(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "expected ')' after expression");
            return expr;
        }

        if (match(IDENTIFIER)) {
            return new Expr.VarExpr(previous());
        }

        throw error(peek(), "Unknown symbol");
    }

    /**
     * start to match statement by statement
     */
    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            Stmt stmt = declaration();
            // if error occur, due to synchronize(),
            // declaration() may return null
            // we shouldn't add null statement
            if (stmt != null) statements.add(stmt);
        }
        return statements;
    }

    void resetTokenPointer() {
        this.current = 0;
    }

    Object parseRepl() {
        allowREPLSingleExpression = true;

        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            Stmt stmt = declaration();
            if (stmt == null) continue;
            statements.add(stmt);

            // this flag will only be set and only set ONCE
            // in expressionStatement()
            // and it is used as a one-time flag
            // to indicate that we've found a single-expression
            // without a SEMICOLON followed
            if (foundREPLSingleExpression) {
                return ((Stmt.ExpressionStmt)stmt).expr;
            }

            // If we FOUND the single-expression during the first
            // declaration(), at this time
            //
            //      foundREPLSingleExpression is true
            //      allowREPLSingleExpression is true
            //
            // the function will return and won't reach the code below.
            // -------------------------------------------------------
            // If NOT, at this time
            //
            //      foundREPLSingleExpression is false
            //      allowREPLSingleExpression is true
            //
            // the `allow` flag should be set to false since the first
            // statement was added to the statement list to make sure
            // that the parser won't try to match single-expression in
            // the following tokens anymore, meanwhile the `found` flag
            // will not be change anymore.
            allowREPLSingleExpression = false;
        }
        return statements;
    }

}
