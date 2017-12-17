package com.armedia.commons.utilities;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;

public class PooledWorkersTest {

	@Test
	public void testBlocking() throws Exception {
		final AtomicLong data = new AtomicLong(0);
		PooledWorkers<Object, Object, String> pw = new PooledWorkers<Object, Object, String>() {

			@Override
			protected Object initialize(Object o) throws Exception {
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

		pw.start(null, 4, "Blocking", true);
		final int testCount = 100;
		Assert.assertEquals(0, data.get());
		for (int i = 1; i <= testCount; i++) {
			pw.addWorkItem(String.format("%08x", i));
		}
		pw.waitForCompletion();
		Assert.assertEquals(testCount, data.get());
	}

	@Test
	public void testInterrupt() throws Exception {
		final AtomicLong data = new AtomicLong(0);
		PooledWorkers<Object, Object, String> pw = new PooledWorkers<Object, Object, String>() {

			@Override
			protected Object initialize(Object o) throws Exception {
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

		pw.start(null, 4, "Interrupt", true);
		final int testCount = 100;
		Assert.assertEquals(0, data.get());
		for (int i = 1; i <= testCount; i++) {
			pw.addWorkItem(String.format("%08x", i));
		}
		while (data.get() < 100) {
			Thread.sleep(100);
		}
		pw.waitForCompletion();
		Assert.assertEquals(testCount, data.get());
	}

	@Test
	public void testNonBlocking() throws Exception {
		final AtomicLong data = new AtomicLong(0);
		PooledWorkers<Object, Object, String> pw = new PooledWorkers<Object, Object, String>() {

			@Override
			protected Object initialize(Object o) throws Exception {
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
		pw.start(null, 4, "NonBlocking", false);
		pw.waitForCompletion();
		Assert.assertEquals(testCount, data.get());
	}
}