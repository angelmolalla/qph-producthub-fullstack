package com.example.demo.security;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class QrCodeService {

    public byte[] generateQrCode(
            String content) {

        try {

            QRCodeWriter writer =
                    new QRCodeWriter();

            BitMatrix matrix =
                    writer.encode(
                            content,
                            BarcodeFormat.QR_CODE,
                            300,
                            300
                    );

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(
                    matrix,
                    "PNG",
                    output
            );

            return output.toByteArray();

        } catch (Exception e) {

            throw new IllegalStateException(
                    "No se pudo generar QR",
                    e
            );
        }
    }
}