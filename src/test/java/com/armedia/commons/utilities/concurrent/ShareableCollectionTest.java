/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2019 Armedia, LLC
 * %%
 * This file is part of the Caliente software.
 * 
 * If the software was purchased under a paid Caliente license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * 
 * Caliente is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Caliente is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Caliente. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 *******************************************************************************/
package com.armedia.commons.utilities.concurrent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
		final Collection<Object> l = EasyMock.createStrictMock(Collection.class);
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
		final ShareableCollection<Object> sc = new ShareableCollection<>(rwl, l);
		final Consumer<Object> c = (s) -> {
		};
		EasyMock.reset(rl, wl, l);
		rl.lock();
		EasyMock.expectLastCall().once();
		l.forEach(EasyMock.same(c));
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l);
		sc.forEach(c);
		EasyMock.verify(rl, wl, l);
	}

	@Test
	public void testSize() {
		final Collection<Object> l = EasyMock.createStrictMock(Collection.class);
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
		final ShareableCollection<Object> sc = new ShareableCollection<>(rwl, l);

		for (int i = 0; i < 100; i++) {
			EasyMock.reset(rl, wl, l);
			rl.lock();
			EasyMock.expectLastCall().once();
			EasyMock.expect(l.size()).andReturn(i).once();
			rl.unlock();
			EasyMock.expectLastCall().once();
			EasyMock.replay(rl, wl, l);
			Assertions.assertEquals(i, sc.size());
			EasyMock.verify(rl, wl, l);
		}
	}

	@Test
	public void testIsEmpty() {
		final Collection<Object> l = EasyMock.createStrictMock(Collection.class);
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
		final ShareableCollection<Object> sc = new ShareableCollection<>(rwl, l);

		EasyMock.reset(rl, wl, l);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.isEmpty()).andReturn(true).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l);
		Assertions.assertTrue(sc.isEmpty());
		EasyMock.verify(rl, wl, l);

		EasyMock.reset(rl, wl, l);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.isEmpty()).andReturn(false).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l);
		Assertions.assertFalse(sc.isEmpty());
		EasyMock.verify(rl, wl, l);
	}

	@Test
	public void testContains() {
		final Collection<Object> l = EasyMock.createStrictMock(Collection.class);
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
		final ShareableCollection<Object> sc = new ShareableCollection<>(rwl, l);

		final Object o = new Object();
		EasyMock.reset(rl, wl, l);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.contains(EasyMock.same(o))).andReturn(true).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l);
		Assertions.assertTrue(sc.contains(o));
		EasyMock.verify(rl, wl, l);

		EasyMock.reset(rl, wl, l);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.contains(EasyMock.same(o))).andReturn(false).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l);
		Assertions.assertFalse(sc.contains(o));
		EasyMock.verify(rl, wl, l);
	}

	@Test
	public void testIterator() {
		final Collection<Object> l = EasyMock.createStrictMock(Collection.class);
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
		final ShareableCollection<Object> sc = new ShareableCollection<>(rwl, l);
		final Iterator<Object> o = EasyMock.createStrictMock(Iterator.class);

		EasyMock.reset(rl, wl, l, o);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.iterator()).andReturn(o).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l, o);
		Iterator<Object> it = sc.iterator();
		EasyMock.verify(rl, wl, l, o);

		Assertions.assertTrue(ShareableIterator.class.isInstance(it));
		ShareableIterator<?> sit = ShareableIterator.class.cast(it);
		Assertions.assertSame(rwl, sit.getShareableLock());

		EasyMock.reset(rl, wl, l, o);
		rl.lock();
		EasyMock.expect(o.hasNext()).andReturn(true).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l, o);
		Assertions.assertTrue(sit.hasNext());
		EasyMock.verify(rl, wl, l, o);
	}

	@Test
	public void testToArray() {
		final Collection<Object> l = EasyMock.createStrictMock(Collection.class);
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
		final ShareableCollection<Object> sc = new ShareableCollection<>(rwl, l);

		final Object[] o = {};

		EasyMock.reset(rl, wl, l);
		rl.lock();
		EasyMock.expect(l.toArray()).andReturn(o);
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l);
		Assertions.assertSame(o, sc.toArray());
		EasyMock.verify(rl, wl, l);
	}

	@Test
	public void testToArrayTArray() {
		final Collection<Object> l = EasyMock.createStrictMock(Collection.class);
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
		final ShareableCollection<Object> sc = new ShareableCollection<>(rwl, l);
		final Object[] o = {};
		final Object[] p = {};

		Assertions.assertThrows(NullPointerException.class, () -> sc.toArray((Object[]) null));

		EasyMock.reset(rl, wl, l);
		rl.lock();
		EasyMock.expect(l.toArray(EasyMock.same(o))).andReturn(p).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l);
		Assertions.assertSame(p, sc.toArray(o));
		EasyMock.verify(rl, wl, l);
	}

	@Test
	public void testAdd() {
		final Collection<Object> l = EasyMock.createStrictMock(Collection.class);
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
		final ShareableCollection<Object> sc = new ShareableCollection<>(rwl, l);
		final Object o = new Object();

		EasyMock.reset(rl, wl, l);
		wl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.add(EasyMock.same(o))).andReturn(true).once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l);
		Assertions.assertTrue(sc.add(o));
		EasyMock.verify(rl, wl, l);

		EasyMock.reset(rl, wl, l);
		wl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.add(EasyMock.same(o))).andReturn(false).once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l);
		Assertions.assertFalse(sc.add(o));
		EasyMock.verify(rl, wl, l);
	}

	@Test
	public void testRemove() {
		final Collection<Object> l = EasyMock.createStrictMock(Collection.class);
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
		final ShareableCollection<Object> sc = new ShareableCollection<>(rwl, l);
		final Object o = new Object();

		EasyMock.reset(rl, wl, l);
		wl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.remove(EasyMock.same(o))).andReturn(true).once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l);
		Assertions.assertTrue(sc.remove(o));
		EasyMock.verify(rl, wl, l);

		EasyMock.reset(rl, wl, l);
		wl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.remove(EasyMock.same(o))).andReturn(false).once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l);
		Assertions.assertFalse(sc.remove(o));
		EasyMock.verify(rl, wl, l);
	}

	@Test
	public void testContainsAll() {
		final Collection<Object> l = EasyMock.createStrictMock(Collection.class);
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
		final ShareableCollection<Object> sc = new ShareableCollection<>(rwl, l);
		final Collection<Object> o = EasyMock.createStrictMock(Collection.class);

		EasyMock.reset(rl, wl, l, o);
		EasyMock.expect(o.isEmpty()).andReturn(true).once();
		EasyMock.replay(rl, wl, l, o);
		Assertions.assertTrue(sc.containsAll(o));
		EasyMock.verify(rl, wl, l, o);

		EasyMock.reset(rl, wl, l, o);
		EasyMock.expect(o.isEmpty()).andReturn(false).once();
		rl.lock();
		EasyMock.expect(l.containsAll(EasyMock.same(o))).andReturn(true).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l, o);
		Assertions.assertTrue(sc.containsAll(o));
		EasyMock.verify(rl, wl, l, o);

		EasyMock.reset(rl, wl, l, o);
		EasyMock.expect(o.isEmpty()).andReturn(false).once();
		rl.lock();
		EasyMock.expect(l.containsAll(EasyMock.same(o))).andReturn(false).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l, o);
		Assertions.assertFalse(sc.containsAll(o));
		EasyMock.verify(rl, wl, l, o);
	}

	@Test
	public void testAddAll() {
		final Collection<Object> l = EasyMock.createStrictMock(Collection.class);
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
		final ShareableCollection<Object> sc = new ShareableCollection<>(rwl, l);
		final Collection<Object> o = EasyMock.createStrictMock(Collection.class);

		EasyMock.reset(rl, wl, l, o);
		EasyMock.expect(o.isEmpty()).andReturn(true).once();
		EasyMock.replay(rl, wl, l, o);
		Assertions.assertFalse(sc.addAll(o));
		EasyMock.verify(rl, wl, l, o);

		EasyMock.reset(rl, wl, l, o);
		EasyMock.expect(o.isEmpty()).andReturn(false).once();
		wl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.addAll(EasyMock.same(o))).andReturn(true).once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l, o);
		Assertions.assertTrue(sc.addAll(o));
		EasyMock.verify(rl, wl, l, o);

		EasyMock.reset(rl, wl, l, o);
		EasyMock.expect(o.isEmpty()).andReturn(false).once();
		wl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.addAll(EasyMock.same(o))).andReturn(false).once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l, o);
		Assertions.assertFalse(sc.addAll(o));
		EasyMock.verify(rl, wl, l, o);
	}

	@Test
	public void testRetainAll() {
		final Collection<Object> l = EasyMock.createStrictMock(Collection.class);
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
		final ShareableCollection<Object> sc = new ShareableCollection<>(rwl, l);
		final Collection<Object> o = EasyMock.createStrictMock(Collection.class);

		EasyMock.reset(rl, wl, l, o);
		EasyMock.expect(o.isEmpty()).andReturn(true).once();
		EasyMock.replay(rl, wl, l, o);
		Assertions.assertFalse(sc.retainAll(o));
		EasyMock.verify(rl, wl, l, o);

		EasyMock.reset(rl, wl, l, o);
		EasyMock.expect(o.isEmpty()).andReturn(false).once();
		wl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.retainAll(EasyMock.same(o))).andReturn(true).once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l, o);
		Assertions.assertTrue(sc.retainAll(o));
		EasyMock.verify(rl, wl, l, o);

		EasyMock.reset(rl, wl, l, o);
		EasyMock.expect(o.isEmpty()).andReturn(false).once();
		wl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.retainAll(EasyMock.same(o))).andReturn(false).once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l, o);
		Assertions.assertFalse(sc.retainAll(o));
		EasyMock.verify(rl, wl, l, o);
	}

	@Test
	public void testRemoveAll() {
		final Collection<Object> l = EasyMock.createStrictMock(Collection.class);
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
		final ShareableCollection<Object> sc = new ShareableCollection<>(rwl, l);
		final Collection<Object> o = EasyMock.createStrictMock(Collection.class);

		EasyMock.reset(rl, wl, l, o);
		EasyMock.expect(o.isEmpty()).andReturn(true).once();
		EasyMock.replay(rl, wl, l, o);
		Assertions.assertFalse(sc.removeAll(o));
		EasyMock.verify(rl, wl, l, o);

		EasyMock.reset(rl, wl, l, o);
		EasyMock.expect(o.isEmpty()).andReturn(false).once();
		wl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.removeAll(EasyMock.same(o))).andReturn(true).once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l, o);
		Assertions.assertTrue(sc.removeAll(o));
		EasyMock.verify(rl, wl, l, o);

		EasyMock.reset(rl, wl, l, o);
		EasyMock.expect(o.isEmpty()).andReturn(false).once();
		wl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.removeAll(EasyMock.same(o))).andReturn(false).once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l, o);
		Assertions.assertFalse(sc.removeAll(o));
		EasyMock.verify(rl, wl, l, o);
	}

	@Test
	public void testClear() {
		final Collection<Object> l = EasyMock.createStrictMock(Collection.class);
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
		final ShareableCollection<Object> sc = new ShareableCollection<>(rwl, l);

		EasyMock.reset(rl, wl, l);
		wl.lock();
		EasyMock.expectLastCall().once();
		l.clear();
		EasyMock.expectLastCall().once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l);
		sc.clear();
		EasyMock.verify(rl, wl, l);
	}

	@Test
	public void testEquals() {
		final Collection<Object> l = EasyMock.createStrictMock(Collection.class);
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
		final ShareableCollection<Object> sc = new ShareableCollection<>(rwl, l);
		final Collection<Object> o = EasyMock.createStrictMock(Collection.class);

		Assertions.assertFalse(sc.equals(null));
		Assertions.assertTrue(sc.equals(sc));
		Assertions.assertFalse(sc.equals(new Object()));

		EasyMock.reset(rl, wl, l, o);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.size()).andReturn(1).once();
		EasyMock.expect(o.size()).andReturn(2).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l, o);
		Assertions.assertFalse(sc.equals(o));
		EasyMock.verify(rl, wl, l, o);

		EasyMock.reset(rl, wl, l, o);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.size()).andReturn(1).once();
		EasyMock.expect(o.size()).andReturn(1).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l, o);
		Assertions.assertFalse(sc.equals(o));
		EasyMock.verify(rl, wl, l, o);
	}

	@Test
	public void testHashCode() {
		final Collection<Object> l = EasyMock.createStrictMock(Collection.class);
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
		final ShareableCollection<Object> sc = new ShareableCollection<>(rwl, l);

		final int hc = Tools.hashTool(sc, null, l);
		EasyMock.reset(rl, wl, l);
		rl.lock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l);
		Assertions.assertEquals(hc, sc.hashCode());
		EasyMock.verify(rl, wl, l);
	}

	@Test
	public void testRemoveIf() {
		final Collection<Object> l = EasyMock.createStrictMock(Collection.class);
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
		final ShareableCollection<Object> sc = new ShareableCollection<>(rwl, l);
		final Predicate<Object> p = EasyMock.createStrictMock(Predicate.class);
		final Iterator<Object> it = EasyMock.createStrictMock(Iterator.class);

		Object o = null;

		EasyMock.reset(rl, wl, l, it, p);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.iterator()).andReturn(it).once();
		o = new Object();
		EasyMock.expect(it.hasNext()).andReturn(true).once();
		EasyMock.expect(it.next()).andReturn(o).once();
		EasyMock.expect(p.test(EasyMock.same(o))).andReturn(false).once();
		EasyMock.expect(it.hasNext()).andReturn(false).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l, it, p);
		Assertions.assertFalse(sc.removeIf(p));
		EasyMock.verify(rl, wl, l, it, p);

		EasyMock.reset(rl, wl, l, it, p);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(l.iterator()).andReturn(it).once();
		o = new Object();
		EasyMock.expect(it.hasNext()).andReturn(true).once();
		EasyMock.expect(it.next()).andReturn(o).once();
		EasyMock.expect(p.test(EasyMock.same(o))).andReturn(false).once();
		o = new Object();
		EasyMock.expect(it.hasNext()).andReturn(true).once();
		EasyMock.expect(it.next()).andReturn(o).once();
		EasyMock.expect(p.test(EasyMock.same(o))).andReturn(true).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		wl.lock();
		EasyMock.expectLastCall().once();
		it.remove();
		EasyMock.expectLastCall().once();
		o = new Object();
		EasyMock.expect(it.hasNext()).andReturn(true).once();
		EasyMock.expect(it.next()).andReturn(o).once();
		EasyMock.expect(p.test(EasyMock.same(o))).andReturn(true).once();
		it.remove();
		EasyMock.expectLastCall().once();
		EasyMock.expect(it.hasNext()).andReturn(false).once();
		rl.lock();
		EasyMock.expectLastCall().once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l, it, p);
		Assertions.assertTrue(sc.removeIf(p));
		EasyMock.verify(rl, wl, l, it, p);
	}

	@Test
	public void testSpliterator() {
		final Collection<Object> l = EasyMock.createStrictMock(Collection.class);
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
		final ShareableCollection<Object> sc = new ShareableCollection<>(rwl, l);
		final Spliterator<Object> o = EasyMock.createStrictMock(Spliterator.class);

		EasyMock.reset(rl, wl, l, o);
		rl.lock();
		EasyMock.expect(l.spliterator()).andReturn(o).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l, o);
		Spliterator<Object> it = sc.spliterator();
		EasyMock.verify(rl, wl, l, o);

		Assertions.assertTrue(ShareableSpliterator.class.isInstance(it));
		ShareableSpliterator<?> sit = ShareableSpliterator.class.cast(it);
		Assertions.assertSame(rwl, sit.getShareableLock());

		EasyMock.reset(rl, wl, l, o);
		rl.lock();
		EasyMock.expect(o.getExactSizeIfKnown()).andReturn(-1L).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, l, o);
		Assertions.assertEquals(-1L, sit.getExactSizeIfKnown());
		EasyMock.verify(rl, wl, l, o);

	}
}
