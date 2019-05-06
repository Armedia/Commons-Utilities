package com.armedia.commons.utilities.function;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TriFunctionTest {

	@Test
	public void testApply() throws Throwable {
		TriFunction<String, Double, Date, UUID> f = null;

		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Double> number = new AtomicReference<>(null);
		final AtomicReference<Date> date = new AtomicReference<>(null);
		final AtomicReference<UUID> uuid = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		date.set(new Date());
		uuid.set(UUID.randomUUID());
		f = (s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			return uuid.get();
		};
		Assertions.assertSame(uuid.get(), f.apply(string.get(), number.get(), date.get()));
	}

	@Test
	public void testAndThen() throws Throwable {
		TriFunction<String, Double, Date, UUID> a = null;
		TriFunction<String, Double, Date, Long> b = null;

		final List<String> callers = new ArrayList<>();
		final Random r = new Random(System.nanoTime());
		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Double> number = new AtomicReference<>(null);
		final AtomicReference<Date> date = new AtomicReference<>(null);
		final AtomicReference<UUID> uuid = new AtomicReference<>(null);
		final AtomicReference<Long> l = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		date.set(new Date());
		uuid.set(UUID.randomUUID());
		l.set(r.nextLong());
		a = (s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("a");
			return uuid.get();
		};

		b = a.andThen((u) -> {
			Assertions.assertSame(uuid.get(), u);
			callers.add("b");
			return l.get();
		});
		Assertions.assertSame(l.get(), b.apply(string.get(), number.get(), date.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		{
			TriFunction<String, Double, Date, UUID> n = (x, y, z) -> null;
			Assertions.assertThrows(NullPointerException.class, () -> n.andThen(null));
		}
	}

}
