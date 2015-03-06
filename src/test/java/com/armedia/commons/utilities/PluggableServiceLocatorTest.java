package com.armedia.commons.utilities;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;

import com.armedia.commons.utilities.PluggableServiceLocator.ErrorListener;

public class PluggableServiceLocatorTest {

	private static final Set<String> SERVICE_CLASSES;
	private static final Set<String> SUBSET_1;
	private static final Set<String> SUBSET_2;

	static {
		Class<?>[] goodClasses = {
			BasicIndexedIteratorTest.class, BinaryEncodingTest.class, BinaryMemoryBufferTest.class,
			CfgToolsStaticTest.class, CfgToolsTest.class, CollectionToolsTest.class, ComparisonTest.class,
			FileNameToolsTest.class, GlobberTest.class
		};
		Set<String> a = new TreeSet<String>();
		Set<String> b = new TreeSet<String>();
		for (Class<?> c : goodClasses) {
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
		PluggableServiceSelector<GoodServiceTest> goodSelector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				return false;
			}
		};
		PluggableServiceLocator<?> goodLocator = null;

		ClassLoader testCl = Thread.currentThread().getContextClassLoader();

		goodLocator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class);
		Assert.assertSame(GoodServiceTest.class, goodLocator.getServiceClass());
		Assert.assertNull(goodLocator.getDefaultSelector());
		Assert.assertSame(Thread.currentThread().getContextClassLoader(), goodLocator.getClassLoader());

		goodLocator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class, goodSelector);
		Assert.assertSame(GoodServiceTest.class, goodLocator.getServiceClass());
		Assert.assertSame(goodSelector, goodLocator.getDefaultSelector());
		Assert.assertSame(Thread.currentThread().getContextClassLoader(), goodLocator.getClassLoader());

		goodLocator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class, testCl);
		Assert.assertSame(GoodServiceTest.class, goodLocator.getServiceClass());
		Assert.assertNull(goodLocator.getDefaultSelector());
		Assert.assertSame(testCl, goodLocator.getClassLoader());

		goodLocator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class, testCl, goodSelector);
		Assert.assertSame(GoodServiceTest.class, goodLocator.getServiceClass());
		Assert.assertSame(goodSelector, goodLocator.getDefaultSelector());
		Assert.assertSame(testCl, goodLocator.getClassLoader());

		testCl = new ClassLoader(testCl) {
		};

		goodLocator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class, testCl);
		Assert.assertSame(GoodServiceTest.class, goodLocator.getServiceClass());
		Assert.assertNull(goodLocator.getDefaultSelector());
		Assert.assertSame(testCl, goodLocator.getClassLoader());

		goodLocator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class, testCl, goodSelector);
		Assert.assertSame(GoodServiceTest.class, goodLocator.getServiceClass());
		Assert.assertSame(goodSelector, goodLocator.getDefaultSelector());
		Assert.assertSame(testCl, goodLocator.getClassLoader());

		try {
			goodLocator = new PluggableServiceLocator<GoodServiceTest>(null, (ClassLoader) null);
			Assert.fail("Did not fail with null arguments");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			goodLocator = new PluggableServiceLocator<GoodServiceTest>(null, testCl);
			Assert.fail("Did not fail with null arguments");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			goodLocator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class, (ClassLoader) null);
			Assert.fail("Did not fail with null arguments");
		} catch (IllegalArgumentException e) {
			// All is well
		}

		try {
			goodLocator = new PluggableServiceLocator<GoodServiceTest>(null, null, null);
			Assert.fail("Did not fail with null arguments");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			goodLocator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class, null, null);
			Assert.fail("Did not fail with null arguments");
		} catch (IllegalArgumentException e) {
			// All is well
		}
		try {
			goodLocator = new PluggableServiceLocator<GoodServiceTest>(null, testCl, null);
			Assert.fail("Did not fail with null arguments");
		} catch (IllegalArgumentException e) {
			// All is well
		}

		// Should work
		goodLocator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class, testCl, null);
	}

	@Test
	public void testDefaultSelector() {
		PluggableServiceSelector<GoodServiceTest> selector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				return false;
			}
		};
		PluggableServiceLocator<GoodServiceTest> goodLocator = null;

		goodLocator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class);
		Assert.assertNull(goodLocator.getDefaultSelector());
		goodLocator.setDefaultSelector(selector);
		Assert.assertSame(selector, goodLocator.getDefaultSelector());
		goodLocator.setDefaultSelector(null);
		Assert.assertNull(goodLocator.getDefaultSelector());

		goodLocator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class, selector);
		Assert.assertSame(selector, goodLocator.getDefaultSelector());
		goodLocator.setDefaultSelector(null);
		Assert.assertNull(goodLocator.getDefaultSelector());
		goodLocator.setDefaultSelector(selector);
		Assert.assertSame(selector, goodLocator.getDefaultSelector());
	}

	@Test
	public void testGetFirst() {
		PluggableServiceLocator<GoodServiceTest> goodLocator = null;
		PluggableServiceSelector<GoodServiceTest> goodSelector = null;

		goodLocator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class);
		Assert.assertNull(goodLocator.getDefaultSelector());
		Assert.assertNotNull(goodLocator.getFirst());

		goodSelector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				return false;
			}
		};
		goodLocator.setDefaultSelector(goodSelector);
		try {
			GoodServiceTest ret = goodLocator.getFirst();
			Assert.fail(String.format("Expected to fail due to no elements, but instead got [%s]", ret.getClass()
				.getCanonicalName()));
		} catch (NoSuchElementException e) {
			// All is well
		}

		goodSelector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				String className = service.getClass().getCanonicalName();
				return PluggableServiceLocatorTest.SUBSET_1.contains(className)
					&& !PluggableServiceLocatorTest.SUBSET_2.contains(className);
			}
		};
		goodLocator.setDefaultSelector(null);
		Assert.assertNotNull(goodLocator.getFirst(goodSelector));
		goodLocator.setDefaultSelector(goodSelector);
		Assert.assertNotNull(goodLocator.getFirst());

		PluggableServiceLocator<BadServiceTest> badLocator = null;

		badLocator = new PluggableServiceLocator<BadServiceTest>(BadServiceTest.class);
		Assert.assertNull(badLocator.getDefaultSelector());
		try {
			badLocator.getFirst();
			Assert.fail("Should have failed to find an instance");
		} catch (NoSuchElementException e) {
			// / all is well
		}

		badLocator = new PluggableServiceLocator<BadServiceTest>(BadServiceTest.class);
		final AtomicReference<Throwable> exception = new AtomicReference<Throwable>();
		ErrorListener listener = new ErrorListener() {
			@Override
			public void errorRaised(Throwable e) {
				exception.set(e);
			}
		};
		badLocator.setErrorListener(listener);
		Assert.assertSame(listener, badLocator.getErrorListener());
		try {
			badLocator.getFirst();
			Assert.fail("Should have failed to find an instance");
		} catch (NoSuchElementException e) {
			// / all is well
		}
		Assert.assertNotNull(exception.get());
		listener = new ErrorListener() {
			@Override
			public void errorRaised(Throwable e) {
				throw new RuntimeException();
			}
		};
		badLocator.setErrorListener(listener);
		try {
			badLocator.getFirst();
			Assert.fail("Should have failed to find an instance");
		} catch (NoSuchElementException e) {
			// / all is well
		}
	}

	@Test
	public void testGetAll() {
		PluggableServiceLocator<GoodServiceTest> goodLocator = null;
		PluggableServiceSelector<GoodServiceTest> goodSelector = null;

		goodLocator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class);
		Assert.assertNull(goodLocator.getDefaultSelector());

		int count = 0;
		for (Iterator<GoodServiceTest> it = goodLocator.getAll(); it.hasNext(); count++) {
			it.next();
		}
		Assert.assertEquals(PluggableServiceLocatorTest.SERVICE_CLASSES.size(), count);

		goodSelector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				return false;
			}
		};
		Assert.assertFalse(goodLocator.getAll(goodSelector).hasNext());
		try {
			GoodServiceTest ret = goodLocator.getAll(goodSelector).next();
			Assert.fail(String.format("Expected to fail due to no elements, but instead got [%s]", ret.getClass()
				.getCanonicalName()));
		} catch (NoSuchElementException e) {
			// All is well
		}

		goodSelector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				return PluggableServiceLocatorTest.SERVICE_CLASSES.contains(service.getClass().getCanonicalName());
			}
		};
		for (Iterator<GoodServiceTest> it = goodLocator.getAll(); it.hasNext();) {
			GoodServiceTest s = it.next();
			if (!goodSelector.matches(s)) {
				Assert.fail(String.format("Got class [%s] but it's not listed as part of %s", s.getClass()
					.getCanonicalName(), PluggableServiceLocatorTest.SERVICE_CLASSES));
			}
		}

		goodSelector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				return PluggableServiceLocatorTest.SUBSET_1.contains(service.getClass().getCanonicalName());
			}
		};
		for (Iterator<GoodServiceTest> it = goodLocator.getAll(goodSelector); it.hasNext();) {
			GoodServiceTest s = it.next();
			if (!goodSelector.matches(s)) {
				Assert.fail(String.format("Got class [%s] but it's not listed as part of %s", s.getClass()
					.getCanonicalName(), PluggableServiceLocatorTest.SUBSET_1));
			}
		}

		goodSelector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				return PluggableServiceLocatorTest.SUBSET_2.contains(service.getClass().getCanonicalName());
			}
		};
		for (Iterator<GoodServiceTest> it = goodLocator.getAll(goodSelector); it.hasNext();) {
			GoodServiceTest s = it.next();
			if (!goodSelector.matches(s)) {
				Assert.fail(String.format("Got class [%s] but it's not listed as part of %s", s.getClass()
					.getCanonicalName(), PluggableServiceLocatorTest.SUBSET_2));
			}
		}

		try {
			goodLocator.getAll().remove();
		} catch (UnsupportedOperationException e) {
			// all is well
		}

		PluggableServiceLocator<BadServiceTest> badLocator = null;

		badLocator = new PluggableServiceLocator<BadServiceTest>(BadServiceTest.class);
		Assert.assertNull(badLocator.getDefaultSelector());
		Assert.assertFalse(badLocator.getAll().hasNext());

		badLocator = new PluggableServiceLocator<BadServiceTest>(BadServiceTest.class);
		final AtomicReference<Throwable> exception = new AtomicReference<Throwable>();
		ErrorListener listener = new ErrorListener() {
			@Override
			public void errorRaised(Throwable e) {
				exception.set(e);
			}
		};
		badLocator.setErrorListener(listener);
		Assert.assertSame(listener, badLocator.getErrorListener());
		Assert.assertFalse(badLocator.getAll().hasNext());
		Assert.assertNotNull(exception.get());
		listener = new ErrorListener() {
			@Override
			public void errorRaised(Throwable e) {
				throw new RuntimeException();
			}
		};
		badLocator.setErrorListener(listener);
		Assert.assertFalse(badLocator.getAll().hasNext());
	}

	@Test
	public void testReload() {
		PluggableServiceLocator<GoodServiceTest> goodLocator = new PluggableServiceLocator<GoodServiceTest>(
			GoodServiceTest.class);
		Map<String, GoodServiceTest> cache = new HashMap<String, GoodServiceTest>();
		for (Iterator<GoodServiceTest> it = goodLocator.getAll(); it.hasNext();) {
			GoodServiceTest s = it.next();
			cache.put(s.getClass().getCanonicalName(), s);
		}
		goodLocator.reload();
		for (Iterator<GoodServiceTest> it = goodLocator.getAll(); it.hasNext();) {
			GoodServiceTest actual = it.next();
			GoodServiceTest expected = cache.get(actual.getClass().getCanonicalName());
			Assert.assertNotNull(String.format("Reload caused class [%s] to be loaded, but it wasn't expected", actual
				.getClass().getCanonicalName()), expected);
			Assert.assertNotSame(expected, actual);
		}
	}

	@Test
	public void testIterator() {
		PluggableServiceLocator<GoodServiceTest> goodLocator = null;
		PluggableServiceSelector<GoodServiceTest> goodSelector = null;

		goodLocator = new PluggableServiceLocator<GoodServiceTest>(GoodServiceTest.class);
		Assert.assertNull(goodLocator.getDefaultSelector());

		int count = 0;
		for (Iterator<GoodServiceTest> it = goodLocator.iterator(); it.hasNext(); count++) {
			it.next();
		}
		Assert.assertEquals(PluggableServiceLocatorTest.SERVICE_CLASSES.size(), count);

		goodSelector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				return false;
			}
		};
		goodLocator.setDefaultSelector(goodSelector);
		Assert.assertFalse(goodLocator.iterator().hasNext());
		try {
			GoodServiceTest ret = goodLocator.iterator().next();
			Assert.fail(String.format("Expected to fail due to no elements, but instead got [%s]", ret.getClass()
				.getCanonicalName()));
		} catch (NoSuchElementException e) {
			// All is well
		}

		goodSelector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				return PluggableServiceLocatorTest.SERVICE_CLASSES.contains(service.getClass().getCanonicalName());
			}
		};
		for (Iterator<GoodServiceTest> it = goodLocator.iterator(); it.hasNext();) {
			GoodServiceTest s = it.next();
			if (!goodSelector.matches(s)) {
				Assert.fail(String.format("Got class [%s] but it's not listed as part of %s", s.getClass()
					.getCanonicalName(), PluggableServiceLocatorTest.SERVICE_CLASSES));
			}
		}

		goodSelector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				return PluggableServiceLocatorTest.SUBSET_1.contains(service.getClass().getCanonicalName());
			}
		};
		goodLocator.setDefaultSelector(goodSelector);
		for (Iterator<GoodServiceTest> it = goodLocator.iterator(); it.hasNext();) {
			GoodServiceTest s = it.next();
			if (!goodSelector.matches(s)) {
				Assert.fail(String.format("Got class [%s] but it's not listed as part of %s", s.getClass()
					.getCanonicalName(), PluggableServiceLocatorTest.SUBSET_1));
			}
		}

		goodSelector = new PluggableServiceSelector<GoodServiceTest>() {
			@Override
			public boolean matches(GoodServiceTest service) {
				return PluggableServiceLocatorTest.SUBSET_2.contains(service.getClass().getCanonicalName());
			}
		};
		goodLocator.setDefaultSelector(goodSelector);
		for (Iterator<GoodServiceTest> it = goodLocator.iterator(); it.hasNext();) {
			GoodServiceTest s = it.next();
			if (!goodSelector.matches(s)) {
				Assert.fail(String.format("Got class [%s] but it's not listed as part of %s", s.getClass()
					.getCanonicalName(), PluggableServiceLocatorTest.SUBSET_2));
			}
		}

		try {
			goodLocator.iterator().remove();
		} catch (UnsupportedOperationException e) {
			// all is well
		}

		PluggableServiceLocator<BadServiceTest> badLocator = null;

		badLocator = new PluggableServiceLocator<BadServiceTest>(BadServiceTest.class);
		Assert.assertNull(badLocator.getDefaultSelector());
		Assert.assertFalse(badLocator.iterator().hasNext());

		badLocator = new PluggableServiceLocator<BadServiceTest>(BadServiceTest.class);
		final AtomicReference<Throwable> exception = new AtomicReference<Throwable>();
		ErrorListener listener = new ErrorListener() {
			@Override
			public void errorRaised(Throwable e) {
				exception.set(e);
			}
		};
		badLocator.setErrorListener(listener);
		Assert.assertSame(listener, badLocator.getErrorListener());
		Assert.assertFalse(badLocator.iterator().hasNext());
		Assert.assertNotNull(exception.get());
		listener = new ErrorListener() {
			@Override
			public void errorRaised(Throwable e) {
				throw new RuntimeException();
			}
		};
		badLocator.setErrorListener(listener);
		Assert.assertFalse(badLocator.iterator().hasNext());
	}
}