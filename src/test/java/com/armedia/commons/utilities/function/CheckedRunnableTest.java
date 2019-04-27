package com.armedia.commons.utilities.function;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CheckedRunnableTest {

	@Test
	public void testRunChecked() throws Throwable {
		CheckedRunnable<Throwable> c = null;

		final AtomicLong callCount = new AtomicLong(0);

		c = () -> callCount.incrementAndGet();
		callCount.set(0);
		c.runChecked();
		Assertions.assertEquals(1, callCount.get());

		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		c = () -> {
			callCount.incrementAndGet();
			throw thrown;
		};
		callCount.set(0);
		try {
			c.runChecked();
			Assertions.fail("Did not raise the cascaded exception");
		} catch (Throwable t) {
			Assertions.assertSame(thrown, t);
		}
		Assertions.assertEquals(1, callCount.get());
	}

	@Test
	public void testRun() {
		CheckedRunnable<Throwable> c = null;

		final AtomicLong callCount = new AtomicLong(0);

		c = () -> callCount.incrementAndGet();
		callCount.set(0);
		c.run();
		Assertions.assertEquals(1, callCount.get());

		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		c = () -> {
			callCount.incrementAndGet();
			throw thrown;
		};
		callCount.set(0);
		try {
			c.run();
			Assertions.fail("Did not raise the cascaded exception");
		} catch (RuntimeException e) {
			Assertions.assertNotSame(thrown, e);
			Assertions.assertEquals(thrown.getMessage(), e.getMessage());
			Assertions.assertSame(thrown, e.getCause());
		}
		Assertions.assertEquals(1, callCount.get());
	}

	@Test
	public void testAndThenChecked() throws Throwable {
		CheckedRunnable<Throwable> a = null;
		CheckedRunnable<Throwable> b = null;

		final List<String> callers = new ArrayList<>();

		a = () -> callers.add("a");
		b = a.andThen((Runnable) () -> callers.add("b"));
		callers.clear();
		b.runChecked();
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		{
			final Throwable thrown = new Throwable(UUID.randomUUID().toString());
			callers.clear();
			a = () -> {
				callers.add("a");
				throw thrown;
			};
			b = a.andThen((Runnable) () -> callers.add("b"));
			try {
				b.runChecked();
				Assertions.fail("Did not raise the chained exception");
			} catch (Throwable t) {
				Assertions.assertSame(thrown, t);
			}
			Assertions.assertEquals(1, callers.size());
			Assertions.assertArrayEquals(new String[] {
				"a",
			}, callers.toArray());
		}

		{
			final RuntimeException thrown = new RuntimeException(UUID.randomUUID().toString());
			callers.clear();
			a = () -> callers.add("a");
			b = a.andThen((Runnable) () -> {
				callers.add("b");
				throw thrown;
			});
			try {
				b.runChecked();
				Assertions.fail("Did not raise the chained exception");
			} catch (Throwable t) {
				Assertions.assertSame(thrown, t);
			}
			Assertions.assertEquals(2, callers.size());
			Assertions.assertArrayEquals(new String[] {
				"a", "b"
			}, callers.toArray());
		}

		{
			CheckedRunnable<Throwable> n = () -> {
			};

			Assertions.assertThrows(NullPointerException.class, () -> n.andThen((Runnable) null));
		}
	}

	@Test
	public void testAndThen() throws Throwable {
		CheckedRunnable<Throwable> a = null;
		CheckedRunnable<Throwable> b = null;

		final List<String> callers = new ArrayList<>();

		a = () -> callers.add("a");
		b = a.andThen(() -> callers.add("b"));
		callers.clear();
		b.runChecked();
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		{
			final Throwable thrown = new Throwable(UUID.randomUUID().toString());
			callers.clear();
			a = () -> {
				callers.add("a");
				throw thrown;
			};
			b = a.andThen(() -> callers.add("b"));
			try {
				b.runChecked();
				Assertions.fail("Did not raise the chained exception");
			} catch (Throwable t) {
				Assertions.assertSame(thrown, t);
			}
			Assertions.assertEquals(1, callers.size());
			Assertions.assertArrayEquals(new String[] {
				"a",
			}, callers.toArray());
		}

		{
			final Throwable thrown = new Throwable(UUID.randomUUID().toString());
			callers.clear();
			a = () -> callers.add("a");
			b = a.andThen(() -> {
				callers.add("b");
				throw thrown;
			});
			try {
				b.runChecked();
				Assertions.fail("Did not raise the chained exception");
			} catch (Throwable t) {
				Assertions.assertSame(thrown, t);
			}
			Assertions.assertEquals(2, callers.size());
			Assertions.assertArrayEquals(new String[] {
				"a", "b"
			}, callers.toArray());
		}

		{
			CheckedRunnable<Throwable> n = () -> {
			};

			Assertions.assertThrows(NullPointerException.class, () -> n.andThen(null));
		}
	}
}