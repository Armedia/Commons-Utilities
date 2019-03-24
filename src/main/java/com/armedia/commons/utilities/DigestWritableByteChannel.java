package com.armedia.commons.utilities;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import com.armedia.commons.utilities.concurrent.BaseReadWriteLockable;

public class DigestWritableByteChannel extends BaseReadWriteLockable implements WritableByteChannel, HashCollector {

	private final WritableByteChannel channel;
	private final MessageDigest digest;

	public DigestWritableByteChannel(WritableByteChannel channel, String digest) throws NoSuchAlgorithmException {
		this( //
			Objects.requireNonNull(channel, "Must provide a non-null WritableByteChannel instance"), //
			MessageDigest.getInstance( //
				Objects.requireNonNull(digest, "Must provide a non-null digest name") //
			) //
		);
	}

	public DigestWritableByteChannel(WritableByteChannel channel, MessageDigest digest) {
		this.channel = Objects.requireNonNull(channel, "Must provide a non-null WritableByteChannel instance");
		this.digest = Objects.requireNonNull(digest, "Must provide a non-null MessageDigest instance");
	}

	@Override
	public MessageDigest getDigest() {
		return this.digest;
	}

	@Override
	public byte[] collectHash() {
		return writeLocked(() -> this.digest.digest());
	}

	@Override
	public void resetHash() {
		writeLocked(this.digest::reset);
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		return writeLocked(() -> {
			int ret = this.channel.write(src.asReadOnlyBuffer());
			this.digest.update(src);
			return ret;
		});
	}

	@Override
	public boolean isOpen() {
		return readLocked(this.channel::isOpen);
	}

	@Override
	public void close() throws IOException {
		writeLocked(this.channel::close);
	}
}