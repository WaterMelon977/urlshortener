package com.sumanth.url_shortener.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SecureCodeGenerator {

    private final String secretKey;
    private final String alphabet;
    private final BigInteger base;

    public SecureCodeGenerator(
            @Value("${app.secure-code.secret-key}") String secretKey,
            @Value("${app.secure-code.alphabet}") String alphabet) {
        this.secretKey = secretKey;
        this.alphabet = alphabet;
        this.base = BigInteger.valueOf(alphabet.length());
    }

    public String generate(long seq) {
        try {
            // 1. Get HMAC-SHA256 of the sequence
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] hashBytes = sha256_HMAC.doFinal(String.valueOf(seq).getBytes(StandardCharsets.UTF_8));

            // 2. Convert to BigInteger (positive)
            BigInteger bigInt = new BigInteger(1, hashBytes);

            // 3. Base62 Encode
            StringBuilder sb = new StringBuilder();
            while (bigInt.compareTo(BigInteger.ZERO) > 0) {
                BigInteger[] divRem = bigInt.divideAndRemainder(base);
                sb.append(alphabet.charAt(divRem[1].intValue()));
                bigInt = divRem[0];
            }

            // If the hash results in 0 (extremely unlikely), handle it
            // However, loop condition (bigInt > 0) handles initialization
            if (sb.length() == 0) {
                return String.valueOf(alphabet.charAt(0));
            }

            // Reverse to get correct Base62 representation (standard practice, though for
            // hash it matters less)
            String base62Hash = sb.reverse().toString();

            // 4. Take first 7 characters
            if (base62Hash.length() > 7) {
                return base62Hash.substring(0, 7);
            } else {
                // If by some extreme chance the hash encoded is shorter than 7 chars, return it
                // as is or pad?
                // SHA-256 is 256 bits, 256 / log2(62) ≈ 43 characters. It will always be > 7.
                return base62Hash;
            }

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error generating secure code", e);
        }
    }
}
