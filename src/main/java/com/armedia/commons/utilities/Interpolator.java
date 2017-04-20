package com.armedia.commons.utilities;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Interpolator {

	public static class ExpressionException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		private final int lineNumber;
		private final int column;
		private final String line;
		private final String expression;
		private final String error;

		public ExpressionException(int lineNumber, int column, String line, String expression, String error) {
			this.lineNumber = lineNumber;
			this.column = column;
			this.line = line;
			this.expression = expression;
			this.error = error;
		}

		public ExpressionException(int lineNumber, int column, String line, String expression, String error,
			Throwable thrown) {
			super(thrown);
			this.lineNumber = lineNumber;
			this.column = column;
			this.line = line;
			this.expression = expression;
			this.error = error;
		}

		public int getLineNumber() {
			return this.lineNumber;
		}

		public int getColumn() {
			return this.column;
		}

		public String getLine() {
			return this.line;
		}

		public String getExpression() {
			return this.expression;
		}

		public String getError() {
			return this.error;
		}
	}

	public static enum FailMode {
		//
		/**
		 * No failures will be reported, the original string will be left intact
		 */
		NONE,

		/**
		 * Only fail if an exception is raised during {@link Interpolator#substitute(String)}
		 */
		EXCEPTION,

		/**
		 * Fail with an {@link ExpressionException} if any error results from
		 * {@link Interpolator#substitute(String)}
		 */
		ALL
		//
		;
	}

	public static final String NEWLINE = String.format("%n");
	public static final FailMode DEFAULT_FAIL = FailMode.EXCEPTION;
	public static final String DEFAULT_PREFIX = "@@[";
	public static final String DEFAULT_SUFFIX = "]@@";

	private final String prefixStr;
	private final Pattern prefix;
	private final String suffixStr;
	private final Pattern suffix;
	private final FailMode failMode;

	public Interpolator() {
		this(Interpolator.DEFAULT_FAIL, Interpolator.DEFAULT_PREFIX, Interpolator.DEFAULT_SUFFIX);
	}

	public Interpolator(FailMode failMode, String prefix, String suffix) {
		this.failMode = failMode;
		this.prefixStr = prefix;
		this.prefix = Pattern.compile(String.format("\\Q%s\\E", prefix));
		this.suffixStr = suffix;
		this.suffix = Pattern.compile(String.format("\\Q%s\\E", suffix));
	}

	public final String getPrefix() {
		return this.prefixStr;
	}

	public final String getSuffix() {
		return this.suffixStr;
	}

	public final FailMode getFailMode() {
		return this.failMode;
	}

	/**
	 * Performs syntax and validity evaluation on the given expression, returning {@code true} if
	 * the expression is syntactically valid, or {@code false} otherwise.
	 *
	 * @param expression
	 *            the expression to check
	 * @return {@code true} if the expression is syntactically valid, or {@code false} otherwise.
	 */
	protected boolean isExpressionValid(String expression) {
		return ((expression != null) && !expression.isEmpty());
	}

	/**
	 * Resolve the given expression, resulting in either a textual value to substitute in its stead,
	 * a {@code null} value indicating that the expression resolved to a {@code null} value (i.e.
	 * missing variable, bad object field name, etc.), or any type of exception raising additional
	 * errors such as syntax errors or naming issues.
	 *
	 * @param expression
	 *            the expression to find a substitution for
	 * @return the value to substitute the expression with, or {@code null} if there is no valid
	 *         substitution but the expression's syntax is valid
	 * @throws Exception
	 *             if there is an expression syntax issue, or if there are any other errors during
	 *             value resolution
	 */
	protected abstract String substitute(String expression) throws Exception;

	public String interpolate(String string) {
		// Should we instead explode? For now, just return null...
		if (string == null) { return null; }

		StringWriter w = new StringWriter();
		try {
			interpolate(new StringReader(string), w);
		} catch (IOException e) {
			// This doesn't happen...but still raise something if it does
			throw new RuntimeException("Unexpected IOException while writing in memory", e);
		} finally {
			try {
				w.close();
			} catch (IOException e) {
				// Do nothing...
			}
		}
		return w.toString();
	}

	public void interpolate(InputStream in, OutputStream out) throws IOException {
		interpolate(in, out, Charset.defaultCharset());
	}

	public void interpolate(InputStream in, OutputStream out, String encoding) throws IOException, ExpressionException {
		if (in == null) { throw new IllegalArgumentException("Must provide a valid InputStream"); }
		if (out == null) { throw new IllegalArgumentException("Must provide a valid OutputStream"); }
		interpolate(in, out, (encoding != null ? Charset.forName(encoding) : Charset.defaultCharset()));
	}

	public void interpolate(InputStream in, OutputStream out, Charset charset) throws IOException {
		if (in == null) { throw new IllegalArgumentException("Must provide a valid reader"); }
		if (out == null) { throw new IllegalArgumentException("Must provide a valid writer"); }
		if (charset == null) {
			charset = Charset.defaultCharset();
		}
		Reader r = new InputStreamReader(in, charset);
		Writer w = new OutputStreamWriter(out, charset);
		interpolate(r, w);
	}

	public void interpolate(Reader in, Writer out) throws IOException {
		if (in == null) { throw new IllegalArgumentException("Must provide a valid Reader"); }
		if (out == null) { throw new IllegalArgumentException("Must provide a valid Writer"); }
		final LineNumberReader r = new LineNumberReader(in);
		out = new BufferedWriter(out);
		boolean first = true;
		nextLine: for (;;) {
			final String line = r.readLine();
			if (line == null) {
				// We're done, break the loop
				out.flush();
				return;
			}

			if (!first) {
				// Write out a line terminator for the previous line,
				// but only from the 2nd line onward
				out.write(Interpolator.NEWLINE);
			}
			first = false;

			final int lineNumber = r.getLineNumber();
			final Matcher prefixMatcher = this.prefix.matcher(line);
			final Matcher suffixMatcher = this.suffix.matcher(line);

			int searchPoint = 0;
			while (prefixMatcher.find(searchPoint)) {
				final int column = prefixMatcher.start();

				if (column > searchPoint) {
					// Write out the static stuff
					out.write(line.substring(searchPoint, column));
					out.flush();
				}

				// Find the next suffix after the prefix
				if (!suffixMatcher.find(prefixMatcher.end())) {
					// Copy the rest of the line, and move on to the next line
					out.write(line.substring(column));
					out.flush();
					continue nextLine;
				}

				// We have a start and an end, so we extract whatever's in between
				final String expression = line.substring(prefixMatcher.end(), suffixMatcher.start());

				String result = null;
				if (!isExpressionValid(expression)) {
					switch (this.failMode) {
						case ALL:
							throw new ExpressionException(lineNumber, column, line, expression,
								"The expression syntax is invalid");

						case EXCEPTION: // Fall-through
						case NONE:
							break;
					}
				} else {
					try {
						result = substitute(expression);
					} catch (Exception e) {
						switch (this.failMode) {
							case ALL: // Fall-through
							case EXCEPTION:
								throw new ExpressionException(lineNumber, column, line, expression,
									"An exception was raised while evaluating the expression", e);

							case NONE:
								// No match, so we leave the expression intact
								break;
						}
					}
				}

				// If there is no result (i.e. it's null) then we MUST substitute the original
				// expression with prefix/suffix attached, since if the user wanted anything else
				// we'd have raised an exception by now and this bit wouldn't be executed
				if (result == null) {
					switch (this.failMode) {
						case ALL:
							throw new ExpressionException(lineNumber, column, line, expression,
								"The expression did not resolve to any valid value (substitute() returned null)");

						case EXCEPTION: // Fall-through
						case NONE:
							// No match, so we leave the original expression intact
							result = line.substring(prefixMatcher.start(), suffixMatcher.end());
							break;
					}
				}
				out.write(result);
				out.flush();

				// Advance the "parse point" to just after the end of the suffix
				searchPoint = suffixMatcher.end();
				if (searchPoint >= line.length()) {
					// If the new search point is at or beyond the end of the line, we're done with
					// this line and we simply loop back
					continue nextLine;
				}
			}

			// If it didn't find a next prefix, we move on
			out.write(line.substring(searchPoint));
		}
	}
}