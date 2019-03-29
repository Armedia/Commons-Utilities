package com.armedia.commons.utilities;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;

public class DigestInputStream extends FilterInputStream implements DigestHashCollector {

	private final MessageDigest digest;
	private final byte[] byteBuf = new byte[1];
	private long length = 0;

	public DigestInputStream(InputStream in, String digest) throws NoSuchAlgorithmException {
		this( //
			Objects.requireNonNull(in, "Must provide a non-null InputStream to wrap around"), //
			MessageDigest.getInstance( //
				Objects.requireNonNull(digest, "Must provide a non-null digest name") //
			) //
		);
	}

	public DigestInputStream(InputStream in, MessageDigest digest) {
		super(Objects.requireNonNull(in, "Must provide a non-null InputStream to wrap around"));
		this.digest = Objects.requireNonNull(digest, "Must provide a non-null digest instance");
	}

	@Override
	public MessageDigest getDigest() {
		return this.digest;
	}

	@Override
	public Pair<Long, byte[]> collectHash() {
		Pair<Long, byte[]> ret = Pair.of(this.length, this.digest.digest());
		this.length = 0;
		return ret;
	}

	@Override
	public void resetHash() {
		this.digest.reset();
		this.length = 0;
	}

	@Override
	public int read() throws IOException {
		if (read(this.byteBuf) >= 0) { return this.byteBuf[0]; }
		return -1;
	}

	@Override
	public int read(byte[] buf, int off, int len) throws IOException {
		int read = super.read(buf, off, len);
		this.digest.update(buf, off, len);
		this.length += len;
		return read;
	}
}