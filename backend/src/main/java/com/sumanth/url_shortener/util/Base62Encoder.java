package com.sumanth.url_shortener.util;

import org.springframework.stereotype.Component;

@Component
public class Base62Encoder {
    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = ALPHABET.length();

    /**
     * Encodes a long value into a Base62 string.
     * 
     * @param value The number to encode (must be non-negative).
     * @return The Base62 encoded string.
     */
    public static String encode(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Value must be non-negative");
        }
        if (value == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }

        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(ALPHABET.charAt((int) (value % BASE)));
            value /= BASE;
        }

        return sb.reverse().toString();
    }

    /**
     * Decodes a Base62 string back into a long value.
     * 
     * @param str The Base62 string to decode.
     * @return The decoded long value.
     */
    public long decode(String str) {
        long result = 0;
        for (int i = 0; i < str.length(); i++) {
            int index = ALPHABET.indexOf(str.charAt(i));
            if (index == -1) {
                throw new IllegalArgumentException("Invalid character in Base62 string: " + str.charAt(i));
            }
            result = result * BASE + index;
        }
        return result;
    }

}
