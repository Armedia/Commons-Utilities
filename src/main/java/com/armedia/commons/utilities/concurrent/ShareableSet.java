package com.armedia.commons.utilities.concurrent;

import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;

public class ShareableSet<ELEMENT> extends ShareableCollection<ELEMENT> implements Set<ELEMENT> {

	public ShareableSet(Set<ELEMENT> set) {
		super(set);
	}

	public ShareableSet(ReadWriteLock rwLock, Set<ELEMENT> set) {
		super(rwLock, set);
	}

	public ShareableSet(ShareableLockable lockable, Set<ELEMENT> set) {
		super(lockable, set);
	}
}