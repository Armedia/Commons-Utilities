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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.ConcurrentInitializer;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;

/**
 * @author drivera@armedia.com
 *
 */
public abstract class LockDispenser<K extends Object, C extends Object> {

	public static <K> LockDispenser<K, Object> getBasic() {
		return new LockDispenser<K, Object>() {
			@Override
			protected Object newLock(K key) {
				return new Object();
			}
		};
	}

	private class LockBox {
		private final K key;

		private Reference<C> lock = null;

		private LockBox(K key) {
			this.key = key;
		}

		private synchronized C get() {
			if ((this.lock == null) || (this.lock.get() == null)) {
				this.lock = newReference(newLock(this.key));
			}
			return this.lock.get();
		}
	}

	private final ConcurrentMap<K, LockBox> locks = new ConcurrentHashMap<K, LockBox>();

	public final C getLock(final K key) {

		LockBox box = ConcurrentUtils.createIfAbsentUnchecked(this.locks, key, new ConcurrentInitializer<LockBox>() {
			@Override
			public LockBox get() throws ConcurrentException {
				return new LockBox(key);
			}
		});

		return box.get();
	}

	protected Reference<C> newReference(C c) {
		return new WeakReference<C>(c);
	}

	protected abstract C newLock(K key);
}