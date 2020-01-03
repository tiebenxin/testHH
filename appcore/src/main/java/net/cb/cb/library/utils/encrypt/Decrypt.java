package net.cb.cb.library.utils.encrypt;

import android.util.Base64;

import java.nio.charset.StandardCharsets;

/**
 * Author : Jimmy.Shine Date : 2014-05-31
 * <p>
 * Decrypt
 * </p>
 */
public class Decrypt {

    /**
     * Base64 解码
     *
     * @param encodeText encode Text
     * @return decode Text
     */
    public static String base64(String encodeText) {
        if (encodeText == null) {
            return null;
        }
        return new String(base64(encodeText.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    /**
     * Base64解码
     *
     * @param encodeBytes encode bytes
     * @return decode bytes
     */
    public static byte[] base64(byte[] encodeBytes) {
        if (encodeBytes == null) {
            return null;
        } else {
            return Base64.decode(encodeBytes, Base64.NO_WRAP);
        }
    }
}
