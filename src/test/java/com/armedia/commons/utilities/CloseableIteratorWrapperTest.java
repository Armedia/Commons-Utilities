package com.armedia.commons.utilities;

import java.util.Iterator;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CloseableIteratorWrapperTest {

	@Test
	public void testConstructors() {
		Iterator<Object> it = EasyMock.createStrictMock(Iterator.class);
		Runnable r = EasyMock.createStrictMock(Runnable.class);

		Assertions.assertThrows(NullPointerException.class, () -> new CloseableIteratorWrapper<>(null));
		Assertions.assertThrows(NullPointerException.class, () -> new CloseableIteratorWrapper<>(null, null));
		Assertions.assertThrows(NullPointerException.class, () -> new CloseableIteratorWrapper<>(null, r));

		EasyMock.reset(it, r);
		EasyMock.replay(it, r);
		try (CloseableIterator<Object> o = new CloseableIteratorWrapper<>(it)) {
		}
		EasyMock.verify(it, r);

		EasyMock.reset(it, r);
		EasyMock.replay(it, r);
		try (CloseableIterator<Object> o = new CloseableIteratorWrapper<>(it, null)) {
		}
		EasyMock.verify(it, r);

		EasyMock.reset(it, r);
		r.run();
		EasyMock.expectLastCall().once();
		EasyMock.replay(it, r);
		try (CloseableIterator<Object> o = new CloseableIteratorWrapper<>(it, r)) {
		}
		EasyMock.verify(it, r);
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