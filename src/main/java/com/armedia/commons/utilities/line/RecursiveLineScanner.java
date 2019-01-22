package com.armedia.commons.utilities.line;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.armedia.commons.utilities.Tools;
import com.armedia.commons.utilities.line.LineScannerConfig.Feature;

class RecursiveLineScanner {

	protected static final Pattern CONTINUATION = Pattern.compile("(?<!\\\\)(?:\\\\{2})*\\\\$");

	private final Set<String> visited = new LinkedHashSet<>();
	private final Map<String, AtomicLong> counters = new LinkedHashMap<>();
	private final Map<String, Collection<Line>> cache = new HashMap<>();

	private final Collection<LineSourceFactory> factories;
	private final LineScannerConfig config;

	private static class Line {
		private final LineSource source;
		private final long position;
		private final String str;

		private Line(LineSource source, long position, String str) {
			this.source = source;
			this.position = position;
			this.str = str;
		}
	}

	public RecursiveLineScanner(Collection<LineSourceFactory> factories, LineScannerConfig config) {
		this.factories = factories;
		this.config = config;
	}

	public final Set<String> getVisited() {
		return this.visited;
	}

	public final Collection<LineSourceFactory> getFactories() {
		return this.factories;
	}

	public final Map<String, AtomicLong> getCounters() {
		return this.counters;
	}

	public final LineScannerConfig getConfig() {
		return this.config;
	}

	private LineSource getLineSource(final Line line) throws IOException, LineProcessorException {
		for (LineSourceFactory f : this.factories) {
			try {
				LineSource ls = f.newInstance(line.str.replaceAll("^\\s*@", ""), line.source);
				if (ls != null) { return ls; }
			} catch (LineSourceException e) {
				throw new LineProcessorException(
					String.format("Failed to read the lines from [%s] (referenced from [%s], line # %d)", line.str,
						line.source.getId(), line.position),
					e);
			}
		}
		return null;
	}

	private boolean isContinued(String line) {
		// Ends on an uneven number of backslashes
		return this.config.hasFeature(Feature.CONTINUATION) && RecursiveLineScanner.CONTINUATION.matcher(line).find();
	}

	private String applyTrim(String s) {
		return this.config.getTrim().apply(s);
	}

	private boolean isIgnoreLine(String s) {
		return this.config.hasFeature(Feature.IGNORE_EMPTY_LINES) && StringUtils.isEmpty(s);
	}

	private boolean isComment(String str) {
		return this.config.hasFeature(Feature.COMMENTS) && str.matches("^\\s*#.*$");
	}

	private boolean isRecursion(String str) {
		return this.config.hasFeature(Feature.RECURSION) && str.matches("^\\s*@.*$");
	}

	private boolean shouldRecurse(String line, int nextDepth) {
		if (!this.config.hasFeature(Feature.RECURSION)) { return false; }
		if (!line.matches("^\\s*@.*$")) { return false; }
		final int maxDepth = this.config.getMaxDepth();
		return ((maxDepth < 0) || (nextDepth < maxDepth));
	}

	private Iterable<Line> preprocess(LineSource ls) throws LineSourceException {
		Collection<Line> result = this.cache.get(ls.getId());
		if (result == null) {
			AtomicLong counter = this.counters.get(ls.getId());
			if (counter == null) {
				counter = new AtomicLong(0);
				this.counters.put(ls.getId(), counter);
			}

			result = new LinkedList<>();
			this.cache.put(ls.getId(), result);

			// Rule 1: if the last character in a line is a backslash, but it's not preceded by
			// a backslash, then the next line is part of this line
			// Rule 2: "@" as a recursion marker is only valid if preceded by spaces, and if the
			// line is not a continuation
			// Rule 3: "#" as a comment marker is only valid if preceded only by spaces, and if
			// the line is not a continuation

			StringBuilder line = new StringBuilder();
			boolean continuing = false;

			long pos = 0;
			for (String rawLine : ls.load()) {
				++pos;
				// We protect against null lines, just to be robust and safe
				rawLine = Tools.coalesce(rawLine, StringUtils.EMPTY);

				// Are we continuing further?
				if (ls.isSupportsContinuation() && isContinued(rawLine)) {
					// Remove the last character, which is a backslash
					rawLine = rawLine.substring(0, rawLine.length() - 1);
					// Add a newline (?)
					rawLine = String.format("%s%n", rawLine);
					continuing = true;
				}

				line.append(rawLine);

				if (continuing) {
					continuing = false;
					continue;
				}

				String finalLine = applyTrim(line.toString());
				line.setLength(0);
				if (!isComment(finalLine)) {
					// It's not a comment, so it's an "actionable" line
					if (!isIgnoreLine(finalLine)) {
						result.add(new Line(ls, pos, finalLine));
						if (!isRecursion(finalLine)) {
							// If it's not a recursion request, then count it...
							counter.incrementAndGet();
						}
					}
				}
			}
		}
		return result;
	}

	private final boolean process(final Function<String, Boolean> processor, final LineSource source,
		final boolean closeAfterRead, final int depth) throws IOException, LineProcessorException, LineSourceException {

		if (!this.visited.add(source.getId())) {
			// Recursion loop!!
			throw new LineProcessorException(
				String.format("The line source [%s] has already been visited: %s", source.getId(), this.visited));
		}

		Iterable<Line> lines = preprocess(source);
		if (closeAfterRead) {
			try {
				source.close();
			} catch (Exception e) {
				// Ignore?
			}
		}

		for (Line line : lines) {

			// Is this a recursion? Are we clear to recurse?
			if (shouldRecurse(line.str, depth + 1)) {
				// Possible recursion!!
				LineSource recursor = getLineSource(line);
				if (recursor != null) {
					// A recursion spec!! Recurse!!
					if (!process(processor, recursor, true, depth + 1)) { return false; }
					continue;
				}
			}

			// This is not a recursion request, so we simply process it as a normal line... ?

			// Not a recursion spec - process it!
			Boolean result = processor.apply(line.str);
			if ((result != null) && !result.booleanValue()) {
				// We've been asked to stop!!! so stop!
				return false;
			}

			// We're done with this visit, so remove it...
		}
		this.visited.remove(source.getId());
		return true;
	}

	final boolean process(final Function<String, Boolean> processor, Iterable<String> source)
		throws IOException, LineProcessorException, LineSourceException {
		return process(processor, LineSource.wrap("<ROOT>", source, false));
	}

	final boolean process(final Function<String, Boolean> processor, LineSource root)
		throws IOException, LineProcessorException, LineSourceException {
		return process(Objects.requireNonNull(processor, "Must provide a non-null Processor instance to process with"),
			Objects.requireNonNull(root, "Must provide a non-null LineSource instance to process from"), false, 0);
	}
}