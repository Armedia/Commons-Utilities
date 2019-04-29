package com.armedia.commons.utilities.concurrent;

import java.util.Collections;
import java.util.ListIterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ShareableListIteratorTest {

	@Test
	public void testConstructors() {
		Assertions.assertThrows(NullPointerException.class, () -> new ShareableListIterator<>(null));
		Assertions.assertThrows(NullPointerException.class,
			() -> new ShareableListIterator<>(ShareableLockable.NULL_LOCK, null));
		Assertions.assertThrows(NullPointerException.class,
			() -> new ShareableListIterator<>((ShareableLockable) null, null));

		final ListIterator<Object> emptyListIterator = Collections.emptyListIterator();
		final ShareableLockable rwl = new ShareableListIterator<>(emptyListIterator);
		new ShareableListIterator<>(ShareableLockable.NULL_LOCK, emptyListIterator);
		Assertions.assertThrows(NullPointerException.class,
			() -> new ShareableListIterator<>((ShareableLockable) null, emptyListIterator));
		new ShareableListIterator<>(rwl, emptyListIterator);

		ReadWriteLock lock = new ReentrantReadWriteLock();
		Assertions.assertSame(lock, new ShareableListIterator<>(lock, emptyListIterator).getShareableLock());
		Assertions.assertSame(rwl.getShareableLock(),
			new ShareableListIterator<>(rwl, emptyListIterator).getShareableLock());
	}

	@Test
	public void testHasPrevious() {
		final ListIterator<Object> li = EasyMock.createStrictMock(ListIterator.class);
		final Lock rl = EasyMock.createStrictMock(Lock.class);
		final Lock wl = EasyMock.createStrictMock(Lock.class);
		final ReadWriteLock rwl = new ReadWriteLock() {
			@Override
			public Lock readLock() {
				return rl;
			}

			@Override
			public Lock writeLock() {
				return wl;
			}
		};
		final ShareableListIterator<Object> sli = new ShareableListIterator<>(rwl, li);

		EasyMock.reset(rl, wl, li);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(li.hasPrevious()).andReturn(true).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, li);
		Assertions.assertTrue(sli.hasPrevious());
		EasyMock.verify(rl, wl, li);

		EasyMock.reset(rl, wl, li);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(li.hasPrevious()).andReturn(false).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, li);
		Assertions.assertFalse(sli.hasPrevious());
		EasyMock.verify(rl, wl, li);
	}

	@Test
	public void testPrevious() {
		final ListIterator<Object> li = EasyMock.createStrictMock(ListIterator.class);
		final Lock rl = EasyMock.createStrictMock(Lock.class);
		final Lock wl = EasyMock.createStrictMock(Lock.class);
		final ReadWriteLock rwl = new ReadWriteLock() {
			@Override
			public Lock readLock() {
				return rl;
			}

			@Override
			public Lock writeLock() {
				return wl;
			}
		};
		final ShareableListIterator<Object> sli = new ShareableListIterator<>(rwl, li);
		final Object o = new Object();

		EasyMock.reset(rl, wl, li);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(li.previous()).andReturn(o).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, li);
		Assertions.assertSame(o, sli.previous());
		EasyMock.verify(rl, wl, li);
	}

	@Test
	public void testNextIndex() {
		final ListIterator<Object> li = EasyMock.createStrictMock(ListIterator.class);
		final Lock rl = EasyMock.createStrictMock(Lock.class);
		final Lock wl = EasyMock.createStrictMock(Lock.class);
		final ReadWriteLock rwl = new ReadWriteLock() {
			@Override
			public Lock readLock() {
				return rl;
			}

			@Override
			public Lock writeLock() {
				return wl;
			}
		};
		final ShareableListIterator<Object> sli = new ShareableListIterator<>(rwl, li);

		for (int i = 0; i < 10; i++) {
			EasyMock.reset(rl, wl, li);
			rl.lock();
			EasyMock.expectLastCall().once();
			EasyMock.expect(li.nextIndex()).andReturn(i).once();
			rl.unlock();
			EasyMock.expectLastCall().once();
			EasyMock.replay(rl, wl, li);
			Assertions.assertEquals(i, sli.nextIndex());
			EasyMock.verify(rl, wl, li);
		}
	}

	@Test
	public void testPreviousIndex() {
		final ListIterator<Object> li = EasyMock.createStrictMock(ListIterator.class);
		final Lock rl = EasyMock.createStrictMock(Lock.class);
		final Lock wl = EasyMock.createStrictMock(Lock.class);
		final ReadWriteLock rwl = new ReadWriteLock() {
			@Override
			public Lock readLock() {
				return rl;
			}

			@Override
			public Lock writeLock() {
				return wl;
			}
		};
		final ShareableListIterator<Object> sli = new ShareableListIterator<>(rwl, li);

		for (int i = 0; i < 10; i++) {
			EasyMock.reset(rl, wl, li);
			rl.lock();
			EasyMock.expectLastCall().once();
			EasyMock.expect(li.previousIndex()).andReturn(i).once();
			rl.unlock();
			EasyMock.expectLastCall().once();
			EasyMock.replay(rl, wl, li);
			Assertions.assertEquals(i, sli.previousIndex());
			EasyMock.verify(rl, wl, li);
		}
	}

	@Test
	public void testSet() {
		final ListIterator<Object> li = EasyMock.createStrictMock(ListIterator.class);
		final Lock rl = EasyMock.createStrictMock(Lock.class);
		final Lock wl = EasyMock.createStrictMock(Lock.class);
		final ReadWriteLock rwl = new ReadWriteLock() {
			@Override
			public Lock readLock() {
				return rl;
			}

			@Override
			public Lock writeLock() {
				return wl;
			}
		};
		final ShareableListIterator<Object> sli = new ShareableListIterator<>(rwl, li);
		final Object o = new Object();

		EasyMock.reset(rl, wl, li);
		wl.lock();
		EasyMock.expectLastCall().once();
		li.set(EasyMock.same(o));
		EasyMock.expectLastCall().once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, li);
		sli.set(o);
		EasyMock.verify(rl, wl, li);
	}

	@Test
	public void testAdd() {
		final ListIterator<Object> li = EasyMock.createStrictMock(ListIterator.class);
		final Lock rl = EasyMock.createStrictMock(Lock.class);
		final Lock wl = EasyMock.createStrictMock(Lock.class);
		final ReadWriteLock rwl = new ReadWriteLock() {
			@Override
			public Lock readLock() {
				return rl;
			}

			@Override
			public Lock writeLock() {
				return wl;
			}
		};
		final ShareableListIterator<Object> sli = new ShareableListIterator<>(rwl, li);
		final Object o = new Object();

		EasyMock.reset(rl, wl, li);
		wl.lock();
		EasyMock.expectLastCall().once();
		li.add(EasyMock.same(o));
		EasyMock.expectLastCall().once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, li);
		sli.add(o);
		EasyMock.verify(rl, wl, li);
	}
}