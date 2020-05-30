/*******************************************************************************
 * #%L
 * Armedia Caliente
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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Consumer;

import com.armedia.commons.utilities.Tools;
import com.armedia.commons.utilities.concurrent.BaseMutexLockable;
import com.armedia.commons.utilities.concurrent.MutexAutoLock;
import com.armedia.commons.utilities.concurrent.MutexLockable;

public class SpyOutputStream extends FilterOutputStream {

	private static final Consumer<OutputStream> NOOP = (o) -> {
	};

	private final MutexLockable lock = new BaseMutexLockable();
	private final Consumer<ByteBuffer> spy;
	private final Consumer<OutputStream> closer;

	private volatile boolean closed = false;

	public SpyOutputStream(OutputStream out, Consumer<ByteBuffer> spy) {
		this(out, spy, null);
	}

	public SpyOutputStream(OutputStream out, Consumer<ByteBuffer> spy, Consumer<OutputStream> closer) {
		super(Objects.requireNonNull(out, "Must provide an OutputStream to spy on"));
		this.spy = Objects.requireNonNull(spy, "Must provide a consumer to spy with");
		this.closer = Tools.coalesce(closer, SpyOutputStream.NOOP);
	}

	private void assertOpen() throws IOException {
		try (MutexAutoLock lock = this.lock.autoMutexLock()) {
			if (this.closed) { throw new IOException("This stream is already closed"); }
		}
	}

	@Override
	public void write(int c) throws IOException {
		try (MutexAutoLock lock = this.lock.autoMutexLock()) {
			assertOpen();
			byte[] buf = new byte[1];
			buf[0] = (byte) c;
			write(buf, 0, buf.length);
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		Objects.requireNonNull(b, "Must provide the data to write out");
		try (MutexAutoLock lock = this.lock.autoMutexLock()) {
			assertOpen();
			write(b, 0, b.length);
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		Objects.requireNonNull(b, "Must provide the data to write out");
		try (MutexAutoLock lock = this.lock.autoMutexLock()) {
			assertOpen();
			ByteBuffer buf = ByteBuffer.wrap(b.clone()).asReadOnlyBuffer();
			super.write(b, off, len);
			try {
				this.spy.accept(buf);
			} catch (Throwable t) {
				// Do nothing... ignore the problem
			}
		}
	}

	@Override
	public void close() throws IOException {
		try (MutexAutoLock lock = this.lock.autoMutexLock()) {
			assertOpen();
			try {
				this.closer.accept(this.out);
				super.close();
			} finally {
				this.closed = true;
			}
		}
	}
}
