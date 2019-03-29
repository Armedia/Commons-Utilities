package com.armedia.commons.utilities;

import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;

public class DigestReader extends FilterReader implements DigestHashCollector {

	private static final Charset CHARSET = Charset.defaultCharset();

	private final MessageDigest digest;
	private long length = 0;

	public DigestReader(InputStream out, String digest) throws NoSuchAlgorithmException {
		this( //
			Objects.requireNonNull(out, "Must provide a non-null InputStream to wrap around"),
			MessageDigest.getInstance( //
				Objects.requireNonNull(digest, "Must provide a non-null digest name") //
			) //
		);
	}

	public DigestReader(InputStream out, MessageDigest digest) {
		this( //
			new InputStreamReader( //
				Objects.requireNonNull(out, "Must provide a non-null InputStream to wrap around") //
			), //
			digest //
		);
	}

	public DigestReader(Reader in, String digest) throws NoSuchAlgorithmException {
		this( //
			Objects.requireNonNull(in, "Must provide a non-null Reader to wrap around"), //
			MessageDigest.getInstance( //
				Objects.requireNonNull(digest, "Must provide a non-null digest name") //
			) //
		);
	}

	public DigestReader(Reader in, MessageDigest digest) {
		super(Objects.requireNonNull(in, "Must provide a non-null Reader to wrap around"));
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
	public int read(CharBuffer target) throws IOException {
		synchronized (this.lock) {
			int read = super.read(target);
			ByteBuffer buf = DigestReader.CHARSET.encode(target.slice());
			this.digest.update(buf);
			this.length += buf.limit();
			return read;
		}
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		synchronized (this.lock) {
			int read = super.read(cbuf, off, len);
			ByteBuffer buf = DigestReader.CHARSET.encode(CharBuffer.wrap(cbuf, off, len));
			this.digest.update(buf);
			this.length += buf.limit();
			return read;
		}
	}
}