package com.coiggahou.lox.util;

public class CharUtil {

    public static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    public static boolean isAlphaOrUnderline(char c) {
        return isAlpha(c) || c == '_';
    }

    /**
     * @return true if c is an alpha or a digit number
     */
    public static boolean isAlnum(char c) {
        return isAlpha(c) || isDigit(c);
    }

    public static boolean isAlnumOrUnderline(char c) {
        return isAlnum(c) || c == '_';
    }

}
