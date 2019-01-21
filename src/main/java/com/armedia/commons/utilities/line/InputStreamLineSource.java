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
		this.in = new LineNumberReader(
			new InputStreamReader(Objects.requireNonNull(in, "Must provide a non-null InputStream instance"),
				Tools.coalesce(charset, Charset.defaultCharset())));
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