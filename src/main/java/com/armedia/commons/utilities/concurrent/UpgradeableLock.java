package com.armedia.commons.utilities.concurrent;

public interface UpgradeableLock {

	public MutexAutoLock upgrade();

}