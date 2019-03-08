package com.armedia.commons.utilities.concurrent;

import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;

public class ReadWriteSet<ELEMENT> extends ReadWriteCollection<ELEMENT> implements Set<ELEMENT> {

	public ReadWriteSet(Set<ELEMENT> set) {
		this(null, set);
	}

	public ReadWriteSet(ReadWriteLock rwLock, Set<ELEMENT> set) {
		super(rwLock, set);
	}

}