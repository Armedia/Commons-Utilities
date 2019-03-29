package com.armedia.commons.utilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DigestReadableByteChannelTest {

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
	void testDigestReadableByteChannel() throws Exception {
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestReadableByteChannel(null, DigestReadableByteChannelTest.NULL_STRING));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestReadableByteChannel(null, DigestReadableByteChannelTest.NULL_DIGEST));
		Assertions.assertThrows(NullPointerException.class, () -> new DigestReadableByteChannel(null, ""));
		Assertions.assertThrows(NullPointerException.class, () -> new DigestReadableByteChannel(null, ""));

		ReadableByteChannel wbc = Channels.newChannel(new NullInputStream(0));

		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestReadableByteChannel(wbc, DigestReadableByteChannelTest.NULL_STRING));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestReadableByteChannel(wbc, DigestReadableByteChannelTest.NULL_DIGEST));

		Assertions.assertThrows(NoSuchAlgorithmException.class, () -> new DigestReadableByteChannel(wbc, ""));
		try (ReadableByteChannel c = new DigestReadableByteChannel(wbc,
			DigestReadableByteChannelTest.SHA256.getAlgorithm())) {

		}
		try (ReadableByteChannel c = new DigestReadableByteChannel(wbc, DigestReadableByteChannelTest.SHA256)) {
		}
	}

	@Test
	void testGetDigest() throws Exception {
		ReadableByteChannel wbc = Channels.newChannel(new NullInputStream(0));
		try (DigestReadableByteChannel c = new DigestReadableByteChannel(wbc, DigestReadableByteChannelTest.SHA256)) {
			Assertions.assertSame(DigestReadableByteChannelTest.SHA256, c.getDigest());
		}
		try (DigestReadableByteChannel c = new DigestReadableByteChannel(wbc,
			DigestReadableByteChannelTest.SHA256.getAlgorithm())) {
			Assertions.assertNotSame(DigestReadableByteChannelTest.SHA256, c.getDigest());
		}
	}

	@Test
	void testCollectHash() throws Exception {
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
						try (DigestReadableByteChannel rbc = new DigestReadableByteChannel(
							Channels.newChannel(new ByteArrayInputStream(d.getLeft())), algorithm)) {
							ByteBuffer buf = ByteBuffer.allocate(d.getLeft().length);
							rbc.read(buf);
							byte[] expected = d.getRight();
							String expectedHex = Hex.encodeHexString(expected);
							byte[] actual = rbc.collectHash();
							String actualHex = Hex.encodeHexString(actual);
							Assertions.assertEquals(expectedHex, actualHex,
								String.format("Failed on item # %d (algo = %s)", ++pos, algorithm));
							Assertions.assertArrayEquals(expected, actual);
						}
					}
				}
			}
		}
	}

	@Test
	void testResetHash() throws Exception {
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
						try (DigestReadableByteChannel rbc = new DigestReadableByteChannel(
							Channels.newChannel(new ByteArrayInputStream(d.getLeft())), algorithm)) {
							ByteBuffer buf = ByteBuffer.allocate(d.getRight().length);
							rbc.read(buf);
							byte[] expected = d.getRight();
							String expectedHex = Hex.encodeHexString(expected);
							rbc.resetHash();
							byte[] actual = rbc.collectHash();
							String actualHex = Hex.encodeHexString(actual);
							Assertions.assertEquals(expectedHex, actualHex,
								String.format("Failed on item # %d (algo = %s)", ++pos, algorithm));
							Assertions.assertArrayEquals(expected, actual);
						}
					}
				}
			}
		}
	}

	@Test
	void testRead() throws Exception {
		byte[] c = RandomStringUtils.random(1000).getBytes();
		try (final ReadableByteChannel channel = new DigestReadableByteChannel(
			Channels.newChannel(new ByteArrayInputStream(c)), DigestReadableByteChannelTest.SHA256)) {
			ByteBuffer buf = ByteBuffer.allocate(c.length);
			channel.read(buf);
			Assertions.assertArrayEquals(c, buf.array());
		}
	}

	@Test
	void testIsOpen() throws Exception {
		ReadableByteChannel wbc = Channels.newChannel(new NullInputStream(0));
		DigestReadableByteChannel C = new DigestReadableByteChannel(wbc, DigestReadableByteChannelTest.SHA256);
		try (DigestReadableByteChannel c = C) {
			Assertions.assertTrue(c.isOpen());
		}
		Assertions.assertFalse(C.isOpen());
	}

	@Test
	void testClose() throws IOException {
		ReadableByteChannel wbc = EasyMock.createMock(ReadableByteChannel.class);
		wbc.close();
		EasyMock.expectLastCall().once();
		EasyMock.replay(wbc);
		DigestReadableByteChannel c = new DigestReadableByteChannel(wbc, DigestReadableByteChannelTest.SHA256);
		c.close();
		EasyMock.verify(wbc);
	}

}
