/*******************************************************************************
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (c) 2010 - 2019 Armedia LLC
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
package com.armedia.commons.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DigestWritableByteChannelTest {

	private static final String NULL_STRING = null;
	private static final MessageDigest NULL_DIGEST = null;
	private static final MessageDigest SHA256;
	static {
		try {
			SHA256 = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testDigestWritableByteChannel() throws Exception {
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestWritableByteChannel(null, DigestWritableByteChannelTest.NULL_STRING));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestWritableByteChannel(null, DigestWritableByteChannelTest.NULL_DIGEST));
		Assertions.assertThrows(NullPointerException.class, () -> new DigestWritableByteChannel(null, ""));
		Assertions.assertThrows(NullPointerException.class, () -> new DigestWritableByteChannel(null, ""));

		WritableByteChannel wbc = Channels.newChannel(NullOutputStream.NULL_OUTPUT_STREAM);

		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestWritableByteChannel(wbc, DigestWritableByteChannelTest.NULL_STRING));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestWritableByteChannel(wbc, DigestWritableByteChannelTest.NULL_DIGEST));

		Assertions.assertThrows(NoSuchAlgorithmException.class, () -> new DigestWritableByteChannel(wbc, ""));
		try (WritableByteChannel c = new DigestWritableByteChannel(wbc,
			DigestWritableByteChannelTest.SHA256.getAlgorithm())) {

		}
		try (WritableByteChannel c = new DigestWritableByteChannel(wbc, DigestWritableByteChannelTest.SHA256)) {
		}
	}

	@Test
	public void testGetDigest() throws Exception {
		WritableByteChannel wbc = Channels.newChannel(NullOutputStream.NULL_OUTPUT_STREAM);
		try (DigestWritableByteChannel c = new DigestWritableByteChannel(wbc, DigestWritableByteChannelTest.SHA256)) {
			Assertions.assertSame(DigestWritableByteChannelTest.SHA256, c.getDigest());
		}
		try (DigestWritableByteChannel c = new DigestWritableByteChannel(wbc,
			DigestWritableByteChannelTest.SHA256.getAlgorithm())) {
			Assertions.assertNotSame(DigestWritableByteChannelTest.SHA256, c.getDigest());
		}
	}

	@Test
	public void testCollectHash() throws Exception {
		final List<Pair<byte[], byte[]>> data = new ArrayList<>();
		for (Provider p : Security.getProviders()) {
			for (Service s : p.getServices()) {
				if (StringUtils.equals(MessageDigest.class.getSimpleName(), s.getType())) {
					data.clear();
					final String algorithm = s.getAlgorithm();

					for (int i = 1; i <= 10; i++) {
						// Encode the characters to bytes
						byte[] c = RandomStringUtils.random(i * 1000).getBytes();
						byte[] hash = MessageDigest.getInstance(algorithm).digest(c);
						data.add(Pair.of(c, hash));
					}

					int pos = 0;
					for (Pair<byte[], byte[]> d : data) {
						try (DigestWritableByteChannel wbc = new DigestWritableByteChannel(
							Channels.newChannel(NullOutputStream.NULL_OUTPUT_STREAM), algorithm)) {
							wbc.write(ByteBuffer.wrap(d.getLeft()));
							byte[] expected = d.getRight();
							String expectedHex = Hex.encodeHexString(expected);
							Pair<Long, byte[]> actual = wbc.collectHash();
							String actualHex = Hex.encodeHexString(actual.getRight());
							Assertions.assertEquals(expectedHex, actualHex,
								String.format("Failed on item # %d (algo = %s)", ++pos, algorithm));
							Assertions.assertArrayEquals(expected, actual.getRight());
							Assertions.assertEquals(d.getLeft().length, actual.getLeft().longValue());
						}
					}
				}
			}
		}
	}

	@Test
	public void testResetHash() throws Exception {
		final List<Pair<byte[], byte[]>> data = new ArrayList<>();

		for (Provider p : Security.getProviders()) {
			for (Service s : p.getServices()) {
				if (StringUtils.equals(MessageDigest.class.getSimpleName(), s.getType())) {
					data.clear();
					final String algorithm = s.getAlgorithm();

					for (int i = 1; i <= 10; i++) {
						// Encode the characters to bytes
						byte[] c = RandomStringUtils.random(i * 1000).getBytes();
						byte[] hash = MessageDigest.getInstance(algorithm).digest();
						data.add(Pair.of(c, hash));
					}

					int pos = 0;
					for (Pair<byte[], byte[]> d : data) {
						try (DigestWritableByteChannel wbc = new DigestWritableByteChannel(
							Channels.newChannel(NullOutputStream.NULL_OUTPUT_STREAM), algorithm)) {
							wbc.write(ByteBuffer.wrap(d.getLeft()));
							byte[] expected = d.getRight();
							String expectedHex = Hex.encodeHexString(expected);
							wbc.resetHash();
							Pair<Long, byte[]> actual = wbc.collectHash();
							String actualHex = Hex.encodeHexString(actual.getRight());
							Assertions.assertEquals(expectedHex, actualHex,
								String.format("Failed on item # %d (algo = %s)", ++pos, algorithm));
							Assertions.assertArrayEquals(expected, actual.getRight());
							Assertions.assertEquals(0, actual.getLeft().longValue());
						}
					}
				}
			}
		}
	}

	@Test
	public void testWrite() throws Exception {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (final WritableByteChannel channel = new DigestWritableByteChannel(Channels.newChannel(baos),
			DigestWritableByteChannelTest.SHA256)) {
			byte[] c = RandomStringUtils.random(1000).getBytes();
			channel.write(ByteBuffer.wrap(c));
			Assertions.assertArrayEquals(c, baos.toByteArray());
		}
	}

	@Test
	public void testIsOpen() throws Exception {
		WritableByteChannel wbc = Channels.newChannel(NullOutputStream.NULL_OUTPUT_STREAM);
		DigestWritableByteChannel C = new DigestWritableByteChannel(wbc, DigestWritableByteChannelTest.SHA256);
		try (DigestWritableByteChannel c = C) {
			Assertions.assertTrue(c.isOpen());
		}
		Assertions.assertFalse(C.isOpen());
	}

	@Test
	public void testClose() throws IOException {
		WritableByteChannel wbc = EasyMock.createMock(WritableByteChannel.class);
		wbc.close();
		EasyMock.expectLastCall().once();
		EasyMock.replay(wbc);
		DigestWritableByteChannel c = new DigestWritableByteChannel(wbc, DigestWritableByteChannelTest.SHA256);
		c.close();
		EasyMock.verify(wbc);
	}

}
