package com.sumanth.url_shortener.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Base62EncoderTest {

    private final Base62Encoder base62Encoder = new Base62Encoder();

    @Test
    void testEncode() {
        assertEquals("0", base62Encoder.encode(0), "Encoding 0 should return '0'");
        assertEquals("1", base62Encoder.encode(1), "Encoding 1 should return '1'");
        assertEquals("Z", base62Encoder.encode(61), "Encoding 61 should return 'Z'");
        assertEquals("10", base62Encoder.encode(62), "Encoding 62 should return '10'");
        assertEquals("21", base62Encoder.encode(125), "Encoding 125 should return '21'");
    }
}
