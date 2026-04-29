package com.lab.ratelimiter.domain;

import com.lab.ratelimiter.domain.model.RateLimitKey;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for RateLimitKey value object.
 * No Spring context — pure Java, runs in < 1ms.
 *
 * OOP tested: Encapsulation (invalid keys cannot exist),
 *             Value equality (two keys with same value are equal).
 */
class RateLimitKeyTest {

    @Test
    void should_create_key_with_type_and_identifier() {
        var key = RateLimitKey.of("user", "123");
        assertThat(key.value()).isEqualTo("user:123");
    }

    @Test
    void should_create_key_for_ip_address() {
        var key = RateLimitKey.of("ip", "192.168.1.1");
        assertThat(key.value()).isEqualTo("ip:192.168.1.1");
    }

    @Test
    void should_reject_null_type() {
        assertThatThrownBy(() -> RateLimitKey.of(null, "123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("type");
    }

    @Test
    void should_reject_blank_type() {
        assertThatThrownBy(() -> RateLimitKey.of("  ", "123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("type");
    }

    @Test
    void should_reject_null_identifier() {
        assertThatThrownBy(() -> RateLimitKey.of("user", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identifier");
    }

    @Test
    void should_reject_blank_identifier() {
        assertThatThrownBy(() -> RateLimitKey.of("user", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identifier");
    }

    @Test
    void two_keys_with_same_value_should_be_equal() {
        // Value Object: equality by value, not by reference (record gives this for free)
        var key1 = RateLimitKey.of("user", "123");
        var key2 = RateLimitKey.of("user", "123");
        assertThat(key1).isEqualTo(key2);
        assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
    }

    @Test
    void two_keys_with_different_values_should_not_be_equal() {
        var userKey = RateLimitKey.of("user", "123");
        var ipKey   = RateLimitKey.of("ip", "123");
        assertThat(userKey).isNotEqualTo(ipKey);
    }
}
