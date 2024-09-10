package com.zhang.mypan.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;


/**
 * 对称加密AES
 */
@Slf4j
public class AESEncryption {
    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "MySecretKey12345";

    // 加密
    public static String encrypt(String plainText) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.encodeBase64String(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 解密
    public static String decrypt(String encryptedText) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.decodeBase64(encryptedText));
            return new String(decryptedBytes);
        } catch (Exception e) {
            log.error("解密失败", e);
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            String encryptedText = encrypt("Hello, World!");
            System.out.println("Encrypted message: " + encryptedText);

            String decryptedText = decrypt(encryptedText);
            System.out.println("Decrypted message: " + decryptedText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}