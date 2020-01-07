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
package com.armedia.commons.utilities;

import java.util.Iterator;
import java.util.stream.Stream;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CloseableIteratorWrapperTest {

	@Test
	public void testConstructors() {
		final Iterator<Object> it = EasyMock.createStrictMock(Iterator.class);
		final Runnable r = EasyMock.createStrictMock(Runnable.class);
		final Stream<Object> s = EasyMock.createStrictMock(Stream.class);
		final Stream<Object> nullStream = null;
		final Iterator<Object> nullIt = null;

		Assertions.assertThrows(NullPointerException.class, () -> new CloseableIteratorWrapper<>(nullIt));
		Assertions.assertThrows(NullPointerException.class, () -> new CloseableIteratorWrapper<>(nullStream));
		Assertions.assertThrows(NullPointerException.class, () -> new CloseableIteratorWrapper<>(null, null));
		Assertions.assertThrows(NullPointerException.class, () -> new CloseableIteratorWrapper<>(null, r));

		EasyMock.reset(it, r, s);
		EasyMock.replay(it, r, s);
		try (CloseableIterator<Object> o = new CloseableIteratorWrapper<>(it)) {
		}
		EasyMock.verify(it, r, s);

		EasyMock.reset(it, r, s);
		EasyMock.expect(s.iterator()).andReturn(it).once();
		s.close();
		EasyMock.expectLastCall().once();
		EasyMock.replay(it, r, s);
		try (CloseableIterator<Object> o = new CloseableIteratorWrapper<>(s)) {
		}
		EasyMock.verify(it, r, s);

		EasyMock.reset(it, r, s);
		EasyMock.replay(it, r, s);
		try (CloseableIterator<Object> o = new CloseableIteratorWrapper<>(it, null)) {
		}
		EasyMock.verify(it, r, s);

		EasyMock.reset(it, r, s);
		r.run();
		EasyMock.expectLastCall().once();
		EasyMock.replay(it, r, s);
		try (CloseableIterator<Object> o = new CloseableIteratorWrapper<>(it, r)) {
		}
		EasyMock.verify(it, r, s);
	}

	@Test
	public void testFindNext() {
		Iterator<Object> it = EasyMock.createStrictMock(Iterator.class);
		EasyMock.reset(it);
		EasyMock.expect(it.hasNext()).andReturn(true).once();
		EasyMock.expect(it.next()).andReturn(it).once();
		EasyMock.expect(it.hasNext()).andReturn(false).once();
		EasyMock.replay(it);
		try (CloseableIterator<Object> o = new CloseableIteratorWrapper<>(it)) {
			Assertions.assertTrue(o.hasNext());
			Assertions.assertSame(it, o.next());
			Assertions.assertFalse(o.hasNext());
		}
		EasyMock.verify(it);
	}

	@Test
	public void testRemove() {
		Iterator<Object> it = EasyMock.createStrictMock(Iterator.class);
		EasyMock.reset(it);
		EasyMock.expect(it.hasNext()).andReturn(true).once();
		EasyMock.expect(it.next()).andReturn(it).once();
		it.remove();
		EasyMock.expectLastCall().once();
		EasyMock.expect(it.hasNext()).andReturn(false).once();
		EasyMock.replay(it);
		try (CloseableIterator<Object> o = new CloseableIteratorWrapper<>(it)) {
			Assertions.assertTrue(o.hasNext());
			Assertions.assertSame(it, o.next());
			o.remove();
			Assertions.assertFalse(o.hasNext());
		}
		EasyMock.verify(it);
	}

	@Test
	public void testClose() {
		Iterator<Object> it = EasyMock.createStrictMock(Iterator.class);
		Runnable r = EasyMock.createStrictMock(Runnable.class);
		EasyMock.reset(it, r);
		r.run();
		EasyMock.expectLastCall().once();
		EasyMock.replay(it, r);
		try (CloseableIterator<Object> o = new CloseableIteratorWrapper<>(it, r)) {
		}
		EasyMock.verify(it, r);

	}
}
