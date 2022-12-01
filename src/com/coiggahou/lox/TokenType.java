package com.coiggahou.lox;

public enum TokenType {

    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN, // ( )
    LEFT_BRACE, RIGHT_BRACE, // { }
    COMMA,                   // ,
    DOT,                     // .
    MINUS, PLUS,             // - +
    SEMICOLON,               // ;
    SLASH,                   // /
    STAR,                    // *

    // One or two character tokens
    BANG, BANG_EQUAL,        // !  !=
    EQUAL, EQUAL_EQUAL,      // =  ==
    GREATER, GREATER_EQUAL,  // >  >=
    LESS, LESS_EQUAL,        // <  <=

    // Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords
    AND, OR,
    TRUE, FALSE,
    IF, ELSE,
    WHILE, FOR,
    NIL,
    PRINT,
    RETURN,
    CLASS, SUPER, THIS,
    VAR,
    FUN, // function declaration

    // end of file
    EOF
}
