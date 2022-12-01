package com.coiggahou.lox;

public class Token {

    final TokenType type;

    /**
     * the word
     */
    final String lexeme;

    /**
     * literal (can be a string or a number)
     */
    final Object literal;

    /**
     * where the token is in the source file
     */
    final int line;

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
