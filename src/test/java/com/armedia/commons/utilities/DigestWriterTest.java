package com.armedia.commons.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.output.NullWriter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DigestWriterTest {

	private static final Writer NULL_WRITER = null;
	private static final String NULL_STRING = null;
	private static final Charset NULL_CHARSET = null;
	private static final CharsetEncoder NULL_ENCODER = null;
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

		Writer o = NullWriter.NULL_WRITER;
		Charset cs = Charset.defaultCharset();
		CharsetEncoder ce = cs.newEncoder();

		Assertions.assertThrows(NullPointerException.class, () -> new DigestWriter(DigestWriterTest.NULL_WRITER,
			DigestWriterTest.NULL_STRING, DigestWriterTest.NULL_STRING));
		Assertions.assertThrows(NullPointerException.class, () -> new DigestWriter(DigestWriterTest.NULL_WRITER,
			DigestWriterTest.NULL_STRING, DigestWriterTest.NULL_DIGEST));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestWriter(DigestWriterTest.NULL_WRITER, DigestWriterTest.NULL_STRING, ""));
		Assertions.assertThrows(NullPointerException.class, () -> new DigestWriter(DigestWriterTest.NULL_WRITER,
			DigestWriterTest.NULL_STRING, DigestWriterTest.SHA256));
		Assertions.assertThrows(NullPointerException.class, () -> new DigestWriter(DigestWriterTest.NULL_WRITER,
			DigestWriterTest.NULL_CHARSET, DigestWriterTest.NULL_STRING));
		Assertions.assertThrows(NullPointerException.class, () -> new DigestWriter(DigestWriterTest.NULL_WRITER,
			DigestWriterTest.NULL_CHARSET, DigestWriterTest.NULL_DIGEST));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestWriter(DigestWriterTest.NULL_WRITER, DigestWriterTest.NULL_CHARSET, ""));
		Assertions.assertThrows(NullPointerException.class, () -> new DigestWriter(DigestWriterTest.NULL_WRITER,
			DigestWriterTest.NULL_CHARSET, DigestWriterTest.SHA256));
		Assertions.assertThrows(NullPointerException.class, () -> new DigestWriter(DigestWriterTest.NULL_WRITER,
			DigestWriterTest.NULL_ENCODER, DigestWriterTest.NULL_STRING));
		Assertions.assertThrows(NullPointerException.class, () -> new DigestWriter(DigestWriterTest.NULL_WRITER,
			DigestWriterTest.NULL_ENCODER, DigestWriterTest.NULL_DIGEST));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestWriter(DigestWriterTest.NULL_WRITER, DigestWriterTest.NULL_ENCODER, ""));
		Assertions.assertThrows(NullPointerException.class, () -> new DigestWriter(DigestWriterTest.NULL_WRITER,
			DigestWriterTest.NULL_ENCODER, DigestWriterTest.SHA256));

		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestWriter(o, DigestWriterTest.NULL_STRING, DigestWriterTest.NULL_STRING));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestWriter(o, DigestWriterTest.NULL_STRING, DigestWriterTest.NULL_DIGEST));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestWriter(o, DigestWriterTest.NULL_CHARSET, DigestWriterTest.NULL_STRING));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestWriter(o, DigestWriterTest.NULL_CHARSET, DigestWriterTest.NULL_DIGEST));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestWriter(o, DigestWriterTest.NULL_ENCODER, DigestWriterTest.NULL_STRING));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestWriter(o, DigestWriterTest.NULL_ENCODER, DigestWriterTest.NULL_DIGEST));
		Assertions.assertThrows(IllegalCharsetNameException.class, () -> new DigestWriter(o, "", ""));
		Assertions.assertThrows(IllegalCharsetNameException.class,
			() -> new DigestWriter(o, "", DigestWriterTest.SHA256.getAlgorithm()));
		Assertions.assertThrows(IllegalCharsetNameException.class,
			() -> new DigestWriter(o, "", DigestWriterTest.SHA256));
		Assertions.assertThrows(IllegalCharsetNameException.class, () -> new DigestWriter(o, "", ""));
		Assertions.assertThrows(NoSuchAlgorithmException.class, () -> new DigestWriter(o, cs.name(), ""));
		Assertions.assertThrows(NoSuchAlgorithmException.class, () -> new DigestWriter(o, ""));

		try (Writer dw = new DigestWriter(o, DigestWriterTest.SHA256)) {
			// Do nothing
		}
		try (Writer dw = new DigestWriter(o, cs.name(), DigestWriterTest.SHA256)) {
			// Do nothing
		}
		try (Writer dw = new DigestWriter(o, ce, DigestWriterTest.SHA256)) {
			// Do nothing
		}
		try (Writer dw = new DigestWriter(o, cs, DigestWriterTest.SHA256)) {
			// Do nothing
		}
		try (Writer dw = new DigestWriter(o, DigestWriterTest.SHA256.getAlgorithm())) {
			// Do nothing
		}
		try (Writer dw = new DigestWriter(o, cs.name(), DigestWriterTest.SHA256.getAlgorithm())) {
			// Do nothing
		}
		try (Writer dw = new DigestWriter(o, ce, DigestWriterTest.SHA256.getAlgorithm())) {
			// Do nothing
		}
		try (Writer dw = new DigestWriter(o, cs, DigestWriterTest.SHA256.getAlgorithm())) {
			// Do nothing
		}
	}

	@Test
	void testGetCharset() throws Exception {
		SortedMap<String, Charset> charsets = Charset.availableCharsets();
		Writer o = NullWriter.NULL_WRITER;
		for (String csn : charsets.keySet()) {
			Charset cs = charsets.get(csn);
			CharsetEncoder cse = null;
			try {
				cse = cs.newEncoder();
			} catch (UnsupportedOperationException e) {
				// That's ok...this guy doesn't work
				continue;
			}
			try (DigestWriter dw = new DigestWriter(o, csn, DigestWriterTest.SHA256)) {
				Assertions.assertSame(cs, dw.getCharset());
			}
			try (DigestWriter dw = new DigestWriter(o, csn, DigestWriterTest.SHA256.getAlgorithm())) {
				Assertions.assertSame(cs, dw.getCharset());
			}
			try (DigestWriter dw = new DigestWriter(o, cse, DigestWriterTest.SHA256)) {
				Assertions.assertSame(cs, dw.getCharset());
			}
			try (DigestWriter dw = new DigestWriter(o, cse, DigestWriterTest.SHA256.getAlgorithm())) {
				Assertions.assertSame(cs, dw.getCharset());
			}
			try (DigestWriter dw = new DigestWriter(o, cs, DigestWriterTest.SHA256)) {
				Assertions.assertSame(cs, dw.getCharset());
			}
			try (DigestWriter dw = new DigestWriter(o, cs, DigestWriterTest.SHA256.getAlgorithm())) {
				Assertions.assertSame(cs, dw.getCharset());
			}
		}
	}

	@Test
	void testFlush() throws IOException {
		Writer out = EasyMock.strictMock(Writer.class);
		out.flush();
		EasyMock.expectLastCall().once();
		out.close();
		EasyMock.expectLastCall().once();
		EasyMock.replay(out);
		try (Writer w = new DigestWriter(out, DigestWriterTest.SHA256)) {
			w.flush();
		}
		EasyMock.verify(out);
	}

	@Test
	void testClose() throws IOException {
		Writer out = EasyMock.createMock(Writer.class);
		out.close();
		EasyMock.expectLastCall().once();
		EasyMock.replay(out);
		try (Writer w = new DigestWriter(out, DigestWriterTest.SHA256)) {
			// Do nothing...
		}
		EasyMock.verify(out);
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

		try (Writer w = new DigestWriter(new OutputStreamWriter(baos), DigestWriterTest.SHA256)) {
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
		final Charset charset = Charset.defaultCharset();
		for (Provider p : Security.getProviders()) {
			for (Service s : p.getServices()) {
				if (StringUtils.equals(MessageDigest.class.getSimpleName(), s.getType())) {
					data.clear();
					final String algorithm = s.getAlgorithm();

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
						try (DigestWriter w = new DigestWriter(NullWriter.NULL_WRITER, algorithm)) {
							w.write(d.getLeft());
							w.flush();
							byte[] expected = d.getRight();
							String expectedHex = Hex.encodeHexString(expected);
							Pair<Long, byte[]> actual = w.collectHash();
							String actualHex = Hex.encodeHexString(actual.getRight());
							Assertions.assertEquals(expectedHex, actualHex,
								String.format("Failed on item # %d (algo = %s)", ++pos, algorithm));
							Assertions.assertArrayEquals(expected, actual.getRight());
							ByteBuffer buf = charset.encode(CharBuffer.wrap(d.getLeft()));
							Assertions.assertEquals(buf.limit(), actual.getLeft().longValue());
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
		final Writer nw = NullWriter.NULL_WRITER;
		for (Provider p : Security.getProviders()) {
			for (Service s : p.getServices()) {
				if (StringUtils.equals(MessageDigest.class.getSimpleName(), s.getType())) {
					MessageDigest md = MessageDigest.getInstance(s.getAlgorithm());
					try (DigestWriter w = new DigestWriter(nw, md)) {
						Assertions.assertSame(md, w.getDigest());
					}
					try (DigestWriter w = new DigestWriter(nw, md.getAlgorithm())) {
						Assertions.assertNotSame(md, w.getDigest());
					}
				}
			}
		}
	}
}
