/*******************************************************************************
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (c) 2010 - 2019 Armedia LLC
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
package com.armedia.commons.utilities;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
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

import com.armedia.commons.utilities.codec.BinaryEncodingTest;

public class PluggableServiceLocatorTest {

	private static final Set<String> GOOD_CLASSES;
	private static final Set<String> SUBSET_1;
	private static final Set<String> SUBSET_2;
	private static final Set<Class<?>> BAD_CLASSES;

	static {
		Class<?>[] goodClasses = {
			BasicIndexedIteratorTest.class, //
			BinaryEncodingTest.class, //
			BinaryMemoryBufferTest.class, //
			CfgToolsStaticTest.class, //
			CfgToolsTest.class, //
			CollectionToolsTest.class, //
			ComparisonTest.class, //
			FileNameToolsTest.class, //
			GlobberTest.class, //
		};
		Set<String> a = new TreeSet<>();
		Set<String> b = new TreeSet<>();
		for (Class<?> c : goodClasses) {
			if (GoodService.class.isAssignableFrom(c)) {
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
		GOOD_CLASSES = Collections.unmodifiableSet(a);
		Assertions.assertTrue(a.size() > 1, "Must have more than one class implementing GoodServiceTest");

		a = new TreeSet<>();
		b = new TreeSet<>();
		int i = 0;
		for (String str : PluggableServiceLocatorTest.GOOD_CLASSES) {
			if (i < (PluggableServiceLocatorTest.GOOD_CLASSES.size() / 2)) {
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

		Class<?>[] badClasses = {
			BadServiceClassInitializationFailed.class, //
			BadServiceError.class, //
			BadServiceException.class, //
			BadServiceUncheckedException.class, //
		};
		Set<Class<?>> bc = new LinkedHashSet<>();
		for (Class<?> c : badClasses) {
			bc.add(c);
		}
		BAD_CLASSES = Tools.freezeSet(bc);
	}

	@Test
	public void testConstructors() {
		Predicate<GoodService> goodSelector = (s) -> false;
		PluggableServiceLocator<?> goodLocator = null;

		ClassLoader testCl = Thread.currentThread().getContextClassLoader();

		goodLocator = new PluggableServiceLocator<>(GoodService.class);
		Assertions.assertSame(GoodService.class, goodLocator.getServiceClass());
		Assertions.assertNull(goodLocator.getDefaultSelector());
		Assertions.assertSame(Thread.currentThread().getContextClassLoader(), goodLocator.getClassLoader());

		goodLocator = new PluggableServiceLocator<>(GoodService.class, goodSelector);
		Assertions.assertSame(GoodService.class, goodLocator.getServiceClass());
		Assertions.assertSame(goodSelector, goodLocator.getDefaultSelector());
		Assertions.assertSame(Thread.currentThread().getContextClassLoader(), goodLocator.getClassLoader());

		goodLocator = new PluggableServiceLocator<>(GoodService.class, testCl);
		Assertions.assertSame(GoodService.class, goodLocator.getServiceClass());
		Assertions.assertNull(goodLocator.getDefaultSelector());
		Assertions.assertSame(testCl, goodLocator.getClassLoader());

		goodLocator = new PluggableServiceLocator<>(GoodService.class, testCl, goodSelector);
		Assertions.assertSame(GoodService.class, goodLocator.getServiceClass());
		Assertions.assertSame(goodSelector, goodLocator.getDefaultSelector());
		Assertions.assertSame(testCl, goodLocator.getClassLoader());

		testCl = new ClassLoader(testCl) {
		};

		goodLocator = new PluggableServiceLocator<>(GoodService.class, testCl);
		Assertions.assertSame(GoodService.class, goodLocator.getServiceClass());
		Assertions.assertNull(goodLocator.getDefaultSelector());
		Assertions.assertSame(testCl, goodLocator.getClassLoader());

		goodLocator = new PluggableServiceLocator<>(GoodService.class, testCl, goodSelector);
		Assertions.assertSame(GoodService.class, goodLocator.getServiceClass());
		Assertions.assertSame(goodSelector, goodLocator.getDefaultSelector());
		Assertions.assertSame(testCl, goodLocator.getClassLoader());

		Assertions.assertThrows(IllegalArgumentException.class,
			() -> new PluggableServiceLocator<GoodService>(null, (ClassLoader) null));
		{
			ClassLoader cl = testCl;
			Assertions.assertThrows(IllegalArgumentException.class,
				() -> new PluggableServiceLocator<GoodService>(null, cl));
			Assertions.assertThrows(IllegalArgumentException.class,
				() -> new PluggableServiceLocator<>(GoodService.class, (ClassLoader) null));
			Assertions.assertThrows(IllegalArgumentException.class,
				() -> new PluggableServiceLocator<GoodService>(null, null, null));
			Assertions.assertThrows(IllegalArgumentException.class,
				() -> new PluggableServiceLocator<>(GoodService.class, null, null));
			Assertions.assertThrows(IllegalArgumentException.class,
				() -> new PluggableServiceLocator<GoodService>(null, cl, null));
		}

		// Should work
		goodLocator = new PluggableServiceLocator<>(GoodService.class, testCl, null);
	}

	@Test
	public void testDefaultSelector() {
		Predicate<GoodService> selector = (s) -> false;
		PluggableServiceLocator<GoodService> goodLocator = null;

		goodLocator = new PluggableServiceLocator<>(GoodService.class);
		Assertions.assertNull(goodLocator.getDefaultSelector());
		goodLocator.setDefaultSelector(selector);
		Assertions.assertSame(selector, goodLocator.getDefaultSelector());
		goodLocator.setDefaultSelector(null);
		Assertions.assertNull(goodLocator.getDefaultSelector());

		goodLocator = new PluggableServiceLocator<>(GoodService.class, selector);
		Assertions.assertSame(selector, goodLocator.getDefaultSelector());
		goodLocator.setDefaultSelector(null);
		Assertions.assertNull(goodLocator.getDefaultSelector());
		goodLocator.setDefaultSelector(selector);
		Assertions.assertSame(selector, goodLocator.getDefaultSelector());
	}

	@Test
	public void testGetFirst() {
		PluggableServiceLocator<GoodService> goodLocator = new PluggableServiceLocator<>(GoodService.class);
		Predicate<GoodService> goodSelector = null;

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
			PluggableServiceLocator<BadService> badLocator = new PluggableServiceLocator<>(BadService.class);
			Assertions.assertNull(badLocator.getDefaultSelector());
			Assertions.assertNull(badLocator.getErrorListener());
			Assertions.assertFalse(badLocator.isHideErrors());
			try {
				badLocator.getFirst();
				Assertions.fail("Should have failed to find an instance");
			} catch (NoSuchElementException e) {
				Assertions.fail("Should have failed with a ServiceConfigurationError");
			} catch (ServiceConfigurationError e) {
				// All is well
			}
		}

		{
			PluggableServiceLocator<BadService> badLocator = new PluggableServiceLocator<>(BadService.class);
			Assertions.assertFalse(badLocator.isHideErrors());
			badLocator.setHideErrors(true);
			Assertions.assertTrue(badLocator.isHideErrors());
			Assertions.assertThrows(NoSuchElementException.class, () -> badLocator.getFirst());
		}

		{
			PluggableServiceLocator<BadService> badLocator = new PluggableServiceLocator<>(BadService.class);
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
			PluggableServiceLocator<BadService> badLocator = new PluggableServiceLocator<>(BadService.class);
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
		PluggableServiceLocator<GoodService> goodLocator = new PluggableServiceLocator<>(GoodService.class);

		Assertions.assertNull(goodLocator.getDefaultSelector());

		int count = 0;
		for (Iterator<GoodService> it = goodLocator.getAll(); it.hasNext(); count++) {
			it.next();
		}
		Assertions.assertEquals(PluggableServiceLocatorTest.GOOD_CLASSES.size(), count);

		{
			Predicate<GoodService> goodSelector = (s) -> false;
			Assertions.assertFalse(goodLocator.getAll(goodSelector).hasNext());
			Assertions.assertThrows(NoSuchElementException.class, () -> goodLocator.getAll(goodSelector).next());
		}

		{
			Predicate<GoodService> goodSelector = (s) -> PluggableServiceLocatorTest.GOOD_CLASSES
				.contains(s.getClass().getCanonicalName());
			for (Iterator<GoodService> it = goodLocator.getAll(); it.hasNext();) {
				GoodService s = it.next();
				if (!goodSelector.test(s)) {
					Assertions.fail(String.format("Got class [%s] but it's not listed as part of %s",
						s.getClass().getCanonicalName(), PluggableServiceLocatorTest.GOOD_CLASSES));
				}
			}
		}

		{
			Predicate<GoodService> goodSelector = (s) -> PluggableServiceLocatorTest.SUBSET_1
				.contains(s.getClass().getCanonicalName());
			for (Iterator<GoodService> it = goodLocator.getAll(goodSelector); it.hasNext();) {
				GoodService s = it.next();
				if (!goodSelector.test(s)) {
					Assertions.fail(String.format("Got class [%s] but it's not listed as part of %s",
						s.getClass().getCanonicalName(), PluggableServiceLocatorTest.SUBSET_1));
				}
			}
		}

		{
			Predicate<GoodService> goodSelector = (s) -> PluggableServiceLocatorTest.SUBSET_2
				.contains(s.getClass().getCanonicalName());
			for (Iterator<GoodService> it = goodLocator.getAll(goodSelector); it.hasNext();) {
				GoodService s = it.next();
				if (!goodSelector.test(s)) {
					Assertions.fail(String.format("Got class [%s] but it's not listed as part of %s",
						s.getClass().getCanonicalName(), PluggableServiceLocatorTest.SUBSET_2));
				}
			}
		}

		Assertions.assertThrows(UnsupportedOperationException.class, () -> goodLocator.getAll().remove());

		PluggableServiceLocator<BadService> badLocator = null;

		badLocator = new PluggableServiceLocator<>(BadService.class);
		Assertions.assertNull(badLocator.getDefaultSelector());
		Assertions.assertNull(badLocator.getErrorListener());
		Assertions.assertFalse(badLocator.isHideErrors());
		Iterator<BadService> it = badLocator.getAll();
		for (Class<?> k : PluggableServiceLocatorTest.BAD_CLASSES) {
			Assertions.assertThrows(ServiceConfigurationError.class, () -> it.hasNext(),
				String.format("While testing [%s]", k.getSimpleName()));
		}
		badLocator = new PluggableServiceLocator<>(BadService.class);
		Assertions.assertFalse(badLocator.isHideErrors());
		badLocator.setHideErrors(true);
		Assertions.assertTrue(badLocator.isHideErrors());
		Assertions.assertFalse(badLocator.getAll().hasNext());

		badLocator = new PluggableServiceLocator<>(BadService.class);
		final AtomicReference<Throwable> exception = new AtomicReference<>();
		BiConsumer<Class<?>, Throwable> listener = (serviceClass, e) -> exception.set(e);
		badLocator.setErrorListener(listener);
		Assertions.assertSame(listener, badLocator.getErrorListener());
		Assertions.assertFalse(badLocator.getAll().hasNext());
		Assertions.assertNotNull(exception.get());

		badLocator = new PluggableServiceLocator<>(BadService.class);
		exception.set(null);
		Assertions.assertFalse(badLocator.isHideErrors());
		badLocator.setHideErrors(true);
		Assertions.assertTrue(badLocator.isHideErrors());
		Assertions.assertFalse(badLocator.getAll().hasNext());
		badLocator.setErrorListener(listener);
		Assertions.assertSame(listener, badLocator.getErrorListener());
		Assertions.assertFalse(badLocator.getAll().hasNext());
		Assertions.assertNull(exception.get());

		badLocator = new PluggableServiceLocator<>(BadService.class);
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
		PluggableServiceLocator<GoodService> goodLocator = new PluggableServiceLocator<>(GoodService.class);
		Map<String, GoodService> cache = new HashMap<>();
		for (Iterator<GoodService> it = goodLocator.getAll(); it.hasNext();) {
			GoodService s = it.next();
			cache.put(s.getClass().getCanonicalName(), s);
		}
		goodLocator.reload();
		for (Iterator<GoodService> it = goodLocator.getAll(); it.hasNext();) {
			GoodService actual = it.next();
			GoodService expected = cache.get(actual.getClass().getCanonicalName());
			Assertions.assertNotNull(expected, String.format(
				"Reload caused class [%s] to be loaded, but it wasn't expected", actual.getClass().getCanonicalName()));
			Assertions.assertNotSame(expected, actual);
		}
	}

	@Test
	public void testIterator() {
		PluggableServiceLocator<GoodService> goodLocator = new PluggableServiceLocator<>(GoodService.class);
		Predicate<GoodService> goodSelector = null;

		Assertions.assertNull(goodLocator.getDefaultSelector());

		int count = 0;
		for (Iterator<GoodService> it = goodLocator.iterator(); it.hasNext(); count++) {
			it.next();
		}
		Assertions.assertEquals(PluggableServiceLocatorTest.GOOD_CLASSES.size(), count);

		goodSelector = (s) -> false;
		goodLocator.setDefaultSelector(goodSelector);
		Assertions.assertFalse(goodLocator.iterator().hasNext());
		Assertions.assertThrows(NoSuchElementException.class, () -> goodLocator.iterator().next());

		goodSelector = (s) -> PluggableServiceLocatorTest.GOOD_CLASSES.contains(s.getClass().getCanonicalName());
		for (Iterator<GoodService> it = goodLocator.iterator(); it.hasNext();) {
			GoodService s = it.next();
			if (!goodSelector.test(s)) {
				Assertions.fail(String.format("Got class [%s] but it's not listed as part of %s",
					s.getClass().getCanonicalName(), PluggableServiceLocatorTest.GOOD_CLASSES));
			}
		}

		goodSelector = (s) -> PluggableServiceLocatorTest.SUBSET_1.contains(s.getClass().getCanonicalName());
		goodLocator.setDefaultSelector(goodSelector);
		for (Iterator<GoodService> it = goodLocator.iterator(); it.hasNext();) {
			GoodService s = it.next();
			if (!goodSelector.test(s)) {
				Assertions.fail(String.format("Got class [%s] but it's not listed as part of %s",
					s.getClass().getCanonicalName(), PluggableServiceLocatorTest.SUBSET_1));
			}
		}

		goodSelector = (s) -> PluggableServiceLocatorTest.SUBSET_2.contains(s.getClass().getCanonicalName());
		goodLocator.setDefaultSelector(goodSelector);
		for (Iterator<GoodService> it = goodLocator.iterator(); it.hasNext();) {
			GoodService s = it.next();
			if (!goodSelector.test(s)) {
				Assertions.fail(String.format("Got class [%s] but it's not listed as part of %s",
					s.getClass().getCanonicalName(), PluggableServiceLocatorTest.SUBSET_2));
			}
		}

		Assertions.assertThrows(UnsupportedOperationException.class, () -> goodLocator.iterator().remove());

		PluggableServiceLocator<BadService> badLocator = null;

		badLocator = new PluggableServiceLocator<>(BadService.class);
		Assertions.assertNull(badLocator.getDefaultSelector());
		Assertions.assertNull(badLocator.getErrorListener());
		Assertions.assertFalse(badLocator.isHideErrors());
		try {
			badLocator.iterator().hasNext();
			Assertions.fail("Should have failed with a ServiceConfigurationError");
		} catch (ServiceConfigurationError e) {
			// All is well...
		}
		badLocator = new PluggableServiceLocator<>(BadService.class);
		Assertions.assertFalse(badLocator.isHideErrors());
		badLocator.setHideErrors(true);
		Assertions.assertTrue(badLocator.isHideErrors());
		Assertions.assertFalse(badLocator.iterator().hasNext());

		badLocator = new PluggableServiceLocator<>(BadService.class);
		final AtomicReference<Throwable> exception = new AtomicReference<>();
		BiConsumer<Class<?>, Throwable> listener = (serviceClass, e) -> exception.set(e);
		badLocator.setErrorListener(listener);
		Assertions.assertSame(listener, badLocator.getErrorListener());
		Assertions.assertFalse(badLocator.iterator().hasNext());
		Assertions.assertNotNull(exception.get());

		badLocator = new PluggableServiceLocator<>(BadService.class);
		exception.set(null);
		Assertions.assertFalse(badLocator.isHideErrors());
		badLocator.setHideErrors(true);
		Assertions.assertTrue(badLocator.isHideErrors());
		Assertions.assertFalse(badLocator.getAll().hasNext());
		badLocator.setErrorListener(listener);
		Assertions.assertSame(listener, badLocator.getErrorListener());
		Assertions.assertFalse(badLocator.iterator().hasNext());
		Assertions.assertNull(exception.get());

		badLocator = new PluggableServiceLocator<>(BadService.class);
		badLocator.setHideErrors(false);
		Assertions.assertFalse(badLocator.isHideErrors());
		listener = (serviceClass, e) -> {
			throw new RuntimeException();
		};
		badLocator.setErrorListener(listener);
		Assertions.assertFalse(badLocator.iterator().hasNext());
	}
}