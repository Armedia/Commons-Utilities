package com.armedia.commons.utilities;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

public class PluggableServiceLocatorTest {

	private static final Set<String> SERVICE_CLASSES;
	private static final Set<String> SUBSET_1;
	private static final Set<String> SUBSET_2;

	static {
		Class<?>[] classes = {
			BasicIndexedIteratorTest.class, BinaryEncodingTest.class, BinaryMemoryBufferTest.class,
			CfgToolsStaticTest.class, CfgToolsTest.class, CollectionToolsTest.class, ComparisonTest.class,
			FileNameToolsTest.class, GlobberTest.class
		};
		Set<String> a = new TreeSet<String>();
		Set<String> b = new TreeSet<String>();
		for (Class<?> c : classes) {
			if (GoodServiceTest.class.isAssignableFrom(c)) {
				a.add(c.getCanonicalName());
			} else {
				b.add(c.getCanonicalName());
			}
		}
		if (!b.isEmpty()) { throw new RuntimeException(
			String
			.format(
				"The following classes must ALL implement the GoodServiceTest interface (or this test changed to not require it): ",
				b)); }
		SERVICE_CLASSES = Collections.unmodifiableSet(a);
		Assert.assertTrue("Must have more than one class implementing GoodServiceTest", a.size() > 1);

		a = new TreeSet<String>();
		b = new TreeSet<String>();
		int i = 0;
		for (String str : PluggableServiceLocatorTest.SERVICE_CLASSES) {
			if (i < (PluggableServiceLocatorTest.SERVICE_CLASSES.size() / 2)) {
				a.add(str);
			} else {
				b.add(str);
			}
			i++;
		}
		Assert.assertFalse(a.isEmpty());
		Assert.assertFalse(b.isEmpty());
		Assert.assertNotEquals(a, b);
		SUBSET_1 = Collections.unmodifiableSet(a);
		SUBSET_2 = Collections.unmodifiableSet(b);
	}

	@Test
	public void testConstructors() {
		PluggableServiceSelector<GoodServiceTest> selector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				return false;
			}
		};
		PluggableServiceLocator<?> locator = null;

		ClassLoader testCl = Thread.currentThread().getContextClassLoader();

		locator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class);
		Assert.assertSame(GoodServiceTest.class, locator.getServiceClass());
		Assert.assertNull(locator.getDefaultSelector());
		Assert.assertSame(Thread.currentThread().getContextClassLoader(), locator.getClassLoader());

		locator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class, selector);
		Assert.assertSame(GoodServiceTest.class, locator.getServiceClass());
		Assert.assertSame(selector, locator.getDefaultSelector());
		Assert.assertSame(Thread.currentThread().getContextClassLoader(), locator.getClassLoader());

		locator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class, testCl);
		Assert.assertSame(GoodServiceTest.class, locator.getServiceClass());
		Assert.assertNull(locator.getDefaultSelector());
		Assert.assertSame(testCl, locator.getClassLoader());

		locator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class, testCl, selector);
		Assert.assertSame(GoodServiceTest.class, locator.getServiceClass());
		Assert.assertSame(selector, locator.getDefaultSelector());
		Assert.assertSame(testCl, locator.getClassLoader());

		testCl = new ClassLoader(testCl) {
		};

		locator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class, testCl);
		Assert.assertSame(GoodServiceTest.class, locator.getServiceClass());
		Assert.assertNull(locator.getDefaultSelector());
		Assert.assertSame(testCl, locator.getClassLoader());

		locator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class, testCl, selector);
		Assert.assertSame(GoodServiceTest.class, locator.getServiceClass());
		Assert.assertSame(selector, locator.getDefaultSelector());
		Assert.assertSame(testCl, locator.getClassLoader());

		try {
			locator = new PluggableServiceLocator<GoodServiceTest>(null, (ClassLoader) null);
			Assert.fail("Did not fail with null arguments");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			locator = new PluggableServiceLocator<GoodServiceTest>(null, testCl);
			Assert.fail("Did not fail with null arguments");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			locator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class, (ClassLoader) null);
			Assert.fail("Did not fail with null arguments");
		} catch (IllegalArgumentException e) {
			// All is well
		}

		try {
			locator = new PluggableServiceLocator<GoodServiceTest>(null, null, null);
			Assert.fail("Did not fail with null arguments");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			locator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class, null, null);
			Assert.fail("Did not fail with null arguments");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			locator = new PluggableServiceLocator<GoodServiceTest>(null, testCl, null);
			Assert.fail("Did not fail with null arguments");
		} catch (IllegalArgumentException e) {
			// All is well
		}

		// Should work
		locator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class, testCl, null);
	}

	@Test
	public void testDefaultSelector() {
		PluggableServiceSelector<GoodServiceTest> selector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				return false;
			}
		};
		PluggableServiceLocator<GoodServiceTest> locator = null;

		locator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class);
		Assert.assertNull(locator.getDefaultSelector());
		locator.setDefaultSelector(selector);
		Assert.assertSame(selector, locator.getDefaultSelector());
		locator.setDefaultSelector(null);
		Assert.assertNull(locator.getDefaultSelector());

		locator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class, selector);
		Assert.assertSame(selector, locator.getDefaultSelector());
		locator.setDefaultSelector(null);
		Assert.assertNull(locator.getDefaultSelector());
		locator.setDefaultSelector(selector);
		Assert.assertSame(selector, locator.getDefaultSelector());
	}

	@Test
	public void testGetFirst() {
		PluggableServiceLocator<GoodServiceTest> locator = null;
		PluggableServiceSelector<GoodServiceTest> selector = null;

		locator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class);
		Assert.assertNull(locator.getDefaultSelector());
		Assert.assertNotNull(locator.getFirst());

		selector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				return false;
			}
		};
		locator.setDefaultSelector(selector);
		try {
			GoodServiceTest ret = locator.getFirst();
			Assert.fail(String.format("Expected to fail due to no elements, but instead got [%s]", ret.getClass()
				.getCanonicalName()));
		} catch (NoSuchElementException e) {
			// All is well
		}

		selector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				String className = service.getClass().getCanonicalName();
				return PluggableServiceLocatorTest.SUBSET_1.contains(className)
					&& !PluggableServiceLocatorTest.SUBSET_2.contains(className);
			}
		};
		locator.setDefaultSelector(null);
		Assert.assertNotNull(locator.getFirst(selector));
		locator.setDefaultSelector(selector);
		Assert.assertNotNull(locator.getFirst());
	}

	@Test
	public void testGetAll() {
		PluggableServiceLocator<GoodServiceTest> locator = null;
		PluggableServiceSelector<GoodServiceTest> selector = null;

		locator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class);
		Assert.assertNull(locator.getDefaultSelector());

		int count = 0;
		for (Iterator<GoodServiceTest> it = locator.getAll(); it.hasNext(); count++) {
			it.next();
		}
		Assert.assertEquals(PluggableServiceLocatorTest.SERVICE_CLASSES.size(), count);

		selector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				return false;
			}
		};
		Assert.assertFalse(locator.getAll(selector).hasNext());
		try {
			GoodServiceTest ret = locator.getAll(selector).next();
			Assert.fail(String.format("Expected to fail due to no elements, but instead got [%s]", ret.getClass()
				.getCanonicalName()));
		} catch (NoSuchElementException e) {
			// All is well
		}

		selector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				return PluggableServiceLocatorTest.SERVICE_CLASSES.contains(service.getClass().getCanonicalName());
			}
		};
		for (Iterator<GoodServiceTest> it = locator.getAll(); it.hasNext();) {
			GoodServiceTest s = it.next();
			if (!selector.matches(s)) {
				Assert.fail(String.format("Got class [%s] but it's not listed as part of %s", s.getClass()
					.getCanonicalName(), PluggableServiceLocatorTest.SERVICE_CLASSES));
			}
		}

		selector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				return PluggableServiceLocatorTest.SUBSET_1.contains(service.getClass().getCanonicalName());
			}
		};
		for (Iterator<GoodServiceTest> it = locator.getAll(selector); it.hasNext();) {
			GoodServiceTest s = it.next();
			if (!selector.matches(s)) {
				Assert.fail(String.format("Got class [%s] but it's not listed as part of %s", s.getClass()
					.getCanonicalName(), PluggableServiceLocatorTest.SUBSET_1));
			}
		}

		selector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				return PluggableServiceLocatorTest.SUBSET_2.contains(service.getClass().getCanonicalName());
			}
		};
		for (Iterator<GoodServiceTest> it = locator.getAll(selector); it.hasNext();) {
			GoodServiceTest s = it.next();
			if (!selector.matches(s)) {
				Assert.fail(String.format("Got class [%s] but it's not listed as part of %s", s.getClass()
					.getCanonicalName(), PluggableServiceLocatorTest.SUBSET_2));
			}
		}

		try {
			locator.getAll().remove();
		} catch (UnsupportedOperationException e) {
			// all is well
		}
	}

	@Test
	public void testReload() {
		PluggableServiceLocator<GoodServiceTest> locator = new PluggableServiceLocator<GoodServiceTest>(
			GoodServiceTest.class);
		Map<String, GoodServiceTest> cache = new HashMap<String, GoodServiceTest>();
		for (Iterator<GoodServiceTest> it = locator.getAll(); it.hasNext();) {
			GoodServiceTest s = it.next();
			cache.put(s.getClass().getCanonicalName(), s);
		}
		locator.reload();
		for (Iterator<GoodServiceTest> it = locator.getAll(); it.hasNext();) {
			GoodServiceTest actual = it.next();
			GoodServiceTest expected = cache.get(actual.getClass().getCanonicalName());
			Assert.assertNotNull(String.format("Reload caused class [%s] to be loaded, but it wasn't expected", actual
				.getClass().getCanonicalName()), expected);
			Assert.assertNotSame(expected, actual);
		}
	}

	@Test
	public void testIterator() {
		PluggableServiceLocator<GoodServiceTest> locator = null;
		PluggableServiceSelector<GoodServiceTest> selector = null;

		locator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class);
		Assert.assertNull(locator.getDefaultSelector());

		int count = 0;
		for (Iterator<GoodServiceTest> it = locator.iterator(); it.hasNext(); count++) {
			it.next();
		}
		Assert.assertEquals(PluggableServiceLocatorTest.SERVICE_CLASSES.size(), count);

		selector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				return false;
			}
		};
		locator.setDefaultSelector(selector);
		Assert.assertFalse(locator.iterator().hasNext());
		try {
			GoodServiceTest ret = locator.iterator().next();
			Assert.fail(String.format("Expected to fail due to no elements, but instead got [%s]", ret.getClass()
				.getCanonicalName()));
		} catch (NoSuchElementException e) {
			// All is well
		}

		selector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				return PluggableServiceLocatorTest.SERVICE_CLASSES.contains(service.getClass().getCanonicalName());
			}
		};
		for (Iterator<GoodServiceTest> it = locator.iterator(); it.hasNext();) {
			GoodServiceTest s = it.next();
			if (!selector.matches(s)) {
				Assert.fail(String.format("Got class [%s] but it's not listed as part of %s", s.getClass()
					.getCanonicalName(), PluggableServiceLocatorTest.SERVICE_CLASSES));
			}
		}

		selector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				return PluggableServiceLocatorTest.SUBSET_1.contains(service.getClass().getCanonicalName());
			}
		};
		locator.setDefaultSelector(selector);
		for (Iterator<GoodServiceTest> it = locator.iterator(); it.hasNext();) {
			GoodServiceTest s = it.next();
			if (!selector.matches(s)) {
				Assert.fail(String.format("Got class [%s] but it's not listed as part of %s", s.getClass()
					.getCanonicalName(), PluggableServiceLocatorTest.SUBSET_1));
			}
		}

		selector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				return PluggableServiceLocatorTest.SUBSET_2.contains(service.getClass().getCanonicalName());
			}
		};
		locator.setDefaultSelector(selector);
		for (Iterator<GoodServiceTest> it = locator.iterator(); it.hasNext();) {
			GoodServiceTest s = it.next();
			if (!selector.matches(s)) {
				Assert.fail(String.format("Got class [%s] but it's not listed as part of %s", s.getClass()
					.getCanonicalName(), PluggableServiceLocatorTest.SUBSET_2));
			}
		}

		try {
			locator.iterator().remove();
		} catch (UnsupportedOperationException e) {
			// all is well
		}
	}
}