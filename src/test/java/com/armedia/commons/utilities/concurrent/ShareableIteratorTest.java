/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (c) 2010 - 2019 Armedia LLC
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
