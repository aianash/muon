package com.shoplane.muon.common.utils;

/**
 * Created by ravmon on 24/8/15.
 */

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class StringEncryptionUtil {
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String SECURERANDOM_ALGORITHM = "SHA1PRNG";

    public static String encryptString(String key, String strToEncrypt) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
        SecureRandom secureRandom = SecureRandom.getInstance(SECURERANDOM_ALGORITHM);

        secureRandom.setSeed(key.getBytes());
        keyGenerator.init(128, secureRandom);
        SecretKey secretKey = keyGenerator.generateKey();
        byte[]  rawSecretKey= secretKey.getEncoded();

        SecretKeySpec secretKeySpec = new SecretKeySpec(rawSecretKey, ENCRYPTION_ALGORITHM);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encryptedString = cipher.doFinal(strToEncrypt.getBytes());

        return new String(encryptedString);
    }

    public static String decryptString(String key, String strToDecrypt) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
        SecureRandom secureRandom = SecureRandom.getInstance(SECURERANDOM_ALGORITHM);

        secureRandom.setSeed(key.getBytes());
        keyGenerator.init(128, secureRandom);
        SecretKey secretKey = keyGenerator.generateKey();
        byte[]  rawSecretKey= secretKey.getEncoded();

        SecretKeySpec secretKeySpec = new SecretKeySpec(rawSecretKey, ENCRYPTION_ALGORITHM);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] decryptedString = cipher.doFinal(strToDecrypt.getBytes());

        return new String(decryptedString);
    }
}
