package com.armedia.commons.utilities;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PooledWorkers<S, Q> {
	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final BlockingQueue<Q> workQueue;
	private final List<Future<?>> futures;
	private final AtomicInteger activeCounter;

	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final AtomicBoolean terminated = new AtomicBoolean(false);
	private int threadCount = 0;
	private ThreadPoolExecutor executor = null;
	private final Map<Long, Thread> blockedThreads = new ConcurrentHashMap<Long, Thread>();

	private final class Worker implements Runnable {
		private final Logger log = PooledWorkers.this.log;

		private final boolean waitForWork;

		private Worker(boolean waitForWork) {
			this.waitForWork = waitForWork;
		}

		@Override
		public void run() {
			final S state;
			try {
				state = prepare();
			} catch (Exception e) {
				this.log.error("Failed to prepare the worker for this thread", e);
				return;
			}
			PooledWorkers.this.activeCounter.incrementAndGet();
			try {
				while (!Thread.interrupted()) {
					if (this.log.isDebugEnabled()) {
						this.log.trace("Polling the queue...");
					}
					Q next = null;
					if (this.waitForWork && !PooledWorkers.this.terminated.get()) {
						final Thread thread = Thread.currentThread();
						try {
							PooledWorkers.this.blockedThreads.put(thread.getId(), thread);
							next = PooledWorkers.this.workQueue.take();
						} catch (InterruptedException e) {
							thread.interrupt();
							this.log.debug("Thread interrupted - worker exiting the work polling loop");
							return;
						} finally {
							PooledWorkers.this.blockedThreads.remove(thread.getId());
						}
					} else {
						next = PooledWorkers.this.workQueue.poll();
						if (next == null) {
							this.log.debug("Queue empty - worker exiting the work polling loop");
							return;
						}
					}

					if (this.log.isDebugEnabled()) {
						this.log.trace(String.format("Polled %s", next));
					}

					try {
						process(state, next);
					} catch (Exception e) {
						this.log.error(String.format("Failed to process item %s", next), e);
					}
				}
			} finally {
				PooledWorkers.this.activeCounter.decrementAndGet();
				cleanup(state);
			}
		}
	}

	public PooledWorkers() {
		this(0);
	}

	public PooledWorkers(int backlogSize) {
		this.workQueue = (backlogSize <= 0 ? new LinkedBlockingQueue<Q>() : new ArrayBlockingQueue<Q>(backlogSize));
		this.futures = new LinkedList<Future<?>>();
		this.activeCounter = new AtomicInteger(0);
	}

	protected abstract S prepare() throws Exception;

	protected abstract void process(S state, Q item) throws Exception;

	protected abstract void cleanup(S state);

	public final void addWorkItem(Q item) throws InterruptedException {
		final Lock l = this.lock.readLock();
		l.lock();
		try {
			this.workQueue.put(item);
		} finally {
			l.unlock();
		}
	}

	public final boolean addWorkItemNonblock(Q item) {
		final Lock l = this.lock.readLock();
		l.lock();
		try {
			return this.workQueue.offer(item);
		} finally {
			l.unlock();
		}
	}

	public final synchronized List<Q> clearWorkItems() {
		final Lock l = this.lock.readLock();
		l.lock();
		try {
			List<Q> ret = new ArrayList<Q>();
			this.workQueue.drainTo(ret);
			return ret;
		} finally {
			l.unlock();
		}
	}

	public final int getQueueSize() {
		final Lock l = this.lock.readLock();
		l.lock();
		try {
			return this.workQueue.size();
		} finally {
			l.unlock();
		}
	}

	public final int getQueueCapacity() {
		final Lock l = this.lock.readLock();
		l.lock();
		try {
			return this.workQueue.remainingCapacity();
		} finally {
			l.unlock();
		}
	}

	public final boolean start(int threadCount) {
		return start(threadCount, true);
	}

	public final boolean start(int threadCount, boolean waitForWork) {
		final Lock l = this.lock.writeLock();
		l.lock();
		try {
			if (this.executor != null) { return false; }
			this.threadCount = threadCount;
			this.activeCounter.set(0);
			this.futures.clear();
			this.terminated.set(false);
			this.blockedThreads.clear();
			Worker worker = new Worker(waitForWork);
			this.executor = new ThreadPoolExecutor(threadCount, threadCount, 30, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());
			for (int i = 0; i < this.threadCount; i++) {
				this.futures.add(this.executor.submit(worker));
			}
			this.executor.shutdown();
			return true;
		} finally {
			l.unlock();
		}
	}

	private final List<Q> waitCleanly() {
		final Lock l = this.lock.writeLock();
		l.lock();
		try {
			this.log.debug("Signaling work completion for the workers");
			this.terminated.set(true);

			List<Q> remaining = new ArrayList<Q>();
			try {
				// We're done, we must wait until all workers are waiting
				this.log.debug(String.format("Waiting for %d workers to finish processing", this.threadCount));
				// First, wake any blocked threads
				for (Thread t : this.blockedThreads.values()) {
					t.interrupt();
				}
				this.blockedThreads.clear();
				for (Future<?> future : this.futures) {
					try {
						future.get();
					} catch (InterruptedException e) {
						this.log.warn("Interrupted while waiting for an executor thread to exit, forcing the shutdown",
							e);
						Thread.currentThread().interrupt();
						this.executor.shutdownNow();
						break;
					} catch (ExecutionException e) {
						this.log.warn("An executor thread raised an exception", e);
					} catch (CancellationException e) {
						this.log.warn("An executor thread was canceled!", e);
					}
				}
				this.log.debug("All the workers are done.");
			} finally {
				this.workQueue.drainTo(remaining);
				for (Q v : remaining) {
					this.log.error(String.format("WORK LEFT PENDING IN THE QUEUE: %s", v));
				}
			}

			// If there are still pending workers, then wait for them to finish for up to 5
			// minutes
			int pending = this.activeCounter.get();
			if (pending > 0) {
				this.log.debug(String.format(
					"Waiting for pending workers to terminate (maximum 5 minutes, %d pending workers)", pending));
				try {
					this.executor.awaitTermination(5, TimeUnit.MINUTES);
				} catch (InterruptedException e) {
					this.log.warn("Interrupted while waiting for normal executor termination", e);
					Thread.currentThread().interrupt();
				}
			}
			return remaining;
		} finally {
			l.unlock();
		}
	}

	public final void waitForCompletion() {
		final Lock l = this.lock.writeLock();
		l.lock();
		try {
			if (this.executor == null) { return; }
			try {
				waitCleanly();
			} finally {
				try {
					this.executor.shutdownNow();
					int pending = this.activeCounter.get();
					if (pending > 0) {
						try {
							this.log.debug(String.format(
								"Waiting an additional 60 seconds for worker termination as a contingency (%d pending workers)",
								pending));
							this.executor.awaitTermination(1, TimeUnit.MINUTES);
						} catch (InterruptedException e) {
							this.log.warn("Interrupted while waiting for immediate executor termination", e);
							Thread.currentThread().interrupt();
						}
					}
				} finally {
					this.executor = null;
					this.threadCount = 0;
				}
			}
		} finally {
			l.unlock();
		}
	}
}