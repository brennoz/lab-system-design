package com.lab.urlshortener.domain;

import com.lab.urlshortener.domain.service.Base62Encoder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Base62EncoderTest {

    private final Base62Encoder encoder = new Base62Encoder();

    @Test
    void encode_then_decode_roundtrip() {
        // Algorithm: Base62 — O(log₆₂ n); encode(decode(x)) must equal x for any valid id
        long id = 99999L;
        assertThat(encoder.decode(encoder.encode(id))).isEqualTo(id);
    }

    @Test
    void encode_id_1_returns_single_char() {
        // ALPHABET[1] = '1'; base-case ensures no off-by-one in loop termination
        assertThat(encoder.encode(1L)).isEqualTo("1");
    }

    @Test
    void encode_62_returns_base62_ten() {
        // 62 in base-62 = "10" (same logic as decimal: 10 in base-10 = "10")
        assertThat(encoder.encode(62L)).isEqualTo("10");
    }
}
