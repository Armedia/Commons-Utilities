package com.armedia.commons.utilities.line;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.armedia.commons.utilities.CloseableIterator;
import com.armedia.commons.utilities.Tools;
import com.armedia.commons.utilities.line.LineIteratorConfig.Feature;

public class LineIterator extends CloseableIterator<String> {

	protected static final Pattern CONTINUATION = Pattern.compile("(?<!\\\\)(?:\\\\{2})*\\\\$");
	private static final String ROOT_ID = "<ROOT>";

	public static final LineIterator NULL_ITERATOR = new LineIterator(null, null, Collections.emptyList());

	private static final LineSource NULL_SOURCE = LineSource.wrap(LineIterator.ROOT_ID, Collections.emptyList());

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

	private class State implements Iterator<Line> {
		private LineSource source = null;
		private Iterator<Line> it = null;

		private State(LineSource source) throws LineSourceException {
			this.source = source;
			this.it = preprocess(this.source).iterator();
		}

		@Override
		public boolean hasNext() {
			return this.it.hasNext();
		}

		@Override
		public Line next() {
			return this.it.next();
		}
	}

	private final Stack<State> stack = new Stack<>();
	private final Set<String> visited = new LinkedHashSet<>();

	private final Map<String, Collection<Line>> cache = new HashMap<>();

	private final Collection<LineSourceFactory> factories;
	private final LineIteratorConfig config = new LineIteratorConfig();
	private final LineSource root;

	private Function<String, String> transformer = Function.identity();

	LineIterator(Collection<LineSourceFactory> factories, LineIteratorConfig config, Iterable<String> root) {
		this(factories, config,
			(root != null) ? LineSource.wrap(LineIterator.ROOT_ID, root) : LineIterator.NULL_SOURCE);
	}

	LineIterator(Collection<LineSourceFactory> factories, LineIteratorConfig config, LineSource root) {
		if (factories != null) {
			List<LineSourceFactory> f = new LinkedList<>(factories);
			f.removeIf(Objects::isNull);
			this.factories = Tools.freezeList(f);
		} else {
			this.factories = Collections.emptyList();
		}
		this.config.copyFrom(config);
		this.root = Tools.coalesce(root, LineIterator.NULL_SOURCE);
	}

	public final Collection<LineSourceFactory> getSourceFactories() {
		return this.factories;
	}

	public final LineIteratorConfig getConfig() {
		return this.config;
	}

	public final Function<String, String> getTransformer() {
		return this.transformer;
	}

	public final void setTransformer(Function<String, String> transformer) {
		this.transformer = Tools.coalesce(transformer, Function.identity());
	}

	private LineSource getLineSource(final Line line) throws LineSourceException {
		final String cleanLine = line.str.replaceAll("^\\s*@", "");
		for (LineSourceFactory f : this.factories) {
			try {
				LineSource ls = f.newInstance(cleanLine, line.source);
				if (ls != null) { return ls; }
			} catch (Exception e) {
				throw new LineSourceException(
					String.format("Failed to read the lines from [%s] (referenced from [%s], line # %d)", line.str,
						line.source.getId(), line.position),
					e);
			}
		}
		return null;
	}

	private boolean isContinued(String line) {
		// Ends on an uneven number of backslashes
		return this.config.hasFeature(Feature.CONTINUATION) && LineIterator.CONTINUATION.matcher(line).find();
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

	private boolean shouldRecurse(String line) {
		if (!isRecursion(line)) { return false; }
		final int maxDepth = this.config.getMaxDepth();
		return ((maxDepth < 0) || (this.stack.size() < (maxDepth + 1)));
	}

	private boolean isContinuedNewlines() {
		return this.config.hasFeature(Feature.CONTINUED_NEWLINES);
	}

	private Collection<Line> preprocess(LineSource ls) throws LineSourceException {
		Collection<Line> result = this.cache.get(ls.getId());
		if (result == null) {
			result = new LinkedList<>();

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
					// Add a newline
					if (isContinuedNewlines()) {
						rawLine = String.format("%s%n", rawLine);
					}
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
					}
				}
			}
			this.cache.put(ls.getId(), Tools.freezeCollection(result));
		}
		return result;
	}

	private Line findNextLine() throws LineSourceException {
		if (this.stack.isEmpty()) {
			// This only happens once...
			this.stack.push(new State(this.root));
			this.visited.add(this.root.getId());
		}

		State state = null;
		for (;;) {
			state = this.stack.peek();
			if (state.hasNext()) {
				// This state is still valid, so continue...
				break;
			}
			// This state is no longer valid, so remove it...
			this.stack.pop(); // remove the state we just peeked
			this.visited.remove(state.source.getId());

			if (this.stack.isEmpty()) {
				// No more states, so return null to end the iterator
				return null;
			}
		}

		Line line = state.next();
		if (!shouldRecurse(line.str)) { return line; }
		LineSource source = getLineSource(line);
		if (source == null) {
			// No line source found ... can't recurse
			throw new LineSourceException(String.format("Bad recursion - can't resolve [%s] (from [%s], line %d)",
				line.str, line.source.getId(), line.position));
		}
		if (!this.visited.add(source.getId())) {
			throw new LineSourceException(String.format(
				"Recursion loop detected - source [%s] is recursed into twice (second time from [%s], line %d): %s",
				line.str, line.source.getId(), line.position, this.visited));
		}

		// We have a line source!! We recurse!
		state = new State(source);
		this.stack.push(state);
		// Recurse!!!
		return findNextLine();
	}

	@Override
	protected Result findNext() throws Exception {
		Line nextLine = findNextLine();
		return (nextLine != null ? found(this.transformer.apply(nextLine.str)) : null);
	}

	@Override
	protected void doClose() throws Exception {
	}
}