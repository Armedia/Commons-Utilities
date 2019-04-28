package com.armedia.commons.utilities.concurrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.Tools;

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
		for (int i = 0; i < 10; i++) {
			l.add(String.format("%02d", i));
		}

		Object[] expected = l.toArray();
		Object[] actual = null;

		EasyMock.reset(rl, wl);
		rl.lock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		c = new ShareableCollection<>(rwl, l);
		actual = c.toArray();
		EasyMock.verify(rl, wl);
		Assertions.assertArrayEquals(expected, actual);
	}

	@Test
	public void testToArrayTArray() {
		final String[] ARR = {};
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
		for (int i = 0; i < 10; i++) {
			l.add(String.format("%02d", i));
		}

		String[] expected = l.toArray(ARR);
		String[] actual = null;

		EasyMock.reset(rl, wl);
		rl.lock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		c = new ShareableCollection<>(rwl, l);
		actual = c.toArray(ARR);
		EasyMock.verify(rl, wl);
		Assertions.assertArrayEquals(expected, actual);
	}

	@Test
	public void testAdd() {
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

		Assertions.assertTrue(l.isEmpty());
		EasyMock.reset(rl, wl);
		wl.lock();
		EasyMock.expectLastCall().once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		c = new ShareableCollection<>(rwl, l);
		Assertions.assertTrue(c.add("a"));
		EasyMock.verify(rl, wl);
		Assertions.assertEquals(1, l.size());
		Assertions.assertEquals("a", l.iterator().next());
	}

	@Test
	public void testRemove() {
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

		Assertions.assertEquals(1, l.size());
		Assertions.assertEquals("a", l.iterator().next());
		EasyMock.reset(rl, wl);
		wl.lock();
		EasyMock.expectLastCall().once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		c = new ShareableCollection<>(rwl, l);
		Assertions.assertTrue(c.remove("a"));
		EasyMock.verify(rl, wl);
		Assertions.assertTrue(l.isEmpty());
	}

	@Test
	public void testContainsAll() {
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
		for (int i = 0; i < 10; i++) {
			l.add(String.format("%02d", i));
		}

		EasyMock.reset(rl, wl);
		rl.lock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		c = new ShareableCollection<>(rwl, l);
		Assertions.assertTrue(c.containsAll(l));
		EasyMock.verify(rl, wl);
		Assertions.assertTrue(c.containsAll(new ArrayList<>()));
	}

	@Test
	public void testAddAll() {
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
		for (int i = 0; i < 10; i++) {
			l.add(String.format("%02d", i));
		}

		EasyMock.reset(rl, wl);
		wl.lock();
		EasyMock.expectLastCall().once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		c = new ShareableCollection<>(rwl, new ArrayList<>());
		Assertions.assertTrue(c.addAll(l));
		EasyMock.verify(rl, wl);

		EasyMock.reset(rl, wl);
		rl.lock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		Assertions.assertEquals(l.size(), c.size());
		EasyMock.verify(rl, wl);

		EasyMock.reset(rl, wl);
		rl.lock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		Assertions.assertTrue(c.containsAll(l));
		EasyMock.verify(rl, wl);

		Assertions.assertFalse(c.addAll(new ArrayList<>()));
	}

	@Test
	public void testRetainAll() {
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
		for (int i = 0; i < 10; i++) {
			l.add(String.format("%02d", i));
		}
		Collection<String> L = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			L.add(String.format("%02d", i));
			L.add(String.format("%02d-b", i));
		}

		EasyMock.reset(rl, wl);
		wl.lock();
		EasyMock.expectLastCall().once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		c = new ShareableCollection<>(rwl, L);
		Assertions.assertTrue(c.retainAll(l));
		EasyMock.verify(rl, wl);

		EasyMock.reset(rl, wl);
		rl.lock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		Assertions.assertEquals(l.size(), c.size());
		EasyMock.verify(rl, wl);

		EasyMock.reset(rl, wl);
		rl.lock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		Assertions.assertTrue(c.containsAll(l));
		EasyMock.verify(rl, wl);

		Assertions.assertFalse(c.retainAll(new ArrayList<>()));
	}

	@Test
	public void testRemoveAll() {
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
		Collection<String> l2 = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			l.add(String.format("%02d", i));
			l2.add(String.format("%02d-b", i));
		}
		Collection<String> L = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			L.add(String.format("%02d", i));
			L.add(String.format("%02d-b", i));
		}

		EasyMock.reset(rl, wl);
		wl.lock();
		EasyMock.expectLastCall().once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		c = new ShareableCollection<>(rwl, L);
		Assertions.assertTrue(c.removeAll(l2));
		EasyMock.verify(rl, wl);

		EasyMock.reset(rl, wl);
		rl.lock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		Assertions.assertEquals(l.size(), c.size());
		EasyMock.verify(rl, wl);

		EasyMock.reset(rl, wl);
		rl.lock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		Assertions.assertTrue(c.containsAll(l));
		EasyMock.verify(rl, wl);

		Assertions.assertFalse(c.removeAll(new ArrayList<>()));
	}

	@Test
	public void testClear() {
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
		for (int i = 0; i < 10; i++) {
			l.add(String.format("%02d", i));
		}

		Assertions.assertFalse(l.isEmpty());

		EasyMock.reset(rl, wl);
		rl.lock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		c = new ShareableCollection<>(rwl, l);
		Assertions.assertFalse(c.isEmpty());
		EasyMock.verify(rl, wl);

		EasyMock.reset(rl, wl);
		wl.lock();
		EasyMock.expectLastCall().once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		c.clear();
		EasyMock.verify(rl, wl);

		EasyMock.reset(rl, wl);
		rl.lock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		Assertions.assertTrue(c.isEmpty());
		EasyMock.verify(rl, wl);
	}

	@Test
	public void testEquals() {
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
		for (int i = 0; i < 10; i++) {
			l.add(String.format("%02d", i));

			c = new ShareableCollection<>(rwl, new ArrayList<>(l));

			Assertions.assertFalse(c.equals(null));
			Assertions.assertFalse(c.equals(new Object()));
			Assertions.assertTrue(c.equals(c));

			EasyMock.reset(rl, wl);
			rl.lock();
			EasyMock.expectLastCall().once();
			rl.unlock();
			EasyMock.expectLastCall().once();
			EasyMock.replay(rl, wl);
			Object o = c;
			Assertions.assertFalse(o.equals(new ArrayList<>()));
			EasyMock.verify(rl, wl);

			EasyMock.reset(rl, wl);
			rl.lock();
			EasyMock.expectLastCall().once();
			rl.unlock();
			EasyMock.expectLastCall().once();
			EasyMock.replay(rl, wl);
			Assertions.assertTrue(c.equals(l));
			EasyMock.verify(rl, wl);
		}
	}

	@Test
	public void testHashCode() {
		// Temporarily hobbled - there seems to be a bug in EasyMock when processing hashCode()
		final Collection<Integer> l = new ArrayList<>();
		ShareableCollection<Integer> c = new ShareableCollection<>(l);
		for (int i = 0; i < 10; i++) {
			l.add(i);
			int cHash = Tools.hashTool(c, null, l);
			Assertions.assertEquals(cHash, c.hashCode());
		}

		/*
		final Collection<String> l = EasyMock.createStrictMock(Collection.class);
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

		c = new ShareableCollection<>(rwl, l);

		EasyMock.reset(rl, wl, l);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.hashCode()).andReturn(123).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l);
		c.hashCode();
		EasyMock.verify(rl, wl, l);
		*/
	}

	@Test
	public void testRemoveIf() {
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
		ShareableCollection<Integer> c = null;
		Collection<Integer> l = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			l.add(i);
		}

		c = new ShareableCollection<>(rwl, l);

		EasyMock.reset(rl, wl);
		rl.lock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		Assertions.assertFalse(c.removeIf(Objects::isNull));
		EasyMock.verify(rl, wl);

		EasyMock.reset(rl, wl);
		rl.lock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		wl.lock();
		EasyMock.expectLastCall().once();
		rl.lock();
		EasyMock.expectLastCall().once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl);
		Assertions.assertTrue(c.removeIf((i) -> (i.intValue() % 3) == 2));
		EasyMock.verify(rl, wl);
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