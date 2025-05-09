/*-
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (C) 2013 - 2025 Armedia, LLC
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
 */
package com.armedia.commons.utilities.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.util.Objects;

import com.armedia.commons.utilities.concurrent.BaseShareableLockable;
import com.armedia.commons.utilities.concurrent.MutexAutoLock;

public class ReadableByteBufferChannel extends BaseShareableLockable implements ReadableByteChannel {

	private final ByteBuffer data;
	private volatile boolean open = true;

	public ReadableByteBufferChannel(ByteBuffer data) {
		this.data = Objects.requireNonNull(data, "Must provide a non-null ByteBuffer");
	}

	@Override
	public boolean isOpen() {
		return shareLocked(() -> this.open);
	}

	@Override
	public void close() {
		shareLockedUpgradable(() -> this.open, () -> {
			this.open = false;
		});
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		try (MutexAutoLock mutex = mutexAutoLock()) {
			if (!this.open) { throw new ClosedChannelException(); }
			if (!this.data.hasRemaining()) { return -1; }

			int remaining = dst.remaining();
			int written = 0;
			while ((written < remaining) && this.data.hasRemaining()) {
				dst.put(this.data.get());
				written++;
			}
			return written;
		}
	}
}
