/**
 * *******************************************************************
 * 
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS.
 * REPRODUCTION OF ANY PORTION OF THE SOURCE CODE, CONTAINED HEREIN,
 * OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE,
 * IS STRICTLY PROHIBITED.
 * 
 * Confidential Property of Armedia LLC.
 * (c) Copyright Armedia LLC 2011.
 * All Rights reserved.
 * 
 * *******************************************************************
 */
package com.armedia.commons.utilities;

import java.nio.charset.Charset;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

import com.armedia.commons.utilities.BinaryEncoding;

/**
 * @author drivera@armedia.com
 * 
 */
public class BinaryEncodingTest {

	private static final byte[] EMPTY = new byte[0];
	private final Random random = new Random(System.currentTimeMillis());

	@Test
	public void testHex() {
		byte[] src = new byte[12345];
		this.random.nextBytes(src);
		BinaryEncoding e = BinaryEncoding.HEX;
		String data = Hex.encodeHexString(src);
		Assert.assertEquals(data, e.encode(src));
		Assert.assertArrayEquals(src, e.decode(data));

		Assert.assertNull(e.encode(null));
		Assert.assertNull(e.decode(null));
		Assert.assertEquals(0, e.decode("").length);
		Assert.assertEquals(0, e.encode(BinaryEncodingTest.EMPTY).length());

		Assert.assertNull(e.normalize(null));
		Assert.assertEquals(data, e.normalize(data));
		Assert.assertEquals(data, e.normalize(data.toUpperCase()));
		Assert.assertEquals(data, e.normalize(data.toLowerCase()));

		data = String.format("%sa", data);
		try {
			e.decode(data);
			Assert.fail("Failed to raise IllegalArgumentException");
		} catch (IllegalArgumentException ex) {
			// All is well
		}

		data = String.format("%sg", data);
		try {
			e.decode(data);
			Assert.fail("Failed to raise IllegalArgumentException");
		} catch (IllegalArgumentException ex) {
			// All is well
		}
	}

	@Test
	public void testBase64() {
		byte[] src = new byte[12345];
		this.random.nextBytes(src);
		BinaryEncoding e = BinaryEncoding.BASE64;
		String data = Base64.encodeBase64String(src);
		Assert.assertEquals(data, e.encode(src));
		Assert.assertArrayEquals(src, e.decode(data));

		Assert.assertNull(e.encode(null));
		Assert.assertNull(e.decode(null));
		Assert.assertEquals(0, e.decode("").length);
		Assert.assertEquals(0, e.encode(BinaryEncodingTest.EMPTY).length());

		Assert.assertNull(e.normalize(null));
		Assert.assertEquals(data, e.normalize(data));

		data = String.format("%s a", data);
		try {
			e.decode(data);
			Assert.fail("Failed to raise IllegalArgumentException");
		} catch (IllegalArgumentException ex) {
			// All is well
		}

		data = data.replace(' ', 'a');
		data = String.format("%s$", data);
		try {
			e.decode(data);
			Assert.fail("Failed to raise IllegalArgumentException");
		} catch (IllegalArgumentException ex) {
			// All is well
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
		Assert.assertArrayEquals(data, e.decode(src));
		Assert.assertEquals(src, e.encode(data));

		Assert.assertNull(e.encode(null));
		Assert.assertNull(e.decode(null));
		Assert.assertEquals(0, e.decode("").length);
		Assert.assertEquals(0, e.encode(BinaryEncodingTest.EMPTY).length());

		Assert.assertNull(e.normalize(null));
		Assert.assertEquals(src, e.normalize(src));
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.BinaryEncoding#identify(java.lang.String)}.
	 */
	@Test
	public void testIdentify() {
		Assert.assertNull(BinaryEncoding.identify(null));
		String[] good = {
			"string", "String", "sTrInG", "STRING", "    string    ", "hex", "Hex", "hEx", "HEX", "    hex    ",
			"base64", "Base64", "bAsE64", "BASE64", "    base64    "
		};
		for (String s : good) {
			Assert.assertNotNull(BinaryEncoding.identify(s));
		}
		try {
			BinaryEncoding.identify("");
		} catch (IllegalArgumentException e) {
			// All is well
		}
	}
}