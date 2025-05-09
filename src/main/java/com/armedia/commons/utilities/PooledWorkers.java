/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2025 Armedia, LLC
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
package com.armedia.commons.utilities;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.armedia.commons.utilities.concurrent.BaseShareableLockable;
import com.armedia.commons.utilities.concurrent.MutexAutoLock;
import com.armedia.commons.utilities.concurrent.SharedAutoLock;

/**
 * A simple multi-threaded worker pool that supports having an optionally size-constrained work
 * queue, and items submitted are processed in order, concurrently. It supports pre-filling the
 * queue for immediate consumption, as well a blocking mode where the worker threads wait for work
 * to be submitted and execute it as it arrives.
 *
 * @param <STATE>
 *            The state class that will be produced by
 *            {@link PooledWorkersLogic#initialize(PooledWorkers)} and consumed by
 *            {@link PooledWorkersLogic#process(Object, Object)} and
 *            {@link PooledWorkersLogic#cleanup(Object)}
 * @param <ITEM>
 *            The item class that will be processed by
 *            {@link PooledWorkersLogic#process(Object, Object)}
 *
 */
public final class PooledWorkers<STATE, ITEM> extends BaseShareableLockable {
	protected static final Duration DEFAULT_MAX_WAIT = Duration.ofMinutes(5);

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final BlockingQueue<ITEM> workQueue;
	private final List<Future<?>> futures;
	private final AtomicInteger activeCounter;
	private final AtomicBoolean aborted = new AtomicBoolean(false);
	private final AtomicBoolean terminated = new AtomicBoolean(false);
	private final int threadCount;
	private final ThreadPoolExecutor executor;
	private final Collection<Thread> threads = new ArrayList<>();
	private final Set<Long> blocked = new HashSet<>();

	private final CountDownLatch startupLatch;

	private final class Task<EX extends Exception> implements Runnable {
		private final Logger log = PooledWorkers.this.log;

		private final boolean waitForWork;
		private final PooledWorkersLogic<STATE, ITEM, EX> logic;

		private Task(PooledWorkersLogic<STATE, ITEM, EX> logic, boolean waitForWork) {
			this.waitForWork = waitForWork;
			this.logic = logic;
		}

		@SuppressWarnings("unchecked")
		private EX castException(Throwable raised) {
			return (EX) raised;
		}

		@Override
		public void run() {
			final STATE state;
			try {
				state = this.logic.initialize(PooledWorkers.this);
			} catch (Throwable t) {
				PooledWorkers.this.startupLatch.countDown();
				workerThreadExited("Failed to initialize the worker state", null, castException(t));
				return;
			}
			PooledWorkers.this.activeCounter.incrementAndGet();
			try {
				boolean first = true;
				while (!Thread.interrupted() && !PooledWorkers.this.aborted.get()) {
					if (this.log.isDebugEnabled()) {
						this.log.trace("Polling the queue...");
					}
					ITEM item = null;
					if (this.waitForWork && !PooledWorkers.this.terminated.get()) {
						final Thread thread = Thread.currentThread();
						try {
							PooledWorkers.this.blocked.add(thread.getId());
							if (first) {
								first = false;
								PooledWorkers.this.startupLatch.countDown();
							}
							item = PooledWorkers.this.workQueue.take();
						} catch (InterruptedException e) {
							thread.interrupt();
							workerThreadExited("Thread interrupted - worker exiting the work polling loop", state,
								null);
							return;
						} finally {
							PooledWorkers.this.blocked.remove(thread.getId());
						}
					} else {
						if (first) {
							first = false;
							PooledWorkers.this.startupLatch.countDown();
						}
						item = PooledWorkers.this.workQueue.poll();
						if (item == null) {
							workerThreadExited("Queue empty - worker exiting the work polling loop", state, null);
							return;
						}
					}

					if (this.log.isDebugEnabled()) {
						this.log.trace("Polled {}", item);
					}

					try {
						// Make sure the interruption status is cleared just before we invoke the
						// processing method
						Thread.interrupted();
						this.logic.process(state, item);
					} catch (Throwable t) {
						this.logic.handleFailure(state, item, castException(t));
					}
				}
			} catch (Exception e) {
				workerThreadExited("Unexpected exception raised", state, e);
			} finally {
				PooledWorkers.this.activeCounter.decrementAndGet();
				this.logic.cleanup(state);
			}
		}
	}

	/**
	 * Construct an instance that will accept a maximum of {@code backLog} items waiting for
	 * processing in the queue, such that new invocations to {@link #addWorkItem(Object)},
	 * {@link #addWorkItem(Object, Duration)} or {@link #addWorkItemNonblock(Object)} will either
	 * block or fail accordingly if the queue is full. If {@code backlogSize <= 0} then the queue
	 * will have "no" capacity limit (in reality, a limit of {@link Integer#MAX_VALUE} items).
	 *
	 * @param backlogSize
	 */
	protected <EX extends Exception> PooledWorkers(PooledWorkersLogic<STATE, ITEM, EX> logic, int threadCount,
		String name, int backlogSize, Collection<? extends ITEM> items, boolean waitForWork) {
		Objects.requireNonNull(logic,
			"Must provide a valid PooledWorkersLogic instance with which to perform the work");
		this.workQueue = (backlogSize <= 0 ? new LinkedBlockingQueue<>() : new ArrayBlockingQueue<>(backlogSize));
		if ((items != null) && !items.isEmpty()) {
			// TODO: What if the list of items exceeds the maximum backlog size?
			this.workQueue.addAll(items);
		}
		this.futures = new LinkedList<>();
		this.activeCounter = new AtomicInteger(0);

		this.threadCount = Math.max(1, threadCount);
		this.activeCounter.set(0);
		this.futures.clear();
		this.terminated.set(false);
		this.threads.clear();
		Task<EX> task = new Task<>(logic, waitForWork);
		ThreadFactory threadFactory = Executors.defaultThreadFactory();
		String finalName = StringUtils.strip(name);
		final String threadNameFormat = String.format("%s-%%0%dd", finalName, String.valueOf(threadCount).length());
		if (!StringUtils.isEmpty(finalName)) {
			final ThreadGroup group = new ThreadGroup(String.format("Threads for PooledWorkers task [%s]", finalName));
			threadFactory = new ThreadFactory() {
				private final AtomicLong counter = new AtomicLong(0);

				@Override
				public Thread newThread(Runnable r) {
					Thread t = new Thread(group, r, String.format(threadNameFormat, this.counter.incrementAndGet()));
					PooledWorkers.this.threads.add(t);
					return t;
				}
			};
		}
		this.executor = new ThreadPoolExecutor(this.threadCount, this.threadCount, 30, TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(), threadFactory);
		this.startupLatch = new CountDownLatch(threadCount);
		for (int i = 0; i < this.threadCount; i++) {
			this.futures.add(this.executor.submit(task));
		}
		this.executor.shutdown();
	}

	public boolean isProcessing() {
		try (SharedAutoLock lock = sharedAutoLock()) {
			return (!this.terminated.get() && !this.aborted.get());
		}
	}

	/**
	 * Callback to process the termination of a worker thread. Invoked before
	 * {@link PooledWorkersLogic#cleanup(Object)} is invoked upon the passed state. If {@code state}
	 * is {@code null}, it means that the initialization of the thread's state data failed and thus
	 * {@code thrown} will not be {@code null}. If {@code thrown} is not {@code null} it means an
	 * unexpected exception was raised during normal processing of the queue (i.e. an
	 * {@link OutOfMemoryError} or some other unforeseeable problem), or during initialization (see
	 * the previous case when {@code state} is {@code null}). The {@code message} is never
	 * {@code null}, and describes the exit situation.
	 *
	 * @param message
	 * @param state
	 * @param thrown
	 */
	protected void workerThreadExited(String message, STATE state, Throwable thrown) {
		if (state == null) {
			// Failed to initialize
			this.log.debug(message, thrown);
		} else if (thrown != null) {
			// Failed to complete processing
			this.log.error("Worker failed: {} (state = {})", message, state, thrown);
		} else {
			// Exited normally
			this.log.debug(message);
		}
	}

	/**
	 * Adds {@code item} to this instance's work queue, blocking if the queue capacity is exhausted
	 * and until {@code item} can be queued. The {@code item} parameter may not be {@code null}.
	 *
	 * @param item
	 * @throws InterruptedException
	 * @throws NullPointerException
	 *             if {@code item} is {@code null}
	 */
	public final void addWorkItem(ITEM item) throws InterruptedException {
		if (item == null) { throw new NullPointerException("Must provide a non-null work item"); }
		shareLocked(() -> this.workQueue.put(item));
	}

	/**
	 * Adds {@code item} to this instance's work queue, blocking if the queue capacity is exhausted
	 * and until {@code item} can be queued, but blocking at most for the specified time amount. The
	 * {@code item} parameter may not be {@code null}.
	 *
	 * @param item
	 * @return {@code true} if the item was added to the queue, or {@code false} otherwise.
	 * @throws InterruptedException
	 * @throws NullPointerException
	 *             if {@code item} is {@code null}
	 */
	public final boolean addWorkItem(ITEM item, Duration maxWait) throws InterruptedException {
		if (item == null) { throw new NullPointerException("Must provide a non-null work item"); }
		long millis = 0;
		if ((maxWait != null) && !maxWait.isZero() && !maxWait.isNegative()) {
			millis = maxWait.toMillis();
		}
		final long finalMillis = millis;
		return shareLocked(() -> this.workQueue.offer(item, finalMillis, TimeUnit.MILLISECONDS));
	}

	/**
	 * Adds an {@code item} to this instance's work queue without ever blocking. It returns
	 * {@code true} if {@code item} was added to the queue, or {@code false} otherwise. The
	 * {@code item} parameter may not be {@code null}.
	 *
	 * @param item
	 * @return {@code true} if the item was added to the queue, or {@code false} otherwise.
	 * @throws NullPointerException
	 *             if {@code item} is {@code null}
	 */
	public final boolean addWorkItemNonblock(ITEM item) {
		if (item == null) { throw new NullPointerException("Must provide a non-null work item"); }
		return shareLocked(() -> this.workQueue.offer(item));
	}

	/**
	 * Removes all remaining work items from the queue and returns them, but without stopping the
	 * workers.
	 *
	 * @return all remaining work items from the queue
	 */
	public final List<ITEM> clearWorkItems() {
		try (SharedAutoLock lock = sharedAutoLock()) {
			List<ITEM> ret = new ArrayList<>();
			this.workQueue.drainTo(ret);
			return ret;
		}
	}

	/**
	 * Returns the current size of the work queue.
	 *
	 * @return the current size of the work queue.
	 */
	public final int getQueueSize() {
		return shareLocked(this.workQueue::size);
	}

	/**
	 * Returns the current remaining capacity of the work queue.
	 *
	 * @return the current remaining capacity of the work queue.
	 */
	public final int getQueueCapacity() {
		return shareLocked(this.workQueue::remainingCapacity);
	}

	/**
	 * Returns the default maximum wait unit count configured for this instance. The default
	 * implementation simply returns {@link #DEFAULT_MAX_WAIT}. Subclasses should override this
	 * method to provide their own custom values.
	 *
	 * @return the default maximum wait unit count configured for this instance
	 */
	public Duration getDefaultMaxWait() {
		return PooledWorkers.DEFAULT_MAX_WAIT;
	}

	private List<ITEM> shutdown(boolean abort, Duration maxWait) {
		try (MutexAutoLock lock = mutexAutoLock()) {
			if (this.executor == null) { return null; }
			Duration actualMaxWait = Tools.coalesce(maxWait, PooledWorkers.DEFAULT_MAX_WAIT);
			if (actualMaxWait.isNegative() || actualMaxWait.isZero()) {
				actualMaxWait = PooledWorkers.DEFAULT_MAX_WAIT;
			}
			try {
				this.aborted.set(abort);
				this.log.debug("Signaling work completion for the workers");
				this.terminated.set(true);

				List<ITEM> remaining = new ArrayList<>();
				try {
					// We're done, we must wait until all workers are waiting
					this.log.debug("Waiting for {} workers to finish processing", this.threadCount);

					// First, wait for threads to finish starting up
					try {
						this.startupLatch.await();
					} catch (InterruptedException e) {
						this.log.debug("Interrupted while waiting for all threads to start up");
					}

					// Now, wake any blocked threads
					for (Thread t : this.threads) {
						if (this.blocked.contains(t.getId())) {
							switch (t.getState()) {
								case TIMED_WAITING:
								case WAITING:
									t.interrupt();
									this.log.debug("Interrupted thread [{}] ({})", t.getName(), t.getId());
									break;
								default:
									// Do nothing - it will exit on its own
							}
						}
					}
					this.threads.clear();

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
				} finally {
					this.workQueue.drainTo(remaining);
				}

				// If there are still pending workers, then wait for them to finish for up to 5
				// minutes
				int pending = this.activeCounter.get();
				if (pending > 0) {
					this.log.debug("Waiting for pending workers to terminate (maximum {}, {} pending workers)",
						actualMaxWait, pending);
					try {
						this.executor.awaitTermination(actualMaxWait.toMillis(), TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						this.log.warn("Interrupted while waiting for normal executor termination", e);
						Thread.currentThread().interrupt();
					}
				}
				return remaining;
			} finally {
				this.executor.shutdownNow();
				int pending = this.activeCounter.get();
				if (pending > 0) {
					try {
						this.log.debug(
							"Waiting an additional 60 seconds for worker termination as a contingency ({} pending workers)",
							pending);
						this.executor.awaitTermination(1, TimeUnit.MINUTES);
					} catch (InterruptedException e) {
						this.log.warn("Interrupted while waiting for immediate executor termination", e);
						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}

	/**
	 * <p>
	 * Disables the submission of new work to this instance, while also waiting cleanly for all
	 * currently executing work items to be processed. It returns a list containing all pending work
	 * that was not attempted. The maximum amount of time to wait is determined by
	 * {@link #getDefaultMaxWait()}.
	 * </p>
	 */
	public final List<ITEM> abortExecution() {
		return abortExecution(null);
	}

	/**
	 * <p>
	 * Disables the submission of new work to this instance, while also waiting cleanly for all
	 * currently executing work items to be processed. It returns a list containing all pending work
	 * that was not attempted. If the {@code maxWait} parameter is null, zero, or negative (per
	 * {@link Duration#isZero()} and {@link Duration#isNegative()}), the value from
	 * {@link #getDefaultMaxWait()} will be used.
	 * </p>
	 *
	 * @param maxWait
	 */
	public final List<ITEM> abortExecution(Duration maxWait) {
		return mutexLocked(() -> shutdown(true, maxWait));
	}

	/**
	 * <p>
	 * Disables the submission of new work to this instance, while also waiting cleanly for all
	 * pending work to conclude. The maximum amount of time to wait is determined by
	 * {@link #getDefaultMaxWait()}.
	 * </p>
	 *
	 * @return a list of items pending processing. Will never be {@code null}, but may be empty
	 */
	public List<ITEM> waitForCompletion() {
		return waitForCompletion(null);
	}

	/**
	 * <p>
	 * Disables the submission of new work to this instance, while also waiting cleanly for all
	 * pending work to conclude. The maximum amount of time to wait is determined by the given
	 * parameter. If the {@code maxWait} parameter is null, zero, or negative (per
	 * {@link Duration#isZero()} and {@link Duration#isNegative()}), the value from
	 * {@link #getDefaultMaxWait()} will be used.
	 * </p>
	 *
	 * @param maxWait
	 * @return a list of items pending processing. Will never be {@code null}, but may be empty
	 */
	public final List<ITEM> waitForCompletion(Duration maxWait) {
		return mutexLocked(() -> shutdown(false, maxWait));
	}

	/**
	 * <p>
	 * This class is used in lieu of a large number of constructors because it allows for greater
	 * flexibility when specifying the available parameters.
	 * </p>
	 *
	 * @author diego
	 *
	 * @param <STATE>
	 * @param <ITEM>
	 * @param <EX>
	 */
	public static class Builder<STATE, ITEM, EX extends Exception> extends BaseShareableLockable {
		private PooledWorkersLogic<STATE, ITEM, EX> logic = null;
		private int threads = 1;
		private String name = null;
		private int backlogLimit = 0;
		private Collection<? extends ITEM> items = null;
		private boolean waitForWork = true;

		/**
		 * <p>
		 * Set the {@link PooledWorkersLogic} that will be used for processing the submitted work
		 * </p>
		 *
		 * @param logic
		 * @return this instance
		 */
		public Builder<STATE, ITEM, EX> logic(PooledWorkersLogic<STATE, ITEM, EX> logic) {
			try (MutexAutoLock lock = mutexAutoLock()) {
				this.logic = logic;
			}
			return this;
		}

		/**
		 * <p>
		 * Returns the {@link PooledWorkersLogic} instance that has been configured for this
		 * instance
		 * </p>
		 *
		 * @return the {@link PooledWorkersLogic} instance that has been configured for this
		 *         instance
		 */
		public PooledWorkersLogic<STATE, ITEM, EX> logic() {
			try (SharedAutoLock lock = sharedAutoLock()) {
				return this.logic;
			}
		}

		/**
		 * <p>
		 * Set the number of threads that will be used for processing the submitted work. Any values
		 * less than or equal to 0 will be rounded up to 1.
		 * </p>
		 *
		 * @param threads
		 * @return this instance
		 */
		public Builder<STATE, ITEM, EX> threads(int threads) {
			try (MutexAutoLock lock = mutexAutoLock()) {
				this.threads = Math.max(1, threads);
			}
			return this;
		}

		/**
		 * <p>
		 * Returns the number of threads that will be used for processing the submitted work.
		 * </p>
		 *
		 * @return the number of threads that will be used for processing the submitted work.
		 */
		public int threads() {
			try (SharedAutoLock lock = sharedAutoLock()) {
				return this.threads;
			}
		}

		/**
		 * <p>
		 * Sets the name that will be used when naming worker threads.
		 * </p>
		 *
		 * @param name
		 * @return this instance
		 */
		public Builder<STATE, ITEM, EX> name(String name) {
			try (MutexAutoLock lock = mutexAutoLock()) {
				this.name = name;
			}
			return this;
		}

		/**
		 * <p>
		 * Returns the name that will be used when naming worker threads.
		 * </p>
		 *
		 * @return the name that will be used when naming worker threads.
		 */
		public String name() {
			try (SharedAutoLock lock = sharedAutoLock()) {
				return this.name;
			}
		}

		/**
		 * <p>
		 * Sets the maximum number of items that can be submitted for processing without blocking.
		 * </p>
		 *
		 * @param backlogLimit
		 * @return this instance
		 */
		public Builder<STATE, ITEM, EX> backlogLimit(int backlogLimit) {
			try (MutexAutoLock lock = mutexAutoLock()) {
				this.backlogLimit = Math.max(0, backlogLimit);
			}
			return this;
		}

		/**
		 * <p>
		 * Returns the maximum number of items that can be submitted for processing without
		 * blocking.
		 * </p>
		 *
		 * @return the maximum number of items that can be submitted for processing without
		 *         blocking.
		 */
		public int backlogLimit() {
			try (SharedAutoLock lock = sharedAutoLock()) {
				return this.backlogLimit;
			}
		}

		/**
		 * <p>
		 * Sets the work items that should be processed immediately upon startup, regardless of
		 * whether additional work should be accepted or not.
		 * </p>
		 *
		 * @param items
		 * @return this instance
		 */
		public Builder<STATE, ITEM, EX> items(Collection<? extends ITEM> items) {
			try (MutexAutoLock lock = mutexAutoLock()) {
				this.items = items;
			}
			return this;
		}

		/**
		 * <p>
		 * Returns the work items that should be processed immediately upon startup.
		 * </p>
		 *
		 * @return the work items that should be processed immediately upon startup.
		 */
		public Collection<? extends ITEM> items() {
			try (SharedAutoLock lock = sharedAutoLock()) {
				return this.items;
			}
		}

		/**
		 * <p>
		 * Set whether this instance should wait for additional work after processing pre-submitted
		 * work items (if any), or not.
		 * </p>
		 *
		 * @param waitForWork
		 * @return this instance
		 */
		public Builder<STATE, ITEM, EX> waitForWork(boolean waitForWork) {
			try (MutexAutoLock lock = mutexAutoLock()) {
				this.waitForWork = waitForWork;
			}
			return this;
		}

		/**
		 * <p>
		 * Returns whether this instance should wait for additional work after processing
		 * pre-submitted work items (if any), or not
		 * </p>
		 *
		 * @return {@code true} if this instance should wait for additional work after processing
		 *         pre-submitted work items (if any), {@code false} otherwise.
		 */
		public boolean waitForWork() {
			try (SharedAutoLock lock = sharedAutoLock()) {
				return this.waitForWork;
			}
		}

		/**
		 * <p>
		 * Starts the work processing by this instance as per the configured parameters using the
		 * other methods.
		 * </p>
		 *
		 * @return a started {@link PooledWorkers} instance that's already running and processing
		 *         any pending work, possibly waiting for more work if configured to do so.
		 */
		public PooledWorkers<STATE, ITEM> start() {
			return new PooledWorkers<>(this.logic, this.threads, this.name, this.backlogLimit, this.items,
				this.waitForWork);
		}
	}
}
