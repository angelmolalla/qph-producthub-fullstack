package com.example.demo.security;

import org.apache.commons.codec.binary.Base32;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;

class TotpServiceTest {

    private TotpService service;

    @BeforeEach
    void setUp() {
        service = new TotpService();
        ReflectionTestUtils.setField(service, "issuer", "QPH ProductHub");
    }

    @Test
    void shouldGenerateBase32Secret() {
        String secret = service.generateSecret();

        assertThat(secret).isNotBlank();
        assertThat(secret).matches("[A-Z2-7]+$");
        assertThat(secret).doesNotContain("=");
    }

    @Test
    void shouldRejectNullOrMalformedCodes() {
        assertThat(service.verifyCode(null, "123456")).isFalse();
        assertThat(service.verifyCode("ABC", null)).isFalse();
        assertThat(service.verifyCode("ABC", "12345")).isFalse();
        assertThat(service.verifyCode("ABC", "ABCDEF")).isFalse();
    }

    @Test
    void shouldBuildOtpAuthUri() {
        String uri = service.buildOtpAuthUri("angelo@example.com", "SECRET123");

        assertThat(uri)
                .startsWith("otpauth://totp/")
                .contains("QPH+ProductHub")
                .contains("angelo%40example.com")
                .contains("secret=SECRET123")
                .contains("algorithm=SHA1")
                .contains("digits=6")
                .contains("period=30");
    }

    @Test
    void shouldVerifyValidCurrentTotp() throws Exception {
        String secret = service.generateSecret();
        long counter = System.currentTimeMillis() / 1000 / 30;
        String code = generateCode(secret, counter);

        assertThat(service.verifyCode(secret, code)).isTrue();
    }

    private String generateCode(String secret, long counter) throws Exception {
        Base32 base32 = new Base32();
        byte[] key = base32.decode(secret);
        byte[] data = ByteBuffer.allocate(8).putLong(counter).array();

        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key, "HmacSHA1"));
        byte[] hash = mac.doFinal(data);

        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);

        return String.format("%06d", binary % 1_000_000);
    }
}
