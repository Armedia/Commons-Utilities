package com.armedia.commons.utilities.concurrent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.tuple.Pair;

public class TrackableReadWriteLock implements ReadWriteLock {

	private static final Class<?>[] LOCK_INTERFACES = {
		Lock.class
	};

	public static class LockCall {
		private final String label;
		private final Lock target;
		private final Object result;
		private final Throwable thrown;
		private final Method method;
		private final Object[] args;

		private LockCall(String label, Lock target, Method method, Object[] args, Object result) {
			this(label, target, method, args, result, null);
		}

		private LockCall(String label, Lock target, Method method, Object[] args, Throwable thrown) {
			this(label, target, method, args, null, thrown);
		}

		private LockCall(String label, Lock target, Method method, Object[] args, Object result, Throwable thrown) {
			this.label = label;
			this.target = target;
			this.result = result;
			this.thrown = thrown;
			this.method = method;
			this.args = (args != null ? args.clone() : args);
		}

		public String getLabel() {
			return this.label;
		}

		public Lock getTarget() {
			return this.target;
		}

		public Object getResult() {
			return this.result;
		}

		public Throwable getThrown() {
			return this.thrown;
		}

		public Method getMethod() {
			return this.method;
		}

		public Object[] getArgs() {
			return this.args;
		}

		@Override
		public String toString() {
			if (this.thrown != null) {
				return String.format("%s::%s(%s) -> %s: %s", this.label, this.method.getName(),
					Arrays.toString(this.args), this.thrown.getClass().getCanonicalName(), this.thrown.getMessage());
			}
			return String.format("%s::%s(%s) == [%s]", this.label, this.method.getName(), Arrays.toString(this.args),
				this.result);
		}
	}

	private class Handler implements InvocationHandler {
		private final String label;
		private final Lock target;

		private Handler(String label, Lock target) {
			this.label = label;
			this.target = target;
		}

		@Override
		public Object invoke(Object p, Method m, Object[] a) throws Throwable {
			final long now = System.nanoTime();
			try {
				Object r = m.invoke(this.target, a);
				TrackableReadWriteLock.this.lockCalls.add(Pair.of(now, new LockCall(this.label, this.target, m, a, r)));
				return r;
			} catch (final Throwable t) {
				TrackableReadWriteLock.this.lockCalls.add(Pair.of(now, new LockCall(this.label, this.target, m, a, t)));
				throw t;
			}
		}
	}

	private final ReentrantReadWriteLock lock;
	private final Lock readLockProxy;
	private final Lock writeLockProxy;

	private final List<Pair<Long, LockCall>> lockCalls = new LinkedList<>();

	public TrackableReadWriteLock() {
		this(new ReentrantReadWriteLock());
	}

	public TrackableReadWriteLock(ReentrantReadWriteLock lock) {
		this.lock = Objects.requireNonNull(lock);
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		this.readLockProxy = Lock.class.cast(Proxy.newProxyInstance(cl, TrackableReadWriteLock.LOCK_INTERFACES,
			new Handler("readLock", lock.readLock())));
		this.writeLockProxy = Lock.class.cast(Proxy.newProxyInstance(cl, TrackableReadWriteLock.LOCK_INTERFACES,
			new Handler("writeLock", lock.writeLock())));
	}

	public List<Pair<Long, LockCall>> getLockCalls() {
		return this.lockCalls;
	}

	public final boolean isFair() {
		return this.lock.isFair();
	}

	public int getReadLockCount() {
		return this.lock.getReadLockCount();
	}

	public boolean isWriteLocked() {
		return this.lock.isWriteLocked();
	}

	public boolean isWriteLockedByCurrentThread() {
		return this.lock.isWriteLockedByCurrentThread();
	}

	public int getWriteHoldCount() {
		return this.lock.getWriteHoldCount();
	}

	public int getReadHoldCount() {
		return this.lock.getReadHoldCount();
	}

	public final boolean hasQueuedThreads() {
		return this.lock.hasQueuedThreads();
	}

	public final boolean hasQueuedThread(Thread thread) {
		return this.lock.hasQueuedThread(thread);
	}

	public final int getQueueLength() {
		return this.lock.getQueueLength();
	}

	public final boolean hasWaiters(Condition condition) {
		return this.lock.hasWaiters(condition);
	}

	public final int getWaitQueueLength(Condition condition) {
		return this.lock.getWaitQueueLength(condition);
	}

	@Override
	public final String toString() {
		return this.lock.toString();
	}

	@Override
	public final Lock readLock() {
		return this.readLockProxy;
	}

	@Override
	public final Lock writeLock() {
		return this.writeLockProxy;
	}

}
