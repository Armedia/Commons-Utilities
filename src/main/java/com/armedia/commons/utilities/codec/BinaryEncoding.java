/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2019 Armedia, LLC
 * %%
 * This file is part of the Caliente software.
 *
 * If the software was purchased under a paid Caliente license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Caliente is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Caliente is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Caliente. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 *******************************************************************************/
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
