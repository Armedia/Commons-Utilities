package com.armedia.commons.utilities;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;

public class DigestOutputStream extends FilterOutputStream implements HashCollector {

	private final MessageDigest digest;
	private long length = 0;

	public DigestOutputStream(OutputStream out, String digest) throws NoSuchAlgorithmException {
		this( //
			Objects.requireNonNull(out, "Must provide a non-null Writer to wrap around"), //
			MessageDigest.getInstance( //
				Objects.requireNonNull(digest, "Must provide a non-null digest name") //
			) //
		);
	}

	public DigestOutputStream(OutputStream out, MessageDigest digest) {
		super(Objects.requireNonNull(out, "Must provide a non-null Writer to wrap around"));
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
	public void write(int c) throws IOException {
		super.write(c);
		this.digest.update((byte) c);
		this.length++;
	}
}