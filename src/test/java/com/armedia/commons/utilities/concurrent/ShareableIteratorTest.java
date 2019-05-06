package com.armedia.commons.utilities.concurrent;

import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ShareableIteratorTest {

	@Test
	public void testConstructors() {
		Assertions.assertThrows(NullPointerException.class, () -> new ShareableIterator<>(null));
		Assertions.assertThrows(NullPointerException.class,
			() -> new ShareableIterator<>(ShareableLockable.NULL_LOCK, null));
		Assertions.assertThrows(NullPointerException.class,
			() -> new ShareableIterator<>((ShareableLockable) null, null));

		final Iterator<Object> emptyIterator = Collections.emptyIterator();
		final ShareableLockable rwl = new ShareableIterator<>(emptyIterator);
		new ShareableIterator<>(ShareableLockable.NULL_LOCK, emptyIterator);
		Assertions.assertThrows(NullPointerException.class,
			() -> new ShareableIterator<>((ShareableLockable) null, emptyIterator));
		new ShareableIterator<>(rwl, emptyIterator);

		ReadWriteLock lock = new ReentrantReadWriteLock();
		Assertions.assertSame(lock, new ShareableIterator<>(lock, emptyIterator).getShareableLock());
		Assertions.assertSame(rwl.getShareableLock(), new ShareableIterator<>(rwl, emptyIterator).getShareableLock());
	}

	@Test
	public void testHasNext() {
		final Iterator<Object> i = EasyMock.createStrictMock(Iterator.class);
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
		final ShareableIterator<Object> si = new ShareableIterator<>(rwl, i);

		EasyMock.reset(rl, wl, i);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(i.hasNext()).andReturn(true).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, i);
		Assertions.assertTrue(si.hasNext());
		EasyMock.verify(rl, wl, i);

		EasyMock.reset(rl, wl, i);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(i.hasNext()).andReturn(false).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, i);
		Assertions.assertFalse(si.hasNext());
		EasyMock.verify(rl, wl, i);
	}

	@Test
	public void testNext() {
		final Iterator<Object> i = EasyMock.createStrictMock(Iterator.class);
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
		final ShareableIterator<Object> si = new ShareableIterator<>(rwl, i);
		final Object o = new Object();

		EasyMock.reset(rl, wl, i);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(i.next()).andReturn(o).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, i);
		Assertions.assertSame(o, si.next());
		EasyMock.verify(rl, wl, i);
	}

	@Test
	public void testRemove() {
		final Iterator<Object> i = EasyMock.createStrictMock(Iterator.class);
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
		final ShareableIterator<Object> si = new ShareableIterator<>(rwl, i);

		EasyMock.reset(rl, wl, i);
		wl.lock();
		EasyMock.expectLastCall().once();
		i.remove();
		EasyMock.expectLastCall().once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, i);
		si.remove();
		EasyMock.verify(rl, wl, i);
	}

	@Test
	public void testForEachRemaining() {
		final Iterator<Object> i = EasyMock.createStrictMock(Iterator.class);
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
		final ShareableIterator<Object> si = new ShareableIterator<>(rwl, i);
		final Consumer<Object> c = EasyMock.createStrictMock(Consumer.class);

		Assertions.assertThrows(NullPointerException.class, () -> si.forEachRemaining(null));

		EasyMock.reset(rl, wl, i, c);
		rl.lock();
		EasyMock.expectLastCall().once();
		i.forEachRemaining(EasyMock.same(c));
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, i, c);
		si.forEachRemaining(c);
		EasyMock.verify(rl, wl, i, c);
	}
}