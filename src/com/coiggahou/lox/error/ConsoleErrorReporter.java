package com.coiggahou.lox.error;


public class ConsoleErrorReporter implements ErrorReporter {
    @Override
    public void report(int lineNumber, String where, String message) {
        System.err.format("[line %d] Error %s: %s", lineNumber, where, message);
    }

}
