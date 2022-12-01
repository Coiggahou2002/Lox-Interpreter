package com.coiggahou.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * to scan through the list of characters and
 * group them together into the smallest sequences
 * that still represent something
 */
public class Scanner {
    /**
     * store the raw source code as a simple string
     */
    private final String source;


    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    TokenType.AND);
        keywords.put("class",  TokenType.CLASS);
        keywords.put("else",   TokenType.ELSE);
        keywords.put("false",  TokenType.FALSE);
        keywords.put("for",    TokenType.FOR);
        keywords.put("fun",    TokenType.FUN);
        keywords.put("if",     TokenType.IF);
        keywords.put("nil",    TokenType.NIL);
        keywords.put("or",     TokenType.OR);
        keywords.put("print",  TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super",  TokenType.SUPER);
        keywords.put("this",   TokenType.THIS);
        keywords.put("true",   TokenType.TRUE);
        keywords.put("var",    TokenType.VAR);
        keywords.put("while",  TokenType.WHILE);
    }


    private final List<Token> tokens = new ArrayList<>();

    /**
     * we use double cursor to scan chars
     * the slow one points to the head of the possible lexeme,
     * the fast one goes forward to detect and match,
     * once matched, extract source[start, current]
     */
    private int start = 0;
    private int current = 0;

    /**
     * tracks which line (in source) current is on
     * for producing token with line information
     */
    private int line = 1;


    public Scanner(String source) {
        this.source = source;
    }



    /**
     * for adding simple tokens which don't even need a text
     */
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    /**
     * for adding normal token which has text and literal
     */
    private void addToken(TokenType type, Object literal) {
        // we save the text of every token here
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }


    private boolean isAtEnd() {
        return current >= source.length();
    }


    private void stepForward() {
        current++;
    }


    /**
     * return the current char, but not stepping forward
     * "peek" means "take a glance at"
     */
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current+1);
    }


    /**
     * return the current char, then move one step forward
     */
    private char eat(){
        char curChar = source.charAt(current);
        stepForward();
        return curChar;
    }

    /**
     * return if the current char is the same as expected
     * if it is, eat the current char and step forward
     * else do nothing
     */
    private boolean currentCharMatch(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        stepForward();
        return true;
    }

    private void scanStringLiteral() {
        while (!isAtEnd() && peek() != '"') {
            // support multi-line string
            if (peek() == '\n') line++;
            eat();
        }
        // if we already reach the end and still didn't find the end quote, report error
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string");
            return;
        }

        /*
            if the above condition not invoked,
            that means we've found the end quote,
            don't forget to eat it
         */
        eat();

        // trim the surrounding quotes
        String literal = source.substring(start+1, current-1);

        addToken(TokenType.STRING, literal);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isAlphaOrUnderline(char c) {
        return isAlpha(c) || c == '_';
    }

    private boolean isAlnum(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAlnumOrUnderline(char c) {
        return isAlnum(c) || c == '_';
    }

    /**
     * eats a number like 3.1415
     */
    private void scanNumber() {
        // eats the digit before the (possible) dot
        while (isDigit(peek())) {
            eat();
        }
        /*
            if the char after '.' is not digit,
            we should leave the '.' for nextToken()
            instead of eating it
         */
        if (peek() == '.' && isDigit(peekNext())) {
            eat(); // eat the '.'
            while (isDigit(peek())) eat();
        }
        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void scanIdentifier() {
        while (isAlnumOrUnderline(peek())) {
            eat();
        }
        String identifierStr = source.substring(start, current);
        /**
         * We first suppose the identifier is a reserved keyword,
         * if it is not a reserved keyword,
         * then it is just a identifier
         */
        TokenType type = keywords.get(identifierStr);
        if (type == null) type = TokenType.IDENTIFIER;

        // why don't add the literal of the identifer here ?
        addToken(type);
    }

    /**
     * try its best to recognize **one** token in the coming character-stream
     * or else report error
     */
    private void scanToken() {
        char c = eat();
        switch (c) {

            // ignore whitespaces
            case ' ':
            case '\r':
            case '\t':
                break;

            // line-break is not a token, but it helps us maintain line number
            case '\n':
                line++;
                break;

            // for single-character tokens, we just need to recognize them
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;

            case '/':
                if (currentCharMatch('/')) {
                    // treat as line comment (just eat all characters met utill the line ends)

                    /*
                        use peek() instead of match() here
                        to make sure '\n' will be eaten in the next scanToken() method
                     */
                    while (!isAtEnd() && peek() != '\n') {
                        eat();
                    }
                }
                else {
                    // treat as arithmetic divider
                    addToken(TokenType.SLASH);
                }
                break;

            /*
              for those one-or-two-character tokens,
              we have to look ahead one more step
             */
            case '!':
                addToken(currentCharMatch('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(currentCharMatch('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(currentCharMatch('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(currentCharMatch('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;

            // string literals
            case '"':
                scanStringLiteral();
                break;


            default:
                if (isDigit(c)) {
                    // if meet 0-9, start scanning number
                    scanNumber();
                }
                else if (isAlphaOrUnderline(c)) {
                    scanIdentifier();
                }
                else {
                    // report error if meeting any illegal character like @^#...
                    Lox.error(line, "Unexpected character");
                }
                break;
        }
    }



    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        // add an extra EOF token at last
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

}
