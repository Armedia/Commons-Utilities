package com.armedia.commons.utilities.function;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CheckedConsumerTest {

	@Test
	public void testAcceptChecked() throws Throwable {
		CheckedConsumer<String, Throwable> c = null;

		final AtomicReference<String> string = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		c = (s) -> Assertions.assertSame(string.get(), s);
		c.acceptChecked(string.get());

		string.set(UUID.randomUUID().toString());
		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		c = (s) -> {
			Assertions.assertSame(string.get(), s);
			throw thrown;
		};
		try {
			c.acceptChecked(string.get());
			Assertions.fail("Did not raise the cascaded exception");
		} catch (Throwable t) {
			Assertions.assertSame(thrown, t);
		}
	}

	@Test
	public void testAccept() {
		CheckedConsumer<String, Throwable> c = null;

		final AtomicReference<String> string = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		c = (s) -> Assertions.assertSame(string.get(), s);
		c.accept(string.get());

		string.set(UUID.randomUUID().toString());
		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		c = (s) -> {
			Assertions.assertSame(string.get(), s);
			throw thrown;
		};
		try {
			c.accept(string.get());
			Assertions.fail("Did not raise the cascaded exception");
		} catch (RuntimeException e) {
			Assertions.assertNotSame(thrown, e);
			Assertions.assertEquals(thrown.getMessage(), e.getMessage());
			Assertions.assertSame(thrown, e.getCause());
		}
	}

	@Test
	public void testAndThen() throws Throwable {
		CheckedConsumer<String, Throwable> a = null;
		CheckedConsumer<String, Throwable> b = null;

		final List<String> callers = new ArrayList<>();
		final AtomicReference<String> string = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		a = (s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("a");
		};

		b = a.andThen((s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("b");
		});
		b.acceptChecked(string.get());
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		{
			final Throwable thrown = new Throwable(UUID.randomUUID().toString());
			callers.clear();
			string.set(UUID.randomUUID().toString());
			a = (s) -> {
				Assertions.assertSame(string.get(), s);
				callers.add("a");
				throw thrown;
			};
			b = a.andThen((s) -> {
				Assertions.assertSame(string.get(), s);
				callers.add("b");
			});
			try {
				b.acceptChecked(string.get());
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
			a = (s) -> {
				Assertions.assertSame(string.get(), s);
				callers.add("a");
			};
			b = a.andThen((s) -> {
				Assertions.assertSame(string.get(), s);
				callers.add("b");
				throw thrown;
			});
			try {
				b.acceptChecked(string.get());
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
			CheckedConsumer<String, Throwable> n = (x) -> {
			};

			Assertions.assertThrows(NullPointerException.class, () -> n.andThen(null));
		}
	}

}
