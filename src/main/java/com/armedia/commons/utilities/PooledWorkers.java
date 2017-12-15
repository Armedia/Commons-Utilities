package com.armedia.commons.utilities;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
	private Q exitValue = null;
	private int threadCount = 0;
	private ThreadPoolExecutor executor = null;

	private final class Worker implements Runnable {
		private final Logger log = PooledWorkers.this.log;

		private final boolean blocking;
		private final Q exitValue;

		private Worker(Q exitValue) {
			this(true, exitValue);
		}

		private Worker(boolean blocking, Q exitValue) {
			this.exitValue = exitValue;
			this.blocking = blocking;
		}

		private boolean isExitValue(Object o) {
			return ((this.exitValue != null) && (o == this.exitValue));
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
					if (this.blocking) {
						try {
							next = PooledWorkers.this.workQueue.take();
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							return;
						}
					} else {
						next = PooledWorkers.this.workQueue.poll();
						if (next == null) {
							this.log.debug("Exiting the work polling loop");
							return;
						}
					}

					// We compare instances, and not values, because we're interested
					// in seeing if the EXACT exit value flag is used, not one that
					// looks the same out of some unfortunate coincidence. By checking
					// instances, we ensure that we will not exit the loop prematurely
					// due to a value collision.
					if (isExitValue(next)) {
						// Work complete
						this.log.debug("Exiting the work polling loop");
						return;
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

	public final boolean start(int threadCount, Q exitValue) {
		return start(threadCount, exitValue, true);
	}

	public final boolean start(int threadCount) {
		return start(threadCount, null, false);
	}

	public final boolean start(int threadCount, Q exitValue, boolean blocking) {
		final Lock l = this.lock.writeLock();
		l.lock();
		try {
			if (this.executor != null) { return false; }
			if (blocking
				&& (exitValue == null)) { throw new IllegalArgumentException("Blocking mode requires an exit value"); }
			this.threadCount = threadCount;
			this.exitValue = exitValue;
			this.activeCounter.set(0);
			this.futures.clear();
			Worker worker = new Worker(exitValue);
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
			boolean waitCleanly = true;
			if (this.exitValue != null) {
				for (int i = 0; i < this.threadCount; i++) {
					try {
						this.workQueue.put(this.exitValue);
					} catch (InterruptedException e) {
						waitCleanly = false;
						// Here we have a problem: we're timing out while adding the exit
						// values...
						this.log.warn("Interrupted while attempting to request executor thread termination", e);
						Thread.currentThread().interrupt();
						this.executor.shutdownNow();
						break;
					}
				}
			}

			List<Q> remaining = new ArrayList<Q>();
			try {
				// We're done, we must wait until all workers are waiting
				if (waitCleanly) {
					this.log.debug(String.format("Waiting for %d workers to finish processing", this.threadCount));
					for (Future<?> future : this.futures) {
						try {
							future.get();
						} catch (InterruptedException e) {
							this.log.warn(
								"Interrupted while waiting for an executor thread to exit, forcing the shutdown", e);
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
				}
			} finally {
				this.workQueue.drainTo(remaining);
				for (Q v : remaining) {
					if ((this.exitValue != null) && (v == this.exitValue)) {
						continue;
					}
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
					this.exitValue = null;
				}
			}
		} finally {
			l.unlock();
		}
	}
}