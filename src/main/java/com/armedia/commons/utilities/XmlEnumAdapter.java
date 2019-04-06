package com.armedia.commons.utilities;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.StringUtils;

public class XmlEnumAdapter<E extends Enum<E>> extends XmlAdapter<String, E> {

	private final Class<E> enumClass;
	private final Map<String, E> caseInsensitiveMap;

	private static <E extends Enum<E>> boolean detectIgnoreCase(Class<E> enumClass) {
		Set<String> lower = new HashSet<>();
		for (E e : enumClass.getEnumConstants()) {
			if (!lower.add(e.name().toLowerCase())) { return false; }
		}
		return true;
	}

	public XmlEnumAdapter(Class<E> enumClass) {
		this(enumClass, null);
	}

	public XmlEnumAdapter(Class<E> enumClass, Boolean ignoreCase) {
		this.enumClass = Objects.requireNonNull(enumClass, "Must provide a non-null Enum class");
		if (!enumClass.isEnum()) {
			throw new IllegalArgumentException(
				String.format("The class %s is not a valid Enum", enumClass.getCanonicalName()));
		}
		ignoreCase = (XmlEnumAdapter.detectIgnoreCase(enumClass)
			&& ((ignoreCase == null) || ignoreCase.booleanValue()));
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
		}
		return ret;
	}
}