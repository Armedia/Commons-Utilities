/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2025 Armedia, LLC
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
import java.util.Random;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.GoodService;

/**
 *
 *
 */
public class BinaryEncodingTest implements GoodService {

	private static final byte[] EMPTY = new byte[0];
	private final Random random = new Random(System.currentTimeMillis());

	@Test
	public void testHex() {
		byte[] src = new byte[12345];
		this.random.nextBytes(src);
		BinaryEncoding e = BinaryEncoding.HEX;
		String data = Hex.encodeHexString(src);
		Assertions.assertEquals(data, e.encode(src));
		Assertions.assertArrayEquals(src, e.decode(data));

		Assertions.assertNull(e.encode(null));
		Assertions.assertNull(e.decode(null));
		Assertions.assertEquals(0, e.decode("").length);
		Assertions.assertEquals(0, e.encode(BinaryEncodingTest.EMPTY).length());

		Assertions.assertNull(e.normalize(null));
		Assertions.assertEquals(data, e.normalize(data));
		Assertions.assertEquals(data, e.normalize(data.toUpperCase()));
		Assertions.assertEquals(data, e.normalize(data.toLowerCase()));

		Assertions.assertThrows(IllegalArgumentException.class, () -> e.decode(String.format("%sa", data)));
		Assertions.assertThrows(IllegalArgumentException.class, () -> e.decode(String.format("%sg", data)));
	}

	@Test
	public void testBase64() {
		byte[] src = new byte[12345];
		this.random.nextBytes(src);
		BinaryEncoding e = BinaryEncoding.BASE64;
		String data = Base64.encodeBase64String(src);
		Assertions.assertEquals(data, e.encode(src));
		Assertions.assertArrayEquals(src, e.decode(data));

		Assertions.assertNull(e.encode(null));
		Assertions.assertNull(e.decode(null));
		Assertions.assertEquals(0, e.decode("").length);
		Assertions.assertEquals(0, e.encode(BinaryEncodingTest.EMPTY).length());

		Assertions.assertNull(e.normalize(null));
		Assertions.assertEquals(data, e.normalize(data));

		{
			String decodeMe = String.format("%s a", data);
			Assertions.assertThrows(IllegalArgumentException.class, () -> e.decode(decodeMe));
		}

		data = data.replace(' ', 'a');
		{
			String decodeMe = String.format("%s$", data);
			Assertions.assertThrows(IllegalArgumentException.class, () -> e.decode(decodeMe));
		}
	}

	@Test
	public void testString() {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < 10; i++) {
			b.append(UUID.randomUUID().toString());
		}
		final String src = b.toString();
		byte[] data = src.getBytes(Charset.forName("UTF-8"));
		BinaryEncoding e = BinaryEncoding.STRING;
		Assertions.assertArrayEquals(data, e.decode(src));
		Assertions.assertEquals(src, e.encode(data));

		Assertions.assertNull(e.encode(null));
		Assertions.assertNull(e.decode(null));
		Assertions.assertEquals(0, e.decode("").length);
		Assertions.assertEquals(0, e.encode(BinaryEncodingTest.EMPTY).length());

		Assertions.assertNull(e.normalize(null));
		Assertions.assertEquals(src, e.normalize(src));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.codec.BinaryEncoding#identify(java.lang.String)}.
	 */
	@Test
	public void testIdentify() {
		Assertions.assertNull(BinaryEncoding.identify(null));
		String[] good = {
			"string", "String", "sTrInG", "STRING", "    string    ", "hex", "Hex", "hEx", "HEX", "    hex    ",
			"base64", "Base64", "bAsE64", "BASE64", "    base64    "
		};
		for (String s : good) {
			Assertions.assertNotNull(BinaryEncoding.identify(s));
		}
		Assertions.assertThrows(IllegalArgumentException.class, () -> BinaryEncoding.identify(""));
	}
}
