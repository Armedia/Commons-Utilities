package com.armedia.commons.utilities.xml;

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

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.StringUtils;

import com.armedia.commons.utilities.Codec;
import com.armedia.commons.utilities.Tools;

public class EnumCodec<E extends Enum<E>> extends XmlAdapter<String, E> implements Codec<E, String> {

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
	private final E nullEnum;
	private final Map<String, E> caseInsensitiveMap;
	private final Set<String> validMarshalledValues;
	private final boolean marshalFolded;

	private static <E extends Enum<E>> boolean canIgnoreCase(Class<E> enumClass) {
		Set<String> lower = new HashSet<>();
		for (E e : enumClass.getEnumConstants()) {
			if (!lower.add(e.name().toLowerCase())) { return false; }
		}
		return true;
	}

	public EnumCodec(Class<E> enumClass) {
		this(enumClass, null, null, EnumCodec.NO_FLAGS);
	}

	public EnumCodec(Class<E> enumClass, Flag... flags) {
		this(enumClass, null, null, flags);
	}

	public EnumCodec(Class<E> enumClass, Iterable<Flag> flags) {
		this(enumClass, null, null, flags);
	}

	public EnumCodec(Class<E> enumClass, String nullString) {
		this(enumClass, nullString, null, EnumCodec.NO_FLAGS);
	}

	public EnumCodec(Class<E> enumClass, String nullString, Flag... flags) {
		this(enumClass, nullString, null, flags);
	}

	public EnumCodec(Class<E> enumClass, String nullString, Iterable<Flag> flags) {
		this(enumClass, nullString, null, flags);
	}

	public EnumCodec(Class<E> enumClass, E nullEnum) {
		this(enumClass, null, nullEnum, EnumCodec.NO_FLAGS);
	}

	public EnumCodec(Class<E> enumClass, E nullEnum, Flag... flags) {
		this(enumClass, null, nullEnum, flags);
	}

	public EnumCodec(Class<E> enumClass, E nullEnum, Iterable<Flag> flags) {
		this(enumClass, null, nullEnum, flags);
	}

	public EnumCodec(Class<E> enumClass, String nullString, E nullEnum) {
		this(enumClass, nullString, nullEnum, EnumCodec.NO_FLAGS);
	}

	public EnumCodec(Class<E> enumClass, String nullString, E nullEnum, Flag... flags) {
		this(enumClass, nullString, nullEnum, (flags != null ? Arrays.asList(flags) : Collections.emptyList()));
	}

	public EnumCodec(Class<E> enumClass, String nullString, E nullEnum, Iterable<Flag> flags) {
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
		this.nullString = nullString;
		this.nullEnum = nullEnum;

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
			this.validMarshalledValues = this.caseInsensitiveMap.keySet();
		} else {
			Set<String> validMarshalledValues = new TreeSet<>();
			for (E e : enumClass.getEnumConstants()) {
				validMarshalledValues.add(e.name());
			}
			this.validMarshalledValues = Tools.freezeSet(validMarshalledValues);
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
			return this.marshal(e);
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
			return this.unmarshal(s);
		} catch (Throwable t) {
			if (RuntimeException.class.isInstance(t)) { throw RuntimeException.class.cast(t); }
			if (Error.class.isInstance(t)) { throw Error.class.cast(t); }
			throw new RuntimeException(String.format("Failed to decode the String [%s]", s), t);
		}
	}

	public final boolean isCaseSensitive() {
		return (this.caseInsensitiveMap == null);
	}

	public final Set<String> getValidMarshalledValues() {
		return this.validMarshalledValues;
	}

	protected E specialUnmarshal(String v) throws Exception {
		return null;
	}

	@Override
	public final E unmarshal(String v) throws Exception {
		if (isNullEncoding(v)) { return this.nullEnum; }

		// Make sure we stip out the spaces
		v = StringUtils.strip(v);

		// If there's no CI map, then valueOf() *must* return a valid value
		if (this.caseInsensitiveMap == null) {
			E ret = specialUnmarshal(v);
			if (ret == null) {
				ret = Enum.valueOf(this.enumClass, v);
			}
			return ret;
		}

		String folded = v.toLowerCase();
		E ret = specialUnmarshal(folded);
		if (ret == null) {
			// If this can be case-insensitive, we fold to lowercase and search
			ret = this.caseInsensitiveMap.get(folded);
		}
		if (ret != null) { return ret; }

		// No match, we emulate valueOf()'s IllegalArgumentException
		throw new IllegalArgumentException(String.format("The value [%s] is not a valid value for the enum class %s", v,
			this.enumClass.getCanonicalName()));
	}

	protected String specialMarshal(E e) throws Exception {
		return null;
	}

	@Override
	public final String marshal(E e) throws Exception {
		if (isNullValue(e)) { return this.nullString; }
		String ret = specialMarshal(e);
		if (ret == null) {
			ret = e.name();
			if (this.marshalFolded && (this.caseInsensitiveMap != null)) {
				ret = ret.toLowerCase();
			}
		}
		return ret;
	}
}