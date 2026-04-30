package com.lab.urlshortener.domain;

import com.lab.urlshortener.domain.model.ShortCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShortCodeTest {

    @Test
    void creates_valid_short_code() {
        ShortCode code = new ShortCode("aB3xK9");
        assertThat(code.value()).isEqualTo("aB3xK9");
    }

    @Test
    void rejects_blank_value() {
        assertThatThrownBy(() -> new ShortCode(""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
