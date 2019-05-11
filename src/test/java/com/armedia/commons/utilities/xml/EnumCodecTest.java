package com.armedia.commons.utilities.xml;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.xml.EnumCodec.Flag;

public class EnumCodecTest {

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

	public <E extends Enum<E>> EnumCodec<E> newAdapter(Class<E> enumClass) {
		return new EnumCodec<>(enumClass);
	}

	@Test
	public void testXmlEnumAdapterClass() throws Exception {
		EnumCodec<?> adapter = null;

		adapter = newAdapter(CaseSensitive.class);
		Assertions.assertTrue(adapter.isCaseSensitive());

		adapter = newAdapter(CaseInsensitive.class);
		Assertions.assertFalse(adapter.isCaseSensitive());

		adapter = newAdapter(Empty.class);
		Assertions.assertTrue(adapter.isCaseSensitive());

		@SuppressWarnings("rawtypes")
		Constructor<EnumCodec> c = EnumCodec.class.getConstructor(Class.class);
		try {
			c.newInstance(Object.class);
			Assertions.fail("Did not fail with a non-enum class");
		} catch (InvocationTargetException e) {
			Assertions.assertSame(IllegalArgumentException.class, e.getCause().getClass());
		}
	}

	public <E extends Enum<E>> EnumCodec<E> newAdapter(Class<E> enumClass, Flag... flags) {
		return new EnumCodec<>(enumClass, flags);
	}

	@Test
	public void testConstructors() throws Exception {
		EnumCodec<?> adapter = null;

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
			Constructor<EnumCodec> c = EnumCodec.class.getConstructor(Class.class, Iterable.class);
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
			Constructor<EnumCodec> c = EnumCodec.class.getConstructor(Class.class, arr.getClass());
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
			Constructor<EnumCodec> c = EnumCodec.class.getConstructor(Class.class, arr.getClass());
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
			adapter = new EnumCodec<>(CaseSensitive.class, c);
			Assertions.assertNull(adapter.getNullEncoding());
			Assertions.assertNull(adapter.getNullValue());
			adapter = new EnumCodec<>(CaseSensitive.class, nullString, c);
			Assertions.assertEquals(nullString, adapter.getNullEncoding());
			adapter = new EnumCodec<>(CaseSensitive.class, nullValue, c);
			Assertions.assertSame(nullValue, adapter.getNullValue());
			adapter = new EnumCodec<>(CaseSensitive.class, nullString, nullValue, c);
			Assertions.assertEquals(nullString, adapter.getNullEncoding());
			Assertions.assertSame(nullValue, adapter.getNullValue());
		}
		{
			String nullString = UUID.randomUUID().toString();
			adapter = new EnumCodec<>(CaseSensitive.class, nullString);
			Assertions.assertEquals(nullString, adapter.getNullEncoding());
			adapter = new EnumCodec<>(CaseSensitive.class, nullString, Flag.STRICT_CASE);
			Assertions.assertEquals(nullString, adapter.getNullEncoding());
			adapter = new EnumCodec<>(CaseSensitive.class, nullString, Flag.MARSHAL_FOLDED);
			Assertions.assertEquals(nullString, adapter.getNullEncoding());
			adapter = new EnumCodec<>(CaseSensitive.class, nullString, Flag.values());
			Assertions.assertEquals(nullString, adapter.getNullEncoding());
		}
		{
			CaseSensitive nullValue = CaseSensitive.FIRSTVALUE;
			adapter = new EnumCodec<>(CaseSensitive.class, nullValue);
			Assertions.assertSame(nullValue, adapter.getNullValue());
			adapter = new EnumCodec<>(CaseSensitive.class, nullValue, Flag.STRICT_CASE);
			Assertions.assertSame(nullValue, adapter.getNullValue());
			adapter = new EnumCodec<>(CaseSensitive.class, nullValue, Flag.MARSHAL_FOLDED);
			Assertions.assertSame(nullValue, adapter.getNullValue());
			adapter = new EnumCodec<>(CaseSensitive.class, nullValue, Flag.values());
			Assertions.assertSame(nullValue, adapter.getNullValue());
		}
		{
			String nullString = UUID.randomUUID().toString();
			CaseSensitive nullValue = CaseSensitive.FIRSTVALUE;
			adapter = new EnumCodec<>(CaseSensitive.class, nullString, nullValue);
			Assertions.assertEquals(nullString, adapter.getNullEncoding());
			Assertions.assertSame(nullValue, adapter.getNullValue());
			adapter = new EnumCodec<>(CaseSensitive.class, nullString, nullValue, Flag.STRICT_CASE);
			Assertions.assertEquals(nullString, adapter.getNullEncoding());
			Assertions.assertSame(nullValue, adapter.getNullValue());
			adapter = new EnumCodec<>(CaseSensitive.class, nullString, nullValue, Flag.MARSHAL_FOLDED);
			Assertions.assertEquals(nullString, adapter.getNullEncoding());
			Assertions.assertSame(nullValue, adapter.getNullValue());
			adapter = new EnumCodec<>(CaseSensitive.class, nullString, nullValue, Flag.values());
			Assertions.assertEquals(nullString, adapter.getNullEncoding());
			Assertions.assertSame(nullValue, adapter.getNullValue());
		}
	}

	private <E extends Enum<E>> void testUnmarshalString(Class<E> enumClass) throws Exception {
		// Autodetect case sensitivity
		EnumCodec<E> adapter = newAdapter(enumClass);
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
			EnumCodec<E> a2 = new EnumCodec<>(enumClass, nullString);
			Assertions.assertNull(a2.unmarshal(nullString));
			E[] values = enumClass.getEnumConstants();
			if (values.length > 0) {
				for (E e : values) {
					a2 = new EnumCodec<>(enumClass, nullString, e);
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

	private <E extends Enum<E>> void testDecode(Class<E> enumClass) throws Exception {
		// Autodetect case sensitivity
		EnumCodec<E> adapter = newAdapter(enumClass);
		Assertions.assertNull(adapter.decode(null));

		for (E e : enumClass.getEnumConstants()) {
			Assertions.assertSame(e, adapter.decode(e.name()),
				String.format("%s::%s", enumClass.getCanonicalName(), e.name()));
			if (!adapter.isCaseSensitive()) {
				Assertions.assertSame(e, adapter.decode(e.name().toUpperCase()),
					String.format("%s::%s", enumClass.getCanonicalName(), e.name().toUpperCase()));
				Assertions.assertSame(e, adapter.decode(e.name().toLowerCase()),
					String.format("%s::%s", enumClass.getCanonicalName(), e.name().toLowerCase()));
			}
		}

		Assertions.assertThrows(IllegalArgumentException.class, () -> adapter.decode(UUID.randomUUID().toString()));

		{
			String nullString = UUID.randomUUID().toString();
			EnumCodec<E> a2 = new EnumCodec<>(enumClass, nullString);
			Assertions.assertNull(a2.decode(nullString));
			E[] values = enumClass.getEnumConstants();
			if (values.length > 0) {
				for (E e : values) {
					a2 = new EnumCodec<>(enumClass, nullString, e);
					Assertions.assertSame(e, a2.decode(nullString));
				}
			}
		}

		{
			// Test the error path
			EnumCodec<E> codec = new EnumCodec<E>(enumClass) {
				@Override
				protected E specialUnmarshal(String v) throws Exception {
					throw new Exception("First, a checked exception");
				}
			};
			Assertions.assertThrows(RuntimeException.class, () -> codec.decode(UUID.randomUUID().toString()));
		}

		{
			// Test the error path
			EnumCodec<E> codec = new EnumCodec<E>(enumClass) {
				@Override
				protected E specialUnmarshal(String v) throws Exception {
					throw new IllegalArgumentException("Then, an unchecked exception");
				}
			};
			Assertions.assertThrows(IllegalArgumentException.class, () -> codec.decode(UUID.randomUUID().toString()));
		}

		{
			// Test the error path
			EnumCodec<E> codec = new EnumCodec<E>(enumClass) {
				@Override
				protected E specialUnmarshal(String v) throws Exception {
					throw new OutOfMemoryError("Finally, an Error");
				}
			};
			Assertions.assertThrows(OutOfMemoryError.class, () -> codec.decode(UUID.randomUUID().toString()));
		}
	}

	@Test
	public void testDecode() throws Exception {
		testDecode(CaseInsensitive.class);
		testDecode(CaseSensitive.class);
		testDecode(Empty.class);
	}

	private <E extends Enum<E>> void testMarshalEnum(Class<E> enumClass) throws Exception {
		EnumCodec<E> adapter = null;
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
				adapter = new EnumCodec<>(enumClass, nullValue);
				Assertions.assertNull(adapter.marshal(nullValue));
				adapter = new EnumCodec<>(enumClass, nullString, nullValue);
				Assertions.assertEquals(nullString, adapter.marshal(nullValue));
			}
		}

		if (arr.length == 0) { return; }
		E e = arr[0];

		{
			// Test the error path
			EnumCodec<E> codec = new EnumCodec<E>(enumClass) {
				@Override
				protected String specialMarshal(E e) throws Exception {
					throw new Exception("First, a checked exception");
				}
			};
			Assertions.assertThrows(RuntimeException.class, () -> codec.encode(e));
		}

		{
			// Test the error path
			EnumCodec<E> codec = new EnumCodec<E>(enumClass) {
				@Override
				protected String specialMarshal(E e) throws Exception {
					throw new IllegalArgumentException("Then, an unchecked exception");
				}
			};
			Assertions.assertThrows(IllegalArgumentException.class, () -> codec.encode(e));
		}

		{
			// Test the error path
			EnumCodec<E> codec = new EnumCodec<E>(enumClass) {
				@Override
				protected String specialMarshal(E e) throws Exception {
					throw new OutOfMemoryError("Finally, an Error");
				}
			};
			Assertions.assertThrows(OutOfMemoryError.class, () -> codec.encode(e));
		}
	}

	@Test
	public void testMarshalEnum() throws Exception {
		testMarshalEnum(CaseInsensitive.class);
		testMarshalEnum(CaseSensitive.class);
		testMarshalEnum(Empty.class);
	}

	private <E extends Enum<E>> void testEncode(Class<E> enumClass) throws Exception {
		EnumCodec<E> adapter = null;
		Flag[][] flags = {
			{}, {
				Flag.MARSHAL_FOLDED
			}, {
				Flag.STRICT_CASE
			}, Flag.values()
		};

		for (Flag[] f : flags) {
			adapter = newAdapter(enumClass, f);
			Assertions.assertNull(adapter.encode(null));

			for (E e : enumClass.getEnumConstants()) {
				Set<Flag> s = EnumSet.noneOf(Flag.class);
				for (Flag ff : f) {
					s.add(ff);
				}
				if (adapter.isCaseSensitive() || !s.contains(Flag.MARSHAL_FOLDED)) {
					Assertions.assertEquals(e.name(), adapter.encode(e),
						String.format("%s::%s", enumClass.getCanonicalName(), e.name()));
				} else {
					Assertions.assertEquals(e.name().toLowerCase(), adapter.encode(e),
						String.format("%s::%s", enumClass.getCanonicalName(), e.name()));
				}
			}
		}

		E[] arr = enumClass.getEnumConstants();
		if (arr.length > 0) {
			String nullString = UUID.randomUUID().toString();
			for (E nullValue : arr) {
				adapter = new EnumCodec<>(enumClass, nullValue);
				Assertions.assertNull(adapter.encode(nullValue));
				adapter = new EnumCodec<>(enumClass, nullString, nullValue);
				Assertions.assertEquals(nullString, adapter.encode(nullValue));
			}
		}
	}

	@Test
	public void testEncode() throws Exception {
		testEncode(CaseInsensitive.class);
		testEncode(CaseSensitive.class);
		testEncode(Empty.class);
	}

	private <E extends Enum<E>> void testSpecialMarshal(final Class<E> enumClass, final E special) throws Exception {
		final String uuid = UUID.randomUUID().toString();
		EnumCodec<E> adapter = new EnumCodec<E>(enumClass, uuid) {
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
		EnumCodec<E> adapter = new EnumCodec<E>(enumClass, special) {
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

		EnumCodec<CaseSensitive> cs = newAdapter(CaseSensitive.class);
		Assertions.assertEquals(expected, cs.getValidMarshalledValues());

		cs = newAdapter(CaseSensitive.class, Flag.MARSHAL_FOLDED);
		Assertions.assertEquals(expected, cs.getValidMarshalledValues());

		// CaseInsensitive -> both identical and folded
		expected = new LinkedHashSet<>();
		for (CaseInsensitive v : CaseInsensitive.values()) {
			expected.add(v.name());
		}
		EnumCodec<CaseInsensitive> ci = newAdapter(CaseInsensitive.class, Flag.STRICT_CASE);
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
		EnumCodec<Empty> e = newAdapter(Empty.class, Flag.STRICT_CASE);
		Assertions.assertEquals(expected, e.getValidMarshalledValues());

		e = newAdapter(Empty.class, Flag.MARSHAL_FOLDED);
		Assertions.assertEquals(expected, e.getValidMarshalledValues());

		e = newAdapter(Empty.class);
		Assertions.assertEquals(expected, e.getValidMarshalledValues());
	}

	private <E extends Enum<E>> void testNullValue(Class<E> enumClass) {
		E[] v = enumClass.getEnumConstants();
		List<E> l = new ArrayList<>(v.length + 1);
		l.add(null);
		for (E e : v) {
			l.add(e);
		}
		for (E e : l) {
			EnumCodec<E> codec = new EnumCodec<>(enumClass, e);
			Assertions.assertTrue(codec.isNullValue(e));
			if (e != null) {
				for (E e2 : l) {
					if ((e == e2) || (e2 == null)) {
						continue;
					}
					Assertions.assertFalse(codec.isNullValue(e2), String.format("Comparing [%s] to [%s]", e, e2));
				}
			}
		}
	}

	private <E extends Enum<E>> void testNullEncoding(Class<E> enumClass) {
		E[] v = enumClass.getEnumConstants();
		List<String> l = new ArrayList<>(v.length + 1);
		l.add(null);
		for (E e : v) {
			l.add(e.name());
		}
		for (String s : l) {
			EnumCodec<E> codec = new EnumCodec<>(enumClass, s);
			Assertions.assertTrue(codec.isNullEncoding(s));
			if (s != null) {
				for (String s2 : l) {
					if ((s == s2) || (s2 == null)) {
						continue;
					}
					Assertions.assertFalse(codec.isNullEncoding(s2), String.format("Comparing [%s] to [%s]", s, s2));
				}
			}
		}
	}

	@Test
	public void testNullValue() {
		testNullValue(CaseSensitive.class);
		testNullValue(CaseInsensitive.class);
		testNullValue(Empty.class);
	}

	@Test
	public void testNullEncoding() {
		testNullEncoding(CaseSensitive.class);
		testNullEncoding(CaseInsensitive.class);
		testNullEncoding(Empty.class);
	}
}