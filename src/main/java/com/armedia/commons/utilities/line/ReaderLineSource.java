/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (c) 2010 - 2019 Armedia LLC
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
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

public class ReaderLineSource extends LineSource {

	private final LineNumberReader in;
	private final boolean close;

	public ReaderLineSource(String id, Reader in) {
		this(id, in, true);
	}

	public ReaderLineSource(String id, Reader in, boolean closeWhenDone) {
		super(id);
		this.in = new LineNumberReader(Objects.requireNonNull(in, "Must provide a non-null Reader instance"));
		this.close = closeWhenDone;
	}

	@Override
	public Iterable<String> load() throws LineSourceException {
		return new Iterable<String>() {
			final Stream<String> stream = ReaderLineSource.this.in.lines();

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
