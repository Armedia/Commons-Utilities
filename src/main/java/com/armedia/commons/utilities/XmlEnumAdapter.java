package com.armedia.commons.utilities;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.StringUtils;

public class XmlEnumAdapter<E extends Enum<E>> extends XmlAdapter<String, E> {

	private static final Iterable<Flag> NO_FLAGS = Collections.emptyList();

	protected static enum Flag {
		//
		STRICT_CASE, //
		MARSHAL_FOLDED, //
		//
		;
	}

	private final Class<E> enumClass;
	private final Map<String, E> caseInsensitiveMap;
	private final boolean marshalFolded;

	private static <E extends Enum<E>> boolean canIgnoreCase(Class<E> enumClass) {
		Set<String> lower = new HashSet<>();
		for (E e : enumClass.getEnumConstants()) {
			if (!lower.add(e.name().toLowerCase())) { return false; }
		}
		return true;
	}

	public XmlEnumAdapter(Class<E> enumClass) {
		this(enumClass, XmlEnumAdapter.NO_FLAGS);
	}

	public XmlEnumAdapter(Class<E> enumClass, Flag... flags) {
		this(enumClass, (flags != null ? Arrays.asList(flags) : Collections.emptyList()));
	}

	public XmlEnumAdapter(Class<E> enumClass, Iterable<Flag> flags) {
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

		final boolean ignoreCase = XmlEnumAdapter.canIgnoreCase(enumClass) && !f.contains(Flag.STRICT_CASE);
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
	}

	public final boolean isCaseSensitive() {
		return (this.caseInsensitiveMap == null);
	}

	protected E specialUnmarshal(String v) throws Exception {
		return null;
	}

	protected E nullEnum() {
		return null;
	}

	@Override
	public final E unmarshal(String v) throws Exception {
		if ((v == null) || Tools.equals(nullString(), v)) { return nullEnum(); }

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

	protected String nullString() {
		return null;
	}

	protected String specialMarshal(E e) throws Exception {
		return null;
	}

	@Override
	public final String marshal(E e) throws Exception {
		if (e == null) { return nullString(); }
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