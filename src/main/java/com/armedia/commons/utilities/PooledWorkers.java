package com.armedia.commons.utilities;

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

import com.armedia.commons.utilities.concurrent.BaseReadWriteLockable;

/**
 * A simple multi-threaded worker pool that supports having an optionally size-constrained work
 * queue, and items submitted are processed in order, concurrently. It supports pre-filling the
 * queue for immediate consumption, as well a blocking mode where the worker threads wait for work
 * to be submitted and execute it as it arrives.
 *
 * @author Diego Rivera &lt;diego.rivera@armedia.com&gt;
 *
 * @param <STATE>
 *            The state class that will be produced by {@link PooledWorkersLogic#initialize()} and
 *            consumed by {@link PooledWorkersLogic#process(Object, Object)} and
 *            {@link PooledWorkersLogic#cleanup(Object)}
 * @param <ITEM>
 *            The item class that will be processed by
 *            {@link PooledWorkersLogic#process(Object, Object)}
 *
 */
public final class PooledWorkers<STATE, ITEM> extends BaseReadWriteLockable {
	protected static final long DEFAULT_MAX_WAIT = 5;
	protected static final TimeUnit DEFAULT_MAX_WAIT_UNIT = TimeUnit.MINUTES;

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final BlockingQueue<ITEM> workQueue;
	private final List<Future<?>> futures;
	private final AtomicInteger activeCounter;

	private final AtomicBoolean aborted = new AtomicBoolean(false);
	private final AtomicBoolean terminated = new AtomicBoolean(false);
	private int threadCount = 0;
	private ThreadPoolExecutor executor = null;
	private final Collection<Thread> threads = new ArrayList<>();
	private final Set<Long> blocked = new HashSet<>();

	private final class Task implements Runnable {
		private final Logger log = PooledWorkers.this.log;

		private final boolean waitForWork;
		private final PooledWorkersLogic<STATE, ITEM> logic;

		private Task(PooledWorkersLogic<STATE, ITEM> logic, boolean waitForWork) {
			this.waitForWork = waitForWork;
			this.logic = logic;
		}

		@Override
		public void run() {
			final STATE state;
			try {
				state = this.logic.initialize();
			} catch (Exception e) {
				workerThreadExited("Failed to initialize the worker state", null, e);
				return;
			}
			PooledWorkers.this.activeCounter.incrementAndGet();
			try {
				while (!Thread.interrupted() && !PooledWorkers.this.aborted.get()) {
					if (this.log.isDebugEnabled()) {
						this.log.trace("Polling the queue...");
					}
					ITEM item = null;
					if (this.waitForWork && !PooledWorkers.this.terminated.get()) {
						final Thread thread = Thread.currentThread();
						try {
							PooledWorkers.this.blocked.add(thread.getId());
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
					} catch (Exception e) {
						this.logic.handleFailure(state, item, e);
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
	 * Create an instance with "no" queue size limitations. Identical to invoking
	 * {@link #PooledWorkers(int) new PooledWorkers(0)}.
	 */
	public PooledWorkers() {
		this(0);
	}

	/**
	 * Construct an instance that will accept a maximum of {@code backLog} items waiting for
	 * processing in the queue, such that new invocations to {@link #addWorkItem(Object)},
	 * {@link #addWorkItem(Object, long, TimeUnit)} or {@link #addWorkItemNonblock(Object)} will
	 * either block or fail accordingly if the queue is full. If {@code backlogSize <= 0} then the
	 * queue will have "no" capacity limit (in reality, a limit of {@link Integer#MAX_VALUE} items).
	 *
	 * @param backlogSize
	 */
	public PooledWorkers(int backlogSize) {
		this.workQueue = (backlogSize <= 0 ? new LinkedBlockingQueue<>() : new ArrayBlockingQueue<>(backlogSize));
		this.futures = new LinkedList<>();
		this.activeCounter = new AtomicInteger(0);
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
		readLockedChecked(() -> this.workQueue.put(item));
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
	public final boolean addWorkItem(ITEM item, long count, TimeUnit timeUnit) throws InterruptedException {
		if (item == null) { throw new NullPointerException("Must provide a non-null work item"); }
		return readLockedChecked(() -> this.workQueue.offer(item, count, timeUnit));
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
		return readLocked(() -> this.workQueue.offer(item));
	}

	/**
	 * Removes all remaining work items from the queue and returns them, but without stopping the
	 * workers.
	 *
	 * @return all remaining work items from the queue
	 */
	public final List<ITEM> clearWorkItems() {
		return readLockedChecked(() -> {
			List<ITEM> ret = new ArrayList<>();
			this.workQueue.drainTo(ret);
			return ret;
		});
	}

	/**
	 * Returns the current size of the work queue.
	 *
	 * @return the current size of the work queue.
	 */
	public final int getQueueSize() {
		return readLocked(this.workQueue::size);
	}

	/**
	 * Returns the current remaining capacity of the work queue.
	 *
	 * @return the current remaining capacity of the work queue.
	 */
	public final int getQueueCapacity() {
		return readLocked(this.workQueue::remainingCapacity);
	}

	/**
	 * @see PooledWorkers#start(PooledWorkersLogic, int, String, boolean)
	 */
	public final boolean start(PooledWorkersLogic<STATE, ITEM> logic, int threadCount) {
		return start(logic, threadCount, null, true);
	}

	/**
	 * @see PooledWorkers#start(PooledWorkersLogic, int, String, boolean)
	 */
	public final boolean start(PooledWorkersLogic<STATE, ITEM> logic, int threadCount, String name) {
		return start(logic, threadCount, null, true);
	}

	/**
	 * @see PooledWorkers#start(PooledWorkersLogic, int, String, boolean)
	 */
	public final boolean start(PooledWorkersLogic<STATE, ITEM> logic, int threadCount, boolean waitForWork) {
		return start(logic, threadCount, null, waitForWork);
	}

	/**
	 * <p>
	 * Starts the work processing by this instance using a maximum of {@code threadCount} threads
	 * (minimum is 1), naming the threads using {@code name} as a pattern, and causing the workers
	 * to wait for work when the work queue empties as indicated by the {@code waitForWork}
	 * parameter ({@code true} = wait, {@code false} = no wait). It returns {@code true} if the work
	 * was started, or {@code false} if the work had already been started.
	 * </p>
	 *
	 * @param logic
	 * @param threadCount
	 * @param name
	 * @param waitForWork
	 *            indicates whether worker threads should wait ({@code true}) for work to become
	 *            available in the work queue, or not ({@code false})
	 * @return {@code true} if the work was started, or {@code false} if the work had already been
	 *         started.
	 */
	public final boolean start(PooledWorkersLogic<STATE, ITEM> logic, int threadCount, String name,
		boolean waitForWork) {
		Objects.requireNonNull(logic, "Must provide the logic that these workers will apply");
		return writeLocked(() -> {
			if (this.executor != null) { return false; }
			this.threadCount = Math.max(1, threadCount);
			this.activeCounter.set(0);
			this.futures.clear();
			this.terminated.set(false);
			this.threads.clear();
			Task task = new Task(logic, waitForWork);
			ThreadFactory threadFactory = Executors.defaultThreadFactory();
			String finalName = StringUtils.strip(name);
			final String threadNameFormat = String.format("%s-%%0%dd", finalName, String.valueOf(threadCount).length());
			if (!StringUtils.isEmpty(finalName)) {
				final ThreadGroup group = new ThreadGroup(
					String.format("Threads for PooledWorkers task [%s]", finalName));
				threadFactory = new ThreadFactory() {
					private final AtomicLong counter = new AtomicLong(0);

					@Override
					public Thread newThread(Runnable r) {
						Thread t = new Thread(group, r,
							String.format(threadNameFormat, this.counter.incrementAndGet()));
						PooledWorkers.this.threads.add(t);
						return t;
					}
				};
			}
			this.executor = new ThreadPoolExecutor(this.threadCount, this.threadCount, 30, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(), threadFactory);
			for (int i = 0; i < this.threadCount; i++) {
				this.futures.add(this.executor.submit(task));
			}
			this.executor.shutdown();
			return true;
		});
	}

	/**
	 * Returns the default maximum wait unit count configured for this instance. The default
	 * implementation simply returns {@link #DEFAULT_MAX_WAIT}. Subclasses should override this
	 * method to provide their own custom values.
	 *
	 * @return the default maximum wait unit count configured for this instance
	 */
	public long getDefaultMaxWait() {
		return PooledWorkers.DEFAULT_MAX_WAIT;
	}

	/**
	 * Returns the default maximum wait unit configured for this instance. The default
	 * implementation simply returns {@link #DEFAULT_MAX_WAIT_UNIT}. Subclasses should override this
	 * method to provide their own custom values.
	 *
	 * @return the default maximum wait unit configured for this instance
	 */
	public TimeUnit getDefaultMaxWaitUnit() {
		return PooledWorkers.DEFAULT_MAX_WAIT_UNIT;
	}

	private List<ITEM> shutdown(boolean abort, long maxWait, TimeUnit timeUnit) {
		return writeLocked(() -> {
			if (this.executor == null) { return null; }
			long actualMaxWait = maxWait;
			TimeUnit actualTimeUnit = timeUnit;
			if ((actualMaxWait <= 0) || (actualTimeUnit == null)) {
				actualMaxWait = getDefaultMaxWait();
				if (actualMaxWait <= 0) {
					actualMaxWait = PooledWorkers.DEFAULT_MAX_WAIT;
				}
				actualTimeUnit = getDefaultMaxWaitUnit();
				if (actualTimeUnit == null) {
					actualMaxWait = PooledWorkers.DEFAULT_MAX_WAIT;
					actualTimeUnit = PooledWorkers.DEFAULT_MAX_WAIT_UNIT;
				}
			}
			try {
				this.aborted.set(abort);
				this.log.debug("Signaling work completion for the workers");
				this.terminated.set(true);

				List<ITEM> remaining = new ArrayList<>();
				try {
					// We're done, we must wait until all workers are waiting
					this.log.debug("Waiting for {} workers to finish processing", this.threadCount);
					// First, wake any blocked threads
					for (Thread t : this.threads) {
						if (this.blocked.contains(t.getId())) {
							switch (t.getState()) {
								case TIMED_WAITING:
								case WAITING:
									t.interrupt();
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
					this.log.debug("Waiting for pending workers to terminate (maximum {} {}, {} pending workers)",
						actualMaxWait, actualTimeUnit.name().toLowerCase(), pending);
					try {
						this.executor.awaitTermination(actualMaxWait, actualTimeUnit);
					} catch (InterruptedException e) {
						this.log.warn("Interrupted while waiting for normal executor termination", e);
						Thread.currentThread().interrupt();
					}
				}
				return remaining;
			} finally {
				try {
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
				} finally {
					this.executor = null;
					this.threadCount = 0;
				}
			}
		});
	}

	/**
	 * <p>
	 * Disables the submission of new work to this instance, while also waiting cleanly for all
	 * currently executing work items to be processed. It returns a list containing all pending work
	 * that was not attempted. The maximum amount of time to wait is determined by
	 * {@link #getDefaultMaxWait()} and {@link #getDefaultMaxWaitUnit()}.
	 * </p>
	 */
	public final List<ITEM> abortExecution() {
		return abortExecution(0, null);
	}

	/**
	 * <p>
	 * Disables the submission of new work to this instance, while also waiting cleanly for all
	 * currently executing work items to be processed. It returns a list containing all pending work
	 * that was not attempted. If the {@code maxWait} parameter is less than or equal to 0, or if
	 * the {@code timeUnit} parameter is {@code null}, the values from {@link #getDefaultMaxWait()}
	 * and {@link #getDefaultMaxWaitUnit()} will be used. If neither of those meets those criteria,
	 * then the values from {@link #DEFAULT_MAX_WAIT} and {@link #DEFAULT_MAX_WAIT_UNIT} are used.
	 * </p>
	 *
	 * @param maxWait
	 * @param timeUnit
	 */
	public final List<ITEM> abortExecution(long maxWait, TimeUnit timeUnit) {
		return writeLocked(() -> shutdown(true, maxWait, timeUnit));
	}

	/**
	 * <p>
	 * Disables the submission of new work to this instance, while also waiting cleanly for all
	 * pending work to conclude. The maximum amount of time to wait is determined by
	 * {@link #getDefaultMaxWait()} and {@link #getDefaultMaxWaitUnit()}.
	 * </p>
	 *
	 * @return a list of items pending processing. Will never be {@code null}, but may be empty
	 */
	public List<ITEM> waitForCompletion() {
		return waitForCompletion(0, null);
	}

	/**
	 * <p>
	 * Disables the submission of new work to this instance, while also waiting cleanly for all
	 * pending work to conclude. The maximum amount of time to wait is determined by the given
	 * parameters. If the {@code maxWait} parameter is less than or equal to 0, or if the
	 * {@code timeUnit} parameter is {@code null}, the values from {@link #getDefaultMaxWait()} and
	 * {@link #getDefaultMaxWaitUnit()} will be used. If neither of those meets those criteria, then
	 * the values from {@link #DEFAULT_MAX_WAIT} and {@link #DEFAULT_MAX_WAIT_UNIT} are used.
	 * </p>
	 *
	 * @param maxWait
	 * @param timeUnit
	 * @return a list of items pending processing. Will never be {@code null}, but may be empty
	 */
	public final List<ITEM> waitForCompletion(long maxWait, TimeUnit timeUnit) {
		return writeLocked(() -> shutdown(false, maxWait, timeUnit));
	}
}