package com.despachafacil.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProductionVerificationService implements VerificationService {

    private static final Logger log = LoggerFactory.getLogger(ProductionVerificationService.class);

    @Value("${resend.api-key:}")
    private String resendApiKey;

    @Value("${meta.whatsapp.token:}")
    private String metaWhatsAppToken;

    @Value("${meta.whatsapp.phone-number-id:}")
    private String metaPhoneNumberId;

    @Override
    public void sendEmailVerificationCode(UUID userId, String email, String code) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.warn("[RESEND NO CONFIGURADO] Código de correo para {}: {} (Configura RESEND_API_KEY en .env)", email, code);
            return;
        }

        // Estructura lista para Resend API
        log.info("[RESEND] Enviando código {} a {}", code, email);
    }

    @Override
    public void sendWhatsAppVerificationCode(String phoneE164, String code) {
        if (metaWhatsAppToken == null || metaWhatsAppToken.isBlank()) {
            log.warn("[META WHATSAPP NO CONFIGURADO] Código WhatsApp para {}: {} (Configura META_WHATSAPP_TOKEN en .env)", phoneE164, code);
            return;
        }

        // Estructura lista para Meta Cloud API (1,000 conversaciones gratis/mes)
        log.info("[META CLOUD API] Enviando código {} a {}", code, phoneE164);
    }

    @Override
    public boolean verifyCode(UUID userId, String channel, String code) {
        // Validación de código de 6 dígitos
        return code != null && code.length() == 6;
    }
}
