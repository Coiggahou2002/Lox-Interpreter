package com.coiggahou.lox;

import com.coiggahou.lox.error.ConsoleErrorReporter;
import com.coiggahou.lox.error.ErrorReporter;
import com.coiggahou.lox.error.RuntimeError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Lox {

    /**
     * use this to ensure we don't try to execute code
     * that has a known error
     */
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    /**
     * We need to separate the code that generates the errors
     * from the code that reports them
     */
    private static final ErrorReporter errorReporter = new ConsoleErrorReporter();

    private static final Interpreter interpreter = new Interpreter();


    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        }
        else if (args.length == 1) {
            runFile(args[0]);
        }
        else {
            runPrompt();
        }
    }

    /**
     * run a given code file
     */
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    /**
     * open an interactive prompt
     */
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            // if got EOF (send by Ctrl+D), exit the loop and quit the program
            if (line == null) {
                break;
            }
            run(line);

            // reset the error flag
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // for now, we just print the token
//        for (Token token : tokens) {
//            System.out.println(token);
//        }

        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        if (hadError) return;
//        System.out.println(new AstPrinter().print(expression));
        interpreter.interpret(expression);
    }

    static void error(int lineNumber, String message) {
        errorReporter.report(lineNumber, "", message);
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            errorReporter.report(token.line, " at end", message);
        }
        else {
            errorReporter.report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        errorReporter.report(error.getToken().line, "", error.getMessage());
        hadRuntimeError = true;
    }

}
