package com.armedia.commons.utilities.concurrent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ShareableSetTest {

	@Test
	public void testShareableSet() {
		Set<Object> s = null;
		Assertions.assertThrows(NullPointerException.class, () -> new ShareableSet<>(s));
		new ShareableSet<>(new HashSet<>());
	}

	@Test
	public void testShareableSetReadWriteLock() {
		ReadWriteLock rwl = null;
		Set<Object> s = null;
		Assertions.assertThrows(NullPointerException.class, () -> new ShareableSet<>(rwl, s));
		new ShareableSet<>(rwl, new HashSet<>());
		Assertions.assertThrows(NullPointerException.class, () -> new ShareableSet<>(new ReentrantReadWriteLock(), s));
		new ShareableSet<>(new ReentrantReadWriteLock(), new HashSet<>());
	}

	@Test
	public void testShareableSetShareableLockable() {
		ShareableLockable lockable = null;
		Set<Object> s = null;
		Assertions.assertThrows(NullPointerException.class, () -> new ShareableSet<>(lockable, s));
		Assertions.assertThrows(NullPointerException.class, () -> new ShareableSet<>(lockable, new HashSet<>()));
		Assertions.assertThrows(NullPointerException.class, () -> new ShareableSet<>(new BaseShareableLockable(), s));
		new ShareableSet<>(new BaseShareableLockable(), new HashSet<>());
	}
}