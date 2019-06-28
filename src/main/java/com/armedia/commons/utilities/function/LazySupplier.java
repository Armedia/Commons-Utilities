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
package com.armedia.commons.utilities.function;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.function.Supplier;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.ConcurrentInitializer;
import org.apache.commons.lang3.tuple.Pair;

import com.armedia.commons.utilities.Tools;
import com.armedia.commons.utilities.concurrent.BaseShareableLockable;

public class LazySupplier<T> extends BaseShareableLockable implements Supplier<T> {

	private static class ConcurrentInitializerException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		private ConcurrentInitializerException(Throwable cause) {
			super(cause);
		}
	}

	private final Condition condition;
	private final Supplier<T> defaultInitializer;
	private final ConcurrentInitializer<T> concurrentInitializer;
	private final T defaultValue;

	private volatile boolean initialized = false;
	private volatile T item = null;

	public LazySupplier() {
		this(null, null);
	}

	public LazySupplier(Supplier<T> defaultInitializer) {
		this(defaultInitializer, null);
	}

	public LazySupplier(T defaultValue) {
		this(null, defaultValue);
	}

	public LazySupplier(Supplier<T> defaultInitializer, T defaultValue) {
		this.defaultInitializer = defaultInitializer;
		this.defaultValue = defaultValue;
		this.concurrentInitializer = this::get;
		this.condition = getMutexLock().newCondition();
	}

	public boolean isDefaulted() {
		return shareLocked(() -> isInitialized() && (this.defaultValue == this.item));
	}

	public boolean isInitialized() {
		return shareLocked(() -> this.initialized);
	}

	public T await() throws InterruptedException {
		shareLockedUpgradable(() -> !this.initialized, () -> {
			this.condition.await();
			this.condition.signal();
		});
		return this.item;
	}

	public T awaitUninterruptibly() {
		shareLockedUpgradable(() -> !this.initialized, () -> {
			this.condition.awaitUninterruptibly();
			this.condition.signal();
		});
		return this.item;
	}

	public Pair<T, Long> awaitNanos(long nanosTimeout) throws InterruptedException {
		final AtomicReference<Long> ret = new AtomicReference<>(null);
		shareLockedUpgradable(() -> !this.initialized, () -> {
			ret.set(this.condition.awaitNanos(nanosTimeout));
			if (this.initialized) {
				this.condition.signal();
				ret.set(null);
			}
		});
		return Pair.of(this.item, ret.get());
	}

	public Pair<T, Boolean> await(long time, TimeUnit unit) throws InterruptedException {
		unit = Tools.coalesce(unit, TimeUnit.MILLISECONDS);
		Pair<T, Long> p = awaitNanos(unit.toNanos(time));
		if (p.getRight() != null) {
			return Pair.of(null, Boolean.TRUE);
		} else {
			return Pair.of(p.getLeft(), Boolean.FALSE);
		}
	}

	public Pair<T, Boolean> awaitUntil(Date deadline) throws InterruptedException {
		final AtomicBoolean ret = new AtomicBoolean(true);
		shareLockedUpgradable(() -> !this.initialized, () -> {
			ret.set(this.condition.awaitUntil(deadline));
			if (ret.get()) {
				this.condition.signal();
			}
		});
		return Pair.of(this.item, !ret.get());
	}

	@Override
	public T get() {
		try {
			return get(this.defaultInitializer);
		} catch (ConcurrentInitializerException e) {
			throw new RuntimeException(e.getCause());
		} catch (Throwable t) {
			throw new RuntimeException(t.getMessage(), t);
		}
	}

	public ConcurrentInitializer<T> asInitializer() {
		return this.concurrentInitializer;
	}

	public T get(Supplier<T> init) {
		shareLockedUpgradable(() -> !this.initialized, () -> {
			Supplier<T> initializer = Tools.coalesce(init, this.defaultInitializer);
			if (initializer != null) {
				this.item = initializer.get();
			} else {
				this.item = this.defaultValue;
			}
			this.initialized = true;
			this.condition.signal();
		});
		return this.item;
	}

	public static <T> LazySupplier<T> fromSupplier(Supplier<T> defaultInitializer) {
		return LazySupplier.fromSupplier(defaultInitializer, null);
	}

	public static <T> LazySupplier<T> fromSupplier(Supplier<T> defaultInitializer, T defaultValue) {
		return new LazySupplier<>((defaultInitializer != null ? () -> defaultInitializer.get() : null), defaultValue);
	}

	public static <T> LazySupplier<T> fromInitializer(ConcurrentInitializer<T> defaultInitializer) {
		return LazySupplier.fromInitializer(defaultInitializer, null);
	}

	public static <T> LazySupplier<T> fromInitializer(ConcurrentInitializer<T> defaultInitializer, T defaultValue) {
		return new LazySupplier<>(defaultInitializer != null ? () -> {
			try {
				return defaultInitializer.get();
			} catch (ConcurrentException e) {
				throw new ConcurrentInitializerException(e);
			}
		} : null, defaultValue);
	}
}