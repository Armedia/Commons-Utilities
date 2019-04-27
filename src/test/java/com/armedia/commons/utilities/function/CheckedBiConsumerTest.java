package com.armedia.commons.utilities.function;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CheckedBiConsumerTest {

	@Test
	public void testAcceptChecked() throws Throwable {
		CheckedBiConsumer<String, Number, Throwable> c = null;

		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Number> number = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		c = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
		};
		c.acceptChecked(string.get(), number.get());

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		c = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			throw thrown;
		};
		try {
			c.acceptChecked(string.get(), number.get());
			Assertions.fail("Did not raise the cascaded exception");
		} catch (Throwable t) {
			Assertions.assertSame(thrown, t);
		}
	}

	@Test
	public void testAccept() {
		CheckedBiConsumer<String, Number, Throwable> c = null;

		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Number> number = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		c = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
		};
		c.accept(string.get(), number.get());

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		c = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			throw thrown;
		};
		try {
			c.accept(string.get(), number.get());
			Assertions.fail("Did not raise the cascaded exception");
		} catch (RuntimeException e) {
			Assertions.assertNotSame(thrown, e);
			Assertions.assertEquals(thrown.getMessage(), e.getMessage());
			Assertions.assertSame(thrown, e.getCause());
		}
	}

	@Test
	public void testAndThen() throws Throwable {
		CheckedBiConsumer<String, Number, Throwable> a = null;
		CheckedBiConsumer<String, Number, Throwable> b = null;

		final List<String> callers = new ArrayList<>();
		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Number> number = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		a = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("a");
		};

		b = a.andThen((s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("b");
		});
		b.acceptChecked(string.get(), number.get());
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		{
			final Throwable thrown = new Throwable(UUID.randomUUID().toString());
			callers.clear();
			string.set(UUID.randomUUID().toString());
			number.set(Math.random());
			a = (s, n) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				callers.add("a");
				throw thrown;
			};
			b = a.andThen((s, n) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				callers.add("b");
			});
			try {
				b.acceptChecked(string.get(), number.get());
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
			string.set(UUID.randomUUID().toString());
			number.set(Math.random());
			a = (s, n) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				callers.add("a");
			};
			b = a.andThen((s, n) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				callers.add("b");
				throw thrown;
			});
			try {
				b.acceptChecked(string.get(), number.get());
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
			CheckedBiConsumer<String, Number, Throwable> n = (x, y) -> {
			};

			Assertions.assertThrows(NullPointerException.class, () -> n.andThen(null));
		}
	}

}
