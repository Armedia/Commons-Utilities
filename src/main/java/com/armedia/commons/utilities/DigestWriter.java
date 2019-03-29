package com.armedia.commons.utilities;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;

public class DigestWriter extends FilterWriter implements DigestHashCollector {

	private static final Charset CHARSET = Charset.defaultCharset();

	private final MessageDigest digest;
	private final char[] charBuf = new char[1];
	private long length = 0;

	public DigestWriter(OutputStream out, String digest) throws NoSuchAlgorithmException {
		this( //
			Objects.requireNonNull(out, "Must provide a non-null OutputStream to wrap around"),
			MessageDigest.getInstance( //
				Objects.requireNonNull(digest, "Must provide a non-null digest name") //
			) //
		);
	}

	public DigestWriter(OutputStream out, MessageDigest digest) {
		this( //
			new OutputStreamWriter( //
				Objects.requireNonNull(out, "Must provide a non-null OutputStream to wrap around") //
			), //
			digest //
		);
	}

	public DigestWriter(Writer out, String digest) throws NoSuchAlgorithmException {
		this( //
			Objects.requireNonNull(out, "Must provide a non-null Writer to wrap around"), //
			MessageDigest.getInstance( //
				Objects.requireNonNull(digest, "Must provide a non-null digest name") //
			) //
		);
	}

	public DigestWriter(Writer out, MessageDigest digest) {
		super(Objects.requireNonNull(out, "Must provide a non-null Writer to wrap around"));
		this.digest = Objects.requireNonNull(digest, "Must provide a non-null digest instance");
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
			ByteBuffer buf = DigestWriter.CHARSET.encode(CharBuffer.wrap(this.charBuf));
			this.digest.update(buf);
			this.length += buf.limit();
		}
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		synchronized (this.lock) {
			super.write(cbuf, off, len);
			ByteBuffer buf = DigestWriter.CHARSET.encode(CharBuffer.wrap(cbuf, off, len));
			this.digest.update(buf);
			this.length += buf.limit();
		}
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		synchronized (this.lock) {
			super.write(str, off, len);
			ByteBuffer buf = DigestWriter.CHARSET.encode(str.substring(off, off + len));
			this.digest.update(buf);
			this.length += buf.limit();
		}
	}
}