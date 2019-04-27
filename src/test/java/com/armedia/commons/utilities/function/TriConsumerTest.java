package com.armedia.commons.utilities.function;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TriConsumerTest {

	@Test
	public void testAccept() {
		TriConsumer<String, Double, UUID> c = null;

		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Double> number = new AtomicReference<>(null);
		final AtomicReference<UUID> uuid = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		uuid.set(UUID.randomUUID());
		c = (s, n, u) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(uuid.get(), u);
		};
		c.accept(string.get(), number.get(), uuid.get());
	}

	@Test
	public void testAndThen() throws Throwable {
		TriConsumer<String, Double, UUID> a = null;
		TriConsumer<String, Double, UUID> b = null;

		final List<String> callers = new ArrayList<>();
		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Double> number = new AtomicReference<>(null);
		final AtomicReference<UUID> uuid = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		uuid.set(UUID.randomUUID());
		a = (s, n, u) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(uuid.get(), u);
			callers.add("a");
		};
		b = a.andThen((s, n, u) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(uuid.get(), u);
			callers.add("b");
		});
		b.accept(string.get(), number.get(), uuid.get());
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		{
			TriConsumer<String, Double, UUID> n = (x, y, z) -> {
			};

			Assertions.assertThrows(NullPointerException.class, () -> n.andThen(null));
		}
	}

}
