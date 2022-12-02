package com.coiggahou.lox.error;

import com.coiggahou.lox.Token;

public class RuntimeError extends RuntimeException{

    private final Token token;

    public Token getToken() {
        return token;
    }

    public RuntimeError(String message) {
        super(message);
        this.token = null;
    }

    public RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
