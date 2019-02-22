package com.armedia.commons.utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

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
		Assert.assertTrue(adapter.isCaseSensitive());

		adapter = newAdapter(CaseInsensitive.class);
		Assert.assertFalse(adapter.isCaseSensitive());

		adapter = newAdapter(Empty.class);
		Assert.assertTrue(adapter.isCaseSensitive());

		@SuppressWarnings("rawtypes")
		Constructor<XmlEnumAdapter> c = XmlEnumAdapter.class.getConstructor(Class.class);
		try {
			c.newInstance(Object.class);
			Assert.fail("Did not fail with a non-enum class");
		} catch (InvocationTargetException e) {
			Assert.assertSame(IllegalArgumentException.class, e.getCause().getClass());
		}
	}

	public <E extends Enum<E>> XmlEnumAdapter<E> newAdapter(Class<E> enumClass, Boolean ignoreCase) {
		return new XmlEnumAdapter<>(enumClass, ignoreCase);
	}

	@Test
	public void testXmlEnumAdapterClassOfEBoolean() throws Exception {
		XmlEnumAdapter<?> adapter = null;

		adapter = newAdapter(CaseSensitive.class, null);
		Assert.assertTrue(adapter.isCaseSensitive());
		adapter = newAdapter(CaseSensitive.class, true);
		Assert.assertTrue(adapter.isCaseSensitive());
		adapter = newAdapter(CaseSensitive.class, false);
		Assert.assertTrue(adapter.isCaseSensitive());

		adapter = newAdapter(CaseInsensitive.class, null);
		Assert.assertFalse(adapter.isCaseSensitive());
		adapter = newAdapter(CaseInsensitive.class, true);
		Assert.assertFalse(adapter.isCaseSensitive());
		adapter = newAdapter(CaseInsensitive.class, false);
		Assert.assertTrue(adapter.isCaseSensitive());

		adapter = newAdapter(Empty.class, null);
		Assert.assertTrue(adapter.isCaseSensitive());
		adapter = newAdapter(Empty.class, true);
		Assert.assertTrue(adapter.isCaseSensitive());
		adapter = newAdapter(Empty.class, false);
		Assert.assertTrue(adapter.isCaseSensitive());

		@SuppressWarnings("rawtypes")
		Constructor<XmlEnumAdapter> c = XmlEnumAdapter.class.getConstructor(Class.class, Boolean.class);
		try {
			c.newInstance(Object.class, null);
			Assert.fail("Did not fail with a non-enum class");
		} catch (InvocationTargetException e) {
			Assert.assertSame(IllegalArgumentException.class, e.getCause().getClass());
		}
		try {
			c.newInstance(Object.class, Boolean.FALSE);
			Assert.fail("Did not fail with a non-enum class");
		} catch (InvocationTargetException e) {
			Assert.assertSame(IllegalArgumentException.class, e.getCause().getClass());
		}
		try {
			c.newInstance(Object.class, Boolean.TRUE);
			Assert.fail("Did not fail with a non-enum class");
		} catch (InvocationTargetException e) {
			Assert.assertSame(IllegalArgumentException.class, e.getCause().getClass());
		}
	}

	private <E extends Enum<E>> void testUnmarshalString(Class<E> enumClass) throws Exception {
		XmlEnumAdapter<E> adapter = null;

		// Autodetect case sensitivity
		adapter = newAdapter(enumClass, null);
		Assert.assertNull(adapter.unmarshal(null));

		for (E e : enumClass.getEnumConstants()) {
			Assert.assertSame(String.format("%s::%s", enumClass.getCanonicalName(), e.name()), e,
				adapter.unmarshal(e.name()));
			if (!adapter.isCaseSensitive()) {
				Assert.assertSame(String.format("%s::%s", enumClass.getCanonicalName(), e.name().toUpperCase()), e,
					adapter.unmarshal(e.name().toUpperCase()));
				Assert.assertSame(String.format("%s::%s", enumClass.getCanonicalName(), e.name().toLowerCase()), e,
					adapter.unmarshal(e.name().toLowerCase()));
			}
		}

		try {
			adapter.unmarshal(UUID.randomUUID().toString());
			Assert.fail(String.format("%s did not fail with a known-invalid value", enumClass.getCanonicalName()));
		} catch (IllegalArgumentException e) {
			// All is well
		}
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
		Assert.assertNull(adapter.marshal(null));

		for (E e : enumClass.getEnumConstants()) {
			Assert.assertEquals(String.format("%s::%s", enumClass.getCanonicalName(), e.name()), e.name(),
				adapter.marshal(e));
		}
	}

	@Test
	public void testMarshalArchetype() throws Exception {
		testMarshalString(CaseInsensitive.class);
		testMarshalString(CaseSensitive.class);
		testMarshalString(Empty.class);
	}

}
