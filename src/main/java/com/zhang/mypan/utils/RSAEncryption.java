package com.zhang.mypan.utils;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import java.security.*;

/**
 * 非对称加密算法
 * 1. 单例 添加到spring容器中
 * 2. 添加到 threadLocal
 */
@Slf4j
public class RSAEncryption {
    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    private final Cipher cipher;
    public static final int KEY_SIZE = 2048;// 指定密钥长度
    public static final String ALGORITHM = "RSA";// 指定加密算法

    public RSAEncryption() {
        // 生成密钥对
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // 获取公钥和私钥
            this.publicKey = keyPair.getPublic();
            this.privateKey = keyPair.getPrivate();
            //
            this.cipher = Cipher.getInstance(ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 加密
    public String encrypt(String message) {
        byte[] cipherText = new byte[0];
        try {

            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            cipherText = cipher.doFinal(message.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Base64.encode(cipherText);
    }

    // 解密
    public String decrypt(String cipherText) {
        // 解密
        byte[] decryptedText = new byte[0];
        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            decryptedText = cipher.doFinal(Base64.decode(cipherText));
        } catch (Exception e) {
            log.error("解密失败", e);
            return null;
        }
        return new String(decryptedText);
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public static void main(String[] args) {
        RSAEncryption RSAEncryption = new RSAEncryption();
        final String hello = RSAEncryption.encrypt("hello");

        System.out.println("解密后" + RSAEncryption.decrypt(hello));

    }
}
