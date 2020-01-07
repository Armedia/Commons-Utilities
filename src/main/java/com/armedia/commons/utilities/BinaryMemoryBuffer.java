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
package com.armedia.commons.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 *
 */
public class BinaryMemoryBuffer extends OutputStream implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final int MINIMUM_CHUNK_SIZE = 128;
	public static final int DEFAULT_CHUNK_SIZE = 1024;

	private final List<byte[]> buffers = new ArrayList<>();

	private final int chunkSize;

	private boolean closed = false;
	private long wpos = 0;

	private static byte[] newBuffer(int size) {
		byte[] buf = new byte[size];
		Arrays.fill(buf, (byte) 0);
		return buf;
	}

	private class BinaryMemoryBufferInputStream extends InputStream {

		private long mark = -1;
		private long rpos = 0;

		private synchronized long blockForInput() throws IOException {
			synchronized (BinaryMemoryBuffer.this) {
				while ((BinaryMemoryBuffer.this.wpos - this.rpos) <= 0) {
					if (BinaryMemoryBuffer.this.closed) { return -1; }
					try {
						BinaryMemoryBuffer.this.wait();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						throw new IOException("Interrupted while waiting for input", e);
					}
				}
				return (BinaryMemoryBuffer.this.wpos - this.rpos);
			}
		}

		@Override
		public int read(byte[] b) throws IOException {
			if (b == null) { throw new NullPointerException("Buffer to read into can't be null"); }
			return read(b, 0, b.length);
		}

		@Override
		public synchronized int read(byte[] b, int off, int len) throws IOException {
			// First, a little parameter QA
			if (b == null) { throw new NullPointerException("The given array was null"); }
			if (len < 0) {
				throw new IllegalArgumentException(String.format("Cannot read negative lengths (%d)", len));
			}
			if (off < 0) {
				throw new IllegalArgumentException(String.format("Cannot read into a negative offset (%d)", off));
			}
			if (b.length < (off + len)) {
				throw new IllegalArgumentException(
					String.format("The given offset (%d) and length (%d) exceed the size of the given byte array (%d)",
						off, len, b.length));
			}

			// Take a shortcut to avoid work
			if (len == 0) { return 0; }

			if (blockForInput() < 0) { return -1; }

			// Ok...so...copy the data over in chunks
			int totalRead = 0;
			while (len > 0) {
				long a = available();
				if (a <= 0) { return totalRead; }

				long c = this.rpos / BinaryMemoryBuffer.this.chunkSize;
				int p = (int) (this.rpos % BinaryMemoryBuffer.this.chunkSize);

				byte[] chunk = BinaryMemoryBuffer.this.buffers.get((int) c);
				int r = Math.min(BinaryMemoryBuffer.this.chunkSize - p, len);
				if (a < r) {
					r = (int) a;
				}
				System.arraycopy(chunk, p, b, off, r);
				off += r;
				len -= r;
				this.rpos += r;
				totalRead += r;
			}
			notify();
			return totalRead;
		}

		@Override
		public synchronized int read() throws IOException {
			if (blockForInput() < 0) { return -1; }
			long chunk = this.rpos / BinaryMemoryBuffer.this.chunkSize;
			long pos = this.rpos % BinaryMemoryBuffer.this.chunkSize;
			byte[] c = BinaryMemoryBuffer.this.buffers.get((int) chunk);
			byte ret = c[(int) pos];
			this.rpos++;
			notify();
			// Ensure the value is between 0 and 255
			return (0x00FF & ret);
		}

		@Override
		public synchronized long skip(long n) throws IOException {
			long trueSkip = Tools.ensureBetween(0L, (getCurrentSize() - this.rpos), n);
			this.rpos += trueSkip;
			return trueSkip;
		}

		@Override
		public synchronized int available() throws IOException {
			long remainder = (getCurrentSize() - this.rpos);
			remainder = Math.min(remainder, Integer.MAX_VALUE);
			return Math.max((int) remainder, 0);
		}

		@Override
		public synchronized void mark(int readlimit) {
			this.mark = this.rpos;
		}

		@Override
		public synchronized void reset() throws IOException {
			this.rpos = this.mark;
			notify();
		}

		@Override
		public boolean markSupported() {
			return true;
		}
	}

	public BinaryMemoryBuffer(int chunkSize) {
		if (chunkSize < BinaryMemoryBuffer.MINIMUM_CHUNK_SIZE) {
			chunkSize = BinaryMemoryBuffer.MINIMUM_CHUNK_SIZE;
		}
		this.chunkSize = chunkSize;
	}

	public BinaryMemoryBuffer() {
		this(BinaryMemoryBuffer.DEFAULT_CHUNK_SIZE);
	}

	public final int getChunkSize() {
		return this.chunkSize;
	}

	public final long getCurrentSize() {
		return this.wpos;
	}

	public synchronized final long getAllocatedSize() {
		return this.buffers.size() * this.chunkSize;
	}

	private synchronized byte[] getWritableChunk() {
		// c is guaranteed to be a valid integer, since it's modulated by an integer value
		long c = this.wpos / this.chunkSize;
		if (c >= this.buffers.size()) {
			byte[] chunk = BinaryMemoryBuffer.newBuffer(this.chunkSize);
			this.buffers.add(chunk);
			return chunk;
		}
		return this.buffers.get((int) c);
	}

	@Override
	public synchronized void write(int b) throws IOException {
		if (this.closed) { throw new IOException("This buffer is closed"); }
		byte[] chunk = getWritableChunk();
		long p = this.wpos % this.chunkSize;
		// p is guaranteed to be a valid integer, since it's modulated by an integer value
		// so it's safe to cast the number
		chunk[(int) p] = (byte) b;
		this.wpos++;
		notify();
	}

	@Override
	public void write(byte[] data) throws IOException {
		if (data == null) { throw new NullPointerException(); }
		write(data, 0, data.length);
	}

	@Override
	public synchronized void write(byte[] b, int off, int len) throws IOException {
		if (this.closed) { throw new IOException("This buffer is closed"); }
		// First, a little parameter QA
		if (b == null) { throw new NullPointerException("The given array was null"); }
		if (len < 0) { throw new IllegalArgumentException(String.format("Cannot copy negative lengths (%d)", len)); }
		if (off < 0) {
			throw new IllegalArgumentException(String.format("Cannot copy from a negative offset (%d)", off));
		}
		if (b.length < (off + len)) {
			throw new IllegalArgumentException(
				String.format("The given offset (%d) and length (%d) exceed the size of the given byte array (%d)", off,
					len, b.length));
		}

		// Take a shortcut to avoid work
		if (len == 0) { return; }

		// Ok...so...copy the data over in chunks
		while (len > 0) {
			byte[] chunk = getWritableChunk();
			// p is guaranteed to be a valid integer, since it's modulated by an integer value
			// so it's safe to cast the number
			long p = this.wpos % this.chunkSize;
			int remainder = Math.min(this.chunkSize - (int) p, len);
			System.arraycopy(b, off, chunk, (int) p, remainder);
			len -= remainder;
			off += remainder;
			this.wpos += remainder;
		}
		notify();
	}

	public final InputStream getInputStream() {
		return new BinaryMemoryBufferInputStream();
	}

	@Override
	public synchronized void close() {
		this.closed = true;
		notify();
	}
}
