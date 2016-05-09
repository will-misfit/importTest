package com.misfit.ble.sample.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESEncrypt {
    private static final String CipherMode = "AES/ECB/NoPadding";

    private static SecretKeySpec createKey(String password) {
        byte[] passcodeBytes = password.trim().getBytes();
        return new SecretKeySpec(passcodeBytes, "AES");
    }


    public static byte[] encrypt(byte[] content, String password) {
        try {
            SecretKeySpec key = createKey(password);
            Cipher cipher = Cipher.getInstance(CipherMode);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(byte[] content, String password) {
        try {
            SecretKeySpec key = createKey(password);
            Cipher cipher = Cipher.getInstance(CipherMode);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
