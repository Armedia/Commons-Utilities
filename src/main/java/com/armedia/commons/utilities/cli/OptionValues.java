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

	private final Map<String, List<Collection<Object>>> occurrences = new TreeMap<>();
	private final Map<String, List<Object>> values = new HashMap<>();

	public static final OptionValues EMPTY = new OptionValues();

	private static final <T extends Number> T mapWithParser(Object obj, Class<T> klazz, Function<String, T> parser) {
		if (obj == null) { return null; }
		if (klazz.isInstance(obj)) { return klazz.cast(obj); }
		return parser.apply(obj.toString());
	}

	private static final Function<Object, Integer> MAP_INTEGER = (o) -> {
		return OptionValues.mapWithParser(o, Integer.class, Integer::valueOf);
	};

	private static final Function<Object, Long> MAP_LONG = (o) -> {
		return OptionValues.mapWithParser(o, Long.class, Long::valueOf);
	};

	private static final Function<Object, Float> MAP_FLOAT = (o) -> {
		return OptionValues.mapWithParser(o, Float.class, Float::valueOf);
	};

	private static final Function<Object, Double> MAP_DOUBLE = (o) -> {
		return OptionValues.mapWithParser(o, Double.class, Double::valueOf);
	};

	private static final Function<Object, BigInteger> MAP_BIG_INTEGER = (o) -> {
		return OptionValues.mapWithParser(o, BigInteger.class, BigInteger::new);
	};

	private static final Function<Object, BigDecimal> MAP_BIG_DECIMAL = (o) -> {
		return OptionValues.mapWithParser(o, BigDecimal.class, BigDecimal::new);
	};

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
			List<Collection<Object>> l = new LinkedList<>();
			for (Collection<Object> c : this.occurrences.get(s)) {
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
				.map(p.getValueProcessor()) // Convert the values however the option sees fit
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

		List<Collection<Object>> occurrences = this.occurrences.get(key);
		if (occurrences == null) {
			occurrences = new LinkedList<>();
			this.occurrences.put(key, occurrences);
		}
		occurrences.add(Tools.freezeCollection(new LinkedList<>(values), true));

		List<Object> l = this.values.get(key);
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

	public Object get(Option param) {
		Object o = get(param, null);
		if (o == null) {
			o = param.getDefault();
		}
		return o;
	}

	public Object get(Option param, Object def) {
		List<Object> l = getAll(param);
		if ((l == null) || l.isEmpty()) { return def; }
		return l.get(0);
	}

	public List<Object> getAll(Option param) {
		return getAll(param, null);
	}

	public List<Object> getAll(Option param, List<Object> def) {
		return Tools.coalesce(this.values.get(getValidKey(param)), def);
	}

	public <T> T getMapped(Option param, Function<Object, T> mapper) {
		return getMapped(param, mapper, null);
	}

	public <T> T getMapped(Option param, Function<Object, T> mapper, T def) {
		Objects.requireNonNull(mapper, "Must provide a mapping function");
		Object o = get(param);
		if (o == null) { return def; }
		return mapper.apply(o);
	}

	public <T> List<T> getAllMapped(Option param, Function<Object, T> mapper) {
		return getAllMapped(param, mapper, null);
	}

	public <T> List<T> getAllMapped(Option param, Function<Object, T> mapper, List<T> def) {
		Objects.requireNonNull(mapper, "Must provide a mapping function");
		List<Object> l = getAll(param);
		if (l == null) { return def; }
		List<T> r = new ArrayList<>(l.size());
		l.forEach((v) -> r.add(mapper.apply(v)));
		return r;
	}

	public <T> T getAs(Option param, Class<T> klazz) {
		return getAs(param, klazz, null);
	}

	public <T> T getAs(Option param, Class<T> klazz, T def) {
		return getMapped(param, Objects.requireNonNull(klazz, "Must provide a class to cast to")::cast, def);
	}

	public <T> List<T> getAllAs(Option param, Class<T> klazz) {
		return getAllAs(param, klazz, null);
	}

	public <T> List<T> getAllAs(Option param, Class<T> klazz, List<T> def) {
		return getAllMapped(param, Objects.requireNonNull(klazz, "Must provide a class to cast to")::cast, def);
	}

	public Boolean getBoolean(Option param) {
		return getBoolean(param, null);
	}

	public Boolean getBoolean(Option param, Boolean def) {
		return getMapped(param, Tools::toBoolean, def);
	}

	public List<Boolean> getBooleans(Option param) {
		return getBooleans(param, null);
	}

	public List<Boolean> getBooleans(Option param, List<Boolean> def) {
		return getAllMapped(param, Tools::toBoolean, def);
	}

	public Integer getInteger(Option param) {
		return getInteger(param, null);
	}

	public Integer getInteger(Option param, Integer def) {
		return getMapped(param, OptionValues.MAP_INTEGER, def);
	}

	public List<Integer> getIntegers(Option param) {
		return getIntegers(param, null);
	}

	public List<Integer> getIntegers(Option param, List<Integer> def) {
		return getAllMapped(param, OptionValues.MAP_INTEGER, def);
	}

	public Long getLong(Option param) {
		return getLong(param, null);
	}

	public Long getLong(Option param, Long def) {
		return getMapped(param, OptionValues.MAP_LONG, def);
	}

	public List<Long> getLongs(Option param) {
		return getLongs(param, null);
	}

	public List<Long> getLongs(Option param, List<Long> def) {
		return getAllMapped(param, OptionValues.MAP_LONG, def);
	}

	public Float getFloat(Option param) {
		return getFloat(param, null);
	}

	public Float getFloat(Option param, Float def) {
		return getMapped(param, OptionValues.MAP_FLOAT, def);
	}

	public List<Float> getFloats(Option param) {
		return getFloats(param, null);
	}

	public List<Float> getFloats(Option param, List<Float> def) {
		return getAllMapped(param, OptionValues.MAP_FLOAT, def);
	}

	public Double getDouble(Option param) {
		return getDouble(param, null);
	}

	public Double getDouble(Option param, Double def) {
		return getMapped(param, OptionValues.MAP_DOUBLE, def);
	}

	public List<Double> getDoubles(Option param) {
		return getDoubles(param, null);
	}

	public List<Double> getDoubles(Option param, List<Double> def) {
		return getAllMapped(param, OptionValues.MAP_DOUBLE, def);
	}

	public BigInteger getBigInteger(Option param) {
		return getBigInteger(param, null);
	}

	public BigInteger getBigInteger(Option param, BigInteger def) {
		return getMapped(param, OptionValues.MAP_BIG_INTEGER, def);
	}

	public List<BigInteger> getBigIntegers(Option param) {
		return getBigIntegers(param, null);
	}

	public List<BigInteger> getBigIntegers(Option param, List<BigInteger> def) {
		return getAllMapped(param, OptionValues.MAP_BIG_INTEGER, def);
	}

	public BigDecimal getBigDecimal(Option param) {
		return getBigDecimal(param, null);
	}

	public BigDecimal getBigDecimal(Option param, BigDecimal def) {
		return getMapped(param, OptionValues.MAP_BIG_DECIMAL, def);
	}

	public List<BigDecimal> getBigDecimals(Option param) {
		return getBigDecimals(param, null);
	}

	public List<BigDecimal> getBigDecimals(Option param, List<BigDecimal> def) {
		return getAllMapped(param, OptionValues.MAP_BIG_DECIMAL, null);
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
		List<Object> v = this.values.get(getValidKey(param));
		if (v == null) { return def; }
		List<String> l = new ArrayList<>(v.size());
		v.forEach((o) -> l.add(Tools.toString(o)));
		return l;
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
		List<Collection<Object>> occurrences = this.occurrences.get(getValidKey(param));
		if (occurrences == null) { return 0; }
		return occurrences.size();
	}

	public Collection<Object> getOccurrenceValues(Option param, int o) {
		List<Collection<Object>> occurrences = this.occurrences.get(getValidKey(param));
		if (occurrences == null) { return null; }
		if ((o < 0) || (o >= occurrences.size())) { throw new IndexOutOfBoundsException(); }
		return occurrences.get(o);
	}

	public int getValueCount(Option param) {
		List<?> values = this.values.get(getValidKey(param));
		return (values != null ? values.size() : 0);
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

	public Collection<Object> getOccurrenceValues(Supplier<Option> param, int occurrence) {
		return getOccurrenceValues(Option.unwrap(param), occurrence);
	}

	public int getValueCount(Supplier<Option> param) {
		return getValueCount(Option.unwrap(param));
	}

	public boolean hasValues(Supplier<Option> param) {
		return hasValues(Option.unwrap(param));
	}
}
