/**
 * *******************************************************************
 *
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS. REPRODUCTION OF ANY PORTION
 * OF THE SOURCE CODE, CONTAINED HEREIN, OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE, IS
 * STRICTLY PROHIBITED.
 *
 * Confidential Property of Armedia LLC. (c) Copyright Armedia LLC 2011-2012. All Rights reserved.
 *
 * *******************************************************************
 */
package com.armedia.commons.utilities;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.ConcurrentInitializer;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;

/**
 * @author drivera@armedia.com
 *
 */
public final class LockDispenser<K, C> {

	@FunctionalInterface
	public static interface LockBuilder<K, C> {
		public C newLock(K key);
	}

	@FunctionalInterface
	public static interface ReferenceBuilder<C> {
		public Reference<C> newReference(C key);
	}

	public static <C> ReferenceBuilder<C> getWeakReferenceBuilder() {
		return (v) -> {
			return new WeakReference<>(v);
		};
	}

	public static <C> ReferenceBuilder<C> getSoftReferenceBuilder() {
		return (v) -> {
			return new SoftReference<>(v);
		};
	}

	/**
	 * <p>
	 * Deprecated in favor of {@link #getSynchronized()}
	 * </p>
	 *
	 * @return the same as {@link #getSynchronized()}
	 */
	@Deprecated
	public static <K> LockDispenser<K, Object> getBasic() {
		return LockDispenser.getSynchronized();
	}

	public static <K> LockDispenser<K, Object> getSynchronized() {
		return new LockDispenser<>((k) -> {
			return new Object();
		});
	}

	public static <K> LockDispenser<K, ReentrantLock> getLock() {
		return new LockDispenser<>((k) -> {
			return new ReentrantLock();
		});
	}

	public static <K> LockDispenser<K, ReentrantReadWriteLock> getReadWriteLock() {
		return new LockDispenser<>((k) -> {
			return new ReentrantReadWriteLock();
		});
	}

	private class LockBox {
		private final K key;

		private Reference<C> lock = null;

		private LockBox(K key) {
			this.key = key;
		}

		private synchronized C get() {
			if ((this.lock == null) || (this.lock.get() == null)) {
				C lock = LockDispenser.this.lockBuilder.newLock(this.key);
				if (lock == null) { throw new RuntimeException(
					"The LockBuilder must always return a non-null lock object instance"); }
				this.lock = LockDispenser.this.referenceBuilder.newReference(lock);
				if (this.lock == null) { throw new RuntimeException(
					"The ReferenceBuilder must always return a non-null Reference<> instance"); }
			}
			return this.lock.get();
		}
	}

	private final LockBuilder<K, C> lockBuilder;
	private final ReferenceBuilder<C> referenceBuilder;
	private final ConcurrentMap<K, LockBox> locks = new ConcurrentHashMap<>();

	public LockDispenser(LockBuilder<K, C> lockBuilder) {
		this(lockBuilder, LockDispenser.getWeakReferenceBuilder());
	}

	public LockDispenser(LockBuilder<K, C> lockBuilder, ReferenceBuilder<C> referenceBuilder) {
		this.lockBuilder = Objects.requireNonNull(lockBuilder, "Must provide a non-null LockBuilder instance");
		this.referenceBuilder = Objects.requireNonNull(referenceBuilder,
			"Must provide a non-null ReferenceBuilder instance");
	}

	public C getLock(final K key) {
		return ConcurrentUtils.createIfAbsentUnchecked(this.locks, key, new ConcurrentInitializer<LockBox>() {
			@Override
			public LockBox get() throws ConcurrentException {
				return new LockBox(key);
			}
		}).get();
	}
}