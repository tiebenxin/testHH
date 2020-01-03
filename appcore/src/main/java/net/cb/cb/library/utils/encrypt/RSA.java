package net.cb.cb.library.utils.encrypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * Author : Jimmy Shine
 * Date : 2017-07-19 14:30
 * <p> RSA </p>
 */
public class RSA implements Serializable {

	public static String SIGNATURE_ALGORITHM = "MD5withRSA";
//	private static final String SIGNATURE_ALGORITHM = "SHA1WithRSA";
	private static final long serialVersionUID = 5630614499297667681L;

	/**
	 * 生成公钥密钥对
	 *
	 * @return
	 * @throws CryptoException
	 */
	public static KeyPair genKeyPair() throws CryptoException {
		try {
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
			keyPairGen.initialize(1024, new SecureRandom());
			return keyPairGen.generateKeyPair();
		} catch (Exception e) {
			throw new CryptoException(e);
		}
	}

	/**
	 * 获取公钥字符串，Base64编码
	 *
	 * @param keyPair
	 * @return
	 */
	public static String publicKeyString(KeyPair keyPair) {
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		return new String(Encrypt.base64(publicKey.getEncoded()));
	}

	/**
	 * 获取私钥字符串，Base64编码
	 *
	 * @param keyPair
	 * @return
	 */
	public static String privateKeyString(KeyPair keyPair) {
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPrivate();
		return new String(Encrypt.base64(publicKey.getEncoded()));
	}

	/**
	 * 获取公钥
	 *
	 * @param keyPair
	 * @return
	 */
	public static PublicKey publicKey(KeyPair keyPair) {
		return keyPair.getPublic();
	}

	/**
	 * 获取私钥
	 *
	 * @param keyPair
	 * @return
	 */
	public static PrivateKey privateKey(KeyPair keyPair) {
		return keyPair.getPrivate();
	}


	/**
	 * Get RSAPublicKey by String
	 *
	 * @param publicKeyStr
	 * @return
	 * @throws CryptoException
	 */

	public static RSAPublicKey publicKey(String publicKeyStr) throws CryptoException {
		try {
			byte[] buffer = Decrypt.base64(publicKeyStr.getBytes());
			KeyFactory keyFactory = KeyFactory.getInstance("RSA","BC");
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
			return (RSAPublicKey) keyFactory.generatePublic(keySpec);
		} catch (Exception e) {
			throw new CryptoException(e);
		}
	}

	/**
	 * Get RSAPrivateKey by String
	 *
	 * @param privateKeyStr
	 * @return
	 * @throws CryptoException
	 */
	public static RSAPrivateKey privateKey(String privateKeyStr) throws CryptoException {
		try {
			byte[] buffer = Decrypt.base64(privateKeyStr.getBytes());
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
		} catch (Exception e) {
			throw new CryptoException(e);
		}
	}

	/**
	 * 密钥加密
	 *
	 * @param key
	 * @param plainTextData
	 * @return
	 * @throws CryptoException
	 */
	public static byte[] encrypt(Key key, byte[] plainTextData) throws CryptoException {
		if (key == null) {
			throw new CryptoException("Public Key cannot be null");
		}
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			InputStream ins = new ByteArrayInputStream(plainTextData);
			ByteArrayOutputStream writer = new ByteArrayOutputStream();
			//rsa解密的字节大小最多是128，将需要解密的内容，按128位拆开解密
			byte[] buf = new byte[117];
			int bufl;

			while ((bufl = ins.read(buf)) != -1) {
				byte[] block = null;

				if (buf.length == bufl) {
					block = buf;
				} else {
					block = new byte[bufl];
					for (int i = 0; i < bufl; i++) {
						block[i] = buf[i];
					}
				}

				writer.write(cipher.doFinal(block));
			}

			return writer.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			return new byte[1];
		}
	}

	/**
	 * 密钥加密
	 *
	 * @param key
	 * @param plainTextData
	 * @return
	 * @throws CryptoException
	 */
	public static byte[] encrypt(Key key, String plainTextData) throws CryptoException {
		return Encrypt.base64(encrypt(key, plainTextData.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * 使用密钥解密
	 *
	 * @param key
	 * @param cipherData
	 * @return
	 * @throws CryptoException
	 */
	public static byte[] decrypt(Key key, byte[] cipherData) throws CryptoException {
		if (key == null) {
			throw new CryptoException("Key cannot be null");
		}
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, key);
	        InputStream ins = new ByteArrayInputStream(cipherData);
	        ByteArrayOutputStream writer = new ByteArrayOutputStream();
	        //rsa解密的字节大小最多是128，将需要解密的内容，按128位拆开解密
	        byte[] buf = new byte[128];
	        int bufl;

	        while ((bufl = ins.read(buf)) != -1) {
	            byte[] block = null;

	            if (buf.length == bufl) {
	                block = buf;
	            } else {
	                block = new byte[bufl];
	                for (int i = 0; i < bufl; i++) {
	                    block[i] = buf[i];
	                }
	            }

	            writer.write(cipher.doFinal(block));
	        }

	        return writer.toByteArray();
		} catch (Exception e) {
			throw new CryptoException(e);
		}
	}

	/**
	 * 密钥解密
	 *
	 * @param key
	 * @param plainTextData
	 * @return
	 * @throws CryptoException
	 */
	public static byte[] decrypt(Key key, String plainTextData) throws CryptoException {
		return Encrypt.base64(decrypt(key, plainTextData.getBytes(StandardCharsets.UTF_8)));
	}


	/**
	 * 数字签名
	 *
	 * @param data
	 * @param privateKey
	 * @return
	 * @throws CryptoException
	 */
	public static byte[] sign(byte[] data, PrivateKey privateKey) throws CryptoException {
		try {
			Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
			signature.initSign(privateKey);
			signature.update(data);
			return signature.sign();
		} catch (Exception e) {
			throw new CryptoException(e);
		}
	}


	/**
	 * 校验签名
	 *
	 * @param data
	 * @param publicKey
	 * @param sign
	 * @return
	 * @throws CryptoException
	 */
	public static boolean valid(byte[] data, PublicKey publicKey, byte[] sign) throws CryptoException {
		try {
			Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
			signature.initVerify(publicKey);
			signature.update(data);
			return signature.verify(sign);
		} catch (Exception e) {
			throw new CryptoException(e);
		}
	}
}
