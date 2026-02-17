package com.sumanth.url_shortener.util;

import com.sumanth.url_shortener.util.SecureCodeGenerator;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class SecureCodeGeneratorTest {

    private final SecureCodeGenerator generator = new SecureCodeGenerator();

    @Test
    public void generate_shouldReturnNonEmptyString() {
        String code = generator.generate(1L);
        assertNotNull(code);
        assertFalse(code.isEmpty());
    }

    @Test
    public void generate_shouldReturnStringLengthAtMost7() {
        String code = generator.generate(123456789L);
        assertTrue(code.length() <= 7, "Length should be at most 7, got: " + code.length());
    }

    @Test
    public void generate_shouldBeUniqueForSequentialInputs() {
        Set<String> codes = new HashSet<>();
        int count = 1000;
        for (long i = 1; i <= count; i++) {
            String code = generator.generate(i);
            assertTrue(codes.add(code), "Duplicate code generated for input: " + i + ", code: " + code);
        }
    }

    @Test
    public void generate_shouldNotBeSequential() {
        String code1 = generator.generate(1L);
        String code2 = generator.generate(2L);

        // Simple check that they are not just incremented
        // In Base62, '1' -> '1', '2' -> '2' etc.
        // Here, hash based, so likely completely different.
        assertNotEquals(code1, code2);

        // Check first char difference to ensure "randomness" look
        assertNotEquals(code1.charAt(0), code2.charAt(0));
    }
}
