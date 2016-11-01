package com.armedia.commons.utilities;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * This enum facilitates the implementation of a hash algorithm to perform one-way hashes (i.e. such
 * as for password hashing).
 * 
 * @author diego
 * 
 */
public enum HashAlgorithm {
	// Supported hashes
	MD5("MD5"),
	SHA1("SHA-1"),
	SHA256("SHA-256");

	private final String name;

	private HashAlgorithm(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();

	private static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9)) {
					buf.append((char) ('0' + halfbyte));
				} else {
					buf.append((char) ('a' + (halfbyte - 10)));
				}
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	/**
	 * Calculate the hash for the given text, and return it in hexadecimal format.
	 * 
	 * @param text
	 */
	public String getHash(String text) {
		return getHash(text.getBytes(HashAlgorithm.DEFAULT_CHARSET));
	}

	public String getHash(byte[] b) {
		try {
			MessageDigest md = MessageDigest.getInstance(getName());
			md.update(b, 0, b.length);
			Arrays.fill(b, (byte) 0);
			String ret = HashAlgorithm.convertToHex(md.digest());
			md.reset();
			return ret;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(String.format("The JVM needs to support the %s hash algorithm", getName()), e);
		}
	}
}