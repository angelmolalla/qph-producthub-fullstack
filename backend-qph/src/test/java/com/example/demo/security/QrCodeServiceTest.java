package com.example.demo.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QrCodeServiceTest {

    private final QrCodeService service = new QrCodeService();

    @Test
    void shouldGeneratePngQrCode() {
        byte[] qr = service.generateQrCode("otpauth://totp/QPH:angelo?secret=ABC123");

        assertThat(qr).isNotEmpty();
        assertThat(qr.length).isGreaterThan(100);
        assertThat(qr[0]).isEqualTo((byte) 0x89);
        assertThat(qr[1]).isEqualTo((byte) 0x50);
        assertThat(qr[2]).isEqualTo((byte) 0x4E);
        assertThat(qr[3]).isEqualTo((byte) 0x47);
    }
}
