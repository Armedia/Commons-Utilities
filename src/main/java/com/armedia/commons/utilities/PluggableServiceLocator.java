package com.armedia.commons.utilities;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

/**
 * <p>
 * This class serves as a wrapper for the boilerplate code that would usually accompany
 * {@link ServiceLoader} implementations. In particular, it allows for customized selection of the
 * actual service instance being returned in a modular fashion.
 * </p>
 *
 * @author drivera@armedia.com
 *
 */
public class PluggableServiceLocator<S> {

	private final ClassLoader classLoader;
	private final Class<S> serviceClass;
	private final ServiceLoader<S> loader;

	private PluggableServiceSelector<S> defaultSelector = null;

	public PluggableServiceLocator(Class<S> serviceClass) {
		this(serviceClass, Thread.currentThread().getContextClassLoader(), null);
	}

	public PluggableServiceLocator(Class<S> serviceClass, PluggableServiceSelector<S> defaultSelector) {
		this(serviceClass, Thread.currentThread().getContextClassLoader(), defaultSelector);
	}

	public PluggableServiceLocator(Class<S> serviceClass, ClassLoader classLoader) {
		this(serviceClass, classLoader, null);
	}

	public PluggableServiceLocator(Class<S> serviceClass, ClassLoader classLoader,
		PluggableServiceSelector<S> defaultSelector) {
		if (serviceClass == null) { throw new IllegalArgumentException(
			"Must provide a service class for which to locate instances"); }
		if (classLoader == null) { throw new IllegalArgumentException(
			"Must provide a classloader in which to locate instances"); }
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
	public final PluggableServiceSelector<S> getDefaultSelector() {
		return this.defaultSelector;
	}

	/**
	 * Set the new default selector.
	 *
	 * @param defaultSelector
	 */
	public final void setDefaultSelector(PluggableServiceSelector<S> defaultSelector) {
		this.defaultSelector = defaultSelector;
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
		return getAll(null).next();
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
	public final S getFirst(PluggableServiceSelector<S> selector) throws NoSuchElementException {
		return getAll(selector).next();
	}

	/**
	 * Returns an {@link Iterator} to scan over all the available service instances. This is
	 * identical to invoking {@link #getAll(PluggableServiceSelector)} with a {@code null}
	 * parameter.
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
	 * {@code true} will be iterated over, skipping over all non-matching instances.
	 *
	 * @param selector
	 * @return an {@link Iterator} that scans over all the available service instances that match
	 *         the given selector
	 */
	public final Iterator<S> getAll(PluggableServiceSelector<S> selector) {
		final PluggableServiceSelector<S> finalSelector = (selector == null ? this.defaultSelector : selector);
		return new Iterator<S>() {
			private final Iterator<S> it = PluggableServiceLocator.this.loader.iterator();

			private S current = null;

			private S findNext() {
				if (this.current == null) {
					while (this.it.hasNext()) {
						S next = this.it.next();
						if ((finalSelector == null) || finalSelector.matches(next)) {
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
}