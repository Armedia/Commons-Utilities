package com.armedia.commons.utilities;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DigestFileChannelTest {

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
	void testDigestFileChannel() throws Exception {
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestFileChannel(null, DigestFileChannelTest.NULL_STRING));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestFileChannel(null, DigestFileChannelTest.NULL_DIGEST));
		Assertions.assertThrows(NullPointerException.class, () -> new DigestFileChannel(null, ""));
		Assertions.assertThrows(NullPointerException.class, () -> new DigestFileChannel(null, ""));

		final File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
		tempFile.deleteOnExit();

		FileChannel fc = EasyMock.createMock(FileChannel.class);
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestFileChannel(fc, DigestFileChannelTest.NULL_STRING));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestFileChannel(fc, DigestFileChannelTest.NULL_DIGEST));
		Assertions.assertThrows(NoSuchAlgorithmException.class, () -> new DigestFileChannel(fc, ""));

		try (RandomAccessFile raf = new RandomAccessFile(tempFile, "r")) {
			try (DigestFileChannel c = new DigestFileChannel(raf.getChannel(),
				DigestFileChannelTest.SHA256.getAlgorithm())) {
			}
		}
		try (RandomAccessFile raf = new RandomAccessFile(tempFile, "r")) {
			try (DigestFileChannel c = new DigestFileChannel(raf.getChannel(), DigestFileChannelTest.SHA256)) {
			}
		}
	}

	@Test
	void testGetDigest() throws Exception {
		final File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
		tempFile.deleteOnExit();
		for (Provider p : Security.getProviders()) {
			for (Service s : p.getServices()) {
				if (StringUtils.equals(MessageDigest.class.getSimpleName(), s.getType())) {
					MessageDigest digest = MessageDigest.getInstance(s.getAlgorithm());
					try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rws")) {
						digest.reset();
						try (DigestFileChannel c = new DigestFileChannel(raf.getChannel(), digest)) {
							Assertions.assertSame(digest, c.getDigest());
						}
					}
					try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rws")) {
						digest.reset();
						try (DigestFileChannel c = new DigestFileChannel(raf.getChannel(), digest.getAlgorithm())) {
							Assertions.assertNotSame(digest, c.getDigest());
							Assertions.assertEquals(digest.getAlgorithm(), c.getDigest().getAlgorithm());
						}
					}
				}
			}
		}
	}

	@Test
	void testCollectHash() throws Exception {
		final File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
		tempFile.deleteOnExit();
		final Random r = new Random(System.nanoTime());
		for (Provider p : Security.getProviders()) {
			for (Service s : p.getServices()) {
				if (StringUtils.equals(MessageDigest.class.getSimpleName(), s.getType())) {
					MessageDigest digest = MessageDigest.getInstance(s.getAlgorithm());
					byte[] data = new byte[16384];
					byte[] hash = null;
					try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rws")) {
						r.nextBytes(data);
						raf.write(data);
						digest.update(data);
						hash = digest.digest();
					}
					try (RandomAccessFile raf = new RandomAccessFile(tempFile, "r")) {
						digest.reset();
						DigestFileChannel dfc = new DigestFileChannel(raf.getChannel(), digest);
						try (DigestFileChannel c = dfc) {
						}
						Pair<Long, byte[]> actual = dfc.collectHash();
						Assertions.assertEquals(Hex.encodeHexString(hash), Hex.encodeHexString(actual.getRight()),
							String.format("Mismatch evaluating %s", s.getAlgorithm()));
						Assertions.assertArrayEquals(hash, actual.getRight());
						Assertions.assertEquals(data.length, actual.getLeft().longValue());
					}
				}
			}
		}
	}

	@Test
	void testResetHash() throws Exception {
		final File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
		tempFile.deleteOnExit();
		final Random r = new Random(System.nanoTime());
		for (Provider p : Security.getProviders()) {
			for (Service s : p.getServices()) {
				if (StringUtils.equals(MessageDigest.class.getSimpleName(), s.getType())) {
					MessageDigest digest = MessageDigest.getInstance(s.getAlgorithm());
					byte[] data = new byte[16384];
					byte[] hash = null;
					try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rws")) {
						r.nextBytes(data);
						raf.write(data);
						digest.update(data);
						digest.reset();
						hash = digest.digest();
					}
					try (RandomAccessFile raf = new RandomAccessFile(tempFile, "r")) {
						digest.reset();
						DigestFileChannel dfc = new DigestFileChannel(raf.getChannel(), digest);
						try (DigestFileChannel c = dfc) {
						}
						dfc.resetHash();
						Pair<Long, byte[]> actual = dfc.collectHash();
						Assertions.assertEquals(Hex.encodeHexString(hash), Hex.encodeHexString(actual.getRight()),
							String.format("Mismatch evaluating %s", s.getAlgorithm()));
						Assertions.assertArrayEquals(hash, actual.getRight());
						Assertions.assertEquals(0, actual.getLeft().longValue());
					}
				}
			}
		}
	}

	@Test
	void testDelegateMethods() throws Exception {
	}
}