package com.armedia.commons.utilities.line;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * Produces the raw lines of text, without trimming or continuation, as read from whatever character
 * stream backs it.
 * </p>
 *
 * @author diego
 *
 */
public abstract class LineSource implements AutoCloseable {

	private final String id;
	private final boolean supportsContinuation;

	protected LineSource(String id) {
		this(id, true);
	}

	protected LineSource(String id, boolean supportsContinuation) {
		this.id = StringUtils.strip(Objects.requireNonNull(id, "Must provide a non-null ID string"));
		if (StringUtils.isEmpty(id)) {
			throw new IllegalArgumentException("The ID string may not be an empty or blank string");
		}
		this.supportsContinuation = supportsContinuation;
	}

	public final boolean isSupportsContinuation() {
		return this.supportsContinuation;
	}

	public final String getId() {
		return this.id;
	}

	public abstract Iterable<String> load() throws LineSourceException;

	@Override
	public void close() throws Exception {
		// Do nothing...
	}

	public static LineSource wrap(String id, Iterable<String> strings) {
		return LineSource.wrap(id, strings, false);
	}

	public static LineSource wrap(String id, final Iterable<String> strings, boolean supportsContinuation) {
		Objects.requireNonNull(id, "Must provide a non-null ID string");
		Objects.requireNonNull(strings, "Must provide a non-null Iterable<String> instance");
		return new LineSource(id, supportsContinuation) {
			@Override
			public Iterable<String> load() throws LineSourceException {
				return strings;
			}
		};
	}
}