package com.despachafacil.api.service;

import java.util.UUID;

public interface VerificationService {
    void sendEmailVerificationCode(UUID userId, String email, String code);
    void sendWhatsAppVerificationCode(String phoneE164, String code);
    boolean verifyCode(UUID userId, String channel, String code);
}
