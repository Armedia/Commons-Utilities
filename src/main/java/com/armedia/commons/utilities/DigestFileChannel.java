package com.armedia.commons.utilities;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;

public class DigestFileChannel extends FileChannel implements DigestHashCollector {

	public class DigestFileLock extends FileLock {

		private final FileLock lock;

		protected DigestFileLock(FileLock wrapped) {
			super(DigestFileChannel.this, wrapped.position(), wrapped.size(), wrapped.isShared());
			this.lock = Objects.requireNonNull(wrapped);
		}

		@Override
		public DigestFileChannel acquiredBy() {
			return DigestFileChannel.this;
		}

		@Override
		public boolean isValid() {
			return this.lock.isValid();
		}

		@Override
		public void release() throws IOException {
			this.lock.release();
		}
	}

	private final MessageDigest digest;
	private final FileChannel channel;
	private long length = 0;

	public DigestFileChannel(FileChannel channel, String digest) throws NoSuchAlgorithmException {
		this( //
			Objects.requireNonNull(channel, "Must provide a non-null FileChannel instance"), //
			MessageDigest.getInstance( //
				Objects.requireNonNull(digest, "Must provide a non-null digest name") //
			) //
		);
	}

	public DigestFileChannel(FileChannel channel, MessageDigest digest) {
		this.channel = Objects.requireNonNull(channel, "Must provide a channel to wrap");
		this.digest = Objects.requireNonNull(digest, "Must provide a non-null MessageDigest instance");
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
	public int read(ByteBuffer dst) throws IOException {
		return this.channel.read(dst);
	}

	@Override
	public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
		return this.channel.read(dsts, offset, length);
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		return this.channel.write(src);
	}

	@Override
	public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
		return this.channel.write(srcs, offset, length);
	}

	@Override
	public long position() throws IOException {
		return this.channel.position();
	}

	@Override
	public DigestFileChannel position(long newPosition) throws IOException {
		this.channel.position(newPosition);
		return this;
	}

	@Override
	public long size() throws IOException {
		return this.channel.size();
	}

	@Override
	public DigestFileChannel truncate(long size) throws IOException {
		this.channel.truncate(size);
		return this;
	}

	@Override
	public void force(boolean metaData) throws IOException {
		this.channel.force(metaData);
	}

	@Override
	public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
		return this.channel.transferTo(position, count, target);
	}

	@Override
	public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
		return this.channel.transferFrom(src, position, count);
	}

	@Override
	public int read(ByteBuffer dst, long position) throws IOException {
		return this.channel.read(dst, position);
	}

	@Override
	public int write(ByteBuffer src, long position) throws IOException {
		return this.channel.write(src, position);
	}

	@Override
	public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException {
		return this.channel.map(mode, position, size);
	}

	@Override
	public DigestFileLock lock(long position, long size, boolean shared) throws IOException {
		return new DigestFileLock(this.channel.lock(position, size, shared));
	}

	@Override
	public DigestFileLock tryLock(long position, long size, boolean shared) throws IOException {
		return new DigestFileLock(this.channel.tryLock(position, size, shared));
	}

	@Override
	protected void implCloseChannel() throws IOException {
		// Close, and calculate the hash
		try {
			// Reposition at the top of the file
			this.channel.position(0);
			// Re-read the contents of the file via 4K buffer, and feed them through the digest
			final ByteBuffer buffer = ByteBuffer.allocate(4096);
			this.length = 0;
			while (true) {
				buffer.clear();
				int read = this.channel.read(buffer);
				if (read < 0) {
					break;
				}
				buffer.flip();
				this.digest.update(buffer);
				this.length += read;
			}
		} finally {
			this.channel.close();
		}
	}
}