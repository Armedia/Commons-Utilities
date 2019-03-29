package com.armedia.commons.utilities;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;

import com.armedia.commons.utilities.concurrent.BaseReadWriteLockable;

public class DigestWritableByteChannel extends BaseReadWriteLockable implements WritableByteChannel, DigestHashCollector {

	private final WritableByteChannel channel;
	private final MessageDigest digest;
	private long length = 0;

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
	public Pair<Long, byte[]> collectHash() {
		return writeLocked(() -> {
			Pair<Long, byte[]> ret = Pair.of(this.length, this.digest.digest());
			this.length = 0;
			return ret;
		});
	}

	@Override
	public void resetHash() {
		writeLocked(() -> {
			this.digest.reset();
			this.length = 0;
		});
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		return writeLocked(() -> {
			ByteBuffer slice = src.slice();
			int ret = this.channel.write(slice);
			this.digest.update(src);
			this.length += slice.capacity();
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