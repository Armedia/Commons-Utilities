/*******************************************************************************
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (C) 2013 - 2019 Armedia
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

import java.util.Comparator;
import java.util.Spliterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ShareableSpliteratorTest {

	@Test
	public void testConstructors() {
		ReadWriteLock rwl = null;
		ShareableLockable sl = null;
		Spliterator<Object> c = null;

		Assertions.assertThrows(NullPointerException.class, () -> new ShareableSpliterator<>(c));
		new ShareableSpliterator<>(EasyMock.createStrictMock(Spliterator.class));
		new ShareableSpliterator<>(new ShareableSpliterator<>(EasyMock.createStrictMock(Spliterator.class)));

		Assertions.assertThrows(NullPointerException.class, () -> new ShareableSpliterator<>(rwl, c));
		new ShareableSpliterator<>(rwl, EasyMock.createStrictMock(Spliterator.class));
		Assertions.assertThrows(NullPointerException.class,
			() -> new ShareableSpliterator<>(new ReentrantReadWriteLock(), c));

		Assertions.assertThrows(NullPointerException.class, () -> new ShareableSpliterator<>(sl, c));
		Assertions.assertThrows(NullPointerException.class,
			() -> new ShareableSpliterator<>(sl, EasyMock.createStrictMock(Spliterator.class)));
		Assertions.assertThrows(NullPointerException.class,
			() -> new ShareableSpliterator<>(new BaseShareableLockable(), c));
		{
			ReadWriteLock l = new ReentrantReadWriteLock();
			ShareableLockable s = new BaseShareableLockable();
			Assertions.assertSame(l,
				new ShareableSpliterator<>(l, EasyMock.createStrictMock(Spliterator.class)).getShareableLock());
			Assertions.assertSame(s.getShareableLock(),
				new ShareableSpliterator<>(s, EasyMock.createStrictMock(Spliterator.class)).getShareableLock());
		}
	}

	@Test
	public void testTryAdvance() {
		final Spliterator<Object> s = EasyMock.createStrictMock(Spliterator.class);
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
		final ShareableSpliterator<Object> ss = new ShareableSpliterator<>(rwl, s);
		final Consumer<? super Object> c = EasyMock.createStrictMock(Consumer.class);

		Assertions.assertThrows(NullPointerException.class, () -> ss.tryAdvance(null));

		EasyMock.reset(s, rl, wl, c);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(s.tryAdvance(EasyMock.same(c))).andReturn(true).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(s, rl, wl, c);
		Assertions.assertTrue(ss.tryAdvance(c));
		EasyMock.verify(s, rl, wl, c);

		EasyMock.reset(s, rl, wl, c);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(s.tryAdvance(EasyMock.same(c))).andReturn(false).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(s, rl, wl, c);
		Assertions.assertFalse(ss.tryAdvance(c));
		EasyMock.verify(s, rl, wl, c);
	}

	@Test
	public void testForEachRemaining() {
		final Spliterator<Object> s = EasyMock.createStrictMock(Spliterator.class);
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
		final ShareableSpliterator<Object> ss = new ShareableSpliterator<>(rwl, s);
		final Consumer<? super Object> c = EasyMock.createStrictMock(Consumer.class);

		Assertions.assertThrows(NullPointerException.class, () -> ss.forEachRemaining(null));

		EasyMock.reset(s, rl, wl, c);
		rl.lock();
		EasyMock.expectLastCall().once();
		s.forEachRemaining(EasyMock.same(c));
		EasyMock.expectLastCall().once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(s, rl, wl, c);
		ss.forEachRemaining(c);
		EasyMock.verify(s, rl, wl, c);
	}

	@Test
	public void testTrySplit() {
		final Spliterator<Object> s = EasyMock.createStrictMock(Spliterator.class);
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
		final ShareableSpliterator<Object> ss = new ShareableSpliterator<>(rwl, s);
		final Consumer<? super Object> c = EasyMock.createStrictMock(Consumer.class);
		final Spliterator<Object> o = EasyMock.createStrictMock(Spliterator.class);

		EasyMock.reset(s, rl, wl, c, o);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(s.trySplit()).andReturn(o).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(s, rl, wl, c, o);
		Spliterator<Object> it = ss.trySplit();
		EasyMock.verify(s, rl, wl, c, o);

		Assertions.assertTrue(ShareableSpliterator.class.isInstance(it));
		ShareableSpliterator<?> sit = ShareableSpliterator.class.cast(it);
		Assertions.assertSame(rwl, sit.getShareableLock());

		EasyMock.reset(s, rl, wl, c, o);
		rl.lock();
		EasyMock.expectLastCall().once();
		EasyMock.expect(s.trySplit()).andReturn(null).once();
		rl.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(s, rl, wl, c, o);
		Assertions.assertNull(ss.trySplit());
		EasyMock.verify(s, rl, wl, c, o);
	}

	@Test
	public void testEstimateSize() {
		final Spliterator<Object> s = EasyMock.createStrictMock(Spliterator.class);
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
		final ShareableSpliterator<Object> ss = new ShareableSpliterator<>(rwl, s);

		for (long i = 0; i < 100; i++) {
			EasyMock.reset(s, rl, wl);
			rl.lock();
			EasyMock.expectLastCall().once();
			EasyMock.expect(s.estimateSize()).andReturn(i).once();
			rl.unlock();
			EasyMock.expectLastCall().once();
			EasyMock.replay(s, rl, wl);
			Assertions.assertEquals(i, ss.estimateSize());
			EasyMock.verify(s, rl, wl);
		}
	}

	@Test
	public void testGetExactSizeIfKnown() {
		final Spliterator<Object> s = EasyMock.createStrictMock(Spliterator.class);
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
		final ShareableSpliterator<Object> ss = new ShareableSpliterator<>(rwl, s);

		for (long i = -1; i < 100; i++) {
			EasyMock.reset(s, rl, wl);
			rl.lock();
			EasyMock.expectLastCall().once();
			EasyMock.expect(s.getExactSizeIfKnown()).andReturn(i).once();
			rl.unlock();
			EasyMock.expectLastCall().once();
			EasyMock.replay(s, rl, wl);
			Assertions.assertEquals(i, ss.getExactSizeIfKnown());
			EasyMock.verify(s, rl, wl);
		}
	}

	@Test
	public void testCharacteristics() {
		final Spliterator<Object> s = EasyMock.createStrictMock(Spliterator.class);
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
		final ShareableSpliterator<Object> ss = new ShareableSpliterator<>(rwl, s);

		for (int i = 0; i < 0xFF; i++) {
			EasyMock.reset(s, rl, wl);
			EasyMock.expect(s.characteristics()).andReturn(i).once();
			EasyMock.replay(s, rl, wl);
			Assertions.assertEquals(i, ss.characteristics());
			EasyMock.verify(s, rl, wl);
		}
	}

	@Test
	public void testHasCharacteristics() {
		final Spliterator<Object> s = EasyMock.createStrictMock(Spliterator.class);
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
		final ShareableSpliterator<Object> ss = new ShareableSpliterator<>(rwl, s);
		for (int i = 0; i < 0xFF; i++) {
			EasyMock.reset(s, rl, wl);
			EasyMock.expect(s.hasCharacteristics(EasyMock.eq(i))).andReturn(true).once();
			EasyMock.replay(s, rl, wl);
			Assertions.assertTrue(ss.hasCharacteristics(i));
			EasyMock.verify(s, rl, wl);

			EasyMock.reset(s, rl, wl);
			EasyMock.expect(s.hasCharacteristics(EasyMock.eq(i))).andReturn(false).once();
			EasyMock.replay(s, rl, wl);
			Assertions.assertFalse(ss.hasCharacteristics(i));
			EasyMock.verify(s, rl, wl);
		}
	}

	@Test
	public void testGetComparator() {
		final Spliterator<Object> s = EasyMock.createStrictMock(Spliterator.class);
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
		final ShareableSpliterator<Object> ss = new ShareableSpliterator<>(rwl, s);
		final Comparator<Object> c = EasyMock.createStrictMock(Comparator.class);
		EasyMock.reset(s, rl, wl, c);
		EasyMock.expect(s.getComparator()).andReturn(c).once();
		EasyMock.replay(s, rl, wl, c);
		Assertions.assertSame(c, ss.getComparator());
		EasyMock.verify(s, rl, wl, c);

		EasyMock.reset(s, rl, wl, c);
		EasyMock.expect(s.getComparator()).andReturn(null).once();
		EasyMock.replay(s, rl, wl, c);
		Assertions.assertNull(ss.getComparator());
		EasyMock.verify(s, rl, wl, c);
	}
}
