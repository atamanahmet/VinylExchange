package com.atamanahmet.vinylexchange.security.encryption;

public interface EncryptionService {

    String encrypt(String plaintext);

    String decrypt(String ciphertext);
}
