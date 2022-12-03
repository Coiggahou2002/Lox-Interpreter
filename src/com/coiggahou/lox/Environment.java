package com.coiggahou.lox;

import com.coiggahou.lox.error.RuntimeError;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();

    private boolean isDefined(String name) {
        return values.containsKey(name);
    }

    /**
     * redefining an existing variable is ALLOWED
     * that's to say, when we write:
     *    var a = 5;
     *    var a = 5555;
     * then the value of a will be over-written
     */
    void define(String name, Object value) {
        values.put(name, value);
    }

    Object get(Token name) {
        if (isDefined(name.lexeme)) {
            return values.get(name.lexeme);
        }
        throw new RuntimeError(name, String.format("Undefined variable %s.", name.lexeme));
    }

    Object assign(Token name, Object value) {
        if (isDefined(name.lexeme)) {
            values.put(name.lexeme, value);
            return value;
        }
        throw new RuntimeError(name, String.format("Cannot assigned a undefined variable %s.", name.lexeme));
    }
}
