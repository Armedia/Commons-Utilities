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

class DigestWritableByteChannelTest {

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
	void testDigestWritableByteChannel() throws Exception {
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestWritableByteChannel(null, DigestWritableByteChannelTest.NULL_STRING));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestWritableByteChannel(null, DigestWritableByteChannelTest.NULL_DIGEST));
		Assertions.assertThrows(NullPointerException.class, () -> new DigestWritableByteChannel(null, ""));
		Assertions.assertThrows(NullPointerException.class, () -> new DigestWritableByteChannel(null, ""));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		WritableByteChannel wbc = Channels.newChannel(baos);

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
	void testGetDigest() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		WritableByteChannel wbc = Channels.newChannel(baos);
		try (DigestWritableByteChannel c = new DigestWritableByteChannel(wbc, DigestWritableByteChannelTest.SHA256)) {
			Assertions.assertSame(DigestWritableByteChannelTest.SHA256, c.getDigest());
		}
		try (DigestWritableByteChannel c = new DigestWritableByteChannel(wbc,
			DigestWritableByteChannelTest.SHA256.getAlgorithm())) {
			Assertions.assertNotSame(DigestWritableByteChannelTest.SHA256, c.getDigest());
		}
	}

	@Test
	void testCollectHash() throws Exception {
		final List<Pair<byte[], byte[]>> data = new ArrayList<>();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final WritableByteChannel channel = Channels.newChannel(baos);

		for (Provider p : Security.getProviders()) {
			for (Service s : p.getServices()) {
				if (StringUtils.equals(MessageDigest.class.getSimpleName(), s.getType())) {
					data.clear();
					final String algorithm = s.getAlgorithm();
					System.out.printf("Checking algorithm [%s]...%n", algorithm);

					for (int i = 1; i <= 10; i++) {
						baos.flush();
						baos.reset();

						// Encode the characters to bytes
						byte[] c = RandomStringUtils.random(i * 1000).getBytes();
						channel.write(ByteBuffer.wrap(c));
						byte[] hash = MessageDigest.getInstance(algorithm).digest(baos.toByteArray());
						data.add(Pair.of(c, hash));
					}

					int pos = 0;
					for (Pair<byte[], byte[]> d : data) {
						try (DigestWritableByteChannel wbc = new DigestWritableByteChannel(
							Channels.newChannel(NullOutputStream.NULL_OUTPUT_STREAM), algorithm)) {
							wbc.write(ByteBuffer.wrap(d.getLeft()));
							byte[] expected = d.getRight();
							String expectedHex = Hex.encodeHexString(expected);
							byte[] actual = wbc.collectHash();
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
					System.out.printf("Checking reset of algorithm [%s]...%n", algorithm);

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
							byte[] actual = wbc.collectHash();
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
	void testWrite() throws Exception {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final WritableByteChannel channel = Channels.newChannel(baos);
		byte[] c = RandomStringUtils.random(1000).getBytes();
		channel.write(ByteBuffer.wrap(c));
		Assertions.assertArrayEquals(c, baos.toByteArray());
	}

	@Test
	void testIsOpen() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		WritableByteChannel wbc = Channels.newChannel(baos);
		DigestWritableByteChannel C = new DigestWritableByteChannel(wbc, DigestWritableByteChannelTest.SHA256);
		try (DigestWritableByteChannel c = C) {
			Assertions.assertTrue(c.isOpen());
		}
		Assertions.assertFalse(C.isOpen());
	}

	@Test
	void testClose() throws IOException {
		WritableByteChannel wbc = EasyMock.createMock(WritableByteChannel.class);
		wbc.close();
		EasyMock.expectLastCall().once();
		EasyMock.replay(wbc);
		DigestWritableByteChannel c = new DigestWritableByteChannel(wbc, DigestWritableByteChannelTest.SHA256);
		c.close();
		EasyMock.verify(wbc);
	}

}
