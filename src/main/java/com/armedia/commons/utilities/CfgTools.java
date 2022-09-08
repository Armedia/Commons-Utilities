/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2022 Armedia, LLC
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

import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

/**
 *
 *
 */
public class CfgTools implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final Map<String, ?> EMPTY_MAP = Collections.emptyMap();
	public static final CfgTools EMPTY = new CfgTools(CfgTools.EMPTY_MAP);

	private static class ValueDecoderException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		private ValueDecoderException(String message) {
			super(message);
		}
	}

	private static void validateSetting(Object setting) {
		if (setting == null) { throw new IllegalArgumentException("Setting cannot be null"); }
	}

	@FunctionalInterface
	private static interface DefaultProvider<V> {
		public V getDefault(Function<Object, V> converter);
	}

	private static class SettingDefault<V> implements DefaultProvider<V> {

		private final ConfigurationSetting setting;

		private SettingDefault(ConfigurationSetting setting) {
			CfgTools.validateSetting(setting);
			this.setting = setting;
		}

		@Override
		public V getDefault(Function<Object, V> converter) {
			return converter.apply(this.setting.getDefaultValue());
		}
	}

	private static final class CONV_Enum<E extends Enum<E>> implements Function<Object, E> {

		private final BiFunction<Object, Exception, E> invalidHandler;
		private final Class<E> enumClass;

		CONV_Enum(Class<E> enumClass, BiFunction<Object, Exception, E> invalidHandler) {
			this.enumClass = enumClass;
			this.invalidHandler = invalidHandler;
		}

		@Override
		public E apply(Object o) {
			if (o == null) { return null; }
			if (this.enumClass.isInstance(o)) { return this.enumClass.cast(o); }
			if (Number.class.isInstance(o)) {
				int pos = Number.class.cast(o).intValue();
				E[] e = this.enumClass.getEnumConstants();
				if ((pos >= 0) && (pos < e.length)) { return e[pos]; }
				// Numbers aren't valid as enum values
				if (this.invalidHandler != null) { return this.invalidHandler.apply(o, null); }
				throw new IllegalArgumentException(
					String.format("The %s number [%s] is not a valid index for enum type %s (the maximum index is %d)",
						o, this.enumClass.getCanonicalName(), e.length - 1));
			}

			// Not a number, not an enum, must turn into a string and try to parse
			final String s = Tools.toString(o);
			try {
				return Enum.valueOf(this.enumClass, s);
			} catch (final IllegalArgumentException e) {
				if (this.invalidHandler != null) { return this.invalidHandler.apply(s, e); }
				throw e;
			}
		}

	}

	private static final Function<Object, Boolean> CONV_Boolean = (v) -> {
		if (v == null) { return null; }
		if (Boolean.class.isInstance(v)) { return Boolean.class.cast(v); }
		String str = Tools.toTrimmedString(v, true);
		if (StringUtils.isEmpty(str)) { return null; }
		// TODO: Do we want to support more "true" values?
		// TODO: Maybe something like Y(es)/T(rue)/1/O(n)/Enable/Enabled/Active?
		return Boolean.valueOf(str);
	};

	private static final Function<Object, Byte> CONV_Byte = (v) -> {
		if (v == null) { return null; }
		if (Number.class.isInstance(v)) { return Number.class.cast(v).byteValue(); }
		String str = Tools.toTrimmedString(v, true);
		if (StringUtils.isEmpty(str)) { return null; }
		return Byte.valueOf(str);
	};

	private static final Function<Object, Short> CONV_Short = (v) -> {
		if (v == null) { return null; }
		if (Number.class.isInstance(v)) { return Number.class.cast(v).shortValue(); }
		String str = Tools.toTrimmedString(v, true);
		if (StringUtils.isEmpty(str)) { return null; }
		return Short.valueOf(str);
	};

	private static final Function<Object, Integer> CONV_Integer = (v) -> {
		if (v == null) { return null; }
		if (Number.class.isInstance(v)) { return Number.class.cast(v).intValue(); }
		String str = Tools.toTrimmedString(v, true);
		if (StringUtils.isEmpty(str)) { return null; }
		return Integer.valueOf(str);
	};

	private static final Function<Object, Long> CONV_Long = (v) -> {
		if (v == null) { return null; }
		if (Number.class.isInstance(v)) { return Number.class.cast(v).longValue(); }
		String str = Tools.toTrimmedString(v, true);
		if (StringUtils.isEmpty(str)) { return null; }
		return Long.valueOf(str);
	};

	private static final Function<Object, Float> CONV_Float = (v) -> {
		if (v == null) { return null; }
		if (Number.class.isInstance(v)) { return Number.class.cast(v).floatValue(); }
		String str = Tools.toTrimmedString(v, true);
		if (StringUtils.isEmpty(str)) { return null; }
		return Float.valueOf(str);
	};

	private static final Function<Object, Double> CONV_Double = (v) -> {
		if (v == null) { return null; }
		if (Number.class.isInstance(v)) { return Number.class.cast(v).doubleValue(); }
		String str = Tools.toTrimmedString(v, true);
		if (StringUtils.isEmpty(str)) { return null; }
		return Double.valueOf(str);
	};

	private static final Function<Object, BigInteger> CONV_BigInteger = (v) -> {
		if (v == null) { return null; }
		if (BigInteger.class.isInstance(v)) { return BigInteger.class.cast(v); }
		String str = Tools.toTrimmedString(v, true);
		if (StringUtils.isEmpty(str)) { return null; }
		return new BigInteger(str);
	};

	private static final Function<Object, BigDecimal> CONV_BigDecimal = (v) -> {
		if (v == null) { return null; }
		if (BigDecimal.class.isInstance(v)) { return BigDecimal.class.cast(v); }
		String str = Tools.toTrimmedString(v, true);
		if (StringUtils.isEmpty(str)) { return null; }
		return new BigDecimal(str);
	};

	private static final Function<Object, String> CONV_String = Tools::toString;

	private static final Function<Object, byte[]> CONV_Binary = (v) -> {
		if (v == null) { return null; }
		if (v.getClass().isArray()) {
			Class<?> component = v.getClass().getComponentType();
			if (component == Byte.TYPE) { return (byte[]) v; }
			if (component == Byte.class) {
				// Still a byte array, but needs unwrapping...
				byte[] b = new byte[Array.getLength(v)];
				for (int i = 0; i < b.length; i++) {
					Byte B = Byte.class.cast(Array.get(v, i));
					b[i] = (B != null ? B.byteValue() : 0);
				}
				return b;
			}
			// Not a byte array...so we can't do anything "nice" with it
		}
		String str = Tools.toString(v);
		if (!Base64.isBase64(str)) {
			throw new ValueDecoderException(String.format("The given value [%s] is not a valid Base64 string", v));
		}
		return Base64.decodeBase64(str);
	};

	private static <V> V getValue(String label, Map<String, ?> settings, DefaultProvider<V> defaultValue,
		Function<Object, V> converter) {
		CfgTools.validateSetting(label);
		Object raw = settings.get(label);
		if ((raw == null) || StringUtils.EMPTY.equals(raw)) { return defaultValue.getDefault(converter); }

		if (raw.getClass().isArray()) {
			if (Array.getLength(raw) < 1) { return defaultValue.getDefault(converter); }
			return converter.apply(Array.get(raw, 0));
		}

		if (Collection.class.isInstance(raw)) {
			Collection<?> c = Collection.class.cast(raw);
			if (c.isEmpty()) { return null; }
			return converter.apply(c.iterator().next());
		}

		return converter.apply(raw);
	}

	private static <V> List<V> convertToList(Object raw, Function<Object, V> converter) {
		if (raw == null) { return Collections.emptyList(); }

		if (raw.getClass().isArray()) {
			final int length = Array.getLength(raw);
			if (length < 1) { return Collections.emptyList(); }

			List<V> result = new ArrayList<>(length);
			for (int i = 0; i < length; i++) {
				result.add(converter.apply(Array.get(raw, i)));
			}
			return Tools.freezeList(result);
		}

		if (Collection.class.isInstance(raw)) {
			Collection<?> c = Collection.class.cast(raw);
			if (c.isEmpty()) { return Collections.emptyList(); }
			List<V> result = new ArrayList<>(c.size());
			c.forEach((r) -> result.add(converter.apply(r)));
			return Tools.freezeList(result);
		}

		return Collections.singletonList(converter.apply(raw));
	}

	private static <V> List<V> getValues(String label, Map<String, ?> settings, DefaultProvider<List<V>> defaultValue,
		Function<Object, V> converter) {
		CfgTools.validateSetting(label);
		Object raw = settings.get(label);
		if ((raw == null) || StringUtils.EMPTY.equals(raw)) {
			return defaultValue.getDefault((v) -> CfgTools.convertToList(v, converter));
		}

		return CfgTools.convertToList(raw, converter);
	}

	/**
	 * Decode the named setting from the given map as an instance of the given {@link Class},
	 * returning the value stored (may be {@code null}), or {@code null} if it's not an instance of
	 * the class. This is equivalent to calling {@link #decodeObject(String, Map, Object)
	 * decodeAs(label, klazz, settings, null)}
	 *
	 * @param label
	 * @param klazz
	 * @param settings
	 * @return the named setting from the given map as an instance of {@code klazz}
	 */
	public static <T> T decodeAs(String label, Class<T> klazz, Map<String, ?> settings, T defaultValue) {
		Objects.requireNonNull(klazz, "Must provide a class to cast the object into");
		return CfgTools.getValue(label, settings, (c) -> defaultValue, klazz::cast);
	}

	/**
	 * Decode the named setting from the given map as a {@link Object} value, returning the value
	 * stored (may be {@code null}), or {@code defaultValue} if it's not defined.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as an {@link Object} value
	 */
	public static Object decodeObject(String label, Map<String, ?> settings, Object defaultValue) {
		return CfgTools.decodeAs(label, Object.class, settings, defaultValue);
	}

	/**
	 * Decode the named setting from the given map as an instance of the given {@link Class},
	 * returning the value stored (may be {@code null}), or {@code null} if it's not an instance of
	 * the class. This is equivalent to calling {@link #decodeObject(String, Map, Object)
	 * decodeAs(label, klazz, settings, null)}
	 *
	 * @param label
	 * @param klazz
	 * @param settings
	 * @return the named setting from the given map as an instance of {@code klazz}
	 */
	public static <T> T decodeAs(String label, Class<T> klazz, Map<String, ?> settings) {
		return CfgTools.decodeAs(label, klazz, settings, null);
	}

	/**
	 * Decode the named setting from the given map as a {@link Object} value, returning the value
	 * stored (may be {@code null}), or {@code null} if it's not defined. This is equivalent to
	 * calling {@link #decodeObject(String, Map, Object) decodeObject(label, settings, null)}
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as an {@link Object} value
	 */
	public static Object decodeObject(String label, Map<String, ?> settings) {
		return CfgTools.decodeAs(label, Object.class, settings);
	}

	/**
	 * Decode the named setting from the given map as an instance of the given {@link Class},
	 * returning the value stored (may be {@code null}), or {@code null} if it's not an instance of
	 * the class. This is equivalent to calling {@link #decodeObject(String, Map, Object)
	 * decodeAs(label, klazz, settings, null)}
	 *
	 * @param setting
	 * @param klazz
	 * @param settings
	 * @return the named setting from the given map as an instance of {@code klazz}
	 */
	public static <T> T decodeAs(ConfigurationSetting setting, Class<T> klazz, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValue(setting.getLabel(), settings, new SettingDefault<>(setting), klazz::cast);
	}

	/**
	 * Decode the given setting from the given map as a {@link Object} value, returning the value
	 * stored (may be {@code null}), or the setting's {@link ConfigurationSetting#getDefaultValue()
	 * default value} if it's not defined. This is equivalent to calling
	 * {@link #decodeObject(String, Map, Object) decodeObject(setting.getLabel(), settings,
	 * setting.getDefaultValue())}.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as an {@link Object} value
	 */
	public static Object decodeObject(ConfigurationSetting setting, Map<String, ?> settings) {
		return CfgTools.decodeAs(setting, Object.class, settings);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of instances of {@link Class}
	 * {@code klazz}, returning the List stored (may be {@code null}), or {@code defaultValue} if
	 * it's not defined.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as an {@link Object} value
	 */
	public static <T> List<T> decodeAllAs(String label, Class<T> klazz, Map<String, ?> settings, List<T> defaultValue) {
		Objects.requireNonNull(klazz, "Must provide a class to cast the list elements into");
		return CfgTools.getValues(label, settings, (c) -> defaultValue, klazz::cast);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of {@link Object} values,
	 * returning the List stored (may be {@code null}), or {@code defaultValue} if it's not defined.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as an {@link Object} value
	 */
	public static List<Object> decodeObjects(String label, Map<String, ?> settings, List<Object> defaultValue) {
		return CfgTools.decodeAllAs(label, Object.class, settings, defaultValue);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of @link Object} values,
	 * returning the list stored (may be {@code null}), or {@code null} if it's not defined. This is
	 * equivalent to calling {@link #decodeObjects(String, Map, List) decodeObjects(label, settings,
	 * null)}
	 *
	 * @param label
	 * @param klazz
	 * @param settings
	 * @return the named setting from the given map as a {@link Object} value
	 */
	public static <T> List<T> decodeAllAs(String label, Class<T> klazz, Map<String, ?> settings) {
		return CfgTools.decodeAllAs(label, klazz, settings, null);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of @link Object} values,
	 * returning the list stored (may be {@code null}), or {@code null} if it's not defined. This is
	 * equivalent to calling {@link #decodeObjects(String, Map, List) decodeObjects(label, settings,
	 * null)}
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link Object} value
	 */
	public static List<Object> decodeObjects(String label, Map<String, ?> settings) {
		return CfgTools.decodeAllAs(label, Object.class, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link List} of {@link Object} values,
	 * returning the list stored (may be {@code null}), or the setting's
	 * {@link ConfigurationSetting#getDefaultValue() default value} (converted to a
	 * List&lt;Object&gt;) if it's not defined. This is equivalent to calling
	 * {@link #decodeObjects(String, Map, List) decodeObjects(setting.getLabel(), settings,
	 * setting.getDefaultValue())}.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link Object} value
	 */
	public static <T> List<T> decodeAllAs(ConfigurationSetting setting, Class<T> klazz, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValues(setting.getLabel(), settings, new SettingDefault<>(setting), klazz::cast);
	}

	/**
	 * Decode the given setting from the given map as a {@link List} of {@link Object} values,
	 * returning the list stored (may be {@code null}), or the setting's
	 * {@link ConfigurationSetting#getDefaultValue() default value} (converted to a
	 * List&lt;Object&gt;) if it's not defined. This is equivalent to calling
	 * {@link #decodeObjects(String, Map, List) decodeObjects(setting.getLabel(), settings,
	 * setting.getDefaultValue())}.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link Object} value
	 */
	public static List<Object> decodeObjects(ConfigurationSetting setting, Map<String, ?> settings) {
		return CfgTools.decodeAllAs(setting, Object.class, settings);
	}

	/**
	 * Decode the named setting from the given map as an {@link Enum} value, returning the value
	 * stored (may be {@code null}), or {@code defaultValue} if it's not defined.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link Enum} value, or {@code null} if the
	 *         setting's value is not a valid enumerated value for the given enum class
	 */
	public static <E extends Enum<E>> E decodeEnum(String label, Class<E> enumClass,
		BiFunction<Object, Exception, E> invalidHandler, Map<String, ?> settings, E defaultValue) {
		return CfgTools.getValue(label, settings, (c) -> defaultValue, new CONV_Enum<>(enumClass, invalidHandler));
	}

	/**
	 * Decode the named setting from the given map as an {@link Enum} value, returning the value
	 * stored (may be {@code null}), or {@code null} if it's not defined. This is equivalent to
	 * calling {@link #decodeEnum(String, Class, Map, Enum) decodeEnum(label, Class<E> enumClass,
	 * settings, null)}
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as an {@link Enum} value
	 * @throws IllegalArgumentException
	 *             if the setting's value doesn't correspond to one of the given enum class's values
	 *             - i.e. if {@link Enum#valueOf(Class, String) Enum.valueOf(enumClass, value)}
	 *             raises it
	 */
	public static <E extends Enum<E>> E decodeEnum(String label, Class<E> enumClass,
		BiFunction<Object, Exception, E> invalidHandler, Map<String, ?> settings) {
		return CfgTools.decodeEnum(label, enumClass, invalidHandler, settings, null);
	}

	/**
	 * Decode the given setting from the given map as an {@link Enum} value, returning the value
	 * stored (may be {@code null}), or the setting's {@link ConfigurationSetting#getDefaultValue()
	 * default value} if it's not defined. This is equivalent to calling
	 * {@link #decodeEnum(String, Class, Map, Enum) decodeEnum(setting.getLabel(), settings,
	 * setting.getDefaultValue())}.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as an {@link Enum} value
	 * @throws IllegalArgumentException
	 *             if the setting's value doesn't correspond to one of the given enum class's values
	 *             - i.e. if {@link Enum#valueOf(Class, String) Enum.valueOf(enumClass, value)}
	 *             raises it
	 */
	public static <E extends Enum<E>> E decodeEnum(ConfigurationSetting setting, Class<E> enumClass,
		BiFunction<Object, Exception, E> invalidHandler, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValue(setting.getLabel(), settings, new SettingDefault<>(setting),
			new CONV_Enum<>(enumClass, invalidHandler));
	}

	/**
	 * Decode the named setting from the given map as an {@link Enum} value, returning the value
	 * stored (may be {@code null}), or {@code defaultValue} if it's not defined.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link Enum} value
	 * @throws IllegalArgumentException
	 *             if the setting's value doesn't correspond to one of the given enum class's values
	 *             - i.e. if {@link Enum#valueOf(Class, String) Enum.valueOf(enumClass, value)}
	 *             raises it
	 */
	public static <E extends Enum<E>> E decodeEnum(String label, Class<E> enumClass, Map<String, ?> settings,
		E defaultValue) {
		return CfgTools.decodeEnum(label, enumClass, null, settings, defaultValue);
	}

	/**
	 * Decode the named setting from the given map as an {@link Enum} value, returning the value
	 * stored (may be {@code null}), or {@code null} if it's not defined. This is equivalent to
	 * calling {@link #decodeEnum(String, Class, Map, Enum) decodeEnum(label, Class<E> enumClass,
	 * settings, null)}
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as an {@link Enum} value
	 * @throws IllegalArgumentException
	 *             if the setting's value doesn't correspond to one of the given enum class's values
	 *             - i.e. if {@link Enum#valueOf(Class, String) Enum.valueOf(enumClass, value)}
	 *             raises it
	 */
	public static <E extends Enum<E>> E decodeEnum(String label, Class<E> enumClass, Map<String, ?> settings) {
		return CfgTools.decodeEnum(label, enumClass, null, settings, null);
	}

	/**
	 * Decode the given setting from the given map as an {@link Enum} value, returning the value
	 * stored (may be {@code null}), or the setting's {@link ConfigurationSetting#getDefaultValue()
	 * default value} if it's not defined. This is equivalent to calling
	 * {@link #decodeEnum(String, Class, Map, Enum) decodeEnum(setting.getLabel(), settings,
	 * setting.getDefaultValue())}.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as an {@link Enum} value
	 * @throws IllegalArgumentException
	 *             if the setting's value doesn't correspond to one of the given enum class's values
	 *             - i.e. if {@link Enum#valueOf(Class, String) Enum.valueOf(enumClass, value)}
	 *             raises it
	 */
	public static <E extends Enum<E>> E decodeEnum(ConfigurationSetting setting, Class<E> enumClass,
		Map<String, ?> settings) {
		return CfgTools.decodeEnum(setting, enumClass, null, settings);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of {@link Enum} values,
	 * returning the List stored (may be {@code null}), or {@code defaultValue} if it's not defined.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link Enum} value
	 * @throws IllegalArgumentException
	 *             if one of the setting's values doesn't correspond to one of the given enum
	 *             class's values - i.e. if {@link Enum#valueOf(Class, String)
	 *             Enum.valueOf(enumClass, value)} raises it
	 */
	public static <E extends Enum<E>> List<E> decodeEnums(String label, Class<E> enumClass,
		BiFunction<Object, Exception, E> invalidHandler, Map<String, ?> settings, List<E> defaultValue) {
		return CfgTools.getValues(label, settings, (c) -> defaultValue, new CONV_Enum<>(enumClass, invalidHandler));
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of @link Enum} values,
	 * returning the list stored (may be {@code null}), or {@code null} if it's not defined. This is
	 * equivalent to calling {@link #decodeEnums(String, Class, Map, List) decodeEnums(label,
	 * settings, null)}
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link Enum} value
	 */
	public static <E extends Enum<E>> List<E> decodeEnums(String label, Class<E> enumClass,
		BiFunction<Object, Exception, E> invalidHandler, Map<String, ?> settings) {
		return CfgTools.decodeEnums(label, enumClass, invalidHandler, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link List} of {@link Enum} values,
	 * returning the list stored (may be {@code null}), or the setting's
	 * {@link ConfigurationSetting#getDefaultValue() default value} (converted to a
	 * List&lt;Enum&gt;) if it's not defined. This is equivalent to calling
	 * {@link #decodeEnums(String, Class, Map, List) decodeEnums(setting.getLabel(), settings,
	 * setting.getDefaultValue())}.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link Enum} value
	 */
	public static <E extends Enum<E>> List<E> decodeEnums(ConfigurationSetting setting, Class<E> enumClass,
		BiFunction<Object, Exception, E> invalidHandler, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValues(setting.getLabel(), settings, new SettingDefault<>(setting),
			new CONV_Enum<>(enumClass, invalidHandler));
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of {@link Enum} values,
	 * returning the List stored (may be {@code null}), or {@code defaultValue} if it's not defined.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link Enum} value
	 * @throws IllegalArgumentException
	 *             if one of the setting's values doesn't correspond to one of the given enum
	 *             class's values - i.e. if {@link Enum#valueOf(Class, String)
	 *             Enum.valueOf(enumClass, value)} raises it
	 */
	public static <E extends Enum<E>> List<E> decodeEnums(String label, Class<E> enumClass, Map<String, ?> settings,
		List<E> defaultValue) {
		return CfgTools.decodeEnums(label, enumClass, null, settings, defaultValue);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of @link Enum} values,
	 * returning the list stored (may be {@code null}), or {@code null} if it's not defined. This is
	 * equivalent to calling {@link #decodeEnums(String, Class, Map, List) decodeEnums(label,
	 * settings, null)}
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link Enum} value
	 */
	public static <E extends Enum<E>> List<E> decodeEnums(String label, Class<E> enumClass, Map<String, ?> settings) {
		return CfgTools.decodeEnums(label, enumClass, null, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link List} of {@link Enum} values,
	 * returning the list stored (may be {@code null}), or the setting's
	 * {@link ConfigurationSetting#getDefaultValue() default value} (converted to a
	 * List&lt;Enum&gt;) if it's not defined. This is equivalent to calling
	 * {@link #decodeEnums(String, Class, Map, List) decodeEnums(setting.getLabel(), settings,
	 * setting.getDefaultValue())}.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link Enum} value
	 */
	public static <E extends Enum<E>> List<E> decodeEnums(ConfigurationSetting setting, Class<E> enumClass,
		Map<String, ?> settings) {
		return CfgTools.decodeEnums(setting, enumClass, null, settings);
	}

	/**
	 * Decode the named setting from the given map as a {@link Boolean} value, returning the value
	 * stored (may be {@code null}), or {@code defaultValue} if it's not defined. The value to be
	 * returned is converted into a Boolean using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link Boolean} value
	 */
	public static Boolean decodeBoolean(String label, Map<String, ?> settings, Boolean defaultValue) {
		return CfgTools.getValue(label, settings, (c) -> defaultValue, CfgTools.CONV_Boolean);
	}

	/**
	 * Decode the named setting from the given map as a {@link Boolean} value, returning the value
	 * stored (may be {@code null}), or {@code null} if it's not defined. This is equivalent to
	 * calling {@link #decodeBoolean(String, Map, Boolean) decodeBoolean(label, settings, null)}.
	 * The value to be returned is converted into a Boolean using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link Boolean} value
	 */
	public static Boolean decodeBoolean(String label, Map<String, ?> settings) {
		return CfgTools.decodeBoolean(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link Boolean} value, returning the value
	 * stored (may be {@code null}), or the setting's {@link ConfigurationSetting#getDefaultValue()
	 * default value} if it's not defined. This is equivalent to calling
	 * {@link #decodeBoolean(String, Map, Boolean) decodeBoolean(setting.getLabel(), settings,
	 * setting.getDefaultValue())}. The value to be returned is converted into a Boolean using a
	 * best-effort strategy.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link Boolean} value
	 */
	public static Boolean decodeBoolean(ConfigurationSetting setting, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValue(setting.getLabel(), settings, new SettingDefault<>(setting), CfgTools.CONV_Boolean);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of {@link Boolean} values,
	 * returning the List stored (may be {@code null}), or {@code defaultValue} if it's not defined.
	 * The value to be returned is converted into a List&lt;Boolean&gt; using a best-effort
	 * strategy.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link Boolean} value
	 */
	public static List<Boolean> decodeBooleans(String label, Map<String, ?> settings, List<Boolean> defaultValue) {
		return CfgTools.getValues(label, settings, (c) -> defaultValue, CfgTools.CONV_Boolean);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of @link Boolean} values,
	 * returning the list stored (may be {@code null}), or {@code null} if it's not defined. This is
	 * equivalent to calling {@link #decodeBooleans(String, Map, List) decodeBooleans(label,
	 * settings, null)}. The value to be returned is converted into a List&lt;Boolean&gt; using a
	 * best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link List} of {@link Boolean} values
	 */
	public static List<Boolean> decodeBooleans(String label, Map<String, ?> settings) {
		return CfgTools.decodeBooleans(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link List} of {@link Boolean} values,
	 * returning the list stored (may be {@code null}), or the setting's
	 * {@link ConfigurationSetting#getDefaultValue() default value} (converted to a
	 * List&lt;Boolean&gt;) if it's not defined. This is equivalent to calling
	 * {@link #decodeBooleans(String, Map, List) decodeBooleans(setting.getLabel(), settings,
	 * setting.getDefaultValue())}. The value to be returned is converted into a List&lt;Boolean&gt;
	 * using a best-effort strategy.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link List} of {@link Boolean} values
	 */
	public static List<Boolean> decodeBooleans(ConfigurationSetting setting, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValues(setting.getLabel(), settings, new SettingDefault<>(setting), CfgTools.CONV_Boolean);
	}

	/**
	 * Decode the named setting from the given map as a {@link Byte} value, returning the value
	 * stored (may be {@code null}), or {@code defaultValue} if it's not defined. The value to be
	 * returned is converted into a Byte using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link Byte} value
	 */
	public static Byte decodeByte(String label, Map<String, ?> settings, Byte defaultValue) {
		return CfgTools.getValue(label, settings, (c) -> defaultValue, CfgTools.CONV_Byte);
	}

	/**
	 * Decode the named setting from the given map as a {@link Byte} value, returning the value
	 * stored (may be {@code null}), or {@code null} if it's not defined. This is equivalent to
	 * calling {@link #decodeByte(String, Map, Byte) decodeByte(label, settings, null)}. The value
	 * to be returned is converted into a Byte using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link Byte} value
	 */
	public static Byte decodeByte(String label, Map<String, ?> settings) {
		return CfgTools.decodeByte(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link Byte} value, returning the value
	 * stored (may be {@code null}), or the setting's {@link ConfigurationSetting#getDefaultValue()
	 * default value} if it's not defined. This is equivalent to calling
	 * {@link #decodeByte(String, Map, Byte) decodeByte(setting.getLabel(), settings,
	 * setting.getDefaultValue())}. The value to be returned is converted into a Byte using a
	 * best-effort strategy.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link Byte} value
	 */
	public static Byte decodeByte(ConfigurationSetting setting, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValue(setting.getLabel(), settings, new SettingDefault<>(setting), CfgTools.CONV_Byte);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of {@link Byte} values,
	 * returning the List stored (may be {@code null}), or {@code defaultValue} if it's not defined.
	 * The value to be returned is converted into a List&lt;Byte&gt; using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link List} of {@link Byte} values
	 */
	public static List<Byte> decodeBytes(String label, Map<String, ?> settings, List<Byte> defaultValue) {
		return CfgTools.getValues(label, settings, (c) -> defaultValue, CfgTools.CONV_Byte);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of @link Byte} values,
	 * returning the list stored (may be {@code null}), or {@code null} if it's not defined. This is
	 * equivalent to calling {@link #decodeBytes(String, Map, List) decodeBytes(label, settings,
	 * null)}. The value to be returned is converted into a List&lt;Byte&gt; using a best-effort
	 * strategy.
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link List} of {@link Byte} values
	 */
	public static List<Byte> decodeBytes(String label, Map<String, ?> settings) {
		return CfgTools.decodeBytes(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link List} of {@link Byte} values,
	 * returning the list stored (may be {@code null}), or the setting's
	 * {@link ConfigurationSetting#getDefaultValue() default value} (converted to a
	 * List&lt;Byte&gt;) if it's not defined. This is equivalent to calling
	 * {@link #decodeBytes(String, Map, List) decodeBytes(setting.getLabel(), settings,
	 * setting.getDefaultValue())}. The value to be returned is converted into a List&lt;Byte&gt;
	 * using a best-effort strategy.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link List} of {@link Byte} values
	 */
	public static List<Byte> decodeBytes(ConfigurationSetting setting, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValues(setting.getLabel(), settings, new SettingDefault<>(setting), CfgTools.CONV_Byte);
	}

	/**
	 * Decode the named setting from the given map as a {@link Short} value, returning the value
	 * stored (may be {@code null}), or {@code defaultValue} if it's not defined. The value to be
	 * returned is converted into a Short using a best-effort strategy.
	 *
	 * @param label
	 *            {@link List} of
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link Short} value
	 */
	public static Short decodeShort(String label, Map<String, ?> settings, Short defaultValue) {
		return CfgTools.getValue(label, settings, (c) -> defaultValue, CfgTools.CONV_Short);
	}

	/**
	 * Decode the named setting from the given map as a {@link Short} value, returning the value
	 * stored (may be {@code null}), or {@code null} if it's not defined. This is equivalent to
	 * calling {@link #decodeShort(String, Map, Short) decodeShort(label, settings, null)}. The
	 * value to be returned is converted into a Short using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link Short} value
	 */
	public static Short decodeShort(String label, Map<String, ?> settings) {
		return CfgTools.decodeShort(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link Short} value, returning the value
	 * stored (may be {@code null}), or the setting's {@link ConfigurationSetting#getDefaultValue()
	 * default value} if it's not defined. This is equivalent to calling
	 * {@link #decodeShort(String, Map, Short) decodeShort(setting.getLabel(), settings,
	 * setting.getDefaultValue())}. The value to be returned is converted into a Short using a
	 * best-effort strategy.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link Short} value
	 */
	public static Short decodeShort(ConfigurationSetting setting, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValue(setting.getLabel(), settings, new SettingDefault<>(setting), CfgTools.CONV_Short);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of {@link Short} values,
	 * returning the List stored (may be {@code null}), or {@code defaultValue} if it's not defined.
	 * The value to be returned is converted into a List&lt;Short&gt; using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link Short} value
	 */
	public static List<Short> decodeShorts(String label, Map<String, ?> settings, List<Short> defaultValue) {
		return CfgTools.getValues(label, settings, (c) -> defaultValue, CfgTools.CONV_Short);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of @link Short} values,
	 * returning the list stored (may be {@code null}), or {@code null} if it's not defined. This is
	 * equivalent to calling {@link #decodeShorts(String, Map, List) decodeShorts(label, settings,
	 * null)}. The value to be returned is converted into a List&lt;Short&gt; using a best-effort
	 * strategy.
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link List} of {@link Short} values
	 */
	public static List<Short> decodeShorts(String label, Map<String, ?> settings) {
		return CfgTools.decodeShorts(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link List} of {@link Short} values,
	 * returning the list stored (may be {@code null}), or the setting's
	 * {@link ConfigurationSetting#getDefaultValue() default value} (converted to a
	 * List&lt;Short&gt;) if it's not defined. This is equivalent to calling
	 * {@link #decodeShorts(String, Map, List) decodeShorts(setting.getLabel(), settings,
	 * setting.getDefaultValue())}. The value to be returned is converted into a List&lt;Short&gt;
	 * using a best-effort strategy.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link List} of {@link Short} values
	 */
	public static List<Short> decodeShorts(ConfigurationSetting setting, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValues(setting.getLabel(), settings, new SettingDefault<>(setting), CfgTools.CONV_Short);
	}

	/**
	 * Decode the named setting from the given map as a {@link Integer} value, returning the value
	 * stored (may be {@code null}), or {@code defaultValue} if it's not defined. The value to be
	 * returned is converted into a Integer using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link List} of {@link Integer} values
	 */
	public static Integer decodeInteger(String label, Map<String, ?> settings, Integer defaultValue) {
		return CfgTools.getValue(label, settings, (c) -> defaultValue, CfgTools.CONV_Integer);
	}

	/**
	 * Decode the named setting from the given map as a {@link Integer} value, returning the value
	 * stored (may be {@code null}), or {@code null} if it's not defined. This is equivalent to
	 * calling {@link #decodeInteger(String, Map, Integer) decodeInteger(label, settings, null)}.
	 * The value to be returned is converted into a Integer using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link Integer} value
	 */
	public static Integer decodeInteger(String label, Map<String, ?> settings) {
		return CfgTools.decodeInteger(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link Integer} value, returning the value
	 * stored (may be {@code null}), or the setting's {@link ConfigurationSetting#getDefaultValue()
	 * default value} if it's not defined. This is equivalent to calling
	 * {@link #decodeInteger(String, Map, Integer) decodeInteger(setting.getLabel(), settings,
	 * setting.getDefaultValue())}. The value to be returned is converted into a Integer using a
	 * best-effort strategy.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link Integer} value
	 */
	public static Integer decodeInteger(ConfigurationSetting setting, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValue(setting.getLabel(), settings, new SettingDefault<>(setting), CfgTools.CONV_Integer);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of {@link Integer} values,
	 * returning the List stored (may be {@code null}), or {@code defaultValue} if it's not defined.
	 * The value to be returned is converted into a List&lt;Integer&gt; using a best-effort
	 * strategy.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link List} of {@link Integer} values
	 */
	public static List<Integer> decodeIntegers(String label, Map<String, ?> settings, List<Integer> defaultValue) {
		return CfgTools.getValues(label, settings, (c) -> defaultValue, CfgTools.CONV_Integer);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of @link Integer} values,
	 * returning the list stored (may be {@code null}), or {@code null} if it's not defined. This is
	 * equivalent to calling {@link #decodeIntegers(String, Map, List) decodeIntegers(label,
	 * settings, null)}. The value to be returned is converted into a List&lt;Integer&gt; using a
	 * best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link List} of {@link Integer} values
	 */
	public static List<Integer> decodeIntegers(String label, Map<String, ?> settings) {
		return CfgTools.decodeIntegers(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link List} of {@link Integer} values,
	 * returning the list stored (may be {@code null}), or the setting's
	 * {@link ConfigurationSetting#getDefaultValue() default value} (converted to a
	 * List&lt;Integer&gt;) if it's not defined. This is equivalent to calling
	 * {@link #decodeIntegers(String, Map, List) decodeIntegers(setting.getLabel(), settings,
	 * setting.getDefaultValue())}. The value to be returned is converted into a List&lt;Integer&gt;
	 * using a best-effort strategy.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link List} of {@link Integer} values
	 */
	public static List<Integer> decodeIntegers(ConfigurationSetting setting, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValues(setting.getLabel(), settings, new SettingDefault<>(setting), CfgTools.CONV_Integer);
	}

	/**
	 * Decode the named setting from the given map as a {@link Long} value, returning the value
	 * stored (may be {@code null}), or {@code defaultValue} if it's not defined. The value to be
	 * returned is converted into a Long using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link Long} value
	 */
	public static Long decodeLong(String label, Map<String, ?> settings, Long defaultValue) {
		return CfgTools.getValue(label, settings, (c) -> defaultValue, CfgTools.CONV_Long);
	}

	/**
	 * Decode the named setting from the given map as a {@link Long} value, returning the value
	 * stored (may be {@code null}), or {@code null} if it's not defined. This is equivalent to
	 * calling {@link #decodeLong(String, Map, Long) decodeLong(label, settings, null)}. The value
	 * to be returned is converted into a Long using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link Long} value
	 */
	public static Long decodeLong(String label, Map<String, ?> settings) {
		return CfgTools.decodeLong(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link Long} value, returning the value
	 * stored (may be {@code null}), or the setting's {@link ConfigurationSetting#getDefaultValue()
	 * default value} if it's not defined. This is equivalent to calling
	 * {@link #decodeLong(String, Map, Long) decodeLong(setting.getLabel(), settings,
	 * setting.getDefaultValue())}. The value to be returned is converted into a Long using a
	 * best-effort strategy.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link Long} value
	 */
	public static Long decodeLong(ConfigurationSetting setting, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValue(setting.getLabel(), settings, new SettingDefault<>(setting), CfgTools.CONV_Long);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of {@link Long} values,
	 * returning the List stored (may be {@code null}), or {@code defaultValue} if it's not defined.
	 * The value to be returned is converted into a List&lt;Long&gt; using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link List} of {@link Long} values
	 */
	public static List<Long> decodeLongs(String label, Map<String, ?> settings, List<Long> defaultValue) {
		return CfgTools.getValues(label, settings, (c) -> defaultValue, CfgTools.CONV_Long);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of @link Long} values,
	 * returning the list stored (may be {@code null}), or {@code null} if it's not defined. This is
	 * equivalent to calling {@link #decodeLongs(String, Map, List) decodeLongs(label, settings,
	 * null)}. The value to be returned is converted into a List&lt;Long&gt; using a best-effort
	 * strategy.
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link List} of {@link Long} values
	 */
	public static List<Long> decodeLongs(String label, Map<String, ?> settings) {
		return CfgTools.decodeLongs(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link List} of {@link Long} values,
	 * returning the list stored (may be {@code null}), or the setting's
	 * {@link ConfigurationSetting#getDefaultValue() default value} (converted to a
	 * List&lt;Long&gt;) if it's not defined. This is equivalent to calling
	 * {@link #decodeLongs(String, Map, List) decodeLongs(setting.getLabel(), settings,
	 * setting.getDefaultValue())}. The value to be returned is converted into a List&lt;Long&gt;
	 * using a best-effort strategy.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link List} of {@link Long} values
	 */
	public static List<Long> decodeLongs(ConfigurationSetting setting, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValues(setting.getLabel(), settings, new SettingDefault<>(setting), CfgTools.CONV_Long);
	}

	/**
	 * Decode the named setting from the given map as a {@link Float} value, returning the value
	 * stored (may be {@code null}), or {@code defaultValue} if it's not defined. The value to be
	 * returned is converted into a Float using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link Float} value
	 */
	public static Float decodeFloat(String label, Map<String, ?> settings, Float defaultValue) {
		return CfgTools.getValue(label, settings, (c) -> defaultValue, CfgTools.CONV_Float);
	}

	/**
	 * Decode the named setting from the given map as a {@link Float} value, returning the value
	 * stored (may be {@code null}), or {@code null} if it's not defined. This is equivalent to
	 * calling {@link #decodeFloat(String, Map, Float) decodeFloat(label, settings, null)}. The
	 * value to be returned is converted into a Float using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link Float} value
	 */
	public static Float decodeFloat(String label, Map<String, ?> settings) {
		return CfgTools.decodeFloat(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link Float} value, returning the value
	 * stored (may be {@code null}), or the setting's {@link ConfigurationSetting#getDefaultValue()
	 * default value} if it's not defined. This is equivalent to calling
	 * {@link #decodeFloat(String, Map, Float) decodeFloat(setting.getLabel(), settings,
	 * setting.getDefaultValue())}. The value to be returned is converted into a Float using a
	 * best-effort strategy.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link Float} value
	 */
	public static Float decodeFloat(ConfigurationSetting setting, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValue(setting.getLabel(), settings, new SettingDefault<>(setting), CfgTools.CONV_Float);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of {@link Float} values,
	 * returning the List stored (may be {@code null}), or {@code defaultValue} if it's not defined.
	 * The value to be returned is converted into a List&lt;Float&gt; using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link List} of {@link Float} values
	 */
	public static List<Float> decodeFloats(String label, Map<String, ?> settings, List<Float> defaultValue) {
		return CfgTools.getValues(label, settings, (c) -> defaultValue, CfgTools.CONV_Float);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of @link Float} values,
	 * returning the list stored (may be {@code null}), or {@code null} if it's not defined. This is
	 * equivalent to calling {@link #decodeFloats(String, Map, List) decodeFloats(label, settings,
	 * null)}. The value to be returned is converted into a List&lt;Float&gt; using a best-effort
	 * strategy.
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link List} of {@link Float} values
	 */
	public static List<Float> decodeFloats(String label, Map<String, ?> settings) {
		return CfgTools.decodeFloats(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link List} of {@link Float} values,
	 * returning the list stored (may be {@code null}), or the setting's
	 * {@link ConfigurationSetting#getDefaultValue() default value} (converted to a
	 * List&lt;Float&gt;) if it's not defined. This is equivalent to calling
	 * {@link #decodeFloats(String, Map, List) decodeFloats(setting.getLabel(), settings,
	 * setting.getDefaultValue())}. The value to be returned is converted into a List&lt;Float&gt;
	 * using a best-effort strategy.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link List} of {@link Float} values
	 */
	public static List<Float> decodeFloats(ConfigurationSetting setting, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValues(setting.getLabel(), settings, new SettingDefault<>(setting), CfgTools.CONV_Float);
	}

	/**
	 * Decode the named setting from the given map as a {@link Double} value, returning the value
	 * stored (may be {@code null}), or {@code defaultValue} if it's not defined. The value to be
	 * returned is converted into a Double using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link Double} value
	 */
	public static Double decodeDouble(String label, Map<String, ?> settings, Double defaultValue) {
		return CfgTools.getValue(label, settings, (c) -> defaultValue, CfgTools.CONV_Double);
	}

	/**
	 * Decode the named setting from the given map as a {@link Double} value, returning the value
	 * stored (may be {@code null}), or {@code null} if it's not defined. This is equivalent to
	 * calling {@link #decodeDouble(String, Map, Double) decodeDouble(label, settings, null)}. The
	 * value to be returned is converted into a Double using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link Double} value
	 */
	public static Double decodeDouble(String label, Map<String, ?> settings) {
		return CfgTools.decodeDouble(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link Double} value, returning the value
	 * stored (may be {@code null}), or the setting's {@link ConfigurationSetting#getDefaultValue()
	 * default value} if it's not defined. This is equivalent to calling
	 * {@link #decodeDouble(String, Map, Double) decodeDouble(setting.getLabel(), settings,
	 * setting.getDefaultValue())}. The value to be returned is converted into a Double using a
	 * best-effort strategy.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link Double} value
	 */
	public static Double decodeDouble(ConfigurationSetting setting, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValue(setting.getLabel(), settings, new SettingDefault<>(setting), CfgTools.CONV_Double);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of {@link Double} values,
	 * returning the List stored (may be {@code null}), or {@code defaultValue} if it's not defined.
	 * The value to be returned is converted into a List&lt;Double&gt; using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link List} of {@link Double} values
	 */
	public static List<Double> decodeDoubles(String label, Map<String, ?> settings, List<Double> defaultValue) {
		return CfgTools.getValues(label, settings, (c) -> defaultValue, CfgTools.CONV_Double);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of @link Double} values,
	 * returning the list stored (may be {@code null}), or {@code null} if it's not defined. This is
	 * equivalent to calling {@link #decodeDoubles(String, Map, List) decodeDoubles(label, settings,
	 * null)}. The value to be returned is converted into a List&lt;Double&gt; using a best-effort
	 * strategy.
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link List} of {@link Double} values
	 */
	public static List<Double> decodeDoubles(String label, Map<String, ?> settings) {
		return CfgTools.decodeDoubles(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link List} of {@link Double} values,
	 * returning the list stored (may be {@code null}), or the setting's
	 * {@link ConfigurationSetting#getDefaultValue() default value} (converted to a
	 * List&lt;Double&gt;) if it's not defined. This is equivalent to calling
	 * {@link #decodeDoubles(String, Map, List) decodeDoubles(setting.getLabel(), settings,
	 * setting.getDefaultValue())}. The value to be returned is converted into a List&lt;Double&gt;
	 * using a best-effort strategy.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link List} of {@link Double} values
	 */
	public static List<Double> decodeDoubles(ConfigurationSetting setting, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValues(setting.getLabel(), settings, new SettingDefault<>(setting), CfgTools.CONV_Double);
	}

	/**
	 * Decode the named setting from the given map as a {@link BigInteger} value, returning the
	 * value stored (may be {@code null}), or {@code defaultValue} if it's not defined. The value to
	 * be returned is converted into a BigInteger using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link BigInteger} value
	 */
	public static BigInteger decodeBigInteger(String label, Map<String, ?> settings, BigInteger defaultValue) {
		return CfgTools.getValue(label, settings, (c) -> defaultValue, CfgTools.CONV_BigInteger);
	}

	/**
	 * Decode the named setting from the given map as a {@link BigInteger} value, returning the
	 * value stored (may be {@code null}), or {@code null} if it's not defined. This is equivalent
	 * to calling {@link #decodeBigInteger(String, Map, BigInteger) decodeBigInteger(label,
	 * settings, null)}. The value to be returned is converted into a BigInteger using a best-effort
	 * strategy.
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link BigInteger} value
	 */
	public static BigInteger decodeBigInteger(String label, Map<String, ?> settings) {
		return CfgTools.decodeBigInteger(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link BigInteger} value, returning the
	 * value stored (may be {@code null}), or the setting's
	 * {@link ConfigurationSetting#getDefaultValue() default value} if it's not defined. This is
	 * equivalent to calling {@link #decodeBigInteger(String, Map, BigInteger)
	 * decodeBigInteger(setting.getLabel(), settings, setting.getDefaultValue())}. The value to be
	 * returned is converted into a BigInteger using a best-effort strategy.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link BigInteger} value
	 */
	public static BigInteger decodeBigInteger(ConfigurationSetting setting, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValue(setting.getLabel(), settings, new SettingDefault<>(setting), CfgTools.CONV_BigInteger);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of {@link BigInteger} values,
	 * returning the List stored (may be {@code null}), or {@code defaultValue} if it's not defined.
	 * The value to be returned is converted into a List&lt;BigInteger&gt; using a best-effort
	 * strategy.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link List} of {@link BigInteger} values
	 */
	public static List<BigInteger> decodeBigIntegers(String label, Map<String, ?> settings,
		List<BigInteger> defaultValue) {
		return CfgTools.getValues(label, settings, (c) -> defaultValue, CfgTools.CONV_BigInteger);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of @link BigInteger} values,
	 * returning the list stored (may be {@code null}), or {@code null} if it's not defined. This is
	 * equivalent to calling {@link #decodeBigIntegers(String, Map, List) decodeBigIntegers(label,
	 * settings, null)}. The value to be returned is converted into a List&lt;BigInteger&gt; using a
	 * best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link List} of {@link BigInteger} values
	 */
	public static List<BigInteger> decodeBigIntegers(String label, Map<String, ?> settings) {
		return CfgTools.decodeBigIntegers(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link List} of {@link BigInteger} values,
	 * returning the list stored (may be {@code null}), or the setting's
	 * {@link ConfigurationSetting#getDefaultValue() default value} (converted to a
	 * List&lt;BigInteger&gt;) if it's not defined. This is equivalent to calling
	 * {@link #decodeBigIntegers(String, Map, List) decodeBigIntegers(setting.getLabel(), settings,
	 * setting.getDefaultValue())}. The value to be returned is converted into a
	 * List&lt;BigInteger&gt; using a best-effort strategy.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link List} of {@link BigInteger} values
	 */
	public static List<BigInteger> decodeBigIntegers(ConfigurationSetting setting, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValues(setting.getLabel(), settings, new SettingDefault<>(setting),
			CfgTools.CONV_BigInteger);
	}

	/**
	 * Decode the named setting from the given map as a {@link BigDecimal} value, returning the
	 * value stored (may be {@code null}), or {@code defaultValue} if it's not defined. The value to
	 * be returned is converted into a BigDecimal using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link BigDecimal} value
	 */
	public static BigDecimal decodeBigDecimal(String label, Map<String, ?> settings, BigDecimal defaultValue) {
		return CfgTools.getValue(label, settings, (c) -> defaultValue, CfgTools.CONV_BigDecimal);
	}

	/**
	 * Decode the named setting from the given map as a {@link BigDecimal} value, returning the
	 * value stored (may be {@code null}), or {@code null} if it's not defined. This is equivalent
	 * to calling {@link #decodeBigDecimal(String, Map, BigDecimal) decodeBigDecimal(label,
	 * settings, null)}. The value to be returned is converted into a BigDecimal using a best-effort
	 * strategy.
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link BigDecimal} value
	 */
	public static BigDecimal decodeBigDecimal(String label, Map<String, ?> settings) {
		return CfgTools.decodeBigDecimal(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link BigDecimal} value, returning the
	 * value stored (may be {@code null}), or the setting's
	 * {@link ConfigurationSetting#getDefaultValue() default value} if it's not defined. This is
	 * equivalent to calling {@link #decodeBigDecimal(String, Map, BigDecimal)
	 * decodeBigDecimal(setting.getLabel(), settings, setting.getDefaultValue())}. The value to be
	 * returned is converted into a BigDecimal using a best-effort strategy.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link BigDecimal} value
	 */
	public static BigDecimal decodeBigDecimal(ConfigurationSetting setting, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValue(setting.getLabel(), settings, new SettingDefault<>(setting), CfgTools.CONV_BigDecimal);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of {@link BigDecimal} values,
	 * returning the List stored (may be {@code null}), or {@code defaultValue} if it's not defined.
	 * The value to be returned is converted into a List&lt;BigDecimal&gt; using a best-effort
	 * strategy.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link List} of {@link BigDecimal} values
	 */
	public static List<BigDecimal> decodeBigDecimals(String label, Map<String, ?> settings,
		List<BigDecimal> defaultValue) {
		return CfgTools.getValues(label, settings, (c) -> defaultValue, CfgTools.CONV_BigDecimal);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of @link BigDecimal} values,
	 * returning the list stored (may be {@code null}), or {@code null} if it's not defined. This is
	 * equivalent to calling {@link #decodeBigDecimals(String, Map, List) decodeBigDecimals(label,
	 * settings, null)}. The value to be returned is converted into a List&lt;BigDecimal&gt; using a
	 * best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link List} of {@link BigDecimal} values
	 */
	public static List<BigDecimal> decodeBigDecimals(String label, Map<String, ?> settings) {
		return CfgTools.decodeBigDecimals(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link List} of {@link BigDecimal} values,
	 * returning the list stored (may be {@code null}), or the setting's
	 * {@link ConfigurationSetting#getDefaultValue() default value} (converted to a
	 * List&lt;BigDecimal&gt;) if it's not defined. This is equivalent to calling
	 * {@link #decodeBigDecimals(String, Map, List) decodeBigDecimals(setting.getLabel(), settings,
	 * setting.getDefaultValue())}. The value to be returned is converted into a
	 * List&lt;BigDecimal&gt; using a best-effort strategy.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link List} of {@link BigDecimal} values
	 */
	public static List<BigDecimal> decodeBigDecimals(ConfigurationSetting setting, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValues(setting.getLabel(), settings, new SettingDefault<>(setting),
			CfgTools.CONV_BigDecimal);
	}

	/**
	 * Decode the named setting from the given map as a {@link String} value, returning the value
	 * stored (may be {@code null}), or {@code defaultValue} if it's not defined. The value to be
	 * returned is converted into a String using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link String} value
	 */
	public static String decodeString(String label, Map<String, ?> settings, String defaultValue) {
		return CfgTools.getValue(label, settings, (c) -> defaultValue, CfgTools.CONV_String);
	}

	/**
	 * Decode the named setting from the given map as a {@link String} value, returning the value
	 * stored (may be {@code null}), or {@code null} if it's not defined. This is equivalent to
	 * calling {@link #decodeString(String, Map, String) decodeString(label, settings, null)}. The
	 * value to be returned is converted into a String using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link String} value
	 */
	public static String decodeString(String label, Map<String, ?> settings) {
		return CfgTools.decodeString(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link String} value, returning the value
	 * stored (may be {@code null}), or the setting's {@link ConfigurationSetting#getDefaultValue()
	 * default value} if it's not defined. This is equivalent to calling
	 * {@link #decodeString(String, Map, String) decodeString(setting.getLabel(), settings,
	 * setting.getDefaultValue())}. The value to be returned is converted into a String using a
	 * best-effort strategy.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link String} value
	 */
	public static String decodeString(ConfigurationSetting setting, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValue(setting.getLabel(), settings, new SettingDefault<>(setting), CfgTools.CONV_String);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of {@link String} values,
	 * returning the List stored (may be {@code null}), or {@code defaultValue} if it's not defined.
	 * The value to be returned is converted into a List&lt;String&gt; using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link List} of {@link String} values
	 */
	public static List<String> decodeStrings(String label, Map<String, ?> settings, List<String> defaultValue) {
		return CfgTools.getValues(label, settings, (c) -> defaultValue, CfgTools.CONV_String);
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of @link String} values,
	 * returning the list stored (may be {@code null}), or {@code null} if it's not defined. This is
	 * equivalent to calling {@link #decodeStrings(String, Map, List) decodeStrings(label, settings,
	 * null)}. The value to be returned is converted into a List&lt;String&gt; using a best-effort
	 * strategy.
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link List} of {@link String} values
	 */
	public static List<String> decodeStrings(String label, Map<String, ?> settings) {
		return CfgTools.decodeStrings(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link List} of {@link String} values,
	 * returning the list stored (may be {@code null}), or the setting's
	 * {@link ConfigurationSetting#getDefaultValue() default value} (converted to a
	 * List&lt;String&gt;) if it's not defined. This is equivalent to calling
	 * {@link #decodeStrings(String, Map, List) decodeStrings(setting.getLabel(), settings,
	 * setting.getDefaultValue())}. The value to be returned is converted into a List&lt;String&gt;
	 * using a best-effort strategy.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link List} of {@link String} values
	 */
	public static List<String> decodeStrings(ConfigurationSetting setting, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.getValues(setting.getLabel(), settings, new SettingDefault<>(setting), CfgTools.CONV_String);
	}

	/**
	 * Decode the named setting from the given map as a {@code byte[]} value, returning the value
	 * stored (may be {@code null}), or {@code defaultValue} if it's not defined. The value to be
	 * returned is converted into a byte[] using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@code byte[]} value
	 */
	public static byte[] decodeBinary(String label, Map<String, ?> settings, byte[] defaultValue)
		throws DecoderException {
		try {
			return CfgTools.getValue(label, settings, (c) -> defaultValue, CfgTools.CONV_Binary);
		} catch (ValueDecoderException e) {
			throw new DecoderException(e.getMessage(), e);
		}
	}

	/**
	 * Decode the named setting from the given map as a {@code byte[]} value, returning the value
	 * stored (may be {@code null}), or {@code null} if it's not defined. This is equivalent to
	 * calling {@link #decodeBinary(String, Map, byte[]) decodeBinary(label, settings, null)}. The
	 * value to be returned is converted into a byte[] using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@code byte[]} value
	 * @throws DecoderException
	 */
	public static byte[] decodeBinary(String label, Map<String, ?> settings) throws DecoderException {
		return CfgTools.decodeBinary(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@code byte[]} value, returning the value
	 * stored (may be {@code null}), or the setting's {@link ConfigurationSetting#getDefaultValue()
	 * default value} if it's not defined. This is equivalent to calling
	 * {@link #decodeBinary(String, Map, byte[]) decodeBinary(setting.getLabel(), settings,
	 * setting.getDefaultValue())}. The value to be returned is converted into a byte[] using a
	 * best-effort strategy.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@code byte[]} value
	 */
	public static byte[] decodeBinary(ConfigurationSetting setting, Map<String, ?> settings) throws DecoderException {
		CfgTools.validateSetting(setting);
		try {
			return CfgTools.getValue(setting.getLabel(), settings, new SettingDefault<>(setting), CfgTools.CONV_Binary);
		} catch (ValueDecoderException e) {
			throw new DecoderException(e.getMessage(), e);
		}
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of {@code byte[]} values,
	 * returning the List stored (may be {@code null}), or {@code defaultValue} if it's not defined.
	 * The value to be returned is converted into a List&lt;byte[]&gt; using a best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link List} of {@code byte[]} values
	 */
	public static List<byte[]> decodeBinaries(String label, Map<String, ?> settings, List<byte[]> defaultValue)
		throws DecoderException {
		try {
			return CfgTools.getValues(label, settings, (c) -> defaultValue, CfgTools.CONV_Binary);
		} catch (ValueDecoderException e) {
			throw new DecoderException(e.getMessage(), e);
		}
	}

	/**
	 * Decode the named setting from the given map as a {@link List} of @code byte[]} values,
	 * returning the list stored (may be {@code null}), or {@code null} if it's not defined. This is
	 * equivalent to calling {@link #decodeBinaries(String, Map, List) decodeBinaries(label,
	 * settings, null)}. The value to be returned is converted into a List&lt;byte[]&gt; using a
	 * best-effort strategy.
	 *
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link List} of {@code byte[]} values
	 * @throws DecoderException
	 */
	public static List<byte[]> decodeBinaries(String label, Map<String, ?> settings) throws DecoderException {
		return CfgTools.decodeBinaries(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link List} of {@code byte[]} values,
	 * returning the list stored (may be {@code null}), or the setting's
	 * {@link ConfigurationSetting#getDefaultValue() default value} (converted to a
	 * List&lt;Binary&gt;) if it's not defined. This is equivalent to calling
	 * {@link #decodeBinaries(String, Map, List) decodeBinaries(setting.getLabel(), settings,
	 * setting.getDefaultValue())}. The value to be returned is converted into a List&lt;byte[]&gt;
	 * using a best-effort strategy.
	 *
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link List} of {@code byte[]} values
	 */
	public static List<byte[]> decodeBinaries(ConfigurationSetting setting, Map<String, ?> settings)
		throws DecoderException {
		CfgTools.validateSetting(setting);
		try {
			return CfgTools.getValues(setting.getLabel(), settings, new SettingDefault<>(setting),
				CfgTools.CONV_Binary);
		} catch (ValueDecoderException e) {
			throw new DecoderException(e.getMessage(), e);
		}

	}

	/**
	 * Check to see if the given setting has a value set ({@code null} or otherwise).
	 *
	 * @param setting
	 * @param settings
	 * @return {@code true} if the setting has a set value, {@code false} otherwise
	 */
	public static boolean hasValue(String setting, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return settings.containsKey(setting);
	}

	/**
	 * Check to see if the given setting has a value set ({@code null} or otherwise).
	 *
	 * @param setting
	 * @param settings
	 * @return {@code true} if the setting has a set value, {@code false} otherwise
	 */
	public static boolean hasValue(ConfigurationSetting setting, Map<String, ?> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.hasValue(setting.getLabel(), settings);
	}

	public static <A, B, C> BiFunction<A, B, C> ignoreFailures() {
		return (a, b) -> null;
	}

	private Map<String, ?> settings;

	public CfgTools() {
		this(null);
	}

	public CfgTools(Map<String, ?> settings) {
		if (settings == null) {
			settings = Collections.emptyMap();
		}
		this.settings = settings;
	}

	public Object getObject(String setting, Object defaultValue) {
		return CfgTools.decodeObject(setting, this.settings, defaultValue);
	}

	public Object getObject(String setting) {
		return CfgTools.decodeObject(setting, this.settings);
	}

	public Object getObject(ConfigurationSetting setting) {
		return CfgTools.decodeObject(setting, this.settings);
	}

	public List<Object> getObjects(String setting, List<Object> defaultValue) {
		return CfgTools.decodeObjects(setting, this.settings, defaultValue);
	}

	public List<Object> getObjects(String setting) {
		return CfgTools.decodeObjects(setting, this.settings);
	}

	public List<Object> getObjects(ConfigurationSetting setting) {
		return CfgTools.decodeObjects(setting, this.settings);
	}

	public <E extends Enum<E>> E getEnum(String setting, Class<E> enumClass, E defaultValue) {
		return CfgTools.decodeEnum(setting, enumClass, this.settings, defaultValue);
	}

	public <E extends Enum<E>> E getEnum(String setting, Class<E> enumClass) {
		return CfgTools.decodeEnum(setting, enumClass, this.settings);
	}

	public <E extends Enum<E>> E getEnum(ConfigurationSetting setting, Class<E> enumClass) {
		return CfgTools.decodeEnum(setting, enumClass, this.settings);
	}

	public <E extends Enum<E>> E getEnum(String setting, Class<E> enumClass,
		BiFunction<Object, Exception, E> invalidHandler, E defaultValue) {
		return CfgTools.decodeEnum(setting, enumClass, invalidHandler, this.settings, defaultValue);
	}

	public <E extends Enum<E>> E getEnum(String setting, Class<E> enumClass,
		BiFunction<Object, Exception, E> invalidHandler) {
		return CfgTools.decodeEnum(setting, enumClass, invalidHandler, this.settings);
	}

	public <E extends Enum<E>> E getEnum(ConfigurationSetting setting, Class<E> enumClass,
		BiFunction<Object, Exception, E> invalidHandler) {
		return CfgTools.decodeEnum(setting, enumClass, invalidHandler, this.settings);
	}

	public <E extends Enum<E>> List<E> getEnums(String setting, Class<E> enumClass,
		BiFunction<Object, Exception, E> invalidHandler, List<E> defaultValue) {
		return CfgTools.decodeEnums(setting, enumClass, invalidHandler, this.settings, defaultValue);
	}

	public <E extends Enum<E>> List<E> getEnums(String setting, Class<E> enumClass,
		BiFunction<Object, Exception, E> invalidHandler) {
		return CfgTools.decodeEnums(setting, enumClass, invalidHandler, this.settings);
	}

	public <E extends Enum<E>> List<E> getEnums(ConfigurationSetting setting, Class<E> enumClass,
		BiFunction<Object, Exception, E> invalidHandler) {
		return CfgTools.decodeEnums(setting, enumClass, invalidHandler, this.settings);
	}

	public <E extends Enum<E>> List<E> getEnums(String setting, Class<E> enumClass, List<E> defaultValue) {
		return CfgTools.decodeEnums(setting, enumClass, this.settings, defaultValue);
	}

	public <E extends Enum<E>> List<E> getEnums(String setting, Class<E> enumClass) {
		return CfgTools.decodeEnums(setting, enumClass, this.settings);
	}

	public <E extends Enum<E>> List<E> getEnums(ConfigurationSetting setting, Class<E> enumClass) {
		return CfgTools.decodeEnums(setting, enumClass, this.settings);
	}

	public Boolean getBoolean(String setting, Boolean defaultValue) {
		return CfgTools.decodeBoolean(setting, this.settings, defaultValue);
	}

	public Boolean getBoolean(String setting) {
		return CfgTools.decodeBoolean(setting, this.settings);
	}

	public Boolean getBoolean(ConfigurationSetting setting) {
		return CfgTools.decodeBoolean(setting, this.settings);
	}

	public List<Boolean> getBooleans(String setting, List<Boolean> defaultValue) {
		return CfgTools.decodeBooleans(setting, this.settings, defaultValue);
	}

	public List<Boolean> getBooleans(String setting) {
		return CfgTools.decodeBooleans(setting, this.settings);
	}

	public List<Boolean> getBooleans(ConfigurationSetting setting) {
		return CfgTools.decodeBooleans(setting, this.settings);
	}

	public Byte getByte(String setting, Byte defaultValue) {
		return CfgTools.decodeByte(setting, this.settings, defaultValue);
	}

	public Byte getByte(String setting) {
		return CfgTools.decodeByte(setting, this.settings);
	}

	public Byte getByte(ConfigurationSetting setting) {
		return CfgTools.decodeByte(setting, this.settings);
	}

	public List<Byte> getBytes(String setting, List<Byte> defaultValue) {
		return CfgTools.decodeBytes(setting, this.settings, defaultValue);
	}

	public List<Byte> getBytes(String setting) {
		return CfgTools.decodeBytes(setting, this.settings);
	}

	public List<Byte> getBytes(ConfigurationSetting setting) {
		return CfgTools.decodeBytes(setting, this.settings);
	}

	public Short getShort(String setting, Short defaultValue) {
		return CfgTools.decodeShort(setting, this.settings, defaultValue);
	}

	public Short getShort(String setting) {
		return CfgTools.decodeShort(setting, this.settings);
	}

	public Short getShort(ConfigurationSetting setting) {
		return CfgTools.decodeShort(setting, this.settings);
	}

	public List<Short> getShorts(String setting, List<Short> defaultValue) {
		return CfgTools.decodeShorts(setting, this.settings, defaultValue);
	}

	public List<Short> getShorts(String setting) {
		return CfgTools.decodeShorts(setting, this.settings);
	}

	public List<Short> getShorts(ConfigurationSetting setting) {
		return CfgTools.decodeShorts(setting, this.settings);
	}

	public Integer getInteger(String setting, Integer defaultValue) {
		return CfgTools.decodeInteger(setting, this.settings, defaultValue);
	}

	public Integer getInteger(String setting) {
		return CfgTools.decodeInteger(setting, this.settings);
	}

	public Integer getInteger(ConfigurationSetting setting) {
		return CfgTools.decodeInteger(setting, this.settings);
	}

	public List<Integer> getIntegers(String setting, List<Integer> defaultValue) {
		return CfgTools.decodeIntegers(setting, this.settings, defaultValue);
	}

	public List<Integer> getIntegers(String setting) {
		return CfgTools.decodeIntegers(setting, this.settings);
	}

	public List<Integer> getIntegers(ConfigurationSetting setting) {
		return CfgTools.decodeIntegers(setting, this.settings);
	}

	public Long getLong(String setting, Long defaultValue) {
		return CfgTools.decodeLong(setting, this.settings, defaultValue);
	}

	public Long getLong(String setting) {
		return CfgTools.decodeLong(setting, this.settings);
	}

	public Long getLong(ConfigurationSetting setting) {
		return CfgTools.decodeLong(setting, this.settings);
	}

	public List<Long> getLongs(String setting, List<Long> defaultValue) {
		return CfgTools.decodeLongs(setting, this.settings, defaultValue);
	}

	public List<Long> getLongs(String setting) {
		return CfgTools.decodeLongs(setting, this.settings);
	}

	public List<Long> getLongs(ConfigurationSetting setting) {
		return CfgTools.decodeLongs(setting, this.settings);
	}

	public Float getFloat(String setting, Float defaultValue) {
		return CfgTools.decodeFloat(setting, this.settings, defaultValue);
	}

	public Float getFloat(String setting) {
		return CfgTools.decodeFloat(setting, this.settings);
	}

	public Float getFloat(ConfigurationSetting setting) {
		return CfgTools.decodeFloat(setting, this.settings);
	}

	public List<Float> getFloats(String setting, List<Float> defaultValue) {
		return CfgTools.decodeFloats(setting, this.settings, defaultValue);
	}

	public List<Float> getFloats(String setting) {
		return CfgTools.decodeFloats(setting, this.settings);
	}

	public List<Float> getFloats(ConfigurationSetting setting) {
		return CfgTools.decodeFloats(setting, this.settings);
	}

	public Double getDouble(String setting, Double defaultValue) {
		return CfgTools.decodeDouble(setting, this.settings, defaultValue);
	}

	public Double getDouble(String setting) {
		return CfgTools.decodeDouble(setting, this.settings);
	}

	public Double getDouble(ConfigurationSetting setting) {
		return CfgTools.decodeDouble(setting, this.settings);
	}

	public List<Double> getDoubles(String setting, List<Double> defaultValue) {
		return CfgTools.decodeDoubles(setting, this.settings, defaultValue);
	}

	public List<Double> getDoubles(String setting) {
		return CfgTools.decodeDoubles(setting, this.settings);
	}

	public List<Double> getDoubles(ConfigurationSetting setting) {
		return CfgTools.decodeDoubles(setting, this.settings);
	}

	public BigInteger getBigInteger(String setting, BigInteger defaultValue) {
		return CfgTools.decodeBigInteger(setting, this.settings, defaultValue);
	}

	public BigInteger getBigInteger(String setting) {
		return CfgTools.decodeBigInteger(setting, this.settings);
	}

	public BigInteger getBigInteger(ConfigurationSetting setting) {
		return CfgTools.decodeBigInteger(setting, this.settings);
	}

	public List<BigInteger> getBigIntegers(String setting, List<BigInteger> defaultValue) {
		return CfgTools.decodeBigIntegers(setting, this.settings, defaultValue);
	}

	public List<BigInteger> getBigIntegers(String setting) {
		return CfgTools.decodeBigIntegers(setting, this.settings);
	}

	public List<BigInteger> getBigIntegers(ConfigurationSetting setting) {
		return CfgTools.decodeBigIntegers(setting, this.settings);
	}

	public BigDecimal getBigDecimal(String setting, BigDecimal defaultValue) {
		return CfgTools.decodeBigDecimal(setting, this.settings, defaultValue);
	}

	public BigDecimal getBigDecimal(String setting) {
		return CfgTools.decodeBigDecimal(setting, this.settings);
	}

	public BigDecimal getBigDecimal(ConfigurationSetting setting) {
		return CfgTools.decodeBigDecimal(setting, this.settings);
	}

	public List<BigDecimal> getBigDecimals(String setting, List<BigDecimal> defaultValue) {
		return CfgTools.decodeBigDecimals(setting, this.settings, defaultValue);
	}

	public List<BigDecimal> getBigDecimals(String setting) {
		return CfgTools.decodeBigDecimals(setting, this.settings);
	}

	public List<BigDecimal> getBigDecimals(ConfigurationSetting setting) {
		return CfgTools.decodeBigDecimals(setting, this.settings);
	}

	public byte[] getBinary(String setting, byte[] defaultValue) throws DecoderException {
		return CfgTools.decodeBinary(setting, this.settings, defaultValue);
	}

	public byte[] getBinary(String setting) throws DecoderException {
		return CfgTools.decodeBinary(setting, this.settings);
	}

	public byte[] getBinary(ConfigurationSetting setting) throws DecoderException {
		return CfgTools.decodeBinary(setting, this.settings);
	}

	public List<byte[]> getBinaries(String setting, List<byte[]> defaultValue) throws DecoderException {
		return CfgTools.decodeBinaries(setting, this.settings, defaultValue);
	}

	public List<byte[]> getBinaries(String setting) throws DecoderException {
		return CfgTools.decodeBinaries(setting, this.settings);
	}

	public List<byte[]> getBinaries(ConfigurationSetting setting) throws DecoderException {
		return CfgTools.decodeBinaries(setting, this.settings);
	}

	public String getString(String setting, String defaultValue) {
		return CfgTools.decodeString(setting, this.settings, defaultValue);
	}

	public String getString(String setting) {
		return CfgTools.decodeString(setting, this.settings);
	}

	public String getString(ConfigurationSetting setting) {
		return CfgTools.decodeString(setting, this.settings);
	}

	public List<String> getStrings(String setting, List<String> defaultValue) {
		return CfgTools.decodeStrings(setting, this.settings, defaultValue);
	}

	public List<String> getStrings(String setting) {
		return CfgTools.decodeStrings(setting, this.settings);
	}

	public List<String> getStrings(ConfigurationSetting setting) {
		return CfgTools.decodeStrings(setting, this.settings);
	}

	public int getCount() {
		return this.settings.size();
	}

	public boolean hasValue(String setting) {
		return CfgTools.hasValue(setting, this.settings);
	}

	public boolean hasValue(ConfigurationSetting setting) {
		return CfgTools.hasValue(setting, this.settings);
	}

	public Set<String> getSettings() {
		return this.settings.keySet();
	}

	@Override
	public String toString() {
		return String.format("CfgTools %s", this.settings);
	}
}
