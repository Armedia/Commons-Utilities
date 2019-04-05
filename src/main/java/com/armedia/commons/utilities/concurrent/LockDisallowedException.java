package com.armedia.commons.utilities.concurrent;

public class LockDisallowedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private final ShareableLockable target;
	private final int readCount;
	private final Thread thread;

	LockDisallowedException(ShareableLockable target, int readCount) {
		super(String.format("Locking operation is not allowed: %d read locks held when requesting a write lock",
			readCount));
		this.target = target;
		this.readCount = readCount;
		this.thread = Thread.currentThread();
	}

	public ShareableLockable getTarget() {
		return this.target;
	}

	public int getReadCount() {
		return this.readCount;
	}

	public Thread getThread() {
		return this.thread;
	}
}
