/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2020 Armedia, LLC
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.Tools;

public class ShareableMapTest {

	@Test
	public void testConstructors() {
		ReadWriteLock rwl = null;
		ShareableLockable sl = null;
		Map<Object, Object> m = null;

		Assertions.assertThrows(NullPointerException.class, () -> new ShareableMap<>(m));
		new ShareableMap<>(new HashMap<>());
		new ShareableMap<>(new ShareableMap<>(new HashMap<>()));

		Assertions.assertThrows(NullPointerException.class, () -> new ShareableMap<>(rwl, m));
		new ShareableMap<>(rwl, new HashMap<>());
		Assertions.assertThrows(NullPointerException.class, () -> new ShareableMap<>(new ReentrantReadWriteLock(), m));

		Assertions.assertThrows(NullPointerException.class, () -> new ShareableMap<>(sl, m));
		Assertions.assertThrows(NullPointerException.class, () -> new ShareableMap<>(sl, new HashMap<>()));
		Assertions.assertThrows(NullPointerException.class, () -> new ShareableMap<>(new BaseShareableLockable(), m));
		{
			ReadWriteLock l = new ReentrantReadWriteLock();
			ShareableLockable s = new BaseShareableLockable();
			Assertions.assertSame(l, new ShareableMap<>(l, new HashMap<>()).getShareableLock());
			Assertions.assertSame(s.getShareableLock(), new ShareableMap<>(s, new HashMap<>()).getShareableLock());
		}
	}

	@Test
	public void testSize() {
		final Map<Object, Object> m = EasyMock.createStrictMock(Map.class);
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
		final ShareableMap<Object, Object> sm = new ShareableMap<>(rwl, m);

		for (int i = 0; i < 10; i++) {
			EasyMock.reset(rl, wl, m);
			rl.lock();
			EasyMock.expectLastCall().once();
			EasyMock.expect(m.size()).andReturn(i).once();
			rl.unlock();
			EasyMock.expectLastCall().once();
			EasyMock.replay(rl, wl, m);
			Assertions.assertEquals(i, sm.size());
			EasyMock.verify(rl, wl, m);
		}
	}

	@Test
	public void testIsEmpty() {
		final Map<Object, Object> m = EasyMock.createStrictMock(Map.class);
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
		final ShareableMap<Object, Object> sm = new ShareableMap<>(rwl, m);

		EasyMock.reset(rl, wl, m);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.isEmpty()).andReturn(true).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m);
		Assertions.assertTrue(sm.isEmpty());
		EasyMock.verify(rl, wl, m);

		EasyMock.reset(rl, wl, m);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.isEmpty()).andReturn(false).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m);
		Assertions.assertFalse(sm.isEmpty());
		EasyMock.verify(rl, wl, m);
	}

	@Test
	public void testContainsKey() {
		final Map<Object, Object> m = EasyMock.createStrictMock(Map.class);
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
		final ShareableMap<Object, Object> sm = new ShareableMap<>(rwl, m);

		Object o = new Object();

		EasyMock.reset(rl, wl, m);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.containsKey(EasyMock.same(o))).andReturn(true).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m);
		Assertions.assertTrue(sm.containsKey(o));
		EasyMock.verify(rl, wl, m);

		o = new Object();
		EasyMock.reset(rl, wl, m);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.containsKey(EasyMock.same(o))).andReturn(false).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m);
		Assertions.assertFalse(sm.containsKey(o));
		EasyMock.verify(rl, wl, m);
	}

	@Test
	public void testContainsValue() {
		final Map<Object, Object> m = EasyMock.createStrictMock(Map.class);
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
		final ShareableMap<Object, Object> sm = new ShareableMap<>(rwl, m);

		Object o = new Object();
		EasyMock.reset(rl, wl, m);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.containsValue(EasyMock.same(o))).andReturn(true).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m);
		Assertions.assertTrue(sm.containsValue(o));
		EasyMock.verify(rl, wl, m);

		EasyMock.reset(rl, wl, m);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.containsValue(EasyMock.same(o))).andReturn(false).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m);
		Assertions.assertFalse(sm.containsValue(o));
		EasyMock.verify(rl, wl, m);
	}

	@Test
	public void testGet() {
		final Map<Object, Object> m = EasyMock.createStrictMock(Map.class);
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
		final ShareableMap<Object, Object> sm = new ShareableMap<>(rwl, m);

		Object o = new Object();
		EasyMock.reset(rl, wl, m);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.get(EasyMock.same(o))).andReturn(null).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m);
		Assertions.assertNull(sm.get(o));
		EasyMock.verify(rl, wl, m);
	}

	@Test
	public void testPut() {
		final Map<Object, Object> m = EasyMock.createStrictMock(Map.class);
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
		final ShareableMap<Object, Object> sm = new ShareableMap<>(rwl, m);

		Object o = new Object();
		Object p = new Object();
		EasyMock.reset(rl, wl, m);
		wl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.put(EasyMock.same(o), EasyMock.same(p))).andReturn(null).once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m);
		Assertions.assertNull(sm.put(o, p));
		EasyMock.verify(rl, wl, m);
	}

	@Test
	public void testRemove() {
		final Map<Object, Object> m = EasyMock.createStrictMock(Map.class);
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
		final ShareableMap<Object, Object> sm = new ShareableMap<>(rwl, m);

		Object o = new Object();
		Object p = new Object();
		EasyMock.reset(rl, wl, m);
		wl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.remove(EasyMock.same(o))).andReturn(p).once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m);
		Assertions.assertSame(p, sm.remove(o));
		EasyMock.verify(rl, wl, m);

		EasyMock.reset(rl, wl, m);
		wl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.remove(EasyMock.same(o), EasyMock.same(p))).andReturn(true).once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m);
		Assertions.assertTrue(sm.remove(o, p));
		EasyMock.verify(rl, wl, m);
	}

	@Test
	public void testPutAll() {
		final Map<Object, Object> m = EasyMock.createStrictMock(Map.class);
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
		final ShareableMap<Object, Object> sm = new ShareableMap<>(rwl, m);

		Assertions.assertThrows(NullPointerException.class, () -> sm.putAll(null));

		EasyMock.reset(rl, wl, m);
		wl.lock();
		EasyMock.expectLastCall().once();
		m.putAll(EasyMock.same(m));
		EasyMock.expectLastCall().once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m);
		sm.putAll(m);
		EasyMock.verify(rl, wl, m);
	}

	@Test
	public void testClear() {
		final Map<Object, Object> m = EasyMock.createStrictMock(Map.class);
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
		final ShareableMap<Object, Object> sm = new ShareableMap<>(rwl, m);

		EasyMock.reset(rl, wl, m);
		wl.lock();
		EasyMock.expectLastCall().once();
		m.clear();
		EasyMock.expectLastCall().once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m);
		sm.clear();
		EasyMock.verify(rl, wl, m);
	}

	@Test
	public void testKeySet() {
		final Map<Object, Object> m = EasyMock.createStrictMock(Map.class);
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
		final ShareableMap<Object, Object> sm = new ShareableMap<>(rwl, m);
		final Set<Object> ks = EasyMock.createStrictMock(Set.class);

		EasyMock.reset(rl, wl, m, ks);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.keySet()).andReturn(ks).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m, ks);
		Assertions.assertNotSame(ks, sm.keySet());
		EasyMock.verify(rl, wl, m, ks);
	}

	@Test
	public void testValues() {
		final Map<Object, Object> m = EasyMock.createStrictMock(Map.class);
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
		final ShareableMap<Object, Object> sm = new ShareableMap<>(rwl, m);
		final Collection<Object> v = EasyMock.createStrictMock(Collection.class);

		EasyMock.reset(rl, wl, m, v);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.values()).andReturn(v).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m, v);
		Assertions.assertNotSame(v, sm.values());
		EasyMock.verify(rl, wl, m, v);
	}

	@Test
	public void testEntrySet() {
		final Map<Object, Object> m = EasyMock.createStrictMock(Map.class);
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
		final ShareableMap<Object, Object> sm = new ShareableMap<>(rwl, m);
		final Set<Map.Entry<Object, Object>> es = EasyMock.createStrictMock(Set.class);

		EasyMock.reset(rl, wl, m, es);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.entrySet()).andReturn(es).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m, es);
		Assertions.assertNotSame(es, sm.entrySet());
		EasyMock.verify(rl, wl, m, es);
	}

	@Test
	public void testEquals() {
		final Map<Object, Object> m = EasyMock.createStrictMock(Map.class);
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
		final ShareableMap<Object, Object> sm = new ShareableMap<>(rwl, m);

		Assertions.assertFalse(sm.equals(null));
		Assertions.assertTrue(sm.equals(sm));
		Object o = sm;
		Assertions.assertFalse(o.equals(new Object()));

		EasyMock.reset(rl, wl, m);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.size()).andReturn(1).once();
		EasyMock.expect(m.size()).andReturn(2).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m);
		Assertions.assertFalse(sm.equals(m));
		EasyMock.verify(rl, wl, m);

		EasyMock.reset(rl, wl, m);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.size()).andReturn(1).times(2);
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m);
		Assertions.assertTrue(sm.equals(m));
		EasyMock.verify(rl, wl, m);
	}

	@Test
	public void testHashCode() {
		final Map<Object, Object> m = EasyMock.createStrictMock(Map.class);
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
		final ShareableMap<Object, Object> sm = new ShareableMap<>(rwl, m);

		EasyMock.reset(rl, wl, m);
		rl.lock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m);
		int hc = Tools.hashTool(sm, null, m);
		Assertions.assertEquals(hc, sm.hashCode());
		EasyMock.verify(rl, wl, m);
	}

	@Test
	public void testGetOrDefault() {
		final Map<Object, Object> m = EasyMock.createStrictMock(Map.class);
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
		final ShareableMap<Object, Object> sm = new ShareableMap<>(rwl, m);

		Object o = new Object();
		Object p = new Object();
		EasyMock.reset(rl, wl, m);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.getOrDefault(EasyMock.same(o), EasyMock.same(p))).andReturn(null);
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m);
		Assertions.assertNull(sm.getOrDefault(o, p));
		EasyMock.verify(rl, wl, m);
	}

	@Test
	public void testForEach() {
		final Map<Object, Object> m = EasyMock.createStrictMock(Map.class);
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
		final ShareableMap<Object, Object> sm = new ShareableMap<>(rwl, m);

		Assertions.assertThrows(NullPointerException.class, () -> sm.forEach(null));

		BiConsumer<Object, Object> o = (k, v) -> {
		};
		EasyMock.reset(rl, wl, m);
		rl.lock();
		EasyMock.expectLastCall().once();
		m.forEach(EasyMock.same(o));
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m);
		sm.forEach(o);
		EasyMock.verify(rl, wl, m);
	}

	@Test
	public void testReplaceAll() {
		final Map<Object, Object> m = EasyMock.createStrictMock(Map.class);
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
		final ShareableMap<Object, Object> sm = new ShareableMap<>(rwl, m);

		Assertions.assertThrows(NullPointerException.class, () -> sm.replaceAll(null));

		BiFunction<Object, Object, Object> o = (k, v) -> null;
		EasyMock.reset(rl, wl, m);
		wl.lock();
		EasyMock.expectLastCall().once();
		m.replaceAll(EasyMock.same(o));
		EasyMock.expectLastCall().once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m);
		sm.replaceAll(o);
		EasyMock.verify(rl, wl, m);
	}

	@Test
	public void testPutIfAbsent() {
		final Map<Object, Object> m = EasyMock.createStrictMock(Map.class);
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
		final ShareableMap<Object, Object> sm = new ShareableMap<>(rwl, m);

		Object o = new Object();
		Object p = new Object();
		EasyMock.reset(rl, wl, m);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.get(EasyMock.same(o))).andReturn(null).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		wl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.get(EasyMock.same(o))).andReturn(null).once();
		EasyMock.expect(m.put(EasyMock.same(o), EasyMock.same(p))).andReturn(null).once();
		rl.lock();
		EasyMock.expectLastCall().once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m);
		Assertions.assertNull(sm.putIfAbsent(o, p));
		EasyMock.verify(rl, wl, m);
	}

	@Test
	public void testReplace() {
		final Map<Object, Object> m = EasyMock.createStrictMock(Map.class);
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
		final ShareableMap<Object, Object> sm = new ShareableMap<>(rwl, m);

		Object o = new Object();
		Object p = new Object();
		Object q = new Object();
		EasyMock.reset(rl, wl, m);
		wl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.replace(EasyMock.same(o), EasyMock.same(p))).andReturn(q).once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m);
		Assertions.assertSame(q, sm.replace(o, p));
		EasyMock.verify(rl, wl, m);

		EasyMock.reset(rl, wl, m);
		wl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.replace(EasyMock.same(o), EasyMock.same(p), EasyMock.same(q))).andReturn(true).once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m);
		Assertions.assertTrue(sm.replace(o, p, q));
		EasyMock.verify(rl, wl, m);
	}

	@Test
	public void testComputeIfAbsent() {
		final Map<Object, Object> m = EasyMock.createStrictMock(Map.class);
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
		final ShareableMap<Object, Object> sm = new ShareableMap<>(rwl, m);

		Assertions.assertThrows(NullPointerException.class, () -> sm.computeIfAbsent(null, null));
		Assertions.assertThrows(NullPointerException.class, () -> sm.computeIfAbsent(new Object(), null));

		final Object o = new Object();
		final Object p = new Object();
		final Function<Object, Object> f = EasyMock.createStrictMock(Function.class);
		EasyMock.reset(rl, wl, m, f);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.get(EasyMock.same(o))).andReturn(null).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		wl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.get(EasyMock.same(o))).andReturn(null).once();
		EasyMock.expect(f.apply(EasyMock.same(o))).andReturn(p).once();
		EasyMock.expect(m.put(EasyMock.same(o), EasyMock.same(p))).andReturn(null).once();
		rl.lock();
		EasyMock.expectLastCall().once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m, f);
		Assertions.assertSame(p, sm.computeIfAbsent(o, f));
		EasyMock.verify(rl, wl, m, f);

		EasyMock.reset(rl, wl, m, f);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.get(EasyMock.same(o))).andReturn(null).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		wl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.get(EasyMock.same(o))).andReturn(null).once();
		EasyMock.expect(f.apply(EasyMock.same(o))).andReturn(null).once();
		rl.lock();
		EasyMock.expectLastCall().once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m, f);
		Assertions.assertSame(null, sm.computeIfAbsent(o, f));
		EasyMock.verify(rl, wl, m, f);
	}

	@Test
	public void testComputeIfPresent() {
		final Map<Object, Object> m = EasyMock.createStrictMock(Map.class);
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
		final ShareableMap<Object, Object> sm = new ShareableMap<>(rwl, m);

		Assertions.assertThrows(NullPointerException.class, () -> sm.computeIfPresent(null, null));
		Assertions.assertThrows(NullPointerException.class, () -> sm.computeIfPresent(new Object(), null));

		final Object o = new Object();
		final Object p = new Object();
		final Object q = new Object();
		final BiFunction<Object, Object, Object> f = EasyMock.createStrictMock(BiFunction.class);
		EasyMock.reset(rl, wl, m, f);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.get(EasyMock.same(o))).andReturn(p).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		wl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.get(EasyMock.same(o))).andReturn(p).once();
		EasyMock.expect(f.apply(EasyMock.same(o), EasyMock.same(p))).andReturn(q).once();
		EasyMock.expect(m.put(EasyMock.same(o), EasyMock.same(q))).andReturn(p).once();
		rl.lock();
		EasyMock.expectLastCall().once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m, f);
		Assertions.assertSame(q, sm.computeIfPresent(o, f));
		EasyMock.verify(rl, wl, m, f);

		EasyMock.reset(rl, wl, m, f);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.get(EasyMock.same(o))).andReturn(p).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		wl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(m.get(EasyMock.same(o))).andReturn(p).once();
		EasyMock.expect(f.apply(EasyMock.same(o), EasyMock.same(p))).andReturn(null).once();
		EasyMock.expect(m.remove(EasyMock.same(o))).andReturn(p).once();
		rl.lock();
		EasyMock.expectLastCall().once();
		wl.unlock();
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(rl, wl, m, f);
		Assertions.assertNull(sm.computeIfPresent(o, f));
		EasyMock.verify(rl, wl, m, f);
	}

	@Test
	public void testCompute() {
	}

	@Test
	public void testMerge() {
	}
}
