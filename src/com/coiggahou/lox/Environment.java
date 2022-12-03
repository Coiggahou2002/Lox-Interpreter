package com.coiggahou.lox;

import com.coiggahou.lox.error.RuntimeError;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    private final Environment enclosing;

    private final Map<String, Object> values = new HashMap<>();

    public Environment() {
        this.enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    /**
     * NOTE: redefining an existing variable is ALLOWED
     *
     * that's to say, when we write:
     *    var a = 5;
     *    var a = 5555;
     * then the value of a will be over-written
     */
    void define(String name, Object value) {
        values.put(name, value);
    }

    /**
     * get the value by a variable name
     * @throws RuntimeError if all scopes on the scope chain have no definition of the variable
     */
    Object get(Token name) {
        // first try to find definition in the local scope
        // if no def in local scope, go up by the scope chain
        if (!values.containsKey(name.lexeme)) {
            if (enclosing != null) {
                return enclosing.get(name);
            }
            throw new RuntimeError(name, String.format("Undefined variable %s.", name.lexeme));
        }
        return values.get(name.lexeme);
    }

    /**
     * assign the specified variable
     * @return the assigned value
     * @throws RuntimeError if all scopes on the scope chain have no definition of the variable
     */
    Object assign(Token name, Object value) {
        if (!values.containsKey(name.lexeme)) {
            if (enclosing != null) {
                return enclosing.assign(name, value);
            }
            throw new RuntimeError(name, String.format("Cannot assigned an undefined variable %s.", name.lexeme));
        }
        values.put(name.lexeme, value);
        return value;
    }
}
