/**
 *
 */

package com.armedia.commons.utilities;

/**
 * @author diego
 * 
 */
public class ValueSynchronizer<V extends Object> {
	private V v = null;

	public ValueSynchronizer() {
		this(null);
	}

	public ValueSynchronizer(V v) {
		this.v = v;
	}

	public synchronized V set(V v) {
		V old = this.v;
		this.v = v;
		// Only wake up a single thread waiting
		notify();
		return old;
	}

	public synchronized V get() {
		return this.v;
	}

	public synchronized V wait(boolean equals, V v) throws InterruptedException {
		boolean waited = false;
		while (equals == Tools.equals(this.v, v)) {
			waited = true;
			wait();
		}
		// Wake up the next thread waiting
		if (waited) {
			notify();
		}
		return v;
	}

	public synchronized V waitFor(V v) throws InterruptedException {
		return wait(false, v);
	}

	public synchronized V waitWhile(V v) throws InterruptedException {
		return wait(true, v);
	}
}