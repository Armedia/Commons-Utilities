/*******************************************************************************
 * #%L
 * Armedia Commons Utilities
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
package com.armedia.commons.utilities;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;

import com.armedia.commons.utilities.concurrent.BaseShareableLockable;
import com.armedia.commons.utilities.concurrent.MutexAutoLock;
import com.armedia.commons.utilities.concurrent.SharedAutoLock;

public class EnumeratedCounter<T extends Enum<T>, R extends Enum<R>> extends BaseShareableLockable {

	private static final String TOTAL_LABEL = "processed".intern();
	private static final String NEW_LINE = String.format("%n");

	private final Map<T, Map<R, AtomicLong>> counters;
	private final Map<R, AtomicLong> cummulative;
	private final Class<T> tClass;
	private final Class<R> rClass;
	private final String formatString;

	public EnumeratedCounter(Class<T> tClass, Class<R> rClass) {
		if (tClass == null) { throw new IllegalArgumentException("Must provide an enum class for the object type"); }
		if (rClass == null) { throw new IllegalArgumentException("Must provide an enum class for the result"); }
		this.rClass = rClass;
		this.tClass = tClass;

		int maxWidth = 0;
		Map<R, AtomicLong> cummulative = new EnumMap<>(rClass);
		for (R result : this.rClass.getEnumConstants()) {
			cummulative.put(result, new AtomicLong(0));
			maxWidth = Math.max(maxWidth, result.name().length());
		}
		this.cummulative = Collections.unmodifiableMap(cummulative);

		Map<T, Map<R, AtomicLong>> counters = new EnumMap<>(tClass);
		for (T type : tClass.getEnumConstants()) {
			Map<R, AtomicLong> results = new EnumMap<>(rClass);
			for (R result : this.rClass.getEnumConstants()) {
				results.put(result, new AtomicLong(0));
			}
			counters.put(type, Collections.unmodifiableMap(results));
		}
		this.counters = Collections.unmodifiableMap(counters);

		maxWidth = Math.max(maxWidth, EnumeratedCounter.TOTAL_LABEL.length());
		this.formatString = String.format("%%s objects %%-%ds: %%6d%%n", maxWidth);
	}

	public final long increment(T type, R result) {
		if (type == null) { throw new IllegalArgumentException("Unsupported null object type"); }
		if (result == null) { throw new IllegalArgumentException("Must provide a valid result to count for"); }
		try (SharedAutoLock lock = autoSharedLock()) {
			AtomicLong counter = getLiveCounters(type).get(result);
			final long ret = counter.incrementAndGet();
			this.cummulative.get(result).incrementAndGet();
			return ret;
		}
	}

	private Map<R, AtomicLong> getLiveCounters(T type) {
		return (type != null ? this.counters.get(type) : this.cummulative);
	}

	public final Map<R, Long> getCounters(T type) {
		Map<R, Long> ret = new EnumMap<>(this.rClass);
		Map<R, AtomicLong> m = getLiveCounters(type);
		try (MutexAutoLock lock = autoMutexLock()) {
			for (Map.Entry<R, AtomicLong> e : m.entrySet()) {
				ret.put(e.getKey(), e.getValue().get());
			}
			return Collections.unmodifiableMap(ret);
		}
	}

	public final Map<T, Map<R, Long>> getCounters() {
		Map<T, Map<R, Long>> ret = new EnumMap<>(this.tClass);
		try (MutexAutoLock lock = autoMutexLock()) {
			for (T t : this.counters.keySet()) {
				ret.put(t, getCounters(t));
			}
			return Collections.unmodifiableMap(ret);
		}
	}

	public final Map<R, Long> getCummulative() {
		return getCounters(null);
	}

	public final Map<R, Long> reset(T type) {
		Map<R, Long> ret = new EnumMap<>(this.rClass);
		Map<R, AtomicLong> m = getLiveCounters(type);
		try (MutexAutoLock lock = autoMutexLock()) {
			for (Map.Entry<R, AtomicLong> e : m.entrySet()) {
				final R r = e.getKey();
				final long val = e.getValue().getAndSet(0);
				ret.put(r, val);
				this.cummulative.get(r).addAndGet(-val);
			}
			return Collections.unmodifiableMap(ret);
		}
	}

	public final Map<T, Map<R, Long>> reset() {
		Map<T, Map<R, Long>> ret = new EnumMap<>(this.tClass);
		try (MutexAutoLock lock = autoMutexLock()) {
			for (T t : this.tClass.getEnumConstants()) {
				ret.put(t, reset(t));
			}
			return Collections.unmodifiableMap(ret);
		}
	}

	public final String generateCummulativeReport() {
		return generateCummulativeReport(0);
	}

	public final String generateCummulativeReport(int indentLevel) {
		return generateReport(this.cummulative, indentLevel, "Total");
	}

	public final String generateReport(T type) {
		return generateReport(type, 0);
	}

	public final String generateFullReport(int indentlevel) {
		try (MutexAutoLock lock = autoMutexLock()) {
			StringBuilder buf = new StringBuilder();
			for (T type : this.tClass.getEnumConstants()) {
				if (buf.length() > 0) {
					buf.append(EnumeratedCounter.NEW_LINE).append(EnumeratedCounter.NEW_LINE);
				}
				buf.append(generateReport(type, indentlevel));
			}
			return buf.toString();
		}
	}

	public final String generateReport(T type, int indentLevel) {
		if (type == null) { throw new IllegalArgumentException("Unsupported null object type"); }
		return generateReport(getLiveCounters(type), indentLevel, String.format("Number of %s", type));
	}

	private final String generateReport(Map<R, AtomicLong> results, int indentLevel, String entryLabel) {
		return mutexLocked(() -> EnumeratedCounter.generateSummary(this.rClass, results, indentLevel, entryLabel,
			EnumeratedCounter.TOTAL_LABEL, this.formatString));
	}

	private static <E extends Enum<E>> String calculateFormatString(Class<E> klass, String totalLabel) {
		int maxWidth = 0;
		for (E e : klass.getEnumConstants()) {
			maxWidth = Math.max(maxWidth, e.name().length());
		}
		maxWidth = Math.max(maxWidth, totalLabel.length());
		return String.format("%%s objects %%-%ds: %%12d%%n", maxWidth);
	}

	public static <E extends Enum<E>> String generateSummary(Class<E> klass, Map<E, ? extends Number> results,
		int indentLevel, String entryLabel, String totalLabel) {
		return EnumeratedCounter.generateSummary(klass, results, indentLevel, entryLabel, totalLabel, null);
	}

	public static <E extends Enum<E>> String generateSummary(Class<E> klass, Map<E, ? extends Number> results,
		int indentLevel, String entryLabel, String totalLabel, String formatString) {

		if (formatString == null) {
			formatString = EnumeratedCounter.calculateFormatString(klass, totalLabel);
		}

		StringBuilder s = new StringBuilder();
		if (indentLevel < 0) {
			indentLevel = 0;
		}
		for (int i = 0; i < indentLevel; i++) {
			s.append('\t');
		}
		final String indent = s.toString();
		s.setLength(0);
		long total = 0;
		for (Map.Entry<E, ? extends Number> e : results.entrySet()) {
			final E r = e.getKey();
			final Number i = e.getValue();
			long longValue = i.longValue();
			total += longValue;
			s.append(indent).append(String.format(formatString, entryLabel, r, longValue));
		}
		String totalLine = String.format(formatString, entryLabel, totalLabel, total);
		// PATCH: need to repeat one less than the length of the line, or we'll overflow by 1...
		s.append(indent).append(StringUtils.repeat("=", totalLine.length() - 1)).append(EnumeratedCounter.NEW_LINE);
		s.append(indent).append(totalLine).append(EnumeratedCounter.NEW_LINE);
		return s.toString();
	}
}