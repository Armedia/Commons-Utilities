package com.armedia.commons.utilities.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ShareableListTest {

	@Test
	public void testConstructors() {
		ReadWriteLock rwl = null;
		ShareableLockable sl = null;
		List<Object> c = null;

		Assertions.assertThrows(NullPointerException.class, () -> new ShareableList<>(c));
		new ShareableList<>(new ArrayList<>());
		new ShareableList<>(new ShareableList<>(new ArrayList<>()));

		Assertions.assertThrows(NullPointerException.class, () -> new ShareableList<>(rwl, c));
		new ShareableList<>(rwl, new ArrayList<>());
		Assertions.assertThrows(NullPointerException.class, () -> new ShareableList<>(new ReentrantReadWriteLock(), c));

		Assertions.assertThrows(NullPointerException.class, () -> new ShareableList<>(sl, c));
		Assertions.assertThrows(NullPointerException.class, () -> new ShareableList<>(sl, new ArrayList<>()));
		Assertions.assertThrows(NullPointerException.class, () -> new ShareableList<>(new BaseShareableLockable(), c));
		{
			ReadWriteLock l = new ReentrantReadWriteLock();
			ShareableLockable s = new BaseShareableLockable();
			Assertions.assertSame(l, new ShareableList<>(l, new ArrayList<>()).getShareableLock());
			Assertions.assertSame(s.getShareableLock(), new ShareableList<>(s, new ArrayList<>()).getShareableLock());
		}
	}

	@Test
	public void testAddAll() {
		final List<Object> l = EasyMock.createStrictMock(List.class);
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
		final ShareableList<Object> sl = new ShareableList<>(rwl, l);
		final Collection<Object> c = EasyMock.createStrictMock(Collection.class);

		EasyMock.reset(rl, wl, l, c);
		EasyMock.expect(c.isEmpty()).andReturn(true).once();
		EasyMock.replay(rl, wl, l, c);
		Assertions.assertFalse(sl.addAll(c));
		EasyMock.verify(rl, wl, l, c);

		EasyMock.reset(rl, wl, l, c);
		EasyMock.expect(c.isEmpty()).andReturn(false).once();
		wl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.addAll(EasyMock.same(c))).andReturn(true).once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l, c);
		Assertions.assertTrue(sl.addAll(c));
		EasyMock.verify(rl, wl, l, c);

		EasyMock.reset(rl, wl, l, c);
		EasyMock.expect(c.isEmpty()).andReturn(false).once();
		wl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.addAll(EasyMock.same(c))).andReturn(false).once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l, c);
		Assertions.assertFalse(sl.addAll(c));
		EasyMock.verify(rl, wl, l, c);

		for (int i = 0; i < 100; i++) {

			EasyMock.reset(rl, wl, l, c);
			EasyMock.expect(c.isEmpty()).andReturn(true).once();
			EasyMock.replay(rl, wl, l, c);
			Assertions.assertFalse(sl.addAll(i, c));
			EasyMock.verify(rl, wl, l, c);

			EasyMock.reset(rl, wl, l, c);
			EasyMock.expect(c.isEmpty()).andReturn(false).once();
			wl.lock();
			EasyMock.expectLastCall().once();
			EasyMock.expect(l.addAll(EasyMock.eq(i), EasyMock.same(c))).andReturn(true).once();
			wl.unlock();
			EasyMock.expectLastCall().once();
			EasyMock.replay(rl, wl, l, c);
			Assertions.assertTrue(sl.addAll(i, c));
			EasyMock.verify(rl, wl, l, c);

			EasyMock.reset(rl, wl, l, c);
			EasyMock.expect(c.isEmpty()).andReturn(false).once();
			wl.lock();
			EasyMock.expectLastCall().once();
			EasyMock.expect(l.addAll(EasyMock.eq(i), EasyMock.same(c))).andReturn(false).once();
			wl.unlock();
			EasyMock.expectLastCall().once();
			EasyMock.replay(rl, wl, l, c);
			Assertions.assertFalse(sl.addAll(i, c));
			EasyMock.verify(rl, wl, l, c);
		}
	}

	@Test
	public void testGet() {
		final List<Object> l = EasyMock.createStrictMock(List.class);
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
		final ShareableList<Object> sl = new ShareableList<>(rwl, l);

		for (int i = 0; i < 100; i++) {
			final Object o = new Object();
			EasyMock.reset(rl, wl, l);
			rl.lock();
			EasyMock.expectLastCall().once();
			EasyMock.expect(l.get(EasyMock.eq(i))).andReturn(o).once();
			rl.unlock();
			EasyMock.expectLastCall().once();
			EasyMock.replay(rl, wl, l);
			Assertions.assertSame(o, sl.get(i));
			EasyMock.verify(rl, wl, l);
		}
	}

	@Test
	public void testSet() {
		final List<Object> l = EasyMock.createStrictMock(List.class);
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
		final ShareableList<Object> sl = new ShareableList<>(rwl, l);

		for (int i = 0; i < 100; i++) {
			final Object o = new Object();
			final Object p = new Object();
			EasyMock.reset(rl, wl, l);
			wl.lock();
			EasyMock.expectLastCall().once();
			EasyMock.expect(l.set(EasyMock.eq(i), EasyMock.same(o))).andReturn(p).once();
			wl.unlock();
			EasyMock.expectLastCall().once();
			EasyMock.replay(rl, wl, l);
			Assertions.assertSame(p, sl.set(i, o));
			EasyMock.verify(rl, wl, l);
		}
	}

	@Test
	public void testAdd() {
		final List<Object> l = EasyMock.createStrictMock(List.class);
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
		final ShareableList<Object> sl = new ShareableList<>(rwl, l);

		for (int i = 0; i < 100; i++) {
			final Object o = new Object();
			EasyMock.reset(rl, wl, l);
			wl.lock();
			EasyMock.expectLastCall().once();
			l.add(EasyMock.eq(i), EasyMock.same(o));
			EasyMock.expectLastCall().once();
			wl.unlock();
			EasyMock.expectLastCall().once();
			EasyMock.replay(rl, wl, l);
			sl.add(i, o);
			EasyMock.verify(rl, wl, l);
		}
	}

	@Test
	public void testRemove() {
		final List<Object> l = EasyMock.createStrictMock(List.class);
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
		final ShareableList<Object> sl = new ShareableList<>(rwl, l);

		for (int i = 0; i < 100; i++) {
			final Object o = new Object();
			EasyMock.reset(rl, wl, l);
			wl.lock();
			EasyMock.expectLastCall().once();
			EasyMock.expect(l.remove(EasyMock.eq(i))).andReturn(o).once();
			wl.unlock();
			EasyMock.expectLastCall().once();
			EasyMock.replay(rl, wl, l);
			Assertions.assertSame(o, sl.remove(i));
			EasyMock.verify(rl, wl, l);
		}
	}

	@Test
	public void testIndexOf() {
		final List<Object> l = EasyMock.createStrictMock(List.class);
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
		final ShareableList<Object> sl = new ShareableList<>(rwl, l);

		for (int i = 0; i < 100; i++) {
			final Object o = new Object();
			EasyMock.reset(rl, wl, l);
			rl.lock();
			EasyMock.expectLastCall().once();
			EasyMock.expect(l.indexOf(EasyMock.same(o))).andReturn(i).once();
			rl.unlock();
			EasyMock.expectLastCall().once();
			EasyMock.replay(rl, wl, l);
			Assertions.assertEquals(i, sl.indexOf(o));
			EasyMock.verify(rl, wl, l);
		}
	}

	@Test
	public void testLastIndexOf() {
		final List<Object> l = EasyMock.createStrictMock(List.class);
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
		final ShareableList<Object> sl = new ShareableList<>(rwl, l);

		for (int i = 0; i < 100; i++) {
			final Object o = new Object();
			EasyMock.reset(rl, wl, l);
			rl.lock();
			EasyMock.expectLastCall().once();
			EasyMock.expect(l.lastIndexOf(EasyMock.same(o))).andReturn(i).once();
			rl.unlock();
			EasyMock.expectLastCall().once();
			EasyMock.replay(rl, wl, l);
			Assertions.assertEquals(i, sl.lastIndexOf(o));
			EasyMock.verify(rl, wl, l);
		}
	}

	@Test
	public void testListIterator() {
		final List<Object> l = EasyMock.createStrictMock(List.class);
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
		final ShareableList<Object> sl = new ShareableList<>(rwl, l);
		final ListIterator<Object> li = EasyMock.createStrictMock(ListIterator.class);

		EasyMock.reset(rl, wl, l, li);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.listIterator()).andReturn(li).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(li.hasNext()).andReturn(false).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l, li);
		ListIterator<Object> it = sl.listIterator();
		Assertions.assertFalse(it.hasNext());
		EasyMock.verify(rl, wl, l, li);

		Assertions.assertTrue(ShareableListIterator.class.isInstance(it));
		ShareableListIterator<?> sit = ShareableListIterator.class.cast(it);
		Assertions.assertSame(rwl, sit.getShareableLock());

		for (int i = 0; i < 10; i++) {
			EasyMock.reset(rl, wl, l, li);
			rl.lock();
			EasyMock.expectLastCall().once();
			EasyMock.expect(l.listIterator(EasyMock.eq(i))).andReturn(li).once();
			rl.unlock();
			EasyMock.expectLastCall().once();
			rl.lock();
			EasyMock.expectLastCall().once();
			EasyMock.expect(li.hasNext()).andReturn(false).once();
			rl.unlock();
			EasyMock.expectLastCall().once();
			EasyMock.replay(rl, wl, l, li);
			it = sl.listIterator(i);
			Assertions.assertFalse(it.hasNext());
			EasyMock.verify(rl, wl, l, li);

			Assertions.assertTrue(ShareableListIterator.class.isInstance(it));
			sit = ShareableListIterator.class.cast(it);
			Assertions.assertSame(rwl, sit.getShareableLock());
		}
	}

	@Test
	public void testSubList() {
		final List<Object> l = EasyMock.createStrictMock(List.class);
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
		final ShareableList<Object> sl = new ShareableList<>(rwl, l);
		final List<Object> s = EasyMock.createStrictMock(List.class);

		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				EasyMock.reset(rl, wl, l, s);
				rl.lock();
				EasyMock.expectLastCall().once();
				EasyMock.expect(l.subList(EasyMock.eq(i), EasyMock.eq(j))).andReturn(s).once();
				rl.unlock();
				EasyMock.expectLastCall().once();
				rl.lock();
				EasyMock.expectLastCall().once();
				EasyMock.expect(s.size()).andReturn((i * 10) + j);
				rl.unlock();
				EasyMock.expectLastCall().once();
				EasyMock.replay(rl, wl, l, s);
				List<Object> it = sl.subList(i, j);
				Assertions.assertEquals((i * 10) + j, it.size());
				EasyMock.verify(rl, wl, l, s);

				Assertions.assertTrue(ShareableList.class.isInstance(it));
				ShareableList<?> sit = ShareableList.class.cast(it);
				Assertions.assertSame(rwl, sit.getShareableLock());
			}
		}
	}
}