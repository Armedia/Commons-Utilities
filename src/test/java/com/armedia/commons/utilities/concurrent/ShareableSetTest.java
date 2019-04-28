package com.armedia.commons.utilities.concurrent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ShareableSetTest {

	@Test
	public void testConstructors() {
		ShareableLockable sl = null;
		ReadWriteLock rwl = null;
		Set<Object> s = null;

		Assertions.assertThrows(NullPointerException.class, () -> new ShareableSet<>(s));
		new ShareableSet<>(new HashSet<>());

		Assertions.assertThrows(NullPointerException.class, () -> new ShareableSet<>(rwl, s));
		new ShareableSet<>(rwl, new HashSet<>());
		Assertions.assertThrows(NullPointerException.class, () -> new ShareableSet<>(new ReentrantReadWriteLock(), s));
		new ShareableSet<>(new ReentrantReadWriteLock(), new HashSet<>());

		Assertions.assertThrows(NullPointerException.class, () -> new ShareableSet<>(sl, s));
		Assertions.assertThrows(NullPointerException.class, () -> new ShareableSet<>(sl, new HashSet<>()));
		Assertions.assertThrows(NullPointerException.class, () -> new ShareableSet<>(new BaseShareableLockable(), s));
		new ShareableSet<>(new BaseShareableLockable(), new HashSet<>());
	}
}