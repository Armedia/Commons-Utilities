package com.armedia.commons.utilities;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;

import com.armedia.commons.utilities.concurrent.BaseReadWriteLockable;

public class DigestReadableByteChannel extends BaseReadWriteLockable implements ReadableByteChannel, DigestHashCollector {

	private final ReadableByteChannel channel;
	private final MessageDigest digest;
	private long length = 0;

	public DigestReadableByteChannel(ReadableByteChannel channel, String digest) throws NoSuchAlgorithmException {
		this( //
			Objects.requireNonNull(channel, "Must provide a non-null ReadableByteChannel instance"), //
			MessageDigest.getInstance( //
				Objects.requireNonNull(digest, "Must provide a non-null digest name") //
			) //
		);
	}

	public DigestReadableByteChannel(ReadableByteChannel channel, MessageDigest digest) {
		this.channel = Objects.requireNonNull(channel, "Must provide a non-null ReadableByteChannel instance");
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
	public int read(final ByteBuffer dst) throws IOException {
		return writeLocked(() -> {
			final ByteBuffer dupe = dst.duplicate();
			final int read = this.channel.read(dst);
			if (read > 0) {
				dupe.limit(dupe.position() + read);
				this.digest.update(dupe);
				this.length += read;
			}
			return read;
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