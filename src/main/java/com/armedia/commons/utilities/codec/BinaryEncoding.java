/**
 * *******************************************************************
 *
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS. REPRODUCTION OF ANY PORTION
 * OF THE SOURCE CODE, CONTAINED HEREIN, OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE, IS
 * STRICTLY PROHIBITED.
 *
 * Confidential Property of Armedia LLC. (c) Copyright Armedia LLC 2011. All Rights reserved.
 *
 * *******************************************************************
 */
package com.armedia.commons.utilities.codec;

import java.nio.charset.Charset;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

public enum BinaryEncoding implements Codec<byte[], String> {
	//
	HEX {
		@Override
		protected byte[] doDecode(String value) {
			try {
				return Hex.decodeHex(value.toCharArray());
			} catch (DecoderException e) {
				throw new IllegalArgumentException(e);
			}
		}

		@Override
		protected String doEncode(byte[] value) {
			return Hex.encodeHexString(value);
		}
	},
	BASE64 {
		@Override
		protected byte[] doDecode(String value) {
			if (!Base64.isBase64(value.replaceAll("\\s", "\\$"))) {
				throw new IllegalArgumentException("The given string has illegal Base64 characters");
			}
			return Base64.decodeBase64(value);
		}

		@Override
		protected String doEncode(byte[] value) {
			return Base64.encodeBase64String(value);
		}
	},
	STRING {
		private final Charset charset = Charset.forName("UTF-8");

		@Override
		protected byte[] doDecode(String value) {
			return value.getBytes(this.charset);
		}

		@Override
		protected String doEncode(byte[] value) {
			return new String(value, this.charset);
		}
	},;

	private static final String EMPTY_STRING = "";
	private static final byte[] NO_BYTES = new byte[0];

	/**
	 * Encodes the given {@code byte} array into a String, using the appropriate codec to perform
	 * translation. If the value is {@code null}, {@code null} is returned. If the value is a
	 * length-0 {@code byte} array (i.e. no data to encode), then the empty string {@code ""} is
	 * returned.
	 *
	 * @param value
	 * @return a string encoded from the given byte array. If the value is {@code null},
	 *         {@code null} is returned
	 */
	@Override
	public final String encode(byte[] value) {
		if (value == null) { return null; }
		if (value.length == 0) { return BinaryEncoding.EMPTY_STRING; }
		return doEncode(value);
	}

	/**
	 * Perform the actual encoding
	 *
	 * @param value
	 *            the binary value to encode
	 * @return the string representation of the binary value, properly encoded
	 */
	protected abstract String doEncode(byte[] value);

	/**
	 * Decodes the given string into a binary byte array, using the appropriate codec to perform
	 * translation. If the value is {@code null}, {@code null} is returned. If the value is the
	 * empty string ({@code ""} - a.k.a. no data to decode), then a length-0 {@code byte} array is
	 * returned.
	 *
	 * @param value
	 * @return a byte array decoded from the given string value. If the value is {@code null},
	 *         {@code null} is returned
	 */
	@Override
	public byte[] decode(String value) {
		if (value == null) { return null; }
		if (value.length() == 0) { return BinaryEncoding.NO_BYTES; }
		return doDecode(value);
	}

	/**
	 * Normalizes the value to a form which would have been produced by the encoding. Equivalent to
	 * invoking {@code encode(decode(value))}.
	 *
	 * @param value
	 * @return the normalized form of the given encoded value
	 */
	public final String normalize(String value) {
		return encode(decode(value));
	}

	/**
	 * Perform the actual decoding
	 *
	 * @param value
	 *            the string representation of the binary value, properly encoded
	 * @return the binary value decoded
	 */
	protected abstract byte[] doDecode(String value);

	/**
	 * Attempts to translate the given string into one of the encodings supported. Importantly, it
	 * strips (as per {@link StringUtils#strip(String)} the string and folds it to uppercase, before
	 * invoking {@link #valueOf(String)}. If the given encoding is {@code null}, then {@code null}
	 * is returned.
	 *
	 * @param encoding
	 * @return the BinaryEncoding instance described by the given string (the string represents the
	 *         name of the encoding)
	 */
	public static BinaryEncoding identify(String encoding) {
		if (encoding == null) { return null; }
		encoding = StringUtils.strip(StringUtils.upperCase(encoding));
		return BinaryEncoding.valueOf(encoding);
	}
}