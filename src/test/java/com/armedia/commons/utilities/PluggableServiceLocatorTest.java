package com.armedia.commons.utilities;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
		Set<String> a = new TreeSet<>();
		Set<String> b = new TreeSet<>();
		for (Class<?> c : goodClasses) {
			if (GoodServiceTest.class.isAssignableFrom(c)) {
				a.add(c.getCanonicalName());
			} else {
				b.add(c.getCanonicalName());
			}
		}
		if (!b.isEmpty()) {
			throw new RuntimeException(String.format(
				"The following classes must ALL implement the GoodServiceTest interface (or this test changed to not require it): ",
				b));
		}
		SERVICE_CLASSES = Collections.unmodifiableSet(a);
		Assertions.assertTrue(a.size() > 1, "Must have more than one class implementing GoodServiceTest");

		a = new TreeSet<>();
		b = new TreeSet<>();
		int i = 0;
		for (String str : PluggableServiceLocatorTest.SERVICE_CLASSES) {
			if (i < (PluggableServiceLocatorTest.SERVICE_CLASSES.size() / 2)) {
				a.add(str);
			} else {
				b.add(str);
			}
			i++;
		}
		Assertions.assertFalse(a.isEmpty());
		Assertions.assertFalse(b.isEmpty());
		Assertions.assertNotEquals(a, b);
		SUBSET_1 = Collections.unmodifiableSet(a);
		SUBSET_2 = Collections.unmodifiableSet(b);
	}

	@Test
	public void testConstructors() {
		Predicate<GoodServiceTest> goodSelector = (s) -> false;
		PluggableServiceLocator<?> goodLocator = null;

		ClassLoader testCl = Thread.currentThread().getContextClassLoader();

		goodLocator = new PluggableServiceLocator<>(GoodServiceTest.class);
		Assertions.assertSame(GoodServiceTest.class, goodLocator.getServiceClass());
		Assertions.assertNull(goodLocator.getDefaultSelector());
		Assertions.assertSame(Thread.currentThread().getContextClassLoader(), goodLocator.getClassLoader());

		goodLocator = new PluggableServiceLocator<>(GoodServiceTest.class, goodSelector);
		Assertions.assertSame(GoodServiceTest.class, goodLocator.getServiceClass());
		Assertions.assertSame(goodSelector, goodLocator.getDefaultSelector());
		Assertions.assertSame(Thread.currentThread().getContextClassLoader(), goodLocator.getClassLoader());

		goodLocator = new PluggableServiceLocator<>(GoodServiceTest.class, testCl);
		Assertions.assertSame(GoodServiceTest.class, goodLocator.getServiceClass());
		Assertions.assertNull(goodLocator.getDefaultSelector());
		Assertions.assertSame(testCl, goodLocator.getClassLoader());

		goodLocator = new PluggableServiceLocator<>(GoodServiceTest.class, testCl, goodSelector);
		Assertions.assertSame(GoodServiceTest.class, goodLocator.getServiceClass());
		Assertions.assertSame(goodSelector, goodLocator.getDefaultSelector());
		Assertions.assertSame(testCl, goodLocator.getClassLoader());

		testCl = new ClassLoader(testCl) {
		};

		goodLocator = new PluggableServiceLocator<>(GoodServiceTest.class, testCl);
		Assertions.assertSame(GoodServiceTest.class, goodLocator.getServiceClass());
		Assertions.assertNull(goodLocator.getDefaultSelector());
		Assertions.assertSame(testCl, goodLocator.getClassLoader());

		goodLocator = new PluggableServiceLocator<>(GoodServiceTest.class, testCl, goodSelector);
		Assertions.assertSame(GoodServiceTest.class, goodLocator.getServiceClass());
		Assertions.assertSame(goodSelector, goodLocator.getDefaultSelector());
		Assertions.assertSame(testCl, goodLocator.getClassLoader());

		Assertions.assertThrows(IllegalArgumentException.class,
			() -> new PluggableServiceLocator<GoodServiceTest>(null, (ClassLoader) null));
		{
			ClassLoader cl = testCl;
			Assertions.assertThrows(IllegalArgumentException.class,
				() -> new PluggableServiceLocator<GoodServiceTest>(null, cl));
			Assertions.assertThrows(IllegalArgumentException.class,
				() -> new PluggableServiceLocator<>(GoodServiceTest.class, (ClassLoader) null));
			Assertions.assertThrows(IllegalArgumentException.class,
				() -> new PluggableServiceLocator<GoodServiceTest>(null, null, null));
			Assertions.assertThrows(IllegalArgumentException.class,
				() -> new PluggableServiceLocator<>(GoodServiceTest.class, null, null));
			Assertions.assertThrows(IllegalArgumentException.class,
				() -> new PluggableServiceLocator<GoodServiceTest>(null, cl, null));
		}

		// Should work
		goodLocator = new PluggableServiceLocator<>(GoodServiceTest.class, testCl, null);
	}

	@Test
	public void testDefaultSelector() {
		Predicate<GoodServiceTest> selector = (s) -> false;
		PluggableServiceLocator<GoodServiceTest> goodLocator = null;

		goodLocator = new PluggableServiceLocator<>(GoodServiceTest.class);
		Assertions.assertNull(goodLocator.getDefaultSelector());
		goodLocator.setDefaultSelector(selector);
		Assertions.assertSame(selector, goodLocator.getDefaultSelector());
		goodLocator.setDefaultSelector(null);
		Assertions.assertNull(goodLocator.getDefaultSelector());

		goodLocator = new PluggableServiceLocator<>(GoodServiceTest.class, selector);
		Assertions.assertSame(selector, goodLocator.getDefaultSelector());
		goodLocator.setDefaultSelector(null);
		Assertions.assertNull(goodLocator.getDefaultSelector());
		goodLocator.setDefaultSelector(selector);
		Assertions.assertSame(selector, goodLocator.getDefaultSelector());
	}

	@Test
	public void testGetFirst() {
		PluggableServiceLocator<GoodServiceTest> goodLocator = new PluggableServiceLocator<>(GoodServiceTest.class);
		Predicate<GoodServiceTest> goodSelector = null;

		Assertions.assertNull(goodLocator.getDefaultSelector());
		Assertions.assertNotNull(goodLocator.getFirst());

		goodSelector = (s) -> false;
		goodLocator.setDefaultSelector(goodSelector);
		Assertions.assertThrows(NoSuchElementException.class, () -> goodLocator.getFirst());

		goodSelector = (s) -> {
			String className = s.getClass().getCanonicalName();
			return PluggableServiceLocatorTest.SUBSET_1.contains(className)
				&& !PluggableServiceLocatorTest.SUBSET_2.contains(className);
		};
		goodLocator.setDefaultSelector(null);
		Assertions.assertNotNull(goodLocator.getFirst(goodSelector));
		goodLocator.setDefaultSelector(goodSelector);
		Assertions.assertNotNull(goodLocator.getFirst());

		{
			PluggableServiceLocator<BadServiceTest> badLocator = new PluggableServiceLocator<>(BadServiceTest.class);
			Assertions.assertNull(badLocator.getDefaultSelector());
			Assertions.assertNull(badLocator.getErrorListener());
			Assertions.assertFalse(badLocator.isHideErrors());
			try {
				badLocator.getFirst();
				Assertions.fail("Should have failed to find an instance");
			} catch (NoSuchElementException e) {
				Assertions.fail("Should have failed with a ServiceConfigurationError");
			} catch (ServiceConfigurationError e) {
				Throwable t = e.getCause();
				Assertions.assertEquals(RuntimeException.class, t.getClass());
				Assertions.assertEquals(ExplodingTest.ERROR_STR, t.getMessage());
			}
		}

		{
			PluggableServiceLocator<BadServiceTest> badLocator = new PluggableServiceLocator<>(BadServiceTest.class);
			Assertions.assertFalse(badLocator.isHideErrors());
			badLocator.setHideErrors(true);
			Assertions.assertTrue(badLocator.isHideErrors());
			Assertions.assertThrows(NoSuchElementException.class, () -> badLocator.getFirst());
		}

		{
			PluggableServiceLocator<BadServiceTest> badLocator = new PluggableServiceLocator<>(BadServiceTest.class);
			Assertions.assertFalse(badLocator.isHideErrors());
			final AtomicReference<Throwable> exception = new AtomicReference<>();
			BiConsumer<Class<?>, Throwable> listener = (serviceClass, e) -> exception.set(e);
			badLocator.setErrorListener(listener);
			Assertions.assertSame(listener, badLocator.getErrorListener());
			Assertions.assertThrows(NoSuchElementException.class, () -> badLocator.getFirst());
			Assertions.assertNotNull(exception.get());
			exception.set(null);
			badLocator.setHideErrors(true);
			Assertions.assertThrows(NoSuchElementException.class, () -> badLocator.getFirst());
			Assertions.assertNull(exception.get());
		}

		{
			PluggableServiceLocator<BadServiceTest> badLocator = new PluggableServiceLocator<>(BadServiceTest.class);
			badLocator.setHideErrors(false);
			Assertions.assertFalse(badLocator.isHideErrors());
			badLocator.setErrorListener((serviceClass, e) -> {
				throw new RuntimeException();
			});
			Assertions.assertThrows(NoSuchElementException.class, () -> badLocator.getFirst());
		}
	}

	@Test
	public void testGetAll() {
		PluggableServiceLocator<GoodServiceTest> goodLocator = new PluggableServiceLocator<>(GoodServiceTest.class);

		Assertions.assertNull(goodLocator.getDefaultSelector());

		int count = 0;
		for (Iterator<GoodServiceTest> it = goodLocator.getAll(); it.hasNext(); count++) {
			it.next();
		}
		Assertions.assertEquals(PluggableServiceLocatorTest.SERVICE_CLASSES.size(), count);

		{
			Predicate<GoodServiceTest> goodSelector = (s) -> false;
			Assertions.assertFalse(goodLocator.getAll(goodSelector).hasNext());
			Assertions.assertThrows(NoSuchElementException.class, () -> goodLocator.getAll(goodSelector).next());
		}

		{
			Predicate<GoodServiceTest> goodSelector = (s) -> PluggableServiceLocatorTest.SERVICE_CLASSES
				.contains(s.getClass().getCanonicalName());
			for (Iterator<GoodServiceTest> it = goodLocator.getAll(); it.hasNext();) {
				GoodServiceTest s = it.next();
				if (!goodSelector.test(s)) {
					Assertions.fail(String.format("Got class [%s] but it's not listed as part of %s",
						s.getClass().getCanonicalName(), PluggableServiceLocatorTest.SERVICE_CLASSES));
				}
			}
		}

		{
			Predicate<GoodServiceTest> goodSelector = (s) -> PluggableServiceLocatorTest.SUBSET_1
				.contains(s.getClass().getCanonicalName());
			for (Iterator<GoodServiceTest> it = goodLocator.getAll(goodSelector); it.hasNext();) {
				GoodServiceTest s = it.next();
				if (!goodSelector.test(s)) {
					Assertions.fail(String.format("Got class [%s] but it's not listed as part of %s",
						s.getClass().getCanonicalName(), PluggableServiceLocatorTest.SUBSET_1));
				}
			}
		}

		{
			Predicate<GoodServiceTest> goodSelector = (s) -> PluggableServiceLocatorTest.SUBSET_2
				.contains(s.getClass().getCanonicalName());
			for (Iterator<GoodServiceTest> it = goodLocator.getAll(goodSelector); it.hasNext();) {
				GoodServiceTest s = it.next();
				if (!goodSelector.test(s)) {
					Assertions.fail(String.format("Got class [%s] but it's not listed as part of %s",
						s.getClass().getCanonicalName(), PluggableServiceLocatorTest.SUBSET_2));
				}
			}
		}

		Assertions.assertThrows(UnsupportedOperationException.class, () -> goodLocator.getAll().remove());

		PluggableServiceLocator<BadServiceTest> badLocator = null;

		badLocator = new PluggableServiceLocator<>(BadServiceTest.class);
		Assertions.assertNull(badLocator.getDefaultSelector());
		Assertions.assertNull(badLocator.getErrorListener());
		Assertions.assertFalse(badLocator.isHideErrors());
		try {
			badLocator.getAll().hasNext();
			Assertions.fail("Should have failed with a ServiceConfigurationError");
		} catch (ServiceConfigurationError e) {
			Throwable t = e.getCause();
			Assertions.assertEquals(RuntimeException.class, t.getClass());
			Assertions.assertEquals(ExplodingTest.ERROR_STR, t.getMessage());
		}
		badLocator = new PluggableServiceLocator<>(BadServiceTest.class);
		Assertions.assertFalse(badLocator.isHideErrors());
		badLocator.setHideErrors(true);
		Assertions.assertTrue(badLocator.isHideErrors());
		Assertions.assertFalse(badLocator.getAll().hasNext());

		badLocator = new PluggableServiceLocator<>(BadServiceTest.class);
		final AtomicReference<Throwable> exception = new AtomicReference<>();
		BiConsumer<Class<?>, Throwable> listener = (serviceClass, e) -> exception.set(e);
		badLocator.setErrorListener(listener);
		Assertions.assertSame(listener, badLocator.getErrorListener());
		Assertions.assertFalse(badLocator.getAll().hasNext());
		Assertions.assertNotNull(exception.get());

		badLocator = new PluggableServiceLocator<>(BadServiceTest.class);
		exception.set(null);
		Assertions.assertFalse(badLocator.isHideErrors());
		badLocator.setHideErrors(true);
		Assertions.assertTrue(badLocator.isHideErrors());
		Assertions.assertFalse(badLocator.getAll().hasNext());
		badLocator.setErrorListener(listener);
		Assertions.assertSame(listener, badLocator.getErrorListener());
		Assertions.assertFalse(badLocator.getAll().hasNext());
		Assertions.assertNull(exception.get());

		badLocator = new PluggableServiceLocator<>(BadServiceTest.class);
		badLocator.setHideErrors(false);
		Assertions.assertFalse(badLocator.isHideErrors());
		listener = (serviceClass, e) -> {
			throw new RuntimeException();
		};
		badLocator.setErrorListener(listener);
		Assertions.assertFalse(badLocator.getAll().hasNext());
	}

	@Test
	public void testReload() {
		PluggableServiceLocator<GoodServiceTest> goodLocator = new PluggableServiceLocator<>(GoodServiceTest.class);
		Map<String, GoodServiceTest> cache = new HashMap<>();
		for (Iterator<GoodServiceTest> it = goodLocator.getAll(); it.hasNext();) {
			GoodServiceTest s = it.next();
			cache.put(s.getClass().getCanonicalName(), s);
		}
		goodLocator.reload();
		for (Iterator<GoodServiceTest> it = goodLocator.getAll(); it.hasNext();) {
			GoodServiceTest actual = it.next();
			GoodServiceTest expected = cache.get(actual.getClass().getCanonicalName());
			Assertions.assertNotNull(expected, String.format(
				"Reload caused class [%s] to be loaded, but it wasn't expected", actual.getClass().getCanonicalName()));
			Assertions.assertNotSame(expected, actual);
		}
	}

	@Test
	public void testIterator() {
		PluggableServiceLocator<GoodServiceTest> goodLocator = new PluggableServiceLocator<>(GoodServiceTest.class);
		Predicate<GoodServiceTest> goodSelector = null;

		Assertions.assertNull(goodLocator.getDefaultSelector());

		int count = 0;
		for (Iterator<GoodServiceTest> it = goodLocator.iterator(); it.hasNext(); count++) {
			it.next();
		}
		Assertions.assertEquals(PluggableServiceLocatorTest.SERVICE_CLASSES.size(), count);

		goodSelector = (s) -> false;
		goodLocator.setDefaultSelector(goodSelector);
		Assertions.assertFalse(goodLocator.iterator().hasNext());
		Assertions.assertThrows(NoSuchElementException.class, () -> goodLocator.iterator().next());

		goodSelector = (s) -> PluggableServiceLocatorTest.SERVICE_CLASSES.contains(s.getClass().getCanonicalName());
		for (Iterator<GoodServiceTest> it = goodLocator.iterator(); it.hasNext();) {
			GoodServiceTest s = it.next();
			if (!goodSelector.test(s)) {
				Assertions.fail(String.format("Got class [%s] but it's not listed as part of %s",
					s.getClass().getCanonicalName(), PluggableServiceLocatorTest.SERVICE_CLASSES));
			}
		}

		goodSelector = (s) -> PluggableServiceLocatorTest.SUBSET_1.contains(s.getClass().getCanonicalName());
		goodLocator.setDefaultSelector(goodSelector);
		for (Iterator<GoodServiceTest> it = goodLocator.iterator(); it.hasNext();) {
			GoodServiceTest s = it.next();
			if (!goodSelector.test(s)) {
				Assertions.fail(String.format("Got class [%s] but it's not listed as part of %s",
					s.getClass().getCanonicalName(), PluggableServiceLocatorTest.SUBSET_1));
			}
		}

		goodSelector = (s) -> PluggableServiceLocatorTest.SUBSET_2.contains(s.getClass().getCanonicalName());
		goodLocator.setDefaultSelector(goodSelector);
		for (Iterator<GoodServiceTest> it = goodLocator.iterator(); it.hasNext();) {
			GoodServiceTest s = it.next();
			if (!goodSelector.test(s)) {
				Assertions.fail(String.format("Got class [%s] but it's not listed as part of %s",
					s.getClass().getCanonicalName(), PluggableServiceLocatorTest.SUBSET_2));
			}
		}

		Assertions.assertThrows(UnsupportedOperationException.class, () -> goodLocator.iterator().remove());

		PluggableServiceLocator<BadServiceTest> badLocator = null;

		badLocator = new PluggableServiceLocator<>(BadServiceTest.class);
		Assertions.assertNull(badLocator.getDefaultSelector());
		Assertions.assertNull(badLocator.getErrorListener());
		Assertions.assertFalse(badLocator.isHideErrors());
		try {
			badLocator.iterator().hasNext();
			Assertions.fail("Should have failed with a ServiceConfigurationError");
		} catch (ServiceConfigurationError e) {
			Throwable t = e.getCause();
			Assertions.assertEquals(RuntimeException.class, t.getClass());
			Assertions.assertEquals(ExplodingTest.ERROR_STR, t.getMessage());
		}
		badLocator = new PluggableServiceLocator<>(BadServiceTest.class);
		Assertions.assertFalse(badLocator.isHideErrors());
		badLocator.setHideErrors(true);
		Assertions.assertTrue(badLocator.isHideErrors());
		Assertions.assertFalse(badLocator.iterator().hasNext());

		badLocator = new PluggableServiceLocator<>(BadServiceTest.class);
		final AtomicReference<Throwable> exception = new AtomicReference<>();
		BiConsumer<Class<?>, Throwable> listener = (serviceClass, e) -> exception.set(e);
		badLocator.setErrorListener(listener);
		Assertions.assertSame(listener, badLocator.getErrorListener());
		Assertions.assertFalse(badLocator.iterator().hasNext());
		Assertions.assertNotNull(exception.get());

		badLocator = new PluggableServiceLocator<>(BadServiceTest.class);
		exception.set(null);
		Assertions.assertFalse(badLocator.isHideErrors());
		badLocator.setHideErrors(true);
		Assertions.assertTrue(badLocator.isHideErrors());
		Assertions.assertFalse(badLocator.getAll().hasNext());
		badLocator.setErrorListener(listener);
		Assertions.assertSame(listener, badLocator.getErrorListener());
		Assertions.assertFalse(badLocator.iterator().hasNext());
		Assertions.assertNull(exception.get());

		badLocator = new PluggableServiceLocator<>(BadServiceTest.class);
		badLocator.setHideErrors(false);
		Assertions.assertFalse(badLocator.isHideErrors());
		listener = (serviceClass, e) -> {
			throw new RuntimeException();
		};
		badLocator.setErrorListener(listener);
		Assertions.assertFalse(badLocator.iterator().hasNext());
	}
}