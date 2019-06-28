/*******************************************************************************
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (C) 2013 - 2019 Armedia
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
package com.armedia.commons.utilities.codec;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;

import com.armedia.commons.utilities.Tools;
import com.armedia.commons.utilities.function.CheckedFunction;

public class EnumCodec<E extends Enum<E>> implements Codec<E, String> {

	private static final Iterable<Flag> NO_FLAGS = Collections.emptyList();

	public static enum Flag {
		//
		STRICT_CASE, //
		MARSHAL_FOLDED, //
		//
		;
	}

	private final Class<E> enumClass;
	private final String nullString;
	private final CheckedFunction<E, String, Exception> specialEncoder;
	private final E nullEnum;
	private final CheckedFunction<String, E, Exception> specialDecoder;
	private final Map<String, E> caseInsensitiveMap;
	private final Set<String> validEncodedValues;
	private final boolean marshalFolded;

	private static <E extends Enum<E>> boolean canIgnoreCase(Class<E> enumClass) {
		Set<String> lower = new HashSet<>();
		for (E e : enumClass.getEnumConstants()) {
			if (!lower.add(e.name().toLowerCase())) { return false; }
		}
		return true;
	}

	public EnumCodec(Class<E> enumClass) {
		this(enumClass, null, EnumCodec.NO_FLAGS);
	}

	public EnumCodec(Class<E> enumClass, Flag... flags) {
		this(enumClass, null, flags);
	}

	public EnumCodec(Class<E> enumClass, Iterable<Flag> flags) {
		this(enumClass, null, flags);
	}

	public EnumCodec(Class<E> enumClass, CheckedCodec<E, String, ?> specialCodec) {
		this(enumClass, specialCodec, EnumCodec.NO_FLAGS);
	}

	public EnumCodec(Class<E> enumClass, CheckedCodec<E, String, ?> specialCodec, Flag... flags) {
		this(enumClass, specialCodec, (flags != null ? Arrays.asList(flags) : Collections.emptyList()));
	}

	public EnumCodec(Class<E> enumClass, CheckedCodec<E, String, ?> specialCodec, Iterable<Flag> flags) {
		this.enumClass = Objects.requireNonNull(enumClass, "Must provide a non-null Enum class");
		if (!enumClass.isEnum()) {
			throw new IllegalArgumentException(
				String.format("The class %s is not a valid Enum", enumClass.getCanonicalName()));
		}
		Set<Flag> f = EnumSet.noneOf(Flag.class);
		for (Flag flag : flags) {
			if (flag != null) {
				f.add(flag);
			}
		}

		final boolean ignoreCase = EnumCodec.canIgnoreCase(enumClass) && !f.contains(Flag.STRICT_CASE);
		this.marshalFolded = (!ignoreCase || f.contains(Flag.MARSHAL_FOLDED));

		Map<String, E> m = null;
		if (ignoreCase) {
			m = new LinkedHashMap<>();
			for (E e : enumClass.getEnumConstants()) {
				m.put(e.name().toLowerCase(), e);
			}
			if (m.isEmpty()) {
				m = null;
			}
		}
		this.caseInsensitiveMap = ((m != null) ? Tools.freezeMap(m) : null);
		if (this.caseInsensitiveMap != null) {
			this.validEncodedValues = this.caseInsensitiveMap.keySet();
		} else {
			Set<String> validMarshalledValues = new TreeSet<>();
			for (E e : enumClass.getEnumConstants()) {
				validMarshalledValues.add(e.name());
			}
			this.validEncodedValues = Tools.freezeSet(validMarshalledValues);
		}

		if (specialCodec != null) {
			this.nullString = specialCodec.getNullEncoding();
			this.specialEncoder = specialCodec::encode;
			this.nullEnum = specialCodec.getNullValue();
			this.specialDecoder = specialCodec::decode;
		} else {
			this.nullString = null;
			this.specialEncoder = (e) -> null;
			this.nullEnum = null;
			this.specialDecoder = (s) -> null;
		}
	}

	@Override
	public final String getNullEncoding() {
		return this.nullString;
	}

	@Override
	public boolean isNullEncoding(String e) {
		if (e == null) { return true; }
		BiFunction<String, String, Boolean> comparer = Tools::equals;
		if (this.caseInsensitiveMap != null) {
			comparer = StringUtils::equalsIgnoreCase;
		}
		return comparer.apply(e, this.nullString);
	}

	@Override
	public String encode(E e) {
		try {
			return this.encodeChecked(e);
		} catch (Throwable t) {
			if (RuntimeException.class.isInstance(t)) { throw RuntimeException.class.cast(t); }
			if (Error.class.isInstance(t)) { throw Error.class.cast(t); }
			throw new RuntimeException(String.format("Failed to encode the enum value [%s]", e), t);
		}
	}

	@Override
	public final E getNullValue() {
		return this.nullEnum;
	}

	@Override
	public boolean isNullValue(E e) {
		return ((e == null) || (e == this.nullEnum));
	}

	@Override
	public E decode(String s) {
		try {
			return this.decodeChecked(s);
		} catch (Throwable t) {
			if (RuntimeException.class.isInstance(t)) { throw RuntimeException.class.cast(t); }
			if (Error.class.isInstance(t)) { throw Error.class.cast(t); }
			throw new RuntimeException(String.format("Failed to decode the String [%s]", s), t);
		}
	}

	public final boolean isCaseSensitive() {
		return (this.caseInsensitiveMap == null);
	}

	public final Set<String> getValidEncodedValues() {
		return this.validEncodedValues;
	}

	public final E decodeChecked(String v) throws Exception {
		if (isNullEncoding(v)) { return this.nullEnum; }

		// Make sure we stip out the spaces
		v = StringUtils.strip(v);

		// If there's no CI map, then valueOf() *must* return a valid value
		if (this.caseInsensitiveMap == null) {
			E ret = this.specialDecoder.applyChecked(v);
			if (ret == null) {
				ret = Enum.valueOf(this.enumClass, v);
			}
			return ret;
		}

		String folded = v.toLowerCase();
		E ret = this.specialDecoder.applyChecked(folded);
		if (ret == null) {
			// If this can be case-insensitive, we fold to lowercase and search
			ret = this.caseInsensitiveMap.get(folded);
		}
		if (ret != null) { return ret; }

		// No match, we emulate valueOf()'s IllegalArgumentException
		throw new IllegalArgumentException(String.format("The value [%s] is not a valid value for the enum class %s", v,
			this.enumClass.getCanonicalName()));
	}

	public final String encodeChecked(E e) throws Exception {
		if (isNullValue(e)) { return this.nullString; }
		String ret = this.specialEncoder.applyChecked(e);
		if (ret == null) {
			ret = e.name();
			if (this.marshalFolded && (this.caseInsensitiveMap != null)) {
				ret = ret.toLowerCase();
			}
		}
		return ret;
	}
}
