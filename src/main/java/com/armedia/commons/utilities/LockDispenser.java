/**
 * *******************************************************************
 * 
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS.
 * REPRODUCTION OF ANY PORTION OF THE SOURCE CODE, CONTAINED HEREIN,
 * OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE,
 * IS STRICTLY PROHIBITED.
 * 
 * Confidential Property of Armedia LLC.
 * (c) Copyright Armedia LLC 2011-2012.
 * All Rights reserved.
 * 
 * *******************************************************************
 */
package com.armedia.commons.utilities;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

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

	private final Map<K, Reference<C>> locks = new HashMap<K, Reference<C>>();

	public final C getLock(K key) {
		synchronized (this.locks) {
			Reference<C> ret = this.locks.get(key);
			C c = (ret != null ? ret.get() : null);
			if (c == null) {
				c = newLock(key);
				if (c == null) {
					this.locks.remove(key);
					return null;
				}
				ret = newReference(c);
				this.locks.put(key, ret);
			}
			return ret.get();
		}
	}

	protected Reference<C> newReference(C c) {
		return new WeakReference<C>(c);
	}

	protected abstract C newLock(K key);
}