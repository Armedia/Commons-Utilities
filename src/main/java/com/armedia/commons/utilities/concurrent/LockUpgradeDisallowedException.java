package com.armedia.commons.utilities.concurrent;

public class LockUpgradeDisallowedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private final ShareableLockable target;
	private final int readCount;

	LockUpgradeDisallowedException(ShareableLockable target, int readCount) {
		super(String.format("Locking operation is not allowed: %d read locks held when requesting a write lock",
			readCount));
		this.target = target;
		this.readCount = readCount;
	}

	public ShareableLockable getTarget() {
		return this.target;
	}

	public int getReadHoldCount() {
		return this.readCount;
	}
}
