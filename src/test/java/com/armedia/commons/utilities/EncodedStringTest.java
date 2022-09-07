package com.armedia.commons.utilities;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.codec.CheckedCodec;
import com.armedia.commons.utilities.codec.FunctionalCheckedCodec;

public class EncodedStringTest {

	private static final Charset CHARSET = StandardCharsets.UTF_8;
	private static final Random RANDOM = new Random(System.nanoTime());
	private static final int KEY_SIZE = 16;
	private static final String CIPHER_ALGORITHM = "AES";
	private static final String CIPHER_SPEC = String.format("%s/ECB/PKCS5Padding", EncodedStringTest.CIPHER_ALGORITHM);

	static {
		SecretKey key = EncodedStringTest.getKey(null);
		try {
			EncodedStringTest.getCipher(key, false);
			EncodedStringTest.getCipher(key, true);
		} catch (Exception e) {
			throw new RuntimeException("Failed to initialize the test cipher", e);
		}
	}

	private static SecretKey getKey(byte[] data) {
		if (data == null) {
			data = new byte[EncodedStringTest.KEY_SIZE];
			EncodedStringTest.RANDOM.nextBytes(data);
		} else if (data.length != EncodedStringTest.KEY_SIZE) {
			byte[] newData = new byte[EncodedStringTest.KEY_SIZE];
			System.arraycopy(data, 0, newData, 0, Math.min(EncodedStringTest.KEY_SIZE, data.length));
			data = newData;
		}
		return new SecretKeySpec(data, EncodedStringTest.CIPHER_ALGORITHM);
	}

	private static Cipher getCipher(SecretKey key, boolean encrypt)
		throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		Cipher ret = Cipher.getInstance(EncodedStringTest.CIPHER_SPEC);
		ret.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, key);
		return ret;
	}

	private CheckedCodec<CharSequence, byte[], Exception> newCodec() {
		return newCodec(SecretKey.class.cast(null));
	}

	private CheckedCodec<CharSequence, byte[], Exception> newCodec(byte[] key) {
		return newCodec(EncodedStringTest.getKey(key));
	}

	private CheckedCodec<CharSequence, byte[], Exception> newCodec(SecretKey key) {
		final SecretKey secretKey = (key != null ? key : EncodedStringTest.getKey(null));
		return new FunctionalCheckedCodec.Builder<CharSequence, byte[], Exception>() //
			.setDecoder((bytes) -> {
				return new String( //
					EncodedStringTest.getCipher(secretKey, false).doFinal(bytes), //
					EncodedStringTest.CHARSET //
				);
			}) //
			.setEncoder((charSeq) -> {
				ByteBuffer data = EncodedStringTest.CHARSET.encode(charSeq.toString());
				byte[] bytes = new byte[data.remaining()];
				data.get(bytes);
				return EncodedStringTest.getCipher(secretKey, true) //
					.doFinal(bytes) //
				;
			}) //
			.setNullEncoding(new byte[] {}) //
			.setNullValue(StringUtils.EMPTY) //
			.build();
	}

	@Test
	public void testToString() throws Exception {
		CheckedCodec<CharSequence, byte[], Exception> codec = newCodec();
		for (int i = 0; i < 10; i++) {
			String str = String.format("String-#-%02d-%s", i, UUID.randomUUID().toString());
			EncodedString encodedStr = EncodedString.from(str, codec);
			Assertions.assertNotEquals(str, encodedStr.toString());
			Assertions.assertFalse(StringUtils.containsIgnoreCase(encodedStr.toString(), str));
		}
	}

	@Test
	public void testHashCodeAndEquals() throws Exception {
		CheckedCodec<CharSequence, byte[], Exception> a = newCodec(new byte[] {
			1, 2, 3, 4, 5
		});
		CheckedCodec<CharSequence, byte[], Exception> b = newCodec(new byte[] {
			5, 4, 3, 2, 1
		});
		for (int i = 0; i < 10; i++) {
			String str = String.format("String-#-%02d-%s", i, UUID.randomUUID().toString());
			EncodedString encodedA = EncodedString.from(str, a);
			Assertions.assertNotEquals(encodedA, null, str);
			Assertions.assertNotEquals(encodedA, this, str);
			Assertions.assertEquals(encodedA, encodedA, str);

			EncodedString encodedB = EncodedString.from(str, b);
			Assertions.assertNotSame(encodedA, encodedB, str);
			Assertions.assertEquals(encodedA, encodedB, str);
			Assertions.assertEquals(encodedA.hashCode(), encodedB.hashCode(), str);

			str = String.format("Alternate-String-#-%02d-%s", i, UUID.randomUUID().toString());
			EncodedString encodedC = EncodedString.from(str, a);
			Assertions.assertNotSame(encodedA, encodedC, str);
			Assertions.assertNotEquals(encodedA, encodedC, str);
			Assertions.assertNotEquals(encodedA.hashCode(), encodedC.hashCode(), str);
		}
	}

	@Test
	public void testDecode() throws Exception {
		CheckedCodec<CharSequence, byte[], Exception> a = newCodec(new byte[] {
			1, 2, 3, 4, 5
		});
		for (int i = 0; i < 10; i++) {
			String str = String.format("String-#-%02d-%s", i, UUID.randomUUID().toString());
			EncodedString encodedA = EncodedString.from(str, a);
			Assertions.assertEquals(str, encodedA.decode());
			Assertions.assertNotEquals(str, encodedA.toString());
			Assertions.assertFalse(StringUtils.containsIgnoreCase(encodedA.toString(), str));
		}
	}

	@Test
	public void testConstruction() throws Exception {
		char[] c = null;
		CharSequence C = null;
		Assertions.assertThrows(NullPointerException.class, () -> EncodedString.from(c, null));
		Assertions.assertThrows(NullPointerException.class, () -> EncodedString.from(C, null));
		CheckedCodec<CharSequence, byte[], Exception> a = newCodec();
		EncodedString.from(c, a);
		EncodedString.from(C, a);
		EncodedString.from(new char[] {}, a);
		EncodedString.from(UUID.randomUUID().toString().toCharArray(), a);
		EncodedString.from(UUID.randomUUID().toString(), a);
	}
}