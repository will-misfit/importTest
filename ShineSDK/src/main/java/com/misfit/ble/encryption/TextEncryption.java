package com.misfit.ble.encryption;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.util.Base64;

import com.misfit.ble.shine.storage.Preferences;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @issue wrong decryption on KitKat.
 * @description For AES CBC, you should use a 16 byte random IV that is
 *              different for each message. When you don't specify an IV in the
 *              init function, the behavior seems to vary widely. I've tried 3
 *              different devices and gotten different behaviors:
 * 
 *              - In KitKat, the app generated a random looking IV during
 *              encrypt, which was different (at least for each new instance of
 *              Cipher) and used all zeros during decrypt; between runs, the IV
 *              varies, so it will never decrypt the same way twice. This is
 *              what makes the app break.
 * 
 *              - On 4.3, the app used an IV of all zeros for both encrypt and
 *              decrypt so it always encrypts and decrypts the same
 * 
 *              - On a rooted cyanogenmod 4.2.2 device, it used an random
 *              looking IV that was the same between runs and between encrypt
 *              and decrypt. I don't know where it's getting / storing this iv.
 * 
 * @reference https://github.com/SURFnet/tiqr/issues/42
 */

public class TextEncryption {
	
	private static String ENCRYPTION_ALGORITHM = "AES";
	private static String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5PADDING";
	private static final int IV_LENGTH = 16;
	
	private static String SHARED_PREFERENCES_KEY = "com.misfitwearables.ble.encryption.TextEncryption";
	private static String SECRET_KEY = "text_encryption_secret_key";
	private static String LEGACY_SECRET_KEY = "text_encryption_key";
	
	private static String CHARSET = "UTF-8";
	
	@SuppressLint("TrulyRandom")
	private static SecretKey generateKey() throws NoSuchAlgorithmException {
	    // Generate a 256-bit key
	    final int outputKeyLength = 256;

	    // Ignore SecureRandom warnings on Android 4.3 and older.
	    SecureRandom secureRandom = new SecureRandom();
	    // Do *not* seed secureRandom! Automatically seeded from system entropy.
	    KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
	    keyGenerator.init(outputKeyLength, secureRandom);
	    SecretKey key = keyGenerator.generateKey();
	    return key;
	}

	private static byte[] getSecretKey() throws NoSuchAlgorithmException {
		SharedPreferences sharedPreferences = Preferences.getSharedPreferences(SHARED_PREFERENCES_KEY);
		String base64 = sharedPreferences.getString(SECRET_KEY, null);
		
		// migrate from the deprecated lookup key in release 1.6.3 for partners. 
		if (base64 == null) {
			base64 = sharedPreferences.getString(LEGACY_SECRET_KEY, null);
			sharedPreferences.edit().putString(SECRET_KEY, base64).apply();
		}
		
		if (base64 == null) {
			base64 = Base64.encodeToString(generateKey().getEncoded(), Base64.DEFAULT);
			sharedPreferences.edit().putString(SECRET_KEY, base64).apply();
		}
		return Base64.decode(base64, Base64.DEFAULT);
	}
	
	private static byte[] getIv() throws NoSuchAlgorithmException, NoSuchProviderException {
		// Use all zeros to be compatible with Android 4.3. Plus, simple encryption is good enough here.
		return new byte[IV_LENGTH];
	}
	
	private static byte[] encrypt(byte[] rawKey, byte[] clear) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException {
		SecretKeySpec keySpec = new SecretKeySpec(rawKey, ENCRYPTION_ALGORITHM);
		Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(getIv()));
		return cipher.doFinal(clear);
	}
	
	private static byte[] decrypt(byte[] rawKey, byte[] encrypted) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException {
		SecretKeySpec keySpec = new SecretKeySpec(rawKey, ENCRYPTION_ALGORITHM);
		Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
		cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(getIv()));
		return cipher.doFinal(encrypted);
	}
	
	public static String encrypt(String text) {
		String encryptedText = null;
		try {
			byte[] secretKey = getSecretKey();
			byte[] encrypted = encrypt(secretKey, text.getBytes(CHARSET));
			encryptedText = Base64.encodeToString(encrypted, Base64.DEFAULT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return encryptedText;
	}
	
	public static String decrypt(String encryptedText) {
		String decryptedText = null;
		try {
			byte[] secretKey = getSecretKey();
			byte[] decrypted = decrypt(secretKey, Base64.decode(encryptedText, Base64.DEFAULT));
			decryptedText = new String(decrypted, CHARSET);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decryptedText;
	}
}
