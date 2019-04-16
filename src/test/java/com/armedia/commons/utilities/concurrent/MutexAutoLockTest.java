package com.armedia.commons.utilities.concurrent;

import java.util.concurrent.locks.Lock;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("resource")
class MutexAutoLockTest {

	@Test
	void testConstructor() {
		final Lock lock = EasyMock.createStrictMock(Lock.class);
		EasyMock.replay(lock);
		Assertions.assertThrows(NullPointerException.class, () -> new MutexAutoLock(null));
		new MutexAutoLock(lock);
		EasyMock.verify(lock);
	}

	@Test
	void testAutoClose() {
		final Lock lock = EasyMock.createStrictMock(Lock.class);
		lock.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(lock);
		try (MutexAutoLock autoLock = new MutexAutoLock(lock)) {
		}
		EasyMock.verify(lock);
	}

	@Test
	void testClose() {
		final Lock lock = EasyMock.createStrictMock(Lock.class);
		lock.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(lock);
		MutexAutoLock autoLock = new MutexAutoLock(lock);
		autoLock.close();
		EasyMock.verify(lock);

		EasyMock.reset(lock);
		lock.unlock();
		EasyMock.expectLastCall().once();
		EasyMock.replay(lock);
		autoLock = new MutexAutoLock(lock);
		autoLock.close();
		// Make sure the 2nd call does nothing
		autoLock.close();
		EasyMock.verify(lock);
	}

}
