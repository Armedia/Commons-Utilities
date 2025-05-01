/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2025 Armedia, LLC
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
package com.armedia.commons.utilities.cli;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.armedia.commons.utilities.Tools;
import com.armedia.commons.utilities.cli.filter.EnumValueFilter;

public final class OptionValues implements Iterable<OptionValue>, Cloneable {

	private final Map<Character, OptionValue> shortOptions = new TreeMap<>();
	private final Map<String, OptionValue> longOptions = new TreeMap<>();
	private final Map<String, OptionValue> optionValues = new TreeMap<>();

	private final Map<String, List<Collection<String>>> occurrences = new TreeMap<>();
	private final Map<String, List<String>> values = new HashMap<>();

	public static final OptionValues EMPTY = new OptionValues();

	private static <T> T mapWithParser(String str, Function<String, T> parser) {
		if (str == null) { return null; }
		return Objects.requireNonNull(parser, "Must provide a parser function").apply(str);
	}

	private static Boolean mapBoolean(String s) {
		return OptionValues.mapWithParser(s, Tools::toBoolean);
	}

	private static Integer mapInteger(String s) {
		return OptionValues.mapWithParser(s, Integer::valueOf);
	}

	private static Long mapLong(String s) {
		return OptionValues.mapWithParser(s, Long::valueOf);
	}

	private static Float mapFloat(String s) {
		return OptionValues.mapWithParser(s, Float::valueOf);
	}

	private static Double mapDouble(String s) {
		return OptionValues.mapWithParser(s, Double::valueOf);
	}

	private static BigInteger mapBigInteger(String s) {
		return OptionValues.mapWithParser(s, BigInteger::new);
	}

	private static BigDecimal mapBigDecimal(String s) {
		return OptionValues.mapWithParser(s, BigDecimal::new);
	}

	OptionValues() {
		// Do nothing...
	}

	@Override
	public OptionValues clone() {
		OptionValues copy = new OptionValues();

		// Copy the OptionValue objects...
		for (String s : this.optionValues.keySet()) {
			OptionValue old = this.optionValues.get(s);
			OptionValue v = new OptionValue(copy, old.getDefinition());
			Character shortOpt = v.getShortOpt();
			if (shortOpt != null) {
				copy.shortOptions.put(shortOpt, v);
			}
			String longOpt = v.getLongOpt();
			if (longOpt != null) {
				copy.longOptions.put(longOpt, v);
			}
			copy.optionValues.put(s, v);
		}

		// Copy the occurrences
		for (String s : this.occurrences.keySet()) {
			List<Collection<String>> l = new LinkedList<>();
			for (Collection<String> c : this.occurrences.get(s)) {
				c = Tools.freezeCollection(new LinkedList<>(c), true);
				l.add(c);
			}
			copy.occurrences.put(s, l);
		}

		// Copy the value lists
		for (String s : this.values.keySet()) {
			copy.values.put(s, new LinkedList<>(this.values.get(s)));
		}

		return copy;
	}

	private String getValidKey(Option param) {
		Objects.requireNonNull(param, "Must provide an option whose presence to check for");
		String key = param.getKey();
		if (key == null) {
			throw new IllegalArgumentException("The given option definition does not define a valid key");
		}
		if (OptionValue.class.isInstance(param)) {
			OptionValue p = OptionValue.class.cast(param);
			if (p.getOptionValues() != this) {
				throw new IllegalArgumentException("The given option is not associated to this OptionValues instance");
			}
		}
		return key;
	}

	void add(Option p) {
		add(p, null);
	}

	void add(Option p, Collection<String> values) {
		if (p == null) { throw new IllegalArgumentException("Must provide a non-null option"); }
		if (values == null) {
			values = Collections.emptyList();
		} else {
			final Function<String, String> mapper = Tools.coalesce(p.getValueProcessor(), Option.IDENTITY);
			values = values.stream() //
				.map(mapper) // Convert the values however the option sees fit
				.filter(Objects::nonNull) // Remove null values
				.collect(Collectors.toCollection(LinkedList::new)) // Collect it all
			;
		}

		final String key = p.getKey();
		OptionValue existing = this.optionValues.get(key);
		if (existing == null) {
			// This is a new option value, so we add the stuff that's needed
			existing = new OptionValue(this, p);
			this.optionValues.put(key, existing);
			Character shortOpt = p.getShortOpt();
			if (shortOpt != null) {
				this.shortOptions.put(shortOpt, existing);
			}
			String longOpt = p.getLongOpt();
			if (longOpt != null) {
				this.longOptions.put(longOpt, existing);
			}
		}

		List<Collection<String>> occurrences = this.occurrences.get(key);
		if (occurrences == null) {
			occurrences = new LinkedList<>();
			this.occurrences.put(key, occurrences);
		}
		occurrences.add(Tools.freezeCollection(new LinkedList<>(values), true));

		List<String> l = this.values.get(key);
		if (l == null) {
			l = new LinkedList<>();
			this.values.put(key, l);
		}
		l.addAll(values);
	}

	@Override
	public Iterator<OptionValue> iterator() {
		return new ArrayList<>(this.optionValues.values()).iterator();
	}

	public Iterable<OptionValue> shortOptions() {
		return new ArrayList<>(this.shortOptions.values());
	}

	public OptionValue getOption(char shortOpt) {
		return this.shortOptions.get(shortOpt);
	}

	public boolean hasOption(char shortOpt) {
		return this.shortOptions.containsKey(shortOpt);
	}

	public Iterable<OptionValue> longOptions() {
		return new ArrayList<>(this.longOptions.values());
	}

	public OptionValue getOption(String longOpt) {
		return this.longOptions.get(longOpt);
	}

	public boolean hasOption(String longOpt) {
		return this.longOptions.containsKey(longOpt);
	}

	public boolean isDefined(Option option) {
		return (getOption(option) != null);
	}

	public OptionValue getOption(Option option) {
		if (option == null) { throw new IllegalArgumentException("Must provide an option definition to retrieve"); }
		return getOptionValueByKey(option.getKey());
	}

	protected final OptionValue getOptionValueByKey(String key) {
		if (key == null) { throw new IllegalArgumentException("Must provide a key to search for"); }
		return this.optionValues.get(key);
	}

	public String getString(Option param) {
		List<String> l = getStrings(param);
		if (l == null) { return param.getDefault(); }
		return l.get(0);
	}

	public String getString(Option param, String def) {
		List<String> l = getStrings(param, null);
		if (l == null) { return def; }
		return l.get(0);
	}

	public List<String> getStrings(Option param) {
		List<String> v = getStrings(param, null);
		if (v == null) { return param.getDefaults(); }
		return v;
	}

	public List<String> getStrings(Option param, List<String> def) {
		List<String> v = this.values.get(getValidKey(param));
		if (v == null) { return def; }
		return new ArrayList<>(v);
	}

	public <T> T getMapped(Option param, Function<String, T> parser) {
		return OptionValues.mapWithParser(getString(param), parser);
	}

	public <T> T getMapped(Option param, Function<String, T> parser, T def) {
		String str = getString(param);
		if (str == null) { return def; }
		return OptionValues.mapWithParser(str, parser);
	}

	public <T> List<T> getAllMapped(Option param, Function<String, T> parser) {
		List<String> l = getStrings(param);
		if (l == null) { return null; }
		if (l.isEmpty()) { return Collections.emptyList(); }
		Objects.requireNonNull(param, "Must provide a parser function");
		List<T> r = new ArrayList<>(l.size());
		l.stream().map(parser).filter(Objects::nonNull).forEach(r::add);
		return r;
	}

	public Boolean getBoolean(Option param) {
		return getMapped(param, OptionValues::mapBoolean);
	}

	public Boolean getBoolean(Option param, Boolean def) {
		return Tools.coalesce(getBoolean(param), def);
	}

	public List<Boolean> getBooleans(Option param) {
		return getAllMapped(param, OptionValues::mapBoolean);
	}

	public Integer getInteger(Option param) {
		return getMapped(param, OptionValues::mapInteger);
	}

	public Integer getInteger(Option param, Integer def) {
		return Tools.coalesce(getInteger(param), def);
	}

	public List<Integer> getIntegers(Option param) {
		return getAllMapped(param, OptionValues::mapInteger);
	}

	public Long getLong(Option param) {
		return getMapped(param, OptionValues::mapLong);
	}

	public Long getLong(Option param, Long def) {
		return Tools.coalesce(getLong(param), def);
	}

	public List<Long> getLongs(Option param) {
		return getAllMapped(param, OptionValues::mapLong);
	}

	public Float getFloat(Option param) {
		return getMapped(param, OptionValues::mapFloat);
	}

	public Float getFloat(Option param, Float def) {
		return Tools.coalesce(getFloat(param), def);
	}

	public List<Float> getFloats(Option param) {
		return getAllMapped(param, OptionValues::mapFloat);
	}

	public Double getDouble(Option param) {
		return getMapped(param, OptionValues::mapDouble);
	}

	public Double getDouble(Option param, Double def) {
		return Tools.coalesce(getDouble(param), def);
	}

	public List<Double> getDoubles(Option param) {
		return getAllMapped(param, OptionValues::mapDouble);
	}

	public BigInteger getBigInteger(Option param) {
		return getMapped(param, OptionValues::mapBigInteger);
	}

	public BigInteger getBigInteger(Option param, BigInteger def) {
		return Tools.coalesce(getBigInteger(param), def);
	}

	public List<BigInteger> getBigIntegers(Option param) {
		return getAllMapped(param, OptionValues::mapBigInteger);
	}

	public BigDecimal getBigDecimal(Option param) {
		return getMapped(param, OptionValues::mapBigDecimal);
	}

	public BigDecimal getBigDecimal(Option param, BigDecimal def) {
		return Tools.coalesce(getBigDecimal(param), def);
	}

	public List<BigDecimal> getBigDecimals(Option param) {
		return getAllMapped(param, OptionValues::mapBigDecimal);
	}

	public <E extends Enum<E>> E getEnum(Class<E> enumClass, Option param) {
		return getEnum(enumClass, null, param);
	}

	public <E extends Enum<E>> E getEnum(Class<E> enumClass, BiFunction<Object, Exception, E> invalidHandler,
		Option param) {
		if (enumClass == null) { throw new IllegalArgumentException("Must provide a non-null Enum class"); }
		if (!enumClass.isEnum()) {
			throw new IllegalArgumentException(
				String.format("Class [%s] is not an Enum class", enumClass.getCanonicalName()));
		}
		String value = getString(param);
		if (value == null) { return null; }

		OptionValueFilter filter = param.getValueFilter();
		if (EnumValueFilter.class.isInstance(filter)) {
			EnumValueFilter<?> enumFilter = EnumValueFilter.class.cast(filter);
			Object o = enumFilter.decode(value);
			if (o != null) { return enumClass.cast(o); }
		}
		try {
			return Enum.valueOf(enumClass, value);
		} catch (final IllegalArgumentException e) {
			if (invalidHandler == null) { throw e; }
			return invalidHandler.apply(value, e);
		}
	}

	public <E extends Enum<E>> E getEnum(Class<E> enumClass, Option param, E def) {
		return getEnum(enumClass, null, param, def);
	}

	public <E extends Enum<E>> E getEnum(Class<E> enumClass, BiFunction<Object, Exception, E> invalidHandler,
		Option param, E def) {
		if (enumClass == null) { throw new IllegalArgumentException("Must provide a non-null Enum class"); }
		if (!enumClass.isEnum()) {
			throw new IllegalArgumentException(
				String.format("Class [%s] is not an Enum class", enumClass.getCanonicalName()));
		}
		String value = getString(param, null);
		if (value == null) { return def; }
		OptionValueFilter filter = param.getValueFilter();
		if (EnumValueFilter.class.isInstance(filter)) {
			EnumValueFilter<?> enumFilter = EnumValueFilter.class.cast(filter);
			Object o = enumFilter.decode(value);
			if (o != null) { return enumClass.cast(o); }
		}
		try {
			return Enum.valueOf(enumClass, value);
		} catch (final IllegalArgumentException e) {
			if (invalidHandler == null) { throw e; }
			return invalidHandler.apply(value, e);
		}
	}

	public <E extends Enum<E>> Set<E> getEnums(Class<E> enumClass, Option param) {
		return getEnums(enumClass, null, param);
	}

	public <E extends Enum<E>> Set<E> getEnums(Class<E> enumClass, BiFunction<Object, Exception, E> invalidHandler,
		Option param) {
		return getEnums(enumClass, null, invalidHandler, param);
	}

	public <E extends Enum<E>> Set<E> getEnums(Class<E> enumClass, String allString,
		BiFunction<Object, Exception, E> invalidHandler, Option param) {
		if (enumClass == null) { throw new IllegalArgumentException("Must provide a non-null Enum class"); }
		if (!enumClass.isEnum()) {
			throw new IllegalArgumentException(
				String.format("Class [%s] is not an Enum class", enumClass.getCanonicalName()));
		}
		List<String> v = getStrings(param, null);
		if (v == null) {
			v = param.getDefaults();
		}
		if (v == null) { return null; }
		Set<E> ret = EnumSet.noneOf(enumClass);
		OptionValueFilter filter = param.getValueFilter();
		EnumValueFilter<?> enumFilter = null;
		if (EnumValueFilter.class.isInstance(filter)) {
			enumFilter = EnumValueFilter.class.cast(filter);
		}
		for (String s : v) {
			if (StringUtils.equalsIgnoreCase(allString, s)) { return EnumSet.allOf(enumClass); }
			if (enumFilter != null) {
				Object o = enumFilter.decode(s);
				if (o != null) {
					ret.add(enumClass.cast(o));
					continue;
				}
			}
			try {
				ret.add(Enum.valueOf(enumClass, s));
			} catch (final IllegalArgumentException e) {
				if (invalidHandler == null) { throw e; }
				E alt = invalidHandler.apply(ret, e);
				if (alt != null) {
					ret.add(alt);
				}
			}
		}
		return ret;
	}

	public <E extends Enum<E>> Set<E> getEnums(Class<E> enumClass, Option param, Set<E> def) {
		return getEnums(enumClass, null, param, def);
	}

	public <E extends Enum<E>> Set<E> getEnums(Class<E> enumClass, BiFunction<Object, Exception, E> invalidHandler,
		Option param, Set<E> def) {
		return getEnums(enumClass, null, invalidHandler, param, def);
	}

	public <E extends Enum<E>> Set<E> getEnums(Class<E> enumClass, String allString,
		BiFunction<Object, Exception, E> invalidHandler, Option param, Set<E> def) {
		return Tools.coalesce(getEnums(enumClass, allString, invalidHandler, param), def);
	}

	public boolean isPresent(Option param) {
		return this.values.containsKey(getValidKey(param));
	}

	public int getOccurrences(Option param) {
		List<Collection<String>> occurrences = this.occurrences.get(getValidKey(param));
		if (occurrences == null) { return 0; }
		return occurrences.size();
	}

	public Collection<String> getOccurrenceValues(Option param, int o) {
		List<Collection<String>> occurrences = this.occurrences.get(getValidKey(param));
		if (occurrences == null) { return null; }
		if ((o < 0) || (o >= occurrences.size())) { throw new IndexOutOfBoundsException(); }
		return occurrences.get(o);
	}

	public int getValueCount(Option param) {
		List<Collection<String>> occurrences = this.occurrences.get(getValidKey(param));
		if (occurrences == null) { return 0; }
		int v = 0;
		for (Collection<String> c : occurrences) {
			v += c.size();
		}
		return v;
	}

	public boolean hasValues(Option param) {
		return (getValueCount(param) > 0);
	}

	public boolean isDefined(Supplier<Option> paramDel) {
		return isDefined(Option.unwrap(paramDel));
	}

	public OptionValue getOption(Supplier<Option> paramDel) {
		return getOption(Option.unwrap(paramDel));
	}

	public Boolean getBoolean(Supplier<Option> paramDel) {
		return getBoolean(Option.unwrap(paramDel));
	}

	public Boolean getBoolean(Supplier<Option> paramDel, Boolean def) {
		return getBoolean(Option.unwrap(paramDel), def);
	}

	public List<Boolean> getBooleans(Supplier<Option> paramDel) {
		return getBooleans(Option.unwrap(paramDel));
	}

	public Integer getInteger(Supplier<Option> paramDel) {
		return getInteger(Option.unwrap(paramDel));
	}

	public Integer getInteger(Supplier<Option> paramDel, Integer def) {
		return getInteger(Option.unwrap(paramDel), def);
	}

	public List<Integer> getIntegers(Supplier<Option> paramDel) {
		return getIntegers(Option.unwrap(paramDel));
	}

	public Long getLong(Supplier<Option> paramDel) {
		return getLong(Option.unwrap(paramDel));
	}

	public Long getLong(Supplier<Option> paramDel, Long def) {
		return getLong(Option.unwrap(paramDel), def);
	}

	public List<Long> getLongs(Supplier<Option> paramDel) {
		return getLongs(Option.unwrap(paramDel));
	}

	public Float getFloat(Supplier<Option> paramDel) {
		return getFloat(Option.unwrap(paramDel));
	}

	public Float getFloat(Supplier<Option> paramDel, Float def) {
		return getFloat(Option.unwrap(paramDel), def);
	}

	public List<Float> getFloats(Supplier<Option> paramDel) {
		return getFloats(Option.unwrap(paramDel));
	}

	public Double getDouble(Supplier<Option> paramDel) {
		return getDouble(Option.unwrap(paramDel));
	}

	public Double getDouble(Supplier<Option> paramDel, Double def) {
		return getDouble(Option.unwrap(paramDel), def);
	}

	public List<Double> getDoubles(Supplier<Option> paramDel) {
		return getDoubles(Option.unwrap(paramDel));
	}

	public BigInteger getBigInteger(Supplier<Option> paramDel) {
		return getBigInteger(Option.unwrap(paramDel));
	}

	public BigInteger getBigInteger(Supplier<Option> paramDel, BigInteger def) {
		return getBigInteger(Option.unwrap(paramDel), def);
	}

	public List<BigInteger> getBigIntegers(Supplier<Option> paramDel) {
		return getBigIntegers(Option.unwrap(paramDel));
	}

	public BigDecimal getBigDecimal(Supplier<Option> paramDel) {
		return getBigDecimal(Option.unwrap(paramDel));
	}

	public BigDecimal getBigDecimal(Supplier<Option> paramDel, BigDecimal def) {
		return getBigDecimal(Option.unwrap(paramDel), def);
	}

	public List<BigDecimal> getBigDecimals(Supplier<Option> paramDel) {
		return getBigDecimals(Option.unwrap(paramDel));
	}

	public String getString(Supplier<Option> paramDel) {
		return getString(Option.unwrap(paramDel));
	}

	public String getString(Supplier<Option> paramDel, String def) {
		return getString(Option.unwrap(paramDel), def);
	}

	public List<String> getStrings(Supplier<Option> paramDel) {
		return getStrings(Option.unwrap(paramDel));
	}

	public List<String> getStrings(Supplier<Option> paramDel, List<String> def) {
		return getStrings(Option.unwrap(paramDel), def);
	}

	public <E extends Enum<E>> E getEnum(Class<E> enumClass, Supplier<Option> paramDel) {
		return getEnum(enumClass, Option.unwrap(paramDel));
	}

	public <E extends Enum<E>> E getEnum(Class<E> enumClass, Supplier<Option> paramDel, E def) {
		return getEnum(enumClass, Option.unwrap(paramDel), def);
	}

	public <E extends Enum<E>> E getEnum(Class<E> enumClass, BiFunction<Object, Exception, E> invalidHandler,
		Supplier<Option> paramDel) {
		return getEnum(enumClass, invalidHandler, Option.unwrap(paramDel));
	}

	public <E extends Enum<E>> E getEnum(Class<E> enumClass, BiFunction<Object, Exception, E> invalidHandler,
		Supplier<Option> paramDel, E def) {
		return getEnum(enumClass, invalidHandler, Option.unwrap(paramDel), def);
	}

	public <E extends Enum<E>> Set<E> getEnums(Class<E> enumClass, Supplier<Option> paramDel) {
		return getEnums(enumClass, Option.unwrap(paramDel));
	}

	public <E extends Enum<E>> Set<E> getEnums(Class<E> enumClass, Supplier<Option> paramDel, Set<E> def) {
		return getEnums(enumClass, Option.unwrap(paramDel), def);
	}

	public <E extends Enum<E>> Set<E> getEnums(Class<E> enumClass, BiFunction<Object, Exception, E> invalidHandler,
		Supplier<Option> paramDel) {
		return getEnums(enumClass, invalidHandler, Option.unwrap(paramDel));
	}

	public <E extends Enum<E>> Set<E> getEnums(Class<E> enumClass, String allString,
		BiFunction<Object, Exception, E> invalidHandler, Supplier<Option> paramDel) {
		return getEnums(enumClass, allString, invalidHandler, Option.unwrap(paramDel));
	}

	public <E extends Enum<E>> Set<E> getEnums(Class<E> enumClass, BiFunction<Object, Exception, E> invalidHandler,
		Supplier<Option> paramDel, Set<E> def) {
		return getEnums(enumClass, invalidHandler, Option.unwrap(paramDel), def);
	}

	public <E extends Enum<E>> Set<E> getEnums(Class<E> enumClass, String allString,
		BiFunction<Object, Exception, E> invalidHandler, Supplier<Option> paramDel, Set<E> def) {
		return getEnums(enumClass, allString, invalidHandler, Option.unwrap(paramDel), def);
	}

	public boolean isPresent(Supplier<Option> paramDel) {
		return isPresent(Option.unwrap(paramDel));
	}

	public int getOccurrences(Supplier<Option> param) {
		return getOccurrences(Option.unwrap(param));
	}

	public Collection<String> getOccurrenceValues(Supplier<Option> param, int occurrence) {
		return getOccurrenceValues(Option.unwrap(param), occurrence);
	}

	public int getValueCount(Supplier<Option> param) {
		return getValueCount(Option.unwrap(param));
	}

	public boolean hasValues(Supplier<Option> param) {
		return hasValues(Option.unwrap(param));
	}
}
