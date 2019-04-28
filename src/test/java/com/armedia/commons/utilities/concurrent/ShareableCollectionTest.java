package com.armedia.commons.utilities.concurrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.concurrent.TrackableReadWriteLock.LockCall;

public class ShareableCollectionTest {

	@Test
	public void testConstructors() {
		ReadWriteLock rwl = null;
		ShareableLockable sl = null;
		Collection<Object> c = null;

		Assertions.assertThrows(NullPointerException.class, () -> new ShareableCollection<>(c));
		new ShareableCollection<>(new HashSet<>());
		new ShareableCollection<>(new ShareableCollection<>(new HashSet<>()));

		Assertions.assertThrows(NullPointerException.class, () -> new ShareableCollection<>(rwl, c));
		new ShareableCollection<>(rwl, new HashSet<>());
		Assertions.assertThrows(NullPointerException.class,
			() -> new ShareableCollection<>(new ReentrantReadWriteLock(), c));

		Assertions.assertThrows(NullPointerException.class, () -> new ShareableCollection<>(sl, c));
		Assertions.assertThrows(NullPointerException.class, () -> new ShareableCollection<>(sl, new HashSet<>()));
		Assertions.assertThrows(NullPointerException.class,
			() -> new ShareableCollection<>(new BaseShareableLockable(), c));
		{
			ReadWriteLock l = new ReentrantReadWriteLock();
			ShareableLockable s = new BaseShareableLockable();
			Assertions.assertSame(l, new ShareableCollection<>(l, new HashSet<>()).getShareableLock());
			Assertions.assertSame(s.getShareableLock(),
				new ShareableCollection<>(s, new HashSet<>()).getShareableLock());
		}
	}

	@Test
	public void testForEach() {
		Collection<String> l = Arrays.asList("a", "b", "c");
		TrackableReadWriteLock lock = new TrackableReadWriteLock();

		l = new ShareableCollection<>(lock, l);
		Assertions.assertFalse(lock.isWriteLockedByCurrentThread());
		Assertions.assertEquals(0, lock.getReadHoldCount());
		l.forEach((e) -> {
			Assertions.assertFalse(lock.isWriteLockedByCurrentThread());
			Assertions.assertEquals(1, lock.getReadHoldCount());
		});
		Assertions.assertFalse(lock.isWriteLockedByCurrentThread());
		Assertions.assertEquals(0, lock.getReadHoldCount());
		List<Pair<Long, LockCall>> calls = lock.getLockCalls();
		Assertions.assertFalse(calls.isEmpty());
		Assertions.assertEquals(2, calls.size());
		Pair<Long, LockCall> c = null;

		c = calls.get(0);
		Assertions.assertEquals("readLock", c.getRight().getLabel());
		Assertions.assertEquals("lock", c.getRight().getMethod().getName());

		c = calls.get(1);
		Assertions.assertEquals("readLock", c.getRight().getLabel());
		Assertions.assertEquals("unlock", c.getRight().getMethod().getName());
	}

	@Test
	public void testSize() {
		for (int i = 1; i < 100; i++) {
			Collection<String> l = new ArrayList<>();
			for (int j = 0; j < i; j++) {
				l.add(String.valueOf(j));
			}

			TrackableReadWriteLock lock = new TrackableReadWriteLock();

			l = new ShareableCollection<>(lock, l);
			Assertions.assertFalse(lock.isWriteLockedByCurrentThread());
			Assertions.assertEquals(0, lock.getReadHoldCount());
			int size = l.size();
			Assertions.assertEquals(i, size);
			Assertions.assertFalse(lock.isWriteLockedByCurrentThread());
			Assertions.assertEquals(0, lock.getReadHoldCount());
			List<Pair<Long, LockCall>> calls = lock.getLockCalls();
			Assertions.assertFalse(calls.isEmpty());
			Assertions.assertEquals(2, calls.size());
			Pair<Long, LockCall> c = null;

			c = calls.get(0);
			Assertions.assertEquals("readLock", c.getRight().getLabel());
			Assertions.assertEquals("lock", c.getRight().getMethod().getName());

			c = calls.get(1);
			Assertions.assertEquals("readLock", c.getRight().getLabel());
			Assertions.assertEquals("unlock", c.getRight().getMethod().getName());
		}
	}

	@Test
	public void testIsEmpty() {
		Collection<String> l = new ArrayList<>();

		TrackableReadWriteLock lock = new TrackableReadWriteLock();

		l = new ShareableCollection<>(lock, l);
		Assertions.assertFalse(lock.isWriteLockedByCurrentThread());
		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertTrue(l.isEmpty());
		Assertions.assertFalse(lock.isWriteLockedByCurrentThread());
		Assertions.assertEquals(0, lock.getReadHoldCount());
		List<Pair<Long, LockCall>> calls = lock.getLockCalls();
		Assertions.assertFalse(calls.isEmpty());
		Assertions.assertEquals(2, calls.size());
		Pair<Long, LockCall> c = null;

		c = calls.get(0);
		Assertions.assertEquals("readLock", c.getRight().getLabel());
		Assertions.assertEquals("lock", c.getRight().getMethod().getName());

		c = calls.get(1);
		Assertions.assertEquals("readLock", c.getRight().getLabel());
		Assertions.assertEquals("unlock", c.getRight().getMethod().getName());

		calls.clear();

		l = Arrays.asList("a");
		l = new ShareableCollection<>(lock, l);
		Assertions.assertFalse(lock.isWriteLockedByCurrentThread());
		Assertions.assertEquals(0, lock.getReadHoldCount());
		Assertions.assertFalse(l.isEmpty());
		Assertions.assertFalse(lock.isWriteLockedByCurrentThread());
		Assertions.assertEquals(0, lock.getReadHoldCount());
		calls = lock.getLockCalls();
		Assertions.assertFalse(calls.isEmpty());
		Assertions.assertEquals(2, calls.size());

		c = calls.get(0);
		Assertions.assertEquals("readLock", c.getRight().getLabel());
		Assertions.assertEquals("lock", c.getRight().getMethod().getName());

		c = calls.get(1);
		Assertions.assertEquals("readLock", c.getRight().getLabel());
		Assertions.assertEquals("unlock", c.getRight().getMethod().getName());

	}

	@Test
	public void testContains() {
	}

	@Test
	public void testIterator() {
	}

	@Test
	public void testToArray() {
	}

	@Test
	public void testToArrayTArray() {
	}

	@Test
	public void testAdd() {
	}

	@Test
	public void testRemove() {
	}

	@Test
	public void testContainsAll() {
	}

	@Test
	public void testAddAll() {
	}

	@Test
	public void testRetainAll() {
	}

	@Test
	public void testRemoveAll() {
	}

	@Test
	public void testClear() {
	}

	@Test
	public void testEqualsObject() {
	}

	@Test
	public void testRemoveIf() {
	}

	@Test
	public void testSpliterator() {
	}
}