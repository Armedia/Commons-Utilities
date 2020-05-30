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

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.Objects;
import java.util.function.Consumer;

import com.armedia.commons.utilities.Tools;

public class SpyWriter extends FilterWriter {

	private static final Consumer<Writer> NOOP = (o) -> {
	};

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
		if (this.closed) { throw new IOException("This stream is already closed"); }
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
		assertOpen();
		char[] buf = new char[1];
		buf[0] = (char) c;
		write(buf, 0, buf.length);
	}

	@Override
	public void write(char[] b) throws IOException {
		Objects.requireNonNull(b, "Must provide the data to write out");
		assertOpen();
		write(b, 0, b.length);
	}

	@Override
	public void write(char[] b, int off, int len) throws IOException {
		Objects.requireNonNull(b, "Must provide the data to write out");
		assertOpen();
		final CharBuffer buf = CharBuffer.wrap(b).asReadOnlyBuffer();
		super.write(b, off, len);
		try {
			this.spy.accept(buf);
		} catch (Throwable t) {
			// Do nothing... ignore the problem
		}
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		Objects.requireNonNull(str, "Must provide the data to write out");
		super.write(str, off, len);
		try {
			this.spy.accept(CharBuffer.wrap(str).asReadOnlyBuffer());
		} catch (Throwable t) {
			// Do nothing... ignore the problem
		}
	}

	@Override
	public void close() throws IOException {
		assertOpen();
		try {
			this.closer.accept(this.out);
			super.close();
		} finally {
			this.closed = true;
		}
	}
}
