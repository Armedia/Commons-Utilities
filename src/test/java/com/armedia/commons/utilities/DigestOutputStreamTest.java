package com.armedia.commons.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DigestOutputStreamTest {

	private static final OutputStream NULL_STREAM = null;
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
	void testDigestOutputStream() throws Exception {
		OutputStream o = NullOutputStream.NULL_OUTPUT_STREAM;
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestOutputStream(DigestOutputStreamTest.NULL_STREAM, DigestOutputStreamTest.NULL_STRING));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestOutputStream(DigestOutputStreamTest.NULL_STREAM, DigestOutputStreamTest.NULL_DIGEST));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestOutputStream(DigestOutputStreamTest.NULL_STREAM, ""));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestOutputStream(DigestOutputStreamTest.NULL_STREAM, DigestOutputStreamTest.SHA256));

		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestOutputStream(o, DigestOutputStreamTest.NULL_STRING));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestOutputStream(o, DigestOutputStreamTest.NULL_DIGEST));
		Assertions.assertThrows(NoSuchAlgorithmException.class, () -> new DigestOutputStream(o, ""));

		try (OutputStream dw = new DigestOutputStream(o, DigestOutputStreamTest.SHA256)) {
			// Do nothing
		}
		try (OutputStream dw = new DigestOutputStream(o, DigestOutputStreamTest.SHA256.getAlgorithm())) {
			// Do nothing
		}
	}

	@Test
	void testFlush() throws IOException {
		final AtomicInteger flushCalled = new AtomicInteger(0);
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
			}

			@Override
			public void flush() throws IOException {
				flushCalled.incrementAndGet();
				super.flush();
			}
		};

		Assertions.assertEquals(0, flushCalled.get());
		try (OutputStream w = new DigestOutputStream(out, DigestOutputStreamTest.SHA256)) {
			w.flush();
			Assertions.assertEquals(1, flushCalled.get());
		}
		Assertions.assertEquals(2, flushCalled.get());
	}

	@Test
	void testClose() throws IOException {
		final AtomicInteger closeCalled = new AtomicInteger(0);
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
			}

			@Override
			public void close() throws IOException {
				closeCalled.incrementAndGet();
			}
		};

		Assertions.assertEquals(0, closeCalled.get());
		try (OutputStream w = new DigestOutputStream(out, DigestOutputStreamTest.SHA256)) {
			// Do nothing...
		}
		Assertions.assertEquals(1, closeCalled.get());
	}

	@Test
	void testWrite() throws IOException {
		final List<byte[]> data = new ArrayList<>();
		final Random r = new Random(System.nanoTime());
		for (int i = 1; i <= 10; i++) {
			byte[] c = new byte[i * 10];
			r.nextBytes(c);
			data.add(c);
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (OutputStream w = new DigestOutputStream(baos, DigestOutputStreamTest.SHA256)) {
			for (byte[] d : data) {
				baos.reset();
				// write(int)
				for (byte b : d) {
					w.write(b);
					w.flush();
				}
				Assertions.assertArrayEquals(d, baos.toByteArray());

				// write(byte[])
				baos.reset();
				w.write(d);
				w.flush();
				Assertions.assertArrayEquals(d, baos.toByteArray());
			}
		}
	}

	@Test
	void testCollectHash() throws Exception {
		final List<Pair<byte[], byte[]>> data = new ArrayList<>();
		final Random r = new Random(System.nanoTime());
		for (Provider p : Security.getProviders()) {
			for (Service s : p.getServices()) {
				if (StringUtils.equals(MessageDigest.class.getSimpleName(), s.getType())) {
					data.clear();
					final String algorithm = s.getAlgorithm();

					for (int i = 1; i <= 10; i++) {
						byte[] c = new byte[i * 1000];
						r.nextBytes(c);
						data.add(Pair.of(c, MessageDigest.getInstance(algorithm).digest(c)));
					}

					int pos = 0;
					for (Pair<byte[], byte[]> d : data) {
						try (DigestOutputStream w = new DigestOutputStream(NullOutputStream.NULL_OUTPUT_STREAM,
							algorithm)) {
							w.write(d.getLeft());
							w.flush();
							byte[] expected = d.getRight();
							String expectedHex = Hex.encodeHexString(expected);
							Pair<Long, byte[]> actual = w.collectHash();
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
	void testResetHash() throws Exception {
		final List<Pair<byte[], byte[]>> data = new ArrayList<>();
		final Random r = new Random(System.nanoTime());
		for (Provider p : Security.getProviders()) {
			for (Service s : p.getServices()) {
				if (StringUtils.equals(MessageDigest.class.getSimpleName(), s.getType())) {
					data.clear();
					final String algorithm = s.getAlgorithm();

					for (int i = 1; i <= 10; i++) {
						// Encode the characters to bytes
						byte[] c = new byte[i * 1000];
						r.nextBytes(c);
						data.add(Pair.of(c, MessageDigest.getInstance(algorithm).digest()));
					}

					int pos = 0;
					for (Pair<byte[], byte[]> d : data) {
						try (DigestOutputStream w = new DigestOutputStream(NullOutputStream.NULL_OUTPUT_STREAM,
							algorithm)) {
							w.write(d.getLeft());
							w.flush();
							byte[] expected = d.getRight();
							String expectedHex = Hex.encodeHexString(expected);
							w.resetHash();
							Pair<Long, byte[]> actual = w.collectHash();
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
	void testGetDigest() throws Exception {
		final OutputStream nos = NullOutputStream.NULL_OUTPUT_STREAM;
		for (Provider p : Security.getProviders()) {
			for (Service s : p.getServices()) {
				if (StringUtils.equals(MessageDigest.class.getSimpleName(), s.getType())) {
					MessageDigest md = MessageDigest.getInstance(s.getAlgorithm());
					try (DigestOutputStream w = new DigestOutputStream(nos, md)) {
						Assertions.assertSame(md, w.getDigest());
					}
					try (DigestOutputStream w = new DigestOutputStream(nos, md.getAlgorithm())) {
						Assertions.assertNotSame(md, w.getDigest());
					}
				}
			}
		}
	}
}
