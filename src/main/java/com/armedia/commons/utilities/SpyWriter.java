package com.armedia.commons.utilities;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.Objects;
import java.util.function.Consumer;

import com.armedia.commons.utilities.concurrent.BaseMutexLockable;
import com.armedia.commons.utilities.concurrent.MutexLockable;

public class SpyWriter extends FilterWriter {

	private static final Consumer<Writer> NOOP = (o) -> {
	};

	private final MutexLockable lock = new BaseMutexLockable();
	private final Consumer<CharBuffer> spy;
	private final Consumer<Writer> closer;

	private volatile boolean closed = false;

	public SpyWriter(Writer out, Consumer<CharBuffer> spy) {
		this(out, spy, null);
	}

	public SpyWriter(Writer out, Consumer<CharBuffer> spy, Consumer<Writer> closer) {
		super(Objects.requireNonNull(out, "Must provide an Writer to spy on"));
		this.spy = Objects.requireNonNull(spy, "Must provide a consumer to spy with");
		this.closer = Tools.coalesce(closer, SpyWriter.NOOP);
	}

	private void assertOpen() throws IOException {
		this.lock.mutexLocked(() -> {
			if (this.closed) { throw new IOException("This stream is already closed"); }
		});
	}

	@Override
	public SpyWriter append(CharSequence csq) throws IOException {
		super.append(csq);
		return this;
	}

	@Override
	public SpyWriter append(CharSequence csq, int start, int end) throws IOException {
		super.append(csq, start, end);
		return this;
	}

	@Override
	public SpyWriter append(char c) throws IOException {
		super.append(c);
		return this;
	}

	@Override
	public void write(int c) throws IOException {
		this.lock.mutexLocked(() -> {
			assertOpen();
			char[] buf = new char[1];
			buf[0] = (char) c;
			write(buf, 0, buf.length);
		});
	}

	@Override
	public void write(char[] b) throws IOException {
		Objects.requireNonNull(b, "Must provide the data to write out");
		this.lock.mutexLocked(() -> {
			assertOpen();
			write(b, 0, b.length);
		});
	}

	@Override
	public void write(char[] b, int off, int len) throws IOException {
		Objects.requireNonNull(b, "Must provide the data to write out");
		this.lock.mutexLocked(() -> {
			assertOpen();
			final CharBuffer buf = CharBuffer.wrap(b).asReadOnlyBuffer();
			super.write(b, off, len);
			try {
				this.spy.accept(buf);
			} catch (Throwable t) {
				// Do nothing... ignore the problem
			}
		});
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		Objects.requireNonNull(str, "Must provide the data to write out");
		this.lock.mutexLocked(() -> {
			super.write(str, off, len);
			try {
				this.spy.accept(CharBuffer.wrap(str).asReadOnlyBuffer());
			} catch (Throwable t) {
				// Do nothing... ignore the problem
			}
		});
	}

	@Override
	public void close() throws IOException {
		this.lock.mutexLocked(() -> {
			assertOpen();
			try {
				this.closer.accept(this.out);
				super.close();
			} finally {
				this.closed = true;
			}
		});
	}
}