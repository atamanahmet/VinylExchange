package com.atamanahmet.vinylexchange.security.encryption;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AesGcmEncryptionService implements EncryptionService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int KEY_LENGTH_BYTES = 32;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesGcmEncryptionService(@Value("${aes.encryption.key}") String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != KEY_LENGTH_BYTES) {
            throw new IllegalArgumentException("AES encryption key must be 256 bits (32 bytes)");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * AES-GCM encrypt: random IV prepended to ciphertext+tag, whole payload base64-encoded.
     */
    @Override
    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[IV_LENGTH_BYTES + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH_BYTES);
            System.arraycopy(encrypted, 0, combined, IV_LENGTH_BYTES, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    /**
     * Base64-decode, split IV from ciphertext+tag, AES-GCM decrypt to UTF-8 string.
     */
    @Override
    public String decrypt(String ciphertext) {
        try {
            byte[] decoded = Base64.getDecoder().decode(ciphertext);
            if (decoded.length <= IV_LENGTH_BYTES) {
                throw new IllegalArgumentException("Invalid ciphertext");
            }

            byte[] iv = Arrays.copyOfRange(decoded, 0, IV_LENGTH_BYTES);
            byte[] encrypted = Arrays.copyOfRange(decoded, IV_LENGTH_BYTES, decoded.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

            byte[] plaintext = cipher.doFinal(encrypted);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }
}
