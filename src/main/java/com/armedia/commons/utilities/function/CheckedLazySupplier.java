/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2020 Armedia, LLC
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
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.ConcurrentInitializer;
import org.apache.commons.lang3.tuple.Pair;

import com.armedia.commons.utilities.Tools;
import com.armedia.commons.utilities.concurrent.BaseShareableLockable;

public class CheckedLazySupplier<T, EX extends Throwable> extends BaseShareableLockable
	implements Supplier<T>, CheckedSupplier<T, EX> {

	private final Condition condition;
	private final CheckedSupplier<T, EX> defaultInitializer;
	private final ConcurrentInitializer<T> concurrentInitializer;
	private final T defaultValue;

	private volatile boolean initialized = false;
	private volatile T item = null;

	public CheckedLazySupplier() {
		this(null, null);
	}

	public CheckedLazySupplier(CheckedSupplier<T, EX> defaultInitializer) {
		this(defaultInitializer, null);
	}

	public CheckedLazySupplier(T defaultValue) {
		this(null, defaultValue);
	}

	public CheckedLazySupplier(CheckedSupplier<T, EX> defaultInitializer, T defaultValue) {
		this.defaultInitializer = defaultInitializer;
		this.defaultValue = defaultValue;
		this.concurrentInitializer = () -> {
			try {
				return getChecked();
			} catch (Throwable t) {
				throw new ConcurrentException(t.getMessage(), t);
			}
		};
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
			return getChecked(this.defaultInitializer);
		} catch (Throwable t) {
			throw new RuntimeException("Lazy initialization failed", t);
		}
	}

	public T get(Supplier<T> initializer) {
		try {
			return getChecked((initializer != null ? initializer::get : null));
		} catch (Throwable t) {
			throw new RuntimeException("Lazy initialization failed", t);
		}
	}

	@Override
	public T getChecked() throws EX {
		return getChecked(this.defaultInitializer);
	}

	public ConcurrentInitializer<T> asInitializer() {
		return this.concurrentInitializer;
	}

	public T getChecked(CheckedSupplier<T, EX> initializer) throws EX {
		shareLockedUpgradable(() -> !this.initialized, () -> {
			CheckedSupplier<T, EX> init = Tools.coalesce(initializer, this.defaultInitializer);
			if (init != null) {
				this.item = init.getChecked();
			} else {
				this.item = this.defaultValue;
			}
			this.initialized = true;
			this.condition.signal();
		});
		return this.item;
	}

	public void reset() {
		shareLockedUpgradable(() -> this.initialized, () -> {
			this.item = null;
			this.initialized = false;
			this.condition.signal();
		});
	}

	public boolean applyIfSet(Consumer<T> consumer) {
		Objects.requireNonNull(consumer, "Must provide a Consumer instance");
		return shareLocked(() -> {
			boolean initialized = this.initialized;
			if (initialized) {
				consumer.accept(this.item);
			}
			return initialized;
		});
	}

	public static <T, EX extends Throwable> CheckedLazySupplier<T, EX> from(CheckedSupplier<T, EX> defaultInitializer) {
		return CheckedLazySupplier.from(defaultInitializer, null);
	}

	public static <T, EX extends Throwable> CheckedLazySupplier<T, EX> from(CheckedSupplier<T, EX> defaultInitializer,
		T defaultValue) {
		return new CheckedLazySupplier<>(defaultInitializer, defaultValue);
	}

	public static <T> CheckedLazySupplier<T, ConcurrentException> fromInitializer(
		ConcurrentInitializer<T> defaultInitializer) {
		return CheckedLazySupplier.fromInitializer(defaultInitializer, null);
	}

	public static <T> CheckedLazySupplier<T, ConcurrentException> fromInitializer(
		ConcurrentInitializer<T> defaultInitializer, T defaultValue) {
		return new CheckedLazySupplier<>(defaultInitializer != null ? () -> defaultInitializer.get() : null,
			defaultValue);
	}
}