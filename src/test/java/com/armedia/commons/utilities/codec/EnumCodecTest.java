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
package com.armedia.commons.utilities.codec;

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

import com.armedia.commons.utilities.codec.EnumCodec.Flag;
import com.armedia.commons.utilities.function.CheckedFunction;

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

	private static <A, B> CheckedFunction<A, B, Exception> nullFunction() {
		return (e) -> null;
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
			CheckedCodec<CaseSensitive, String, Exception> special = null;
			Collection<Flag> c = Collections.emptyList();
			String nullString = UUID.randomUUID().toString();
			CaseSensitive nullValue = CaseSensitive.FIRSTVALUE;
			adapter = new EnumCodec<>(CaseSensitive.class, c);
			Assertions.assertNull(adapter.getNullEncoding());
			Assertions.assertNull(adapter.getNullValue());
			special = new FunctionalCheckedCodec<>(EnumCodecTest.nullFunction(), null, null,
				EnumCodecTest.nullFunction(), nullString, null);
			adapter = new EnumCodec<>(CaseSensitive.class, special, c);
			Assertions.assertEquals(nullString, adapter.getNullEncoding());
			special = new FunctionalCheckedCodec<>(EnumCodecTest.nullFunction(), nullValue, null,
				EnumCodecTest.nullFunction(), null, null);
			adapter = new EnumCodec<>(CaseSensitive.class, special, c);
			Assertions.assertSame(nullValue, adapter.getNullValue());
			special = new FunctionalCheckedCodec<>(EnumCodecTest.nullFunction(), nullValue, null,
				EnumCodecTest.nullFunction(), nullString, null);
			adapter = new EnumCodec<>(CaseSensitive.class, special, c);
			Assertions.assertEquals(nullString, adapter.getNullEncoding());
			Assertions.assertSame(nullValue, adapter.getNullValue());
		}
		{
			String nullString = UUID.randomUUID().toString();
			CheckedCodec<CaseSensitive, String, Exception> special = new FunctionalCheckedCodec<>(
				EnumCodecTest.nullFunction(), null, null, EnumCodecTest.nullFunction(), nullString, null);
			adapter = new EnumCodec<>(CaseSensitive.class, special);
			Assertions.assertEquals(nullString, adapter.getNullEncoding());
			adapter = new EnumCodec<>(CaseSensitive.class, special, Flag.STRICT_CASE);
			Assertions.assertEquals(nullString, adapter.getNullEncoding());
			adapter = new EnumCodec<>(CaseSensitive.class, special, Flag.MARSHAL_FOLDED);
			Assertions.assertEquals(nullString, adapter.getNullEncoding());
			adapter = new EnumCodec<>(CaseSensitive.class, special, Flag.values());
			Assertions.assertEquals(nullString, adapter.getNullEncoding());
		}
		{
			CaseSensitive nullValue = CaseSensitive.FIRSTVALUE;
			CheckedCodec<CaseSensitive, String, Exception> special = new FunctionalCheckedCodec<>(
				EnumCodecTest.nullFunction(), nullValue, null, EnumCodecTest.nullFunction(), null, null);
			adapter = new EnumCodec<>(CaseSensitive.class, special);
			Assertions.assertSame(nullValue, adapter.getNullValue());
			adapter = new EnumCodec<>(CaseSensitive.class, special, Flag.STRICT_CASE);
			Assertions.assertSame(nullValue, adapter.getNullValue());
			adapter = new EnumCodec<>(CaseSensitive.class, special, Flag.MARSHAL_FOLDED);
			Assertions.assertSame(nullValue, adapter.getNullValue());
			adapter = new EnumCodec<>(CaseSensitive.class, special, Flag.values());
			Assertions.assertSame(nullValue, adapter.getNullValue());
		}
		{
			String nullString = UUID.randomUUID().toString();
			CaseSensitive nullValue = CaseSensitive.FIRSTVALUE;
			CheckedCodec<CaseSensitive, String, Exception> special = new FunctionalCheckedCodec<>(
				EnumCodecTest.nullFunction(), nullValue, null, EnumCodecTest.nullFunction(), nullString, null);
			adapter = new EnumCodec<>(CaseSensitive.class, special);
			Assertions.assertEquals(nullString, adapter.getNullEncoding());
			Assertions.assertSame(nullValue, adapter.getNullValue());
			adapter = new EnumCodec<>(CaseSensitive.class, special, Flag.STRICT_CASE);
			Assertions.assertEquals(nullString, adapter.getNullEncoding());
			Assertions.assertSame(nullValue, adapter.getNullValue());
			adapter = new EnumCodec<>(CaseSensitive.class, special, Flag.MARSHAL_FOLDED);
			Assertions.assertEquals(nullString, adapter.getNullEncoding());
			Assertions.assertSame(nullValue, adapter.getNullValue());
			adapter = new EnumCodec<>(CaseSensitive.class, special, Flag.values());
			Assertions.assertEquals(nullString, adapter.getNullEncoding());
			Assertions.assertSame(nullValue, adapter.getNullValue());
		}
	}

	private <E extends Enum<E>> void testDecodeString(Class<E> enumClass) throws Exception {
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
			CheckedCodec<E, String, Exception> special = new FunctionalCheckedCodec<>(EnumCodecTest.nullFunction(),
				null, null, EnumCodecTest.nullFunction(), nullString, null);
			EnumCodec<E> a2 = new EnumCodec<>(enumClass, special);
			Assertions.assertNull(a2.decode(nullString));
			E[] values = enumClass.getEnumConstants();
			if (values.length > 0) {
				for (E e : values) {
					special = new FunctionalCheckedCodec<>(EnumCodecTest.nullFunction(), e, null,
						EnumCodecTest.nullFunction(), nullString, null);
					a2 = new EnumCodec<>(enumClass, special);
					Assertions.assertSame(e, a2.decode(nullString));
				}
			}
		}
	}

	@Test
	public void testDecodeString() throws Exception {
		testDecodeString(CaseInsensitive.class);
		testDecodeString(CaseSensitive.class);
		testDecodeString(Empty.class);
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
			CheckedCodec<E, String, Exception> special = new FunctionalCheckedCodec<>(EnumCodecTest.nullFunction(),
				null, null, EnumCodecTest.nullFunction(), nullString, null);
			EnumCodec<E> a2 = new EnumCodec<>(enumClass, special);
			Assertions.assertNull(a2.decode(nullString));
			E[] values = enumClass.getEnumConstants();
			if (values.length > 0) {
				for (E e : values) {
					special = new FunctionalCheckedCodec<>(EnumCodecTest.nullFunction(), e, null,
						EnumCodecTest.nullFunction(), nullString, null);
					a2 = new EnumCodec<>(enumClass, special);
					Assertions.assertSame(e, a2.decode(nullString));
				}
			}
		}

		{
			// Test the error path
			CheckedFunction<String, E, Exception> f = (e) -> {
				throw new Exception("First, a checked exception");
			};
			CheckedCodec<E, String, Exception> special = new FunctionalCheckedCodec<>(EnumCodecTest.nullFunction(),
				null, null, f, null, null);
			EnumCodec<E> codec = new EnumCodec<>(enumClass, special);
			Assertions.assertThrows(RuntimeException.class, () -> codec.decode(UUID.randomUUID().toString()));
		}

		{
			// Test the error path
			CheckedFunction<String, E, Exception> f = (e) -> {
				throw new IllegalArgumentException("Then, an unchecked exception");
			};
			CheckedCodec<E, String, Exception> special = new FunctionalCheckedCodec<>(EnumCodecTest.nullFunction(),
				null, null, f, null, null);
			EnumCodec<E> codec = new EnumCodec<>(enumClass, special);
			Assertions.assertThrows(IllegalArgumentException.class, () -> codec.decode(UUID.randomUUID().toString()));
		}

		{
			// Test the error path
			CheckedFunction<String, E, Exception> f = (e) -> {
				throw new OutOfMemoryError("Finally, an Error");
			};
			CheckedCodec<E, String, Exception> special = new FunctionalCheckedCodec<>(EnumCodecTest.nullFunction(),
				null, null, f, null, null);
			EnumCodec<E> codec = new EnumCodec<>(enumClass, special);
			Assertions.assertThrows(OutOfMemoryError.class, () -> codec.decode(UUID.randomUUID().toString()));
		}
	}

	@Test
	public void testDecode() throws Exception {
		testDecode(CaseInsensitive.class);
		testDecode(CaseSensitive.class);
		testDecode(Empty.class);
	}

	private <E extends Enum<E>> void testEncodeEnum(Class<E> enumClass) throws Exception {
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
			CheckedCodec<E, String, Exception> special = null;
			for (E nullValue : arr) {
				special = new FunctionalCheckedCodec<>(EnumCodecTest.nullFunction(), nullValue, null,
					EnumCodecTest.nullFunction(), null, null);
				adapter = new EnumCodec<>(enumClass, special);
				Assertions.assertNull(adapter.encode(nullValue));
				special = new FunctionalCheckedCodec<>(EnumCodecTest.nullFunction(), nullValue, null,
					EnumCodecTest.nullFunction(), nullString, null);
				adapter = new EnumCodec<>(enumClass, special);
				Assertions.assertEquals(nullString, adapter.encode(nullValue));
			}
		}

		if (arr.length == 0) { return; }
		E e = arr[0];

		{
			// Test the error path
			CheckedFunction<E, String, Exception> f = (v) -> {
				throw new Exception("First, a checked exception");
			};
			CheckedCodec<E, String, Exception> special = new FunctionalCheckedCodec<>(f, null, null,
				EnumCodecTest.nullFunction(), null, null);
			EnumCodec<E> codec = new EnumCodec<>(enumClass, special);
			Assertions.assertThrows(RuntimeException.class, () -> codec.encode(e));
		}

		{
			// Test the error path
			CheckedFunction<E, String, Exception> f = (v) -> {
				throw new IllegalArgumentException("Then, an unchecked exception");
			};
			CheckedCodec<E, String, Exception> special = new FunctionalCheckedCodec<>(f, null, null,
				EnumCodecTest.nullFunction(), null, null);
			EnumCodec<E> codec = new EnumCodec<>(enumClass, special);
			Assertions.assertThrows(IllegalArgumentException.class, () -> codec.encode(e));
		}

		{
			// Test the error path
			CheckedFunction<E, String, Exception> f = (v) -> {
				throw new OutOfMemoryError("Finally, an Error");
			};
			CheckedCodec<E, String, Exception> special = new FunctionalCheckedCodec<>(f, null, null,
				EnumCodecTest.nullFunction(), null, null);
			EnumCodec<E> codec = new EnumCodec<>(enumClass, special);
			Assertions.assertThrows(OutOfMemoryError.class, () -> codec.encode(e));
		}
	}

	@Test
	public void testEncodeEnum() throws Exception {
		testEncodeEnum(CaseInsensitive.class);
		testEncodeEnum(CaseSensitive.class);
		testEncodeEnum(Empty.class);
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
			CheckedCodec<E, String, Exception> special = null;
			for (E nullValue : arr) {
				special = new FunctionalCheckedCodec<>(EnumCodecTest.nullFunction(), nullValue, null,
					EnumCodecTest.nullFunction(), null, null);
				adapter = new EnumCodec<>(enumClass, special);
				Assertions.assertNull(adapter.encode(nullValue));
				special = new FunctionalCheckedCodec<>(EnumCodecTest.nullFunction(), nullValue, null,
					EnumCodecTest.nullFunction(), nullString, null);
				adapter = new EnumCodec<>(enumClass, special);
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

	private <E extends Enum<E>> void testSpecialEncode(final Class<E> enumClass, final E special) throws Exception {
		final String uuid = UUID.randomUUID().toString();
		final CheckedCodec<E, String, Exception> sc = new FunctionalCheckedCodec<>((e) -> {
			if (e == special) { return uuid; }
			return null;
		}, special, null, EnumCodecTest.nullFunction(), uuid, null);
		EnumCodec<E> adapter = new EnumCodec<>(enumClass, sc);

		Assertions.assertSame(uuid, adapter.encode(null));
		Assertions.assertSame(uuid, adapter.encode(special));
		for (E v : enumClass.getEnumConstants()) {
			if (v != special) {
				Assertions.assertNotEquals(uuid, adapter.encode(v));
			}
		}
	}

	@Test
	public void testSpecialEncode() throws Exception {
		testSpecialEncode(CaseInsensitive.class, CaseInsensitive.First);
		testSpecialEncode(CaseSensitive.class, CaseSensitive.firstvalue);
	}

	private <E extends Enum<E>> void testSpecialDecode(final Class<E> enumClass, final E special) throws Exception {
		final String uuid = UUID.randomUUID().toString();
		final CheckedCodec<E, String, Exception> sc = new FunctionalCheckedCodec<>(EnumCodecTest.nullFunction(),
			special, null, (s) -> {
				if (StringUtils.equalsIgnoreCase(uuid, s)) { return special; }
				return null;
			}, uuid, null);
		EnumCodec<E> adapter = new EnumCodec<>(enumClass, sc);

		Assertions.assertSame(special, adapter.decode(null));
		Assertions.assertSame(special, adapter.decode(uuid));

		for (E v : enumClass.getEnumConstants()) {
			if (v != special) {
				Assertions.assertNotEquals(uuid, adapter.decode(v.name()));
			}
		}
	}

	@Test
	public void testSpecialDecode() throws Exception {
		testSpecialDecode(CaseInsensitive.class, CaseInsensitive.First);
		testSpecialDecode(CaseSensitive.class, CaseSensitive.firstvalue);
	}

	@Test
	public void testGetValidEncodedValues() {
		Set<String> expected = null;
		// CaseSensitive -> always same
		expected = new LinkedHashSet<>();
		for (CaseSensitive v : CaseSensitive.values()) {
			expected.add(v.name());
		}

		EnumCodec<CaseSensitive> cs = newAdapter(CaseSensitive.class);
		Assertions.assertEquals(expected, cs.getValidEncodedValues());

		cs = newAdapter(CaseSensitive.class, Flag.MARSHAL_FOLDED);
		Assertions.assertEquals(expected, cs.getValidEncodedValues());

		// CaseInsensitive -> both identical and folded
		expected = new LinkedHashSet<>();
		for (CaseInsensitive v : CaseInsensitive.values()) {
			expected.add(v.name());
		}
		EnumCodec<CaseInsensitive> ci = newAdapter(CaseInsensitive.class, Flag.STRICT_CASE);
		Assertions.assertEquals(expected, ci.getValidEncodedValues());

		expected = new LinkedHashSet<>();
		for (CaseInsensitive v : CaseInsensitive.values()) {
			expected.add(v.name().toLowerCase());
		}
		ci = newAdapter(CaseInsensitive.class, Flag.MARSHAL_FOLDED);
		Assertions.assertEquals(expected, ci.getValidEncodedValues());

		ci = newAdapter(CaseInsensitive.class);
		Assertions.assertEquals(expected, ci.getValidEncodedValues());

		// Empty -> nothing
		expected = new LinkedHashSet<>();
		EnumCodec<Empty> e = newAdapter(Empty.class, Flag.STRICT_CASE);
		Assertions.assertEquals(expected, e.getValidEncodedValues());

		e = newAdapter(Empty.class, Flag.MARSHAL_FOLDED);
		Assertions.assertEquals(expected, e.getValidEncodedValues());

		e = newAdapter(Empty.class);
		Assertions.assertEquals(expected, e.getValidEncodedValues());
	}

	private <E extends Enum<E>> void testNullValue(Class<E> enumClass) {
		E[] v = enumClass.getEnumConstants();
		List<E> l = new ArrayList<>(v.length + 1);
		l.add(null);
		for (E e : v) {
			l.add(e);
		}
		for (E e : l) {
			final CheckedCodec<E, String, Exception> special = new FunctionalCheckedCodec<>(
				EnumCodecTest.nullFunction(), e, null, EnumCodecTest.nullFunction(), null, null);
			EnumCodec<E> codec = new EnumCodec<>(enumClass, special);
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
			final CheckedCodec<E, String, Exception> special = new FunctionalCheckedCodec<>(
				EnumCodecTest.nullFunction(), null, null, EnumCodecTest.nullFunction(), s, null);
			EnumCodec<E> codec = new EnumCodec<>(enumClass, special);
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
