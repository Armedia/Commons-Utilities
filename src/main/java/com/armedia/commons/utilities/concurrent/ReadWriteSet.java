package com.armedia.commons.utilities.concurrent;

import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;

public class ReadWriteSet<ELEMENT> extends ReadWriteCollection<ELEMENT> implements Set<ELEMENT> {

	public ReadWriteSet(Set<ELEMENT> set) {
		this(ReadWriteLockable.NULL_LOCK, set);
	}

	public ReadWriteSet(ReadWriteLock rwLock, Set<ELEMENT> set) {
		super(rwLock, set);
	}

	public ReadWriteSet(ReadWriteLockable lockable, Set<ELEMENT> set) {
		super(lockable, set);
	}
}