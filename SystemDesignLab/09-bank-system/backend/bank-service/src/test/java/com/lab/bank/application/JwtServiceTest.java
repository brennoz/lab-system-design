package com.lab.bank.application;

import com.lab.bank.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    // 256-bit base64 secret (required by HS256)
    private static final String SECRET = "dGhpc2lzYXZlcnlsb25nc2VjcmV0a2V5Zm9ydGhlYmFua3N5c3RlbQ==";
    private final JwtService jwtService = new JwtService(SECRET, 86_400_000L);

    @Test
    void generates_valid_token_and_extracts_email() {
        String token = jwtService.generate("alice@example.com");

        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.extractEmail(token)).isEqualTo("alice@example.com");
    }

    @Test
    void rejects_tampered_token() {
        String token = jwtService.generate("alice@example.com");
        String tampered = token.substring(0, token.length() - 6) + "xxxxxx";

        assertThat(jwtService.isValid(tampered)).isFalse();
    }
}
