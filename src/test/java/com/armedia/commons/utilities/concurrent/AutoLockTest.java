package com.armedia.commons.utilities.concurrent;

import java.util.concurrent.locks.Lock;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("resource")
class AutoLockTest {

	@Test
	void testAutoLock() {
		final Lock lock = EasyMock.createStrictMock(Lock.class);
		EasyMock.replay(lock);
		Assertions.assertThrows(NullPointerException.class, () -> new AutoLock(null));
		new AutoLock(lock);
		EasyMock.verify(lock);
	}

	@Test
	void testLock() {
		final Lock lock = EasyMock.createStrictMock(Lock.class);
		lock.lock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(lock);
		AutoLock autoLock = new AutoLock(lock);
		autoLock.lock();
		EasyMock.verify(lock);
		Assertions.assertSame(lock, autoLock.getLock());
	}

	@Test
	void testUnlock() {
		final Lock lock = EasyMock.createStrictMock(Lock.class);
		lock.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(lock);
		AutoLock autoLock = new AutoLock(lock);
		autoLock.unlock();
		EasyMock.verify(lock);
		Assertions.assertSame(lock, autoLock.getLock());
	}

	@Test
	void testGetLock() {
		final Lock lock = EasyMock.createStrictMock(Lock.class);
		EasyMock.replay(lock);
		AutoLock autoLock = new AutoLock(lock);
		EasyMock.verify(lock);
		Assertions.assertSame(lock, autoLock.getLock());
	}

	@Test
	void testAutoClose() {
		final Lock lock = EasyMock.createStrictMock(Lock.class);

		lock.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(lock);
		try (AutoLock autoLock = new AutoLock(lock)) {
			Assertions.assertSame(lock, autoLock.getLock());
		}
		EasyMock.verify(lock);
	}

	@Test
	void testClose() {
		final Lock lock = EasyMock.createStrictMock(Lock.class);

		lock.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(lock);
		AutoLock autoLock = new AutoLock(lock);
		Assertions.assertSame(lock, autoLock.getLock());
		autoLock.close();
		EasyMock.verify(lock);
	}

}
