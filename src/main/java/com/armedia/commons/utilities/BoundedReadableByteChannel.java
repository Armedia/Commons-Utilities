package com.armedia.commons.utilities;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Objects;

/**
 * <p>
 * This is an implementation of {@link ReadableByteChannel} that limits the amount of data that can
 * be read. Using a negative or 0 limit value will result in all {@link #read(ByteBuffer) read}
 * operations returning -1 (end-of-stream). Otherwise, the channel will return up to the given
 * number of bytes in total.
 * </p>
 *
 * @author diego.rivera@armedia.com
 *
 */
public class BoundedReadableByteChannel implements ReadableByteChannel {

	/**
	 * The channel to forward all the important calls to
	 */
	protected final ReadableByteChannel channel;

	/**
	 * The number of bytes this instance was limited to upon creation
	 */
	protected final long limit;

	/**
	 * The number of bytes remaining to be read before we hit our limit
	 */
	protected long remaining;

	/**
	 * <p>
	 * Construct a new instance wrapping the given channel, to supply at most {@code remaining}
	 * bytes.
	 * </p>
	 *
	 * @param channel
	 *            the {@link ReadableByteChannel} to wrap around
	 * @param limit
	 *            the maximum number of bytes to allow reading of
	 * @throws NullPointerException
	 *             if {@code channel} is {@code null}
	 */
	public BoundedReadableByteChannel(ReadableByteChannel channel, long limit) {
		this.channel = Objects.requireNonNull(channel, "Must provide a ReadableByteChannel instance to wrap around");
		this.limit = (limit <= 0 ? 0 : limit);
		this.remaining = (limit <= 0 ? 0 : limit);
	}

	/**
	 * <p>
	 * Returns the limit with which this instance was created, with one caveat: any negative value
	 * is represented as {@code 0}.
	 * </p>
	 *
	 * @return the limit with which this instance was created (never negative)
	 */
	public long getLimit() {
		return this.limit;
	}

	/**
	 * <p>
	 * Returns the underlying {@link ReadableByteChannel} instance.
	 * </p>
	 *
	 * @return the underlying {@link ReadableByteChannel} instance
	 */
	public ReadableByteChannel getChannel() {
		return this.channel;
	}

	/**
	 * <p>
	 * Returns the number of bytes remaining to be read.
	 * </p>
	 *
	 * @return the number of bytes remaining to be read (never negative)
	 */
	public long getRemaining() {
		return (this.remaining <= 0 ? 0 : this.remaining);
	}

	/**
	 * <p>
	 * Tells whether or not the underlying channel is open, and therefore this channel is open.
	 * </p>
	 *
	 * @return true if, and only if, the underlying channel is open
	 */
	@Override
	public boolean isOpen() {
		return this.channel.isOpen();
	}

	/**
	 * <p>
	 * This is a wrapper around {@link ReadableByteChannel#read(ByteBuffer)} that implements the
	 * size limitation. See that method for more documentation on the contract.
	 * </p>
	 *
	 * @param dst
	 *            The buffer into which bytes are to be transferred
	 * @return The number of bytes read, possibly zero, or {@code -1} if the channel has reached
	 *         end-of-stream
	 * @throws IOException
	 *             If some other I/O error occurs while invoking the underlying channel's
	 *             {@link ReadableByteChannel#read(ByteBuffer) read()} method.
	 */
	@Override
	public int read(ByteBuffer dst) throws IOException {
		ByteBuffer tgt = dst;
		int wanted = dst.remaining();
		if (this.remaining <= 0) { return -1; }
		if (wanted == 0) { return 0; }
		if (wanted > this.remaining) {
			wanted = (int) this.remaining;
			tgt = dst.slice();
			tgt.limit(wanted);
		}

		int read = this.channel.read(tgt);
		if (tgt != dst) {
			dst.position(dst.position() + tgt.position());
		}
		return read;
	}

	/**
	 * <p>
	 * Closes the underlying channel, and by extension this channel. See
	 * {@link ByteChannel#close()}.
	 * </p>
	 *
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	@Override
	public void close() throws IOException {
		this.channel.close();
	}
}