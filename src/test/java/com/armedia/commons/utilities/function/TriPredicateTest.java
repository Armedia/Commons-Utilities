package com.armedia.commons.utilities.function;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TriPredicateTest {

	@Test
	public void testApply() {
		TriPredicate<String, Double, Date> p = null;

		final Random r = new Random(System.nanoTime());
		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Double> number = new AtomicReference<>(null);
		final AtomicReference<Date> date = new AtomicReference<>(null);
		final AtomicReference<Boolean> bool = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		date.set(new Date());
		bool.set(r.nextBoolean());
		p = (s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			return bool.get();
		};
		Assertions.assertSame(bool.get(), p.test(string.get(), number.get(), date.get()));
	}

	@Test
	public void testAnd() throws Throwable {
		TriPredicate<String, Double, Date> a = null;
		TriPredicate<String, Double, Date> b = null;

		final List<String> callers = new ArrayList<>();
		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Double> number = new AtomicReference<>(null);
		final AtomicReference<Date> date = new AtomicReference<>(null);

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		date.set(new Date());
		a = (s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("a");
			return true;
		};
		b = a.and((s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("b");
			return true;
		});
		Assertions.assertTrue(b.test(string.get(), number.get(), date.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		date.set(new Date());
		a = (s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("a");
			return false;
		};
		b = a.and((s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("b");
			return true;
		});
		Assertions.assertFalse(b.test(string.get(), number.get(), date.get()));
		Assertions.assertEquals(1, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		date.set(new Date());
		a = (s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("a");
			return true;
		};
		b = a.and((s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("b");
			return false;
		});
		Assertions.assertFalse(b.test(string.get(), number.get(), date.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		date.set(new Date());
		a = (s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("a");
			return false;
		};
		b = a.and((s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("b");
			return false;
		});
		Assertions.assertFalse(b.test(string.get(), number.get(), date.get()));
		Assertions.assertEquals(1, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a"
		}, callers.toArray());

		{
			TriPredicate<String, Double, Date> n = (x, y, z) -> false;
			Assertions.assertThrows(NullPointerException.class, () -> n.and(null));
		}
	}

	@Test
	public void testOr() throws Throwable {
		TriPredicate<String, Double, Date> a = null;
		TriPredicate<String, Double, Date> b = null;

		final List<String> callers = new ArrayList<>();
		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Double> number = new AtomicReference<>(null);
		final AtomicReference<Date> date = new AtomicReference<>(null);

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		date.set(new Date());
		a = (s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("a");
			return true;
		};
		b = a.or((s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("b");
			return true;
		});
		Assertions.assertTrue(b.test(string.get(), number.get(), date.get()));
		Assertions.assertEquals(1, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		date.set(new Date());
		a = (s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("a");
			return false;
		};
		b = a.or((s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("b");
			return true;
		});
		Assertions.assertTrue(b.test(string.get(), number.get(), date.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		date.set(new Date());
		a = (s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("a");
			return true;
		};
		b = a.or((s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("b");
			return false;
		});
		Assertions.assertTrue(b.test(string.get(), number.get(), date.get()));
		Assertions.assertEquals(1, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		date.set(new Date());
		a = (s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("a");
			return false;
		};
		b = a.or((s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("b");
			return false;
		});
		Assertions.assertFalse(b.test(string.get(), number.get(), date.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		{
			TriPredicate<String, Double, Date> n = (x, y, z) -> false;
			Assertions.assertThrows(NullPointerException.class, () -> n.or(null));
		}
	}

	@Test
	public void testNegate() throws Throwable {
		TriPredicate<String, Double, Date> a = null;

		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Double> number = new AtomicReference<>(null);
		final AtomicReference<Date> date = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		date.set(new Date());
		a = (s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			return true;
		};
		Assertions.assertFalse(a.negate().test(string.get(), number.get(), date.get()));

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		date.set(new Date());
		a = (s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			return false;
		};
		Assertions.assertTrue(a.negate().test(string.get(), number.get(), date.get()));
	}

	@Test
	public void testXor() throws Throwable {
		TriPredicate<String, Double, Date> a = null;
		TriPredicate<String, Double, Date> b = null;

		final List<String> callers = new ArrayList<>();
		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Double> number = new AtomicReference<>(null);
		final AtomicReference<Date> date = new AtomicReference<>(null);

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		date.set(new Date());
		a = (s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("a");
			return true;
		};
		b = a.xor((s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("b");
			return true;
		});
		Assertions.assertFalse(b.test(string.get(), number.get(), date.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		date.set(new Date());
		a = (s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("a");
			return false;
		};
		b = a.xor((s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("b");
			return true;
		});
		Assertions.assertTrue(b.test(string.get(), number.get(), date.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		date.set(new Date());
		a = (s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("a");
			return true;
		};
		b = a.xor((s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("b");
			return false;
		});
		Assertions.assertTrue(b.test(string.get(), number.get(), date.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		date.set(new Date());
		a = (s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			callers.add("a");
			return false;
		};
		b = a.xor((s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("b");
			return false;
		});
		Assertions.assertFalse(b.test(string.get(), number.get(), date.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		{
			TriPredicate<String, Double, Date> n = (x, y, z) -> false;
			Assertions.assertThrows(NullPointerException.class, () -> n.xor(null));
		}
	}
}
