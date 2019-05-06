package com.armedia.commons.utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.XmlEnumAdapter.Flag;

public class XmlEnumAdapterTest {

	private static enum CaseSensitive {
		//
		FirstValue, //
		firstvalue, //
		FIRSTVALUE, //
		//
		;
	}

	private static enum CaseInsensitive {
		//
		First, //
		Second, //
		Third, //
		//
		;
	}

	private static enum Empty {
		//
		//
		;
	}

	public <E extends Enum<E>> XmlEnumAdapter<E> newAdapter(Class<E> enumClass) {
		return new XmlEnumAdapter<>(enumClass);
	}

	@Test
	public void testXmlEnumAdapterClass() throws Exception {
		XmlEnumAdapter<?> adapter = null;

		adapter = newAdapter(CaseSensitive.class);
		Assertions.assertTrue(adapter.isCaseSensitive());

		adapter = newAdapter(CaseInsensitive.class);
		Assertions.assertFalse(adapter.isCaseSensitive());

		adapter = newAdapter(Empty.class);
		Assertions.assertTrue(adapter.isCaseSensitive());

		@SuppressWarnings("rawtypes")
		Constructor<XmlEnumAdapter> c = XmlEnumAdapter.class.getConstructor(Class.class);
		try {
			c.newInstance(Object.class);
			Assertions.fail("Did not fail with a non-enum class");
		} catch (InvocationTargetException e) {
			Assertions.assertSame(IllegalArgumentException.class, e.getCause().getClass());
		}
	}

	public <E extends Enum<E>> XmlEnumAdapter<E> newAdapter(Class<E> enumClass, Flag... flags) {
		return new XmlEnumAdapter<>(enumClass, flags);
	}

	@Test
	public void testConstructors() throws Exception {
		XmlEnumAdapter<?> adapter = null;

		adapter = newAdapter(CaseSensitive.class);
		Assertions.assertTrue(adapter.isCaseSensitive());
		adapter = newAdapter(CaseSensitive.class, Flag.STRICT_CASE);
		Assertions.assertTrue(adapter.isCaseSensitive());
		adapter = newAdapter(CaseSensitive.class, Flag.MARSHAL_FOLDED);
		Assertions.assertTrue(adapter.isCaseSensitive());
		adapter = newAdapter(CaseSensitive.class, Flag.values());
		Assertions.assertTrue(adapter.isCaseSensitive());

		adapter = newAdapter(CaseInsensitive.class);
		Assertions.assertFalse(adapter.isCaseSensitive());
		adapter = newAdapter(CaseInsensitive.class, Flag.STRICT_CASE);
		Assertions.assertTrue(adapter.isCaseSensitive());
		adapter = newAdapter(CaseInsensitive.class, Flag.MARSHAL_FOLDED);
		Assertions.assertFalse(adapter.isCaseSensitive());
		adapter = newAdapter(CaseInsensitive.class, Flag.values());
		Assertions.assertTrue(adapter.isCaseSensitive());

		adapter = newAdapter(Empty.class);
		Assertions.assertTrue(adapter.isCaseSensitive());
		adapter = newAdapter(Empty.class, Flag.STRICT_CASE);
		Assertions.assertTrue(adapter.isCaseSensitive());
		adapter = newAdapter(Empty.class, Flag.MARSHAL_FOLDED);
		Assertions.assertTrue(adapter.isCaseSensitive());
		adapter = newAdapter(Empty.class, Flag.values());
		Assertions.assertTrue(adapter.isCaseSensitive());

		adapter = newAdapter(CaseSensitive.class);
		Assertions.assertTrue(adapter.isCaseSensitive());
		adapter = newAdapter(CaseSensitive.class, Flag.STRICT_CASE, null);
		Assertions.assertTrue(adapter.isCaseSensitive());
		adapter = newAdapter(CaseSensitive.class, Flag.values());
		Assertions.assertTrue(adapter.isCaseSensitive());

		adapter = newAdapter(CaseInsensitive.class);
		Assertions.assertFalse(adapter.isCaseSensitive());
		adapter = newAdapter(CaseInsensitive.class, Flag.STRICT_CASE, null);
		Assertions.assertTrue(adapter.isCaseSensitive());
		adapter = newAdapter(CaseInsensitive.class, Flag.values());
		Assertions.assertTrue(adapter.isCaseSensitive());

		adapter = newAdapter(Empty.class);
		Assertions.assertTrue(adapter.isCaseSensitive());
		adapter = newAdapter(Empty.class, Flag.STRICT_CASE, null);
		Assertions.assertTrue(adapter.isCaseSensitive());
		adapter = newAdapter(Empty.class, Flag.values());
		Assertions.assertTrue(adapter.isCaseSensitive());

		{
			@SuppressWarnings("rawtypes")
			Constructor<XmlEnumAdapter> c = XmlEnumAdapter.class.getConstructor(Class.class, Iterable.class);
			try {
				c.newInstance(Object.class, null);
				Assertions.fail("Did not fail with a non-enum class");
			} catch (InvocationTargetException e) {
				Assertions.assertSame(IllegalArgumentException.class, e.getCause().getClass());
			}
			try {
				c.newInstance(Object.class, Collections.emptyList());
				Assertions.fail("Did not fail with a non-enum class");
			} catch (InvocationTargetException e) {
				Assertions.assertSame(IllegalArgumentException.class, e.getCause().getClass());
			}
		}
		{
			Flag[] arr = {};
			@SuppressWarnings("rawtypes")
			Constructor<XmlEnumAdapter> c = XmlEnumAdapter.class.getConstructor(Class.class, arr.getClass());
			try {
				c.newInstance(Object.class, null);
				Assertions.fail("Did not fail with a non-enum class");
			} catch (InvocationTargetException e) {
				Assertions.assertSame(IllegalArgumentException.class, e.getCause().getClass());
			}
			try {
				c.newInstance(Object.class, arr);
				Assertions.fail("Did not fail with a non-enum class");
			} catch (InvocationTargetException e) {
				Assertions.assertSame(IllegalArgumentException.class, e.getCause().getClass());
			}
			try {
				c.newInstance(Object.class, Flag.values());
				Assertions.fail("Did not fail with a non-enum class");
			} catch (InvocationTargetException e) {
				Assertions.assertSame(IllegalArgumentException.class, e.getCause().getClass());
			}
		}
		{
			Flag[] arr = {};
			@SuppressWarnings("rawtypes")
			Constructor<XmlEnumAdapter> c = XmlEnumAdapter.class.getConstructor(Class.class, arr.getClass());
			try {
				c.newInstance(Object.class, null);
				Assertions.fail("Did not fail with a non-enum class");
			} catch (InvocationTargetException e) {
				Assertions.assertSame(IllegalArgumentException.class, e.getCause().getClass());
			}
			try {
				c.newInstance(Object.class, arr);
				Assertions.fail("Did not fail with a non-enum class");
			} catch (InvocationTargetException e) {
				Assertions.assertSame(IllegalArgumentException.class, e.getCause().getClass());
			}
			try {
				c.newInstance(Object.class, Flag.values());
				Assertions.fail("Did not fail with a non-enum class");
			} catch (InvocationTargetException e) {
				Assertions.assertSame(IllegalArgumentException.class, e.getCause().getClass());
			}
		}

		{
			Collection<Flag> c = Collections.emptyList();
			String nullString = UUID.randomUUID().toString();
			CaseSensitive nullValue = CaseSensitive.FIRSTVALUE;
			adapter = new XmlEnumAdapter<>(CaseSensitive.class, c);
			Assertions.assertNull(adapter.getNullString());
			Assertions.assertNull(adapter.getNullValue());
			adapter = new XmlEnumAdapter<>(CaseSensitive.class, nullString, c);
			Assertions.assertEquals(nullString, adapter.getNullString());
			adapter = new XmlEnumAdapter<>(CaseSensitive.class, nullValue, c);
			Assertions.assertSame(nullValue, adapter.getNullValue());
			adapter = new XmlEnumAdapter<>(CaseSensitive.class, nullString, nullValue, c);
			Assertions.assertEquals(nullString, adapter.getNullString());
			Assertions.assertSame(nullValue, adapter.getNullValue());
		}
		{
			String nullString = UUID.randomUUID().toString();
			adapter = new XmlEnumAdapter<>(CaseSensitive.class, nullString);
			Assertions.assertEquals(nullString, adapter.getNullString());
			adapter = new XmlEnumAdapter<>(CaseSensitive.class, nullString, Flag.STRICT_CASE);
			Assertions.assertEquals(nullString, adapter.getNullString());
			adapter = new XmlEnumAdapter<>(CaseSensitive.class, nullString, Flag.MARSHAL_FOLDED);
			Assertions.assertEquals(nullString, adapter.getNullString());
			adapter = new XmlEnumAdapter<>(CaseSensitive.class, nullString, Flag.values());
			Assertions.assertEquals(nullString, adapter.getNullString());
		}
		{
			CaseSensitive nullValue = CaseSensitive.FIRSTVALUE;
			adapter = new XmlEnumAdapter<>(CaseSensitive.class, nullValue);
			Assertions.assertSame(nullValue, adapter.getNullValue());
			adapter = new XmlEnumAdapter<>(CaseSensitive.class, nullValue, Flag.STRICT_CASE);
			Assertions.assertSame(nullValue, adapter.getNullValue());
			adapter = new XmlEnumAdapter<>(CaseSensitive.class, nullValue, Flag.MARSHAL_FOLDED);
			Assertions.assertSame(nullValue, adapter.getNullValue());
			adapter = new XmlEnumAdapter<>(CaseSensitive.class, nullValue, Flag.values());
			Assertions.assertSame(nullValue, adapter.getNullValue());
		}
		{
			String nullString = UUID.randomUUID().toString();
			CaseSensitive nullValue = CaseSensitive.FIRSTVALUE;
			adapter = new XmlEnumAdapter<>(CaseSensitive.class, nullString, nullValue);
			Assertions.assertEquals(nullString, adapter.getNullString());
			Assertions.assertSame(nullValue, adapter.getNullValue());
			adapter = new XmlEnumAdapter<>(CaseSensitive.class, nullString, nullValue, Flag.STRICT_CASE);
			Assertions.assertEquals(nullString, adapter.getNullString());
			Assertions.assertSame(nullValue, adapter.getNullValue());
			adapter = new XmlEnumAdapter<>(CaseSensitive.class, nullString, nullValue, Flag.MARSHAL_FOLDED);
			Assertions.assertEquals(nullString, adapter.getNullString());
			Assertions.assertSame(nullValue, adapter.getNullValue());
			adapter = new XmlEnumAdapter<>(CaseSensitive.class, nullString, nullValue, Flag.values());
			Assertions.assertEquals(nullString, adapter.getNullString());
			Assertions.assertSame(nullValue, adapter.getNullValue());
		}
	}

	private <E extends Enum<E>> void testUnmarshalString(Class<E> enumClass) throws Exception {
		// Autodetect case sensitivity
		XmlEnumAdapter<E> adapter = newAdapter(enumClass);
		Assertions.assertNull(adapter.unmarshal(null));

		for (E e : enumClass.getEnumConstants()) {
			Assertions.assertSame(e, adapter.unmarshal(e.name()),
				String.format("%s::%s", enumClass.getCanonicalName(), e.name()));
			if (!adapter.isCaseSensitive()) {
				Assertions.assertSame(e, adapter.unmarshal(e.name().toUpperCase()),
					String.format("%s::%s", enumClass.getCanonicalName(), e.name().toUpperCase()));
				Assertions.assertSame(e, adapter.unmarshal(e.name().toLowerCase()),
					String.format("%s::%s", enumClass.getCanonicalName(), e.name().toLowerCase()));
			}
		}

		Assertions.assertThrows(IllegalArgumentException.class, () -> adapter.unmarshal(UUID.randomUUID().toString()));

		{
			String nullString = UUID.randomUUID().toString();
			XmlEnumAdapter<E> a2 = new XmlEnumAdapter<>(enumClass, nullString);
			Assertions.assertNull(a2.unmarshal(nullString));
			E[] values = enumClass.getEnumConstants();
			if (values.length > 0) {
				for (E e : values) {
					a2 = new XmlEnumAdapter<>(enumClass, nullString, e);
					Assertions.assertSame(e, a2.unmarshal(nullString));
				}
			}
		}
	}

	@Test
	public void testUnmarshalString() throws Exception {
		testUnmarshalString(CaseInsensitive.class);
		testUnmarshalString(CaseSensitive.class);
		testUnmarshalString(Empty.class);
	}

	private <E extends Enum<E>> void testMarshalEnum(Class<E> enumClass) throws Exception {
		XmlEnumAdapter<E> adapter = null;
		Flag[][] flags = {
			{}, {
				Flag.MARSHAL_FOLDED
			}, {
				Flag.STRICT_CASE
			}, Flag.values()
		};

		for (Flag[] f : flags) {
			adapter = newAdapter(enumClass, f);
			Assertions.assertNull(adapter.marshal(null));

			for (E e : enumClass.getEnumConstants()) {
				Set<Flag> s = EnumSet.noneOf(Flag.class);
				for (Flag ff : f) {
					s.add(ff);
				}
				if (adapter.isCaseSensitive() || !s.contains(Flag.MARSHAL_FOLDED)) {
					Assertions.assertEquals(e.name(), adapter.marshal(e),
						String.format("%s::%s", enumClass.getCanonicalName(), e.name()));
				} else {
					Assertions.assertEquals(e.name().toLowerCase(), adapter.marshal(e),
						String.format("%s::%s", enumClass.getCanonicalName(), e.name()));
				}
			}
		}

		E[] arr = enumClass.getEnumConstants();
		if (arr.length > 0) {
			String nullString = UUID.randomUUID().toString();
			for (E nullValue : arr) {
				adapter = new XmlEnumAdapter<>(enumClass, nullValue);
				Assertions.assertNull(adapter.marshal(nullValue));
				adapter = new XmlEnumAdapter<>(enumClass, nullString, nullValue);
				Assertions.assertEquals(nullString, adapter.marshal(nullValue));
			}
		}
	}

	@Test
	public void testMarshalEnum() throws Exception {
		testMarshalEnum(CaseInsensitive.class);
		testMarshalEnum(CaseSensitive.class);
		testMarshalEnum(Empty.class);
	}

	private <E extends Enum<E>> void testSpecialMarshal(final Class<E> enumClass, final E special) throws Exception {
		final String uuid = UUID.randomUUID().toString();
		XmlEnumAdapter<E> adapter = new XmlEnumAdapter<E>(enumClass, uuid) {
			@Override
			protected String specialMarshal(E e) throws Exception {
				if (e == special) { return uuid; }
				return super.specialMarshal(e);
			}
		};

		Assertions.assertSame(uuid, adapter.marshal(null));
		Assertions.assertSame(uuid, adapter.marshal(special));

		for (E v : enumClass.getEnumConstants()) {
			if (v != special) {
				Assertions.assertNotEquals(uuid, adapter.marshal(v));
			}
		}
	}

	@Test
	public void testSpecialMarshal() throws Exception {
		testSpecialMarshal(CaseInsensitive.class, CaseInsensitive.First);
		testSpecialMarshal(CaseSensitive.class, CaseSensitive.firstvalue);
	}

	private <E extends Enum<E>> void testSpecialUnmarshal(final Class<E> enumClass, final E special) throws Exception {
		final String uuid = UUID.randomUUID().toString();
		XmlEnumAdapter<E> adapter = new XmlEnumAdapter<E>(enumClass, special) {
			@Override
			protected E specialUnmarshal(String s) throws Exception {
				if (StringUtils.equalsIgnoreCase(uuid, s)) { return special; }
				return super.specialUnmarshal(s);
			}
		};

		Assertions.assertSame(special, adapter.unmarshal(null));
		Assertions.assertSame(special, adapter.unmarshal(uuid));

		for (E v : enumClass.getEnumConstants()) {
			if (v != special) {
				Assertions.assertNotEquals(uuid, adapter.unmarshal(v.name()));
			}
		}
	}

	@Test
	public void testSpecialUnmarshal() throws Exception {
		testSpecialUnmarshal(CaseInsensitive.class, CaseInsensitive.First);
		testSpecialUnmarshal(CaseSensitive.class, CaseSensitive.firstvalue);
	}

	@Test
	public void testGetValidMarshalledValues() {
		Set<String> expected = null;
		// CaseSensitive -> always same
		expected = new LinkedHashSet<>();
		for (CaseSensitive v : CaseSensitive.values()) {
			expected.add(v.name());
		}

		XmlEnumAdapter<CaseSensitive> cs = newAdapter(CaseSensitive.class);
		Assertions.assertEquals(expected, cs.getValidMarshalledValues());

		cs = newAdapter(CaseSensitive.class, Flag.MARSHAL_FOLDED);
		Assertions.assertEquals(expected, cs.getValidMarshalledValues());

		// CaseInsensitive -> both identical and folded
		expected = new LinkedHashSet<>();
		for (CaseInsensitive v : CaseInsensitive.values()) {
			expected.add(v.name());
		}
		XmlEnumAdapter<CaseInsensitive> ci = newAdapter(CaseInsensitive.class, Flag.STRICT_CASE);
		Assertions.assertEquals(expected, ci.getValidMarshalledValues());

		expected = new LinkedHashSet<>();
		for (CaseInsensitive v : CaseInsensitive.values()) {
			expected.add(v.name().toLowerCase());
		}
		ci = newAdapter(CaseInsensitive.class, Flag.MARSHAL_FOLDED);
		Assertions.assertEquals(expected, ci.getValidMarshalledValues());

		ci = newAdapter(CaseInsensitive.class);
		Assertions.assertEquals(expected, ci.getValidMarshalledValues());

		// Empty -> nothing
		expected = new LinkedHashSet<>();
		XmlEnumAdapter<Empty> e = newAdapter(Empty.class, Flag.STRICT_CASE);
		Assertions.assertEquals(expected, e.getValidMarshalledValues());

		e = newAdapter(Empty.class, Flag.MARSHAL_FOLDED);
		Assertions.assertEquals(expected, e.getValidMarshalledValues());

		e = newAdapter(Empty.class);
		Assertions.assertEquals(expected, e.getValidMarshalledValues());
	}
}