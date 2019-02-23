package com.armedia.commons.utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
	public void testXmlEnumAdapterClassOfE() throws Exception {
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

	public <E extends Enum<E>> XmlEnumAdapter<E> newAdapter(Class<E> enumClass, Boolean ignoreCase) {
		return new XmlEnumAdapter<>(enumClass, ignoreCase);
	}

	@Test
	public void testXmlEnumAdapterClassOfEBoolean() throws Exception {
		XmlEnumAdapter<?> adapter = null;

		adapter = newAdapter(CaseSensitive.class, null);
		Assertions.assertTrue(adapter.isCaseSensitive());
		adapter = newAdapter(CaseSensitive.class, true);
		Assertions.assertTrue(adapter.isCaseSensitive());
		adapter = newAdapter(CaseSensitive.class, false);
		Assertions.assertTrue(adapter.isCaseSensitive());

		adapter = newAdapter(CaseInsensitive.class, null);
		Assertions.assertFalse(adapter.isCaseSensitive());
		adapter = newAdapter(CaseInsensitive.class, true);
		Assertions.assertFalse(adapter.isCaseSensitive());
		adapter = newAdapter(CaseInsensitive.class, false);
		Assertions.assertTrue(adapter.isCaseSensitive());

		adapter = newAdapter(Empty.class, null);
		Assertions.assertTrue(adapter.isCaseSensitive());
		adapter = newAdapter(Empty.class, true);
		Assertions.assertTrue(adapter.isCaseSensitive());
		adapter = newAdapter(Empty.class, false);
		Assertions.assertTrue(adapter.isCaseSensitive());

		@SuppressWarnings("rawtypes")
		Constructor<XmlEnumAdapter> c = XmlEnumAdapter.class.getConstructor(Class.class, Boolean.class);
		try {
			c.newInstance(Object.class, null);
			Assertions.fail("Did not fail with a non-enum class");
		} catch (InvocationTargetException e) {
			Assertions.assertSame(IllegalArgumentException.class, e.getCause().getClass());
		}
		try {
			c.newInstance(Object.class, Boolean.FALSE);
			Assertions.fail("Did not fail with a non-enum class");
		} catch (InvocationTargetException e) {
			Assertions.assertSame(IllegalArgumentException.class, e.getCause().getClass());
		}
		try {
			c.newInstance(Object.class, Boolean.TRUE);
			Assertions.fail("Did not fail with a non-enum class");
		} catch (InvocationTargetException e) {
			Assertions.assertSame(IllegalArgumentException.class, e.getCause().getClass());
		}
	}

	private <E extends Enum<E>> void testUnmarshalString(Class<E> enumClass) throws Exception {
		// Autodetect case sensitivity
		XmlEnumAdapter<E> adapter = newAdapter(enumClass, null);
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
	}

	@Test
	public void testUnmarshalString() throws Exception {
		testUnmarshalString(CaseInsensitive.class);
		testUnmarshalString(CaseSensitive.class);
		testUnmarshalString(Empty.class);
	}

	private <E extends Enum<E>> void testMarshalString(Class<E> enumClass) throws Exception {
		XmlEnumAdapter<E> adapter = null;

		adapter = newAdapter(enumClass, null);
		Assertions.assertNull(adapter.marshal(null));

		for (E e : enumClass.getEnumConstants()) {
			Assertions.assertEquals(e.name(), adapter.marshal(e),
				String.format("%s::%s", enumClass.getCanonicalName(), e.name()));
		}
	}

	@Test
	public void testMarshalArchetype() throws Exception {
		testMarshalString(CaseInsensitive.class);
		testMarshalString(CaseSensitive.class);
		testMarshalString(Empty.class);
	}

	private <E extends Enum<E>> void testSpecialMarshal(final Class<E> enumClass, final E special) throws Exception {
		final String uuid = UUID.randomUUID().toString();
		XmlEnumAdapter<E> adapter = new XmlEnumAdapter<E>(enumClass) {
			@Override
			protected String nullString() {
				return uuid;
			}

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
		XmlEnumAdapter<E> adapter = new XmlEnumAdapter<E>(enumClass) {
			@Override
			protected E nullEnum() {
				return special;
			}

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
}