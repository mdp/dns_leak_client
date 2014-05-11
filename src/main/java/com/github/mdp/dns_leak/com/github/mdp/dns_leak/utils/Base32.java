package com.github.mdp.dns_leak.com.github.mdp.dns_leak.utils;

/**
 * Created by mdp on 5/6/14.
 */
public class Base32 {
    public static final String BASE_32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    // Convert to arbitrary Base
    public static String intToBase32(int num, String symbols) {
        if (num == 0) { return String.valueOf(symbols.charAt(0)); }
        final int B = symbols.length();
        StringBuilder sb = new StringBuilder();
        while (num != 0) {
            sb.append(symbols.charAt((int) (num % B)));
            num /= B;
        }
        return sb.reverse().toString();
    }
}

