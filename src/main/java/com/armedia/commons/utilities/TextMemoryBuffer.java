/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2019 Armedia, LLC
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
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
 *
 */
public class TextMemoryBuffer extends Writer implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final int MINIMUM_CHUNK_SIZE = 128;
	public static final int DEFAULT_CHUNK_SIZE = 1024;

	private final List<char[]> buffers = new ArrayList<>();

	private final int chunkSize;

	private boolean closed = false;
	private long wpos = 0;

	private static char[] newBuffer(int size) {
		char[] buf = new char[size];
		Arrays.fill(buf, '\0');
		return buf;
	}

	private class TextMemoryBufferCharSequence implements CharSequence {
		private final int offset;
		private final int length;

		private TextMemoryBufferCharSequence() {
			this(0, (int) TextMemoryBuffer.this.wpos);
		}

		private TextMemoryBufferCharSequence(int offset, int length) {
			this.offset = offset;
			this.length = length;
		}

		@Override
		public int length() {
			return this.length;
		}

		@Override
		public char charAt(int index) {
			index += this.offset;
			if (index >= (this.offset + this.length)) { throw new ArrayIndexOutOfBoundsException(index - this.offset); }
			int chunk = index / TextMemoryBuffer.this.chunkSize;
			int pos = index % TextMemoryBuffer.this.chunkSize;
			return TextMemoryBuffer.this.buffers.get(chunk)[pos];
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			if (start > end) { throw new ArrayIndexOutOfBoundsException("Start can't be after the end"); }
			int newStart = start + this.offset;
			int newLength = (end + this.offset) - newStart;
			if ((newStart + newLength) > this.length) { throw new ArrayIndexOutOfBoundsException(); }
			return new TextMemoryBufferCharSequence(newStart, newLength);
		}
	}

	public class TextMemoryBufferReader extends Reader {

		private long mark = -1;
		private long rpos = 0;

		private synchronized long blockForInput() throws IOException {
			synchronized (TextMemoryBuffer.this) {
				while ((TextMemoryBuffer.this.wpos - this.rpos) <= 0) {
					if (TextMemoryBuffer.this.closed) { return -1; }
					try {
						TextMemoryBuffer.this.wait();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						throw new IOException("Interrupted while waiting for input", e);
					}
				}
				return (TextMemoryBuffer.this.wpos - this.rpos);
			}
		}

		@Override
		public int read(char[] b) throws IOException {
			if (b == null) { throw new NullPointerException("Buffer to read into can't be null"); }
			return read(b, 0, b.length);
		}

		@Override
		public synchronized int read(char[] b, int off, int len) throws IOException {
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

				long c = this.rpos / TextMemoryBuffer.this.chunkSize;
				int p = (int) (this.rpos % TextMemoryBuffer.this.chunkSize);

				char[] chunk = TextMemoryBuffer.this.buffers.get((int) c);
				int r = Math.min(TextMemoryBuffer.this.chunkSize - p, len);
				if (a < r) {
					r = (int) a;
				}
				System.arraycopy(chunk, p, b, off, r);
				off += r;
				len -= r;
				this.rpos += r;
				totalRead += r;
				notify();
			}
			return totalRead;
		}

		@Override
		public synchronized int read() throws IOException {
			if (blockForInput() < 0) { return -1; }
			long chunk = this.rpos / TextMemoryBuffer.this.chunkSize;
			long pos = this.rpos % TextMemoryBuffer.this.chunkSize;
			char[] c = TextMemoryBuffer.this.buffers.get((int) chunk);
			char ret = c[(int) pos];
			this.rpos++;
			notify();
			return ret;
		}

		@Override
		public synchronized long skip(long n) throws IOException {
			long trueSkip = Tools.ensureBetween(0L, (getCurrentSize() - this.rpos), n);
			this.rpos += trueSkip;
			return trueSkip;
		}

		public synchronized int available() {
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

		@Override
		public void close() throws IOException {
		}
	}

	public TextMemoryBuffer(int chunkSize) {
		if (chunkSize < TextMemoryBuffer.MINIMUM_CHUNK_SIZE) {
			chunkSize = TextMemoryBuffer.MINIMUM_CHUNK_SIZE;
		}
		this.chunkSize = chunkSize;
	}

	public TextMemoryBuffer() {
		this(TextMemoryBuffer.DEFAULT_CHUNK_SIZE);
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

	private synchronized char[] getWritableChunk() {
		// c is guaranteed to be a valid integer, since it's modulated by an integer value
		long c = this.wpos / this.chunkSize;
		if (c >= this.buffers.size()) {
			char[] chunk = TextMemoryBuffer.newBuffer(this.chunkSize);
			this.buffers.add(chunk);
			return chunk;
		}
		return this.buffers.get((int) c);
	}

	@Override
	public void write(char[] data) throws IOException {
		if (data == null) { throw new NullPointerException(); }
		Objects.requireNonNull(data, "Must provide a non-null char[]");
		write(data, 0, data.length);
	}

	public synchronized void write(CharSequence seq) throws IOException {
		if (this.closed) { throw new IOException("This buffer is closed"); }
		Objects.requireNonNull(seq, "Must provide a non-null CharSequence");
		write(seq, 0, seq.length());
	}

	@Override
	public synchronized void write(char[] b, int off, int len) throws IOException {
		if (this.closed) { throw new IOException("This buffer is closed"); }
		// First, a little parameter QA
		Objects.requireNonNull(b, "Must provide a non-null char[]");
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
			char[] chunk = getWritableChunk();
			// p is guaranteed to be a valid integer, since it's modulated by an integer value
			// so it's safe to cast the number
			long p = this.wpos % this.chunkSize;
			int remainder = Math.min(this.chunkSize - (int) p, len);
			System.arraycopy(b, off, chunk, (int) p, remainder);
			len -= remainder;
			off += remainder;
			this.wpos += remainder;
			notify();
		}
	}

	public synchronized void write(CharSequence seq, int off, int len) throws IOException {
		if (this.closed) { throw new IOException("This buffer is closed"); }
		Objects.requireNonNull(seq, "Must provide a non-null CharSequence");
		if (len < 0) { throw new IllegalArgumentException(String.format("Cannot copy negative lengths (%d)", len)); }
		if (off < 0) {
			throw new IllegalArgumentException(String.format("Cannot copy from a negative offset (%d)", off));
		}
		if (seq.length() < (off + len)) {
			throw new IllegalArgumentException(
				String.format("The given offset (%d) and length (%d) exceed the size of the given CharSequence (%d)",
					off, len, seq.length()));
		}
		// Take a shortcut to avoid work
		if (len == 0) { return; }

		// Use a 1-character buffer to copy the thing over and re-use existing, tested code
		char[] buf = new char[1];
		for (int i = 0; i < len; i++) {
			buf[0] = seq.charAt(i + off);
			write(buf);
		}
	}

	public final CharSequence getCharSequence() {
		return new TextMemoryBufferCharSequence();
	}

	public final TextMemoryBufferReader getReader() {
		return new TextMemoryBufferReader();
	}

	@Override
	public synchronized void close() {
		this.closed = true;
		notify();
	}

	@Override
	public void flush() throws IOException {
	}
}
