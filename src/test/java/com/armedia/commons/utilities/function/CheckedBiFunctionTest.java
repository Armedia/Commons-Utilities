package com.armedia.commons.utilities.function;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CheckedBiFunctionTest {

	@Test
	void testApplyChecked() throws Throwable {
		CheckedBiFunction<String, Double, UUID, Throwable> f = null;

		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Double> number = new AtomicReference<>(null);
		final AtomicReference<UUID> uuid = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		uuid.set(UUID.randomUUID());
		f = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			return uuid.get();
		};
		Assertions.assertSame(uuid.get(), f.applyChecked(string.get(), number.get()));

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		f = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			throw thrown;
		};
		try {
			f.applyChecked(string.get(), number.get());
			Assertions.fail("Did not raise the cascaded exception");
		} catch (Throwable t) {
			Assertions.assertSame(thrown, t);
		}
	}

	@Test
	void testApply() {
		CheckedBiFunction<String, Double, UUID, Throwable> f = null;

		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Double> number = new AtomicReference<>(null);
		final AtomicReference<UUID> uuid = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		uuid.set(UUID.randomUUID());
		f = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			return uuid.get();
		};
		Assertions.assertSame(uuid.get(), f.apply(string.get(), number.get()));

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		uuid.set(UUID.randomUUID());
		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		f = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			throw thrown;
		};
		try {
			f.apply(string.get(), number.get());
			Assertions.fail("Did not raise the cascaded exception");
		} catch (RuntimeException e) {
			Assertions.assertNotSame(thrown, e);
			Assertions.assertEquals(thrown.getMessage(), e.getMessage());
			Assertions.assertSame(thrown, e.getCause());
		}
	}

	@Test
	void testAndThen() throws Throwable {
		CheckedBiFunction<String, Double, UUID, Throwable> a = null;
		CheckedBiFunction<String, Double, Long, Throwable> b = null;

		final List<String> callers = new ArrayList<>();
		final Random r = new Random(System.nanoTime());
		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Double> number = new AtomicReference<>(null);
		final AtomicReference<UUID> uuid = new AtomicReference<>(null);
		final AtomicReference<Long> l = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		uuid.set(UUID.randomUUID());
		l.set(r.nextLong());
		a = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("a");
			return uuid.get();
		};

		b = a.andThen((u) -> {
			Assertions.assertSame(uuid.get(), u);
			callers.add("b");
			return l.get();
		});
		Assertions.assertSame(l.get(), b.applyChecked(string.get(), number.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		{
			final Throwable thrown = new Throwable(UUID.randomUUID().toString());
			callers.clear();
			string.set(UUID.randomUUID().toString());
			number.set(Math.random());
			uuid.set(UUID.randomUUID());
			l.set(r.nextLong());
			a = (s, n) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				callers.add("a");
				throw thrown;
			};
			b = a.andThen((u) -> {
				Assertions.assertSame(uuid.get(), u);
				callers.add("b");
				return l.get();
			});
			try {
				b.applyChecked(string.get(), number.get());
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
			uuid.set(UUID.randomUUID());
			l.set(r.nextLong());
			a = (s, n) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				callers.add("a");
				return uuid.get();
			};
			b = a.andThen((u) -> {
				Assertions.assertSame(uuid.get(), u);
				callers.add("b");
				throw thrown;
			});
			try {
				b.applyChecked(string.get(), number.get());
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
			CheckedBiFunction<String, Double, UUID, Throwable> n = (x, y) -> null;
			Assertions.assertThrows(NullPointerException.class, () -> n.andThen(null));
		}
	}

}
