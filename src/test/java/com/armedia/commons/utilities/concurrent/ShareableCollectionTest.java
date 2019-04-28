package com.armedia.commons.utilities.concurrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
		final Collection<String> l = Arrays.asList("a", "b", "c");
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
		ShareableCollection<String> c = null;

		EasyMock.reset(rl, wl);
		c = new ShareableCollection<>(rwl, l);
		rl.lock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		c.forEach((e) -> Assertions.assertNotNull(e));
		EasyMock.verify(rl, wl);
	}

	@Test
	public void testSize() {
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
		ShareableCollection<String> c = null;

		for (int i = 1; i < 100; i++) {
			Collection<String> l = new ArrayList<>();
			for (int j = 0; j < i; j++) {
				l.add(String.valueOf(j));
			}

			EasyMock.reset(rl, wl);
			c = new ShareableCollection<>(rwl, l);
			rl.lock();
			EasyMock.expectLastCall().once();
			rl.unlock();
			EasyMock.expectLastCall().once();
			EasyMock.replay(rl, wl);
			Assertions.assertEquals(i, c.size());
			EasyMock.verify(rl, wl);
		}
	}

	@Test
	public void testIsEmpty() {
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
		ShareableCollection<String> c = null;
		Collection<String> l = new ArrayList<>();

		EasyMock.reset(rl, wl);
		c = new ShareableCollection<>(rwl, l);
		rl.lock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		Assertions.assertTrue(c.isEmpty());
		EasyMock.verify(rl, wl);

		EasyMock.reset(rl, wl);
		l.add("a");
		rl.lock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		Assertions.assertFalse(c.isEmpty());
		EasyMock.verify(rl, wl);
	}

	@Test
	public void testContains() {
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
		ShareableCollection<String> c = null;
		Collection<String> l = new ArrayList<>();
		l.add("a");

		EasyMock.reset(rl, wl);
		c = new ShareableCollection<>(rwl, l);
		rl.lock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		Assertions.assertTrue(c.contains("a"));
		EasyMock.verify(rl, wl);

		EasyMock.reset(rl, wl);
		rl.lock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		Assertions.assertFalse(c.contains("b"));
		EasyMock.verify(rl, wl);
	}

	@Test
	public void testIterator() {
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
		ShareableCollection<String> c = null;
		Collection<String> l = new ArrayList<>();
		l.add("a");

		EasyMock.reset(rl, wl);
		rl.lock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		c = new ShareableCollection<>(rwl, l);
		Iterator<String> it = c.iterator();
		EasyMock.verify(rl, wl);

		Assertions.assertTrue(ShareableIterator.class.isInstance(it));
		ShareableIterator<?> sit = ShareableIterator.class.cast(it);
		Assertions.assertSame(rwl, sit.getShareableLock());
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
		ShareableCollection<String> c = null;
		Collection<String> l = new ArrayList<>();
		l.add("a");

		EasyMock.reset(rl, wl);
		rl.lock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		c = new ShareableCollection<>(rwl, l);
		Spliterator<String> it = c.spliterator();
		EasyMock.verify(rl, wl);

		Assertions.assertTrue(ShareableSpliterator.class.isInstance(it));
		ShareableSpliterator<?> sit = ShareableSpliterator.class.cast(it);
		Assertions.assertSame(rwl, sit.getShareableLock());
	}
}