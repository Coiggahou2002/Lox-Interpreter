package com.coiggahou.lox.error;

public interface ErrorReporter {
    void report(int lineNumber, String where, String message);
}
