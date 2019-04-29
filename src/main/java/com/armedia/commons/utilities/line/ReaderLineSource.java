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