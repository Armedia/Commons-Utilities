package com.armedia.commons.utilities.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReadWriteIteratorTest {

	@Test
	public void testConstructors() {
		Assertions.assertThrows(NullPointerException.class, () -> new ReadWriteIterator<>(null));
		Assertions.assertThrows(NullPointerException.class,
			() -> new ReadWriteIterator<>(ReadWriteLockable.NULL_LOCK, null));
		Assertions.assertThrows(NullPointerException.class,
			() -> new ReadWriteIterator<>((ReadWriteLockable) null, null));

		final Iterator<Object> emptyIterator = Collections.emptyIterator();
		final ReadWriteLockable rwl = new ReadWriteIterator<>(emptyIterator);
		new ReadWriteIterator<>(ReadWriteLockable.NULL_LOCK, emptyIterator);
		Assertions.assertThrows(NullPointerException.class,
			() -> new ReadWriteIterator<>((ReadWriteLockable) null, emptyIterator));
		new ReadWriteIterator<>(rwl, emptyIterator);

		ReadWriteLock lock = new ReentrantReadWriteLock();
		Assertions.assertSame(lock, new ReadWriteIterator<>(lock, emptyIterator).getMainLock());
		Assertions.assertSame(rwl.getMainLock(), new ReadWriteIterator<>(rwl, emptyIterator).getMainLock());
	}

	@Test
	public void testTraversal() {
		final List<String> elements = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			elements.add(String.valueOf(i));
		}

		{
			ReadWriteIterator<String> it = new ReadWriteIterator<>(Collections.emptyIterator());
			Assertions.assertFalse(it.hasNext());
			Assertions.assertThrows(NoSuchElementException.class, () -> it.next());
			Assertions.assertThrows(NullPointerException.class, () -> it.forEachRemaining(null));
		}
		{
			ReadWriteIterator<String> it = new ReadWriteIterator<>(elements.iterator());
			for (int i = 0; i < 10; i++) {
				Assertions.assertTrue(it.hasNext());
				String a = String.valueOf(i);
				Assertions.assertEquals(a, it.next());
			}
		}
		{
			List<String> newElements = new ArrayList<>(elements);
			ReadWriteIterator<String> it = new ReadWriteIterator<>(newElements.iterator());
			Assertions.assertFalse(newElements.isEmpty());
			for (int i = 0; i < 10; i++) {
				Assertions.assertTrue(it.hasNext());
				String a = String.valueOf(i);
				Assertions.assertEquals(a, it.next());
				it.remove();
			}
			Assertions.assertTrue(newElements.isEmpty());
		}

		// Now test the locking
		{
			ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
			ReadWriteIterator<String> it = new ReadWriteIterator<>(rwl, elements.iterator());
			AtomicInteger current = new AtomicInteger(0);
			Assertions.assertEquals(0, rwl.getReadHoldCount());
			it.forEachRemaining((e) -> {
				String v = String.valueOf(current.getAndIncrement());
				Assertions.assertEquals(v, e);
				Assertions.assertEquals(1, rwl.getReadHoldCount());
				Assertions.assertFalse(rwl.writeLock().isHeldByCurrentThread());
			});
			Assertions.assertEquals(0, rwl.getReadHoldCount());
		}
		{
			ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
			ReadWriteIterator<String> it = new ReadWriteIterator<>(rwl, new Iterator<String>() {
				@Override
				public boolean hasNext() {
					Assertions.assertEquals(1, rwl.getReadHoldCount());
					Assertions.assertFalse(rwl.writeLock().isHeldByCurrentThread());
					return false;
				}

				@Override
				public String next() {
					Assertions.assertEquals(1, rwl.getReadHoldCount());
					Assertions.assertFalse(rwl.writeLock().isHeldByCurrentThread());
					throw new NoSuchElementException();
				}

			});
			Assertions.assertFalse(it.hasNext());
			Assertions.assertThrows(NoSuchElementException.class, () -> it.next());
		}
		{
			ReadWriteIterator<String> base = new ReadWriteIterator<>(elements.iterator());
			ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
			ReadWriteIterator<String> it = new ReadWriteIterator<>(rwl, new Iterator<String>() {
				@Override
				public boolean hasNext() {
					Assertions.assertEquals(1, rwl.getReadHoldCount());
					Assertions.assertFalse(rwl.writeLock().isHeldByCurrentThread());
					return base.hasNext();
				}

				@Override
				public String next() {
					Assertions.assertEquals(1, rwl.getReadHoldCount());
					Assertions.assertFalse(rwl.writeLock().isHeldByCurrentThread());
					return base.next();
				}

				@Override
				public void remove() {
					Assertions.assertEquals(0, rwl.getReadHoldCount());
					Assertions.assertTrue(rwl.writeLock().isHeldByCurrentThread());
					base.remove();
				}

			});
			for (int i = 0; i < 10; i++) {
				Assertions.assertTrue(it.hasNext());
				String a = String.valueOf(i);
				Assertions.assertEquals(a, it.next());
				it.remove();
			}
			Assertions.assertFalse(it.hasNext());
			Assertions.assertThrows(NoSuchElementException.class, () -> it.next());
		}
	}

	@Test
	public void testNext() {
		final List<String> elements = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			elements.add(String.valueOf(i));
		}

		{
			ReadWriteIterator<String> it = new ReadWriteIterator<>(Collections.emptyIterator());
			Assertions.assertFalse(it.hasNext());
			Assertions.assertThrows(NoSuchElementException.class, () -> it.next());
		}
		{
			ReadWriteIterator<String> it = new ReadWriteIterator<>(elements.iterator());
			for (int i = 0; i < 10; i++) {
				Assertions.assertTrue(it.hasNext());
				String a = String.valueOf(i);
				Assertions.assertEquals(a, it.next());
			}
		}
		{
			List<String> newElements = new ArrayList<>(elements);
			ReadWriteIterator<String> it = new ReadWriteIterator<>(newElements.iterator());
			Assertions.assertFalse(newElements.isEmpty());
			for (int i = 0; i < 10; i++) {
				Assertions.assertTrue(it.hasNext());
				String a = String.valueOf(i);
				Assertions.assertEquals(a, it.next());
				it.remove();
			}
			Assertions.assertTrue(newElements.isEmpty());
		}

		// Now test the locking
		{
			ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
			ReadWriteIterator<String> it = new ReadWriteIterator<>(rwl, elements.iterator());
			AtomicInteger current = new AtomicInteger(0);
			Assertions.assertEquals(0, rwl.getReadHoldCount());
			it.forEachRemaining((e) -> {
				String v = String.valueOf(current.getAndIncrement());
				Assertions.assertEquals(v, e);
				Assertions.assertEquals(1, rwl.getReadHoldCount());
				Assertions.assertFalse(rwl.writeLock().isHeldByCurrentThread());
			});
			Assertions.assertEquals(0, rwl.getReadHoldCount());
		}
		{
			ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
			ReadWriteIterator<String> it = new ReadWriteIterator<>(rwl, new Iterator<String>() {
				@Override
				public boolean hasNext() {
					Assertions.assertEquals(1, rwl.getReadHoldCount());
					Assertions.assertFalse(rwl.writeLock().isHeldByCurrentThread());
					return false;
				}

				@Override
				public String next() {
					Assertions.assertEquals(1, rwl.getReadHoldCount());
					Assertions.assertFalse(rwl.writeLock().isHeldByCurrentThread());
					throw new NoSuchElementException();
				}

			});
			Assertions.assertFalse(it.hasNext());
			Assertions.assertThrows(NoSuchElementException.class, () -> it.next());
		}
	}
}