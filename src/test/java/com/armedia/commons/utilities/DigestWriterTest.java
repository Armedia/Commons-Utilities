package com.armedia.commons.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
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
import org.apache.commons.io.output.NullWriter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DigestWriterTest {

	private static final Writer NULL_WRITER = null;
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
	void testDigestWriter() throws Exception {
		Writer w = NullWriter.NULL_WRITER;
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestWriter(DigestWriterTest.NULL_WRITER, DigestWriterTest.NULL_STRING));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestWriter(DigestWriterTest.NULL_WRITER, DigestWriterTest.NULL_DIGEST));
		Assertions.assertThrows(NullPointerException.class, () -> new DigestWriter(DigestWriterTest.NULL_WRITER, ""));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestWriter(DigestWriterTest.NULL_WRITER, DigestWriterTest.SHA256));

		Assertions.assertThrows(NullPointerException.class, () -> new DigestWriter(w, DigestWriterTest.NULL_STRING));
		Assertions.assertThrows(NullPointerException.class, () -> new DigestWriter(w, DigestWriterTest.NULL_DIGEST));
		Assertions.assertThrows(NoSuchAlgorithmException.class, () -> new DigestWriter(w, ""));

		try (Writer dw = new DigestWriter(w, DigestWriterTest.SHA256)) {
			// Do nothing
		}

		try (Writer dw = new DigestWriter(w, DigestWriterTest.SHA256.getAlgorithm())) {
			// Do nothing
		}

		OutputStream o = NullOutputStream.NULL_OUTPUT_STREAM;
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestWriter(DigestWriterTest.NULL_STREAM, DigestWriterTest.NULL_STRING));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestWriter(DigestWriterTest.NULL_STREAM, DigestWriterTest.NULL_DIGEST));
		Assertions.assertThrows(NullPointerException.class, () -> new DigestWriter(DigestWriterTest.NULL_STREAM, ""));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestWriter(DigestWriterTest.NULL_STREAM, DigestWriterTest.SHA256));

		Assertions.assertThrows(NullPointerException.class, () -> new DigestWriter(o, DigestWriterTest.NULL_STRING));
		Assertions.assertThrows(NullPointerException.class, () -> new DigestWriter(o, DigestWriterTest.NULL_DIGEST));
		Assertions.assertThrows(NoSuchAlgorithmException.class, () -> new DigestWriter(o, ""));

		try (Writer dw = new DigestWriter(o, DigestWriterTest.SHA256)) {
			// Do nothing
		}
		try (Writer dw = new DigestWriter(o, DigestWriterTest.SHA256.getAlgorithm())) {
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
		try (Writer w = new DigestWriter(out, DigestWriterTest.SHA256)) {
			w.flush();
			Assertions.assertEquals(1, flushCalled.get());
		}
		Assertions.assertEquals(1, flushCalled.get());
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
		try (Writer w = new DigestWriter(out, DigestWriterTest.SHA256)) {
			// Do nothing...
		}
		Assertions.assertEquals(1, closeCalled.get());
	}

	@Test
	void testWrite() throws IOException {
		final List<Pair<char[], byte[]>> data = new ArrayList<>();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final Writer baosWriter = new OutputStreamWriter(baos);
		final Random r = new Random(System.nanoTime());
		for (int i = 1; i <= 10; i++) {
			char[] c = new char[i * 10];
			for (int j = 0; j < c.length; j++) {
				c[j] = (char) (r.nextInt(0xFF));
			}

			baosWriter.flush();
			baos.flush();
			baos.reset();

			// Encode the characters to bytes
			baosWriter.write(c);
			baosWriter.flush();
			baos.flush();
			data.add(Pair.of(c, baos.toByteArray()));
		}

		try (Writer w = new DigestWriter(baos, DigestWriterTest.SHA256)) {
			for (Pair<char[], byte[]> d : data) {
				// write(int)
				baos.reset();
				w.write(d.getLeft());
				w.flush();
				Assertions.assertArrayEquals(d.getRight(), baos.toByteArray());

				// write(char[])
				baos.reset();
				w.write(d.getLeft());
				w.flush();
				Assertions.assertArrayEquals(d.getRight(), baos.toByteArray());

				// write(String)
				baos.reset();
				w.write(new String(d.getLeft()));
				w.flush();
				Assertions.assertArrayEquals(d.getRight(), baos.toByteArray());
			}
		}
	}

	@Test
	void testCollectHash() throws Exception {
		final List<Pair<char[], byte[]>> data = new ArrayList<>();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final Writer baosWriter = new OutputStreamWriter(baos);

		for (Provider p : Security.getProviders()) {
			for (Service s : p.getServices()) {
				if (StringUtils.equals(MessageDigest.class.getSimpleName(), s.getType())) {
					data.clear();
					final String algorithm = s.getAlgorithm();
					System.out.printf("Checking algorithm [%s]...%n", algorithm);

					for (int i = 1; i <= 10; i++) {
						baosWriter.flush();
						baos.flush();
						baos.reset();

						// Encode the characters to bytes
						char[] c = RandomStringUtils.random(i * 1000).toCharArray();
						baosWriter.write(c);
						baosWriter.flush();
						baos.flush();
						byte[] hash = MessageDigest.getInstance(algorithm).digest(baos.toByteArray());
						data.add(Pair.of(c, hash));
					}

					int pos = 0;
					for (Pair<char[], byte[]> d : data) {
						try (DigestWriter w = new DigestWriter(NullOutputStream.NULL_OUTPUT_STREAM, algorithm)) {
							w.write(d.getLeft());
							w.flush();
							byte[] expected = d.getRight();
							String expectedHex = Hex.encodeHexString(expected);
							byte[] actual = w.collectHash();
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
		final List<Pair<char[], byte[]>> data = new ArrayList<>();

		for (Provider p : Security.getProviders()) {
			for (Service s : p.getServices()) {
				if (StringUtils.equals(MessageDigest.class.getSimpleName(), s.getType())) {
					data.clear();
					final String algorithm = s.getAlgorithm();
					System.out.printf("Checking algorithm [%s]...%n", algorithm);

					for (int i = 1; i <= 10; i++) {
						// Encode the characters to bytes
						char[] c = RandomStringUtils.random(i * 1000).toCharArray();
						byte[] hash = MessageDigest.getInstance(algorithm).digest();
						data.add(Pair.of(c, hash));
					}

					int pos = 0;
					for (Pair<char[], byte[]> d : data) {
						try (DigestWriter w = new DigestWriter(NullWriter.NULL_WRITER, algorithm)) {
							w.write(d.getLeft());
							w.flush();
							byte[] expected = d.getRight();
							String expectedHex = Hex.encodeHexString(expected);
							w.resetHash();
							byte[] actual = w.collectHash();
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
	void testAppend() throws Exception {
		final StringWriter sw = new StringWriter();
		try (DigestWriter dw = new DigestWriter(sw, DigestWriterTest.SHA256)) {

			final String str = RandomStringUtils.random(1000);
			final StringBuffer buf = sw.getBuffer();

			for (int i = 0; i < str.length(); i++) {
				buf.setLength(0);
				dw.append(str.charAt(i));
				Assertions.assertEquals(str.charAt(i), buf.charAt(0));
			}

			buf.setLength(0);
			dw.append(str);
			Assertions.assertEquals(str, sw.toString());

			final int chunkSize = 100;
			final int chunks = str.length() / chunkSize;
			for (int i = 0; i < chunks; i++) {
				int start = (i * chunkSize);
				buf.setLength(0);
				String sub = str.substring(start, start + chunkSize);
				dw.append(str, start, start + chunkSize);
				Assertions.assertEquals(sub, sw.toString());
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
					try (DigestWriter w = new DigestWriter(nos, md)) {
						Assertions.assertSame(md, w.getDigest());
					}
					try (DigestWriter w = new DigestWriter(nos, md.getAlgorithm())) {
						Assertions.assertNotSame(md, w.getDigest());
					}
				}
			}
		}
	}
}
