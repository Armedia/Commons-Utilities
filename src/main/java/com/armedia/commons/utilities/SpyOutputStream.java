package com.armedia.commons.utilities;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Consumer;

import com.armedia.commons.utilities.concurrent.BaseMutexLockable;
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
		this.lock.mutexLocked(() -> {
			if (this.closed) { throw new IOException("This stream is already closed"); }
		});
	}

	@Override
	public void write(int c) throws IOException {
		this.lock.mutexLocked(() -> {
			assertOpen();
			byte[] buf = new byte[1];
			buf[0] = (byte) c;
			write(buf, 0, buf.length);
		});
	}

	@Override
	public void write(byte[] b) throws IOException {
		Objects.requireNonNull(b, "Must provide the data to write out");
		this.lock.mutexLocked(() -> {
			assertOpen();
			write(b, 0, b.length);
		});
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		Objects.requireNonNull(b, "Must provide the data to write out");
		this.lock.mutexLocked(() -> {
			assertOpen();
			ByteBuffer buf = ByteBuffer.wrap(b.clone()).asReadOnlyBuffer();
			super.write(b, off, len);
			try {
				this.spy.accept(buf);
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