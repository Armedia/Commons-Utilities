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

	protected LineSource(String id) {
		this.id = StringUtils.strip(Objects.requireNonNull(id, "Must provide a non-null ID string"));
		if (StringUtils.isEmpty(id)) {
			throw new IllegalArgumentException("The ID string may not be an empty or blank string");
		}
	}

	public final String getId() {
		return this.id;
	}

	public abstract Iterable<String> load() throws LineSourceException;

	@Override
	public void close() throws Exception {
		// Do nothing...
	}
}