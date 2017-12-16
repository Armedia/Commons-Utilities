package com.armedia.commons.utilities;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;

public class PooledWorkersTest {

	@Test
	public void testBlocking() throws Exception {
		final AtomicLong data = new AtomicLong(0);
		PooledWorkers<Object, String> pw = new PooledWorkers<Object, String>() {

			@Override
			protected Object prepare() throws Exception {
				return null;
			}

			@Override
			protected void process(Object state, String item) throws InterruptedException {
				data.incrementAndGet();
				Thread.sleep(10);
			}

			@Override
			protected void cleanup(Object state) {
			}
		};

		pw.start(4, true);
		final int testCount = 100;
		Assert.assertEquals(0, data.get());
		for (int i = 1; i <= testCount; i++) {
			pw.addWorkItem(String.format("%08x", i));
		}
		pw.waitForCompletion();
		Assert.assertEquals(testCount, data.get());
	}

	@Test
	public void testNonBlocking() throws Exception {
		final AtomicLong data = new AtomicLong(0);
		PooledWorkers<Object, String> pw = new PooledWorkers<Object, String>() {

			@Override
			protected Object prepare() throws Exception {
				return null;
			}

			@Override
			protected void process(Object state, String item) throws InterruptedException {
				data.incrementAndGet();
				Thread.sleep(10);
			}

			@Override
			protected void cleanup(Object state) {
			}
		};

		final int testCount = 100;
		for (int i = 1; i <= testCount; i++) {
			pw.addWorkItem(String.format("%08x", i));
		}
		Assert.assertEquals(0, data.get());
		pw.start(4, false);
		pw.waitForCompletion();
		Assert.assertEquals(testCount, data.get());
	}
}