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
	private final boolean supportsRecursion;

	protected LineSource(String id) {
		this(id, true, true);
	}

	protected LineSource(String id, boolean supportsContinuation) {
		this(id, supportsContinuation, true);
	}

	protected LineSource(String id, boolean supportsContinuation, boolean supportsRecursion) {
		this.id = StringUtils.strip(Objects.requireNonNull(id, "Must provide a non-null ID string"));
		if (StringUtils.isEmpty(id)) {
			throw new IllegalArgumentException("The ID string may not be an empty or blank string");
		}
		this.supportsContinuation = supportsContinuation;
		this.supportsRecursion = supportsRecursion;
	}

	public final boolean isSupportsContinuation() {
		return this.supportsContinuation;
	}

	public final boolean isSupportsRecursion() {
		return this.supportsRecursion;
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