/*******************************************************************************
 * #%L
 * Armedia Caliente
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
 *******************************************************************************/
package com.armedia.commons.utilities.line;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

import com.armedia.commons.utilities.Tools;

public class InputStreamLineSource extends LineSource {

	private final LineNumberReader in;
	private final boolean close;

	public InputStreamLineSource(String id, InputStream in) {
		this(id, in, null, true);
	}

	public InputStreamLineSource(String id, InputStream in, Charset charset) {
		this(id, in, charset, true);

	}

	public InputStreamLineSource(String id, InputStream in, boolean closeWhenDone) {
		this(id, in, null, closeWhenDone);
	}

	public InputStreamLineSource(String id, InputStream in, Charset charset, boolean closeWhenDone) {
		super(id);
		InputStreamReader r = new InputStreamReader(Objects.requireNonNull(in, "Must provide a non-null InputStream instance"),
        	Tools.coalesce(charset, Charset.defaultCharset()));
        this.in = new LineNumberReader(r);
		this.close = closeWhenDone;
	}

	@Override
	public Iterable<String> load() throws LineSourceException {
		return new Iterable<String>() {
			final Stream<String> stream = InputStreamLineSource.this.in.lines();

			@Override
			public Iterator<String> iterator() {
				return this.stream.iterator();
			}
		};
	}

	@Override
	public void close() throws IOException {
		if (this.close) {
			this.in.close();
		}
	}
}
