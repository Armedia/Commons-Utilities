/*******************************************************************************
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (C) 2013 - 2020 Armedia, LLC
 * %%
 * This file is part of the Caliente software.
 *
 * If the software was purchased under a paid Caliente license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Caliente is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Caliente is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Caliente. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 *******************************************************************************/
package com.armedia.commons.utilities.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import com.armedia.commons.utilities.concurrent.BaseShareableLockable;
import com.armedia.commons.utilities.concurrent.MutexAutoLock;
import com.armedia.commons.utilities.concurrent.ShareableLockable;

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
public class BoundedReadableByteChannel extends ReadableByteChannelWrapper {

	private final ShareableLockable lock = new BaseShareableLockable();

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
		super(channel);
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
	 * Returns the number of bytes remaining to be read.
	 * </p>
	 *
	 * @return the number of bytes remaining to be read (never negative)
	 */
	public long getRemaining() {
		return this.lock.shareLocked(() -> (this.remaining <= 0 ? 0 : this.remaining));
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
		try (MutexAutoLock lock = this.lock.autoMutexLock()) {
			ByteBuffer tgt = dst;
			int wanted = dst.remaining();
			if (this.remaining <= 0) { return -1; }
			if (wanted == 0) { return 0; }
			if (wanted > this.remaining) {
				wanted = (int) this.remaining;
				tgt = dst.slice();
				tgt.limit(wanted);
			}

			int read = this.wrapped.read(tgt);
			this.remaining -= read;
			if (tgt != dst) {
				dst.position(dst.position() + tgt.position());
			}
			return read;
		}
	}

}