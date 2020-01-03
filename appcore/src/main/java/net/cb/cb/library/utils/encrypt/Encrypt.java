package net.cb.cb.library.utils.encrypt;


import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Author : Jimmy.Shine Date : 2014-05-31
 * <p>
 * Encrypt
 * </p>
 */
public class Encrypt {

    /**
     * md5 加密
     *
     * @param plaintext 明文
     * @param salt      盐值
     * @return 密文
     * @throws CryptoException CryptoException
     */
    public static String md5(String plaintext, String salt) throws CryptoException {
        if (plaintext == null) {
            return null;
        }
        plaintext += (null == salt ? "" : salt);
        return md5(plaintext);
    }

    /**
     * MD5 加密
     *
     * @param plaintext 明文
     * @return 密文
     * @throws CryptoException CryptoException
     */
    public static String md5(String plaintext) throws CryptoException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            StringBuilder ciphertext = new StringBuilder();
            for (byte b : digest) {
                ciphertext.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
            }
            return ciphertext.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException();
        }
    }

    /**
     * MD5 加密
     *
     * @param plainBytes 明文
     * @return 密文
     * @throws CryptoException CryptoException
     */
    public static String md5(byte[] plainBytes) throws CryptoException {
        if (plainBytes == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainBytes);
            byte[] digest = md.digest();
            StringBuilder ciphertext = new StringBuilder();
            for (byte b : digest) {
                ciphertext.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
            }
            return ciphertext.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException();
        }
    }

    /**
     * Base64编码
     *
     * @param sourceText source text
     * @return encoded text
     */
    public static String base64(String sourceText) {
        if (sourceText == null) {
            return null;
        } else {
            return new String(base64(sourceText.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        }
    }

    /**
     * Base64编码
     *
     * @param sourceBytes source bytes
     * @return encoded bytes
     */
    public static byte[] base64(byte[] sourceBytes) {
        if (sourceBytes == null) {
            return null;
        }
        return Base64.encode(sourceBytes, Base64.NO_WRAP );
    }

}
