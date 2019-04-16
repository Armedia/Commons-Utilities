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
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;

/**
 * @author drivera@armedia.com
 *
 */
public final class LockDispenser<K, C> {

	/**
	 * <p>
	 * Deprecated in favor of {@link #synchronizableObject()}
	 * </p>
	 *
	 * @return the same as {@link #synchronizableObject()}
	 */
	@Deprecated
	public static <K> LockDispenser<K, Object> getBasic() {
		return LockDispenser.basic();
	}

	public static <K> LockDispenser<K, Object> basic() {
		return LockDispenser.synchronizableObject();
	}

	public static <K> LockDispenser<K, Object> synchronizableObject() {
		return new LockDispenser<>((k) -> new Object());
	}

	public static <K> LockDispenser<K, ReentrantLock> reentrantLock() {
		return new LockDispenser<>((k) -> new ReentrantLock());
	}

	public static <K> LockDispenser<K, ReentrantReadWriteLock> reentrantReadWriteLock() {
		return new LockDispenser<>((k) -> new ReentrantReadWriteLock());
	}

	private class LockBox {
		private final K key;

		private Reference<C> lock = null;

		private LockBox(K key) {
			this.key = key;
		}

		private synchronized C get() {
			if ((this.lock == null) || (this.lock.get() == null)) {
				C lock = LockDispenser.this.lockBuilder.apply(this.key);
				if (lock == null) {
					throw new RuntimeException("The LockBuilder must always return a non-null lock object instance");
				}
				this.lock = LockDispenser.this.referenceBuilder.apply(lock);
				if (this.lock == null) {
					throw new RuntimeException(
						"The ReferenceBuilder must always return a non-null Reference<> instance");
				}
			}
			return this.lock.get();
		}
	}

	private final Function<K, C> lockBuilder;
	private final Function<C, Reference<C>> referenceBuilder;
	private final ConcurrentMap<K, LockBox> locks = new ConcurrentHashMap<>();

	public LockDispenser(Function<K, C> lockBuilder) {
		this(lockBuilder, WeakReference::new);
	}

	public LockDispenser(Function<K, C> lockBuilder, Function<C, Reference<C>> referenceBuilder) {
		this.lockBuilder = Objects.requireNonNull(lockBuilder, "Must provide a non-null LockBuilder instance");
		this.referenceBuilder = Objects.requireNonNull(referenceBuilder,
			"Must provide a non-null ReferenceBuilder instance");
	}

	public C getLock(final K key) {
		return ConcurrentUtils.createIfAbsentUnchecked(this.locks, key, () -> new LockBox(key)).get();
	}
}