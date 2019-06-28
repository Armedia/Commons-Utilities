/*******************************************************************************
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (C) 2013 - 2019 Armedia
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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * <p>
 * This class serves as a wrapper for the boilerplate code that would usually accompany
 * {@link ServiceLoader} implementations. In particular, it allows for customized selection of the
 * actual service instance being returned in a modular fashion.
 * </p>
 *
 *
 *
 */
public class PluggableServiceLocator<S> implements Iterable<S> {

	private final ClassLoader classLoader;
	private final Class<S> serviceClass;
	private final ServiceLoader<S> loader;

	private Predicate<S> defaultSelector = null;
	private BiConsumer<Class<?>, Throwable> listener = null;
	private boolean hideErrors = false;

	public PluggableServiceLocator(Class<S> serviceClass) {
		this(serviceClass, Thread.currentThread().getContextClassLoader(), null);
	}

	public PluggableServiceLocator(Class<S> serviceClass, Predicate<S> defaultSelector) {
		this(serviceClass, Thread.currentThread().getContextClassLoader(), defaultSelector);
	}

	public PluggableServiceLocator(Class<S> serviceClass, ClassLoader classLoader) {
		this(serviceClass, classLoader, null);
	}

	public PluggableServiceLocator(Class<S> serviceClass, ClassLoader classLoader, Predicate<S> defaultSelector) {
		if (serviceClass == null) {
			throw new IllegalArgumentException("Must provide a service class for which to locate instances");
		}
		if (classLoader == null) {
			throw new IllegalArgumentException("Must provide a classloader in which to locate instances");
		}
		this.classLoader = classLoader;
		this.serviceClass = serviceClass;
		this.loader = ServiceLoader.load(this.serviceClass, classLoader);
		this.defaultSelector = defaultSelector;
	}

	public final ClassLoader getClassLoader() {
		return this.classLoader;
	}

	public final Class<S> getServiceClass() {
		return this.serviceClass;
	}

	/**
	 * Returns the currently set default selector.
	 *
	 * @return the currently set default selector
	 */
	public final Predicate<S> getDefaultSelector() {
		return this.defaultSelector;
	}

	/**
	 * Set the new default selector.
	 *
	 * @param defaultSelector
	 */
	public final void setDefaultSelector(Predicate<S> defaultSelector) {
		this.defaultSelector = defaultSelector;
	}

	public final BiConsumer<Class<?>, Throwable> getErrorListener() {
		return this.listener;
	}

	public final void setErrorListener(BiConsumer<Class<?>, Throwable> listener) {
		this.listener = listener;
	}

	public final boolean isHideErrors() {
		return this.hideErrors;
	}

	public final void setHideErrors(boolean hideErrors) {
		this.hideErrors = hideErrors;
	}

	/**
	 * Direct delegate to {@link ServiceLoader#reload()}.
	 */
	public final void reload() {
		this.loader.reload();
	}

	/**
	 * Returns the first available service instance. This is identical to invoking
	 * {@code getAll(null).next()}.
	 *
	 * @return the first service instance that matches the currently configured default selector
	 * @throws NoSuchElementException
	 *             when there is no matching service available
	 */
	public final S getFirst() throws NoSuchElementException {
		return getFirst(null);
	}

	/**
	 * Returns the first service instance that matches the given selector. This is identical to
	 * invoking {@code getAll(selector).next()}.
	 *
	 * @param selector
	 *            the selector to use when finding service matches
	 * @return the first service instance that matches the currently configured default selector
	 * @throws NoSuchElementException
	 *             when there is no matching service available
	 */
	public final S getFirst(Predicate<S> selector) throws NoSuchElementException {
		return getAll(selector).next();
	}

	/**
	 * Returns an {@link Iterator} to scan over all the available service instances. This is
	 * identical to invoking {@link #getAll(Predicate)} with a {@code null} parameter.
	 *
	 * @return an {@link Iterator} to scan over all the available service instances.
	 */
	public final Iterator<S> getAll() {
		return getAll(null);
	}

	/**
	 * Returns an {@link Iterator} that scans over all the available service instances that match
	 * the given selector. If the selector is {@code null}, then all available instances will match.
	 * Otherwise, only those instances {@code I} for which {@code selector.matches(I)} returns
	 * {@code true} will be iterated over, skipping over all non-matching instances. Exceptions
	 * raised during detection will be ignored.
	 *
	 * @param selector
	 * @return an {@link Iterator} that scans over all the available service instances that match
	 *         the given selector
	 */
	public final Iterator<S> getAll(final Predicate<S> selector) {
		return new Iterator<S>() {
			private final Predicate<S> finalSelector = (selector == null ? PluggableServiceLocator.this.defaultSelector
				: selector);
			private final Class<S> serviceClass = PluggableServiceLocator.this.serviceClass;
			private final Iterator<S> it = PluggableServiceLocator.this.loader.iterator();
			private final BiConsumer<Class<?>, Throwable> listener = PluggableServiceLocator.this.listener;
			private final boolean hideErrors = PluggableServiceLocator.this.hideErrors;

			private S current = null;

			private void handleThrown(Throwable t) {
				if (this.hideErrors) { return; }
				if (this.listener == null) {
					if (Error.class.isInstance(t)) { throw Error.class.cast(t); }
					throw new ServiceConfigurationError(t.getMessage(), t);
				}
				try {
					this.listener.accept(this.serviceClass, t);
				} catch (Throwable t2) {
					// Do nothing...
				}
			}

			private S findNext() {
				if (this.current == null) {
					while (this.it.hasNext()) {
						final S next;
						try {
							next = this.it.next();
						} catch (Throwable t) {
							handleThrown(t);
							continue;
						}
						if ((this.finalSelector == null) || this.finalSelector.test(next)) {
							this.current = next;
							break;
						}
					}
				}
				return this.current;
			}

			@Override
			public boolean hasNext() {
				return (findNext() != null);
			}

			@Override
			public S next() {
				S ret = findNext();
				this.current = null;
				if (ret == null) { throw new NoSuchElementException(); }
				return ret;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Overrides {@link Iterable#iterator()}, but is equivalent to invoking {@link #getAll()}.
	 */
	@Override
	public Iterator<S> iterator() {
		return getAll();
	}
}
