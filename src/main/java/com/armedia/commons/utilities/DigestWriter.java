package com.armedia.commons.utilities;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;

public class DigestWriter extends FilterWriter implements DigestHashCollector {

	private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();

	private final MessageDigest digest;
	private final Charset charset;
	private final char[] charBuf = new char[1];
	private long length = 0;

	public DigestWriter(Writer out, String digest) throws NoSuchAlgorithmException {
		this(out, DigestWriter.DEFAULT_CHARSET, digest);
	}

	public DigestWriter(Writer out, MessageDigest digest) {
		this(out, DigestWriter.DEFAULT_CHARSET, digest);
	}

	public DigestWriter(Writer out, String charset, String digest) throws NoSuchAlgorithmException {
		this(out, Charset.forName(Objects.requireNonNull(charset, "Must provide a non-null charset name")), digest);
	}

	public DigestWriter(Writer out, String charset, MessageDigest digest) {
		this(out, Charset.forName(Objects.requireNonNull(charset, "Must provide a non-null charset name")), digest);
	}

	public DigestWriter(Writer out, CharsetEncoder charset, String digest) throws NoSuchAlgorithmException {
		this(out, Objects.requireNonNull(charset, "Must provide a non-null CharsetEncoder").charset(), digest);
	}

	public DigestWriter(Writer out, CharsetEncoder charset, MessageDigest digest) {
		this(out, Objects.requireNonNull(charset, "Must provide a non-null CharsetEncoder").charset(), digest);
	}

	public DigestWriter(Writer out, Charset charset, String digest) throws NoSuchAlgorithmException {
		this( //
			Objects.requireNonNull(out, "Must provide a non-null Writer to wrap around"), //
			Objects.requireNonNull(charset, "Must provide a non-null charset"), //
			MessageDigest.getInstance( //
				Objects.requireNonNull(digest, "Must provide a non-null digest name") //
			) //
		);
	}

	public DigestWriter(Writer out, Charset charset, MessageDigest digest) {
		super(Objects.requireNonNull(out, "Must provide a non-null Writer to wrap around"));
		this.digest = Objects.requireNonNull(digest, "Must provide a non-null digest instance");
		this.charset = Objects.requireNonNull(charset, "Must provide a non-null charset");
	}

	public Charset getCharset() {
		return this.charset;
	}

	@Override
	public MessageDigest getDigest() {
		return this.digest;
	}

	@Override
	public Pair<Long, byte[]> collectHash() {
		synchronized (this.lock) {
			Pair<Long, byte[]> ret = Pair.of(this.length, this.digest.digest());
			this.length = 0;
			return ret;
		}
	}

	@Override
	public void resetHash() {
		synchronized (this.lock) {
			this.digest.reset();
			this.length = 0;
		}
	}

	@Override
	public DigestWriter append(char c) throws IOException {
		synchronized (this.lock) {
			super.append(c);
			return this;
		}
	}

	@Override
	public DigestWriter append(CharSequence csq) throws IOException {
		synchronized (this.lock) {
			super.append(csq);
			return this;
		}
	}

	@Override
	public DigestWriter append(CharSequence csq, int start, int end) throws IOException {
		synchronized (this.lock) {
			super.append(csq, start, end);
			return this;
		}
	}

	@Override
	public void write(int c) throws IOException {
		synchronized (this.lock) {
			super.write(c);
			this.charBuf[0] = (char) c;
			ByteBuffer buf = this.charset.encode(CharBuffer.wrap(this.charBuf));
			this.digest.update(buf);
			this.length += buf.limit();
		}
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		synchronized (this.lock) {
			super.write(cbuf, off, len);
			ByteBuffer buf = this.charset.encode(CharBuffer.wrap(cbuf, off, len));
			this.digest.update(buf);
			this.length += buf.limit();
		}
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		synchronized (this.lock) {
			super.write(str, off, len);
			ByteBuffer buf = this.charset.encode(str.substring(off, off + len));
			this.digest.update(buf);
			this.length += buf.limit();
		}
	}
}