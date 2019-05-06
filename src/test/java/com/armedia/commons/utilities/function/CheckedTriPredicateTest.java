package com.armedia.commons.utilities.function;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CheckedTriPredicateTest {

	@Test
	public void testApplyChecked() throws Throwable {
		CheckedTriPredicate<String, Double, Date, Throwable> p = null;

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
		Assertions.assertEquals(bool.get(), p.testChecked(string.get(), number.get(), date.get()));

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		date.set(new Date());
		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		p = (s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			throw thrown;
		};
		try {
			p.testChecked(string.get(), number.get(), date.get());
			Assertions.fail("Did not raise the cascaded exception");
		} catch (Throwable t) {
			Assertions.assertSame(thrown, t);
		}
	}

	@Test
	public void testApply() {
		CheckedTriPredicate<String, Double, Date, Throwable> p = null;

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

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		date.set(new Date());
		bool.set(r.nextBoolean());
		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		p = (s, n, d) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			Assertions.assertSame(date.get(), d);
			throw thrown;
		};
		try {
			p.test(string.get(), number.get(), date.get());
			Assertions.fail("Did not raise the cascaded exception");
		} catch (RuntimeException e) {
			Assertions.assertNotSame(thrown, e);
			Assertions.assertEquals(thrown.getMessage(), e.getMessage());
			Assertions.assertSame(thrown, e.getCause());
		}
	}

	@Test
	public void testAnd() throws Throwable {
		CheckedTriPredicate<String, Double, Date, Throwable> a = null;
		CheckedTriPredicate<String, Double, Date, Throwable> b = null;

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
		Assertions.assertTrue(b.testChecked(string.get(), number.get(), date.get()));
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
		Assertions.assertFalse(b.testChecked(string.get(), number.get(), date.get()));
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
		Assertions.assertFalse(b.testChecked(string.get(), number.get(), date.get()));
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
		Assertions.assertFalse(b.testChecked(string.get(), number.get(), date.get()));
		Assertions.assertEquals(1, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a"
		}, callers.toArray());

		{
			final Throwable thrown = new Throwable(UUID.randomUUID().toString());
			callers.clear();
			string.set(UUID.randomUUID().toString());
			number.set(Math.random());
			date.set(new Date());
			a = (s, n, d) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				Assertions.assertSame(date.get(), d);
				callers.add("a");
				throw thrown;
			};
			b = a.and((s, n, d) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				Assertions.assertSame(date.get(), d);
				callers.add("b");
				return true;
			});
			try {
				b.testChecked(string.get(), number.get(), date.get());
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
				throw thrown;
			});
			try {
				b.testChecked(string.get(), number.get(), date.get());
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
			CheckedTriPredicate<String, Double, Date, Throwable> n = (x, y, z) -> false;
			Assertions.assertThrows(NullPointerException.class, () -> n.and(null));
		}
	}

	@Test
	public void testOr() throws Throwable {
		CheckedTriPredicate<String, Double, Date, Throwable> a = null;
		CheckedTriPredicate<String, Double, Date, Throwable> b = null;

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
		Assertions.assertTrue(b.testChecked(string.get(), number.get(), date.get()));
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
		Assertions.assertTrue(b.testChecked(string.get(), number.get(), date.get()));
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
		Assertions.assertTrue(b.testChecked(string.get(), number.get(), date.get()));
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
		Assertions.assertFalse(b.testChecked(string.get(), number.get(), date.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		{
			final Throwable thrown = new Throwable(UUID.randomUUID().toString());
			callers.clear();
			string.set(UUID.randomUUID().toString());
			number.set(Math.random());
			date.set(new Date());
			a = (s, n, d) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				Assertions.assertSame(date.get(), d);
				callers.add("a");
				throw thrown;
			};
			b = a.or((s, n, d) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				Assertions.assertSame(date.get(), d);
				callers.add("b");
				return true;
			});
			try {
				b.testChecked(string.get(), number.get(), date.get());
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
				throw thrown;
			});
			try {
				b.testChecked(string.get(), number.get(), date.get());
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
			CheckedTriPredicate<String, Double, Date, Throwable> n = (x, y, z) -> false;
			Assertions.assertThrows(NullPointerException.class, () -> n.or(null));
		}
	}

	@Test
	public void testNegate() throws Throwable {
		CheckedTriPredicate<String, Double, Date, Throwable> a = null;

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

		{
			final Throwable thrown = new Throwable(UUID.randomUUID().toString());
			string.set(UUID.randomUUID().toString());
			number.set(Math.random());
			date.set(new Date());
			a = (s, n, d) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				Assertions.assertSame(date.get(), d);
				throw thrown;
			};
			try {
				a.negate().testChecked(string.get(), number.get(), date.get());
				Assertions.fail("Did not raise the chained exception");
			} catch (Throwable t) {
				Assertions.assertSame(thrown, t);
			}
		}
	}

	@Test
	public void testXor() throws Throwable {
		CheckedTriPredicate<String, Double, Date, Throwable> a = null;
		CheckedTriPredicate<String, Double, Date, Throwable> b = null;

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
		Assertions.assertFalse(b.testChecked(string.get(), number.get(), date.get()));
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
		Assertions.assertTrue(b.testChecked(string.get(), number.get(), date.get()));
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
		Assertions.assertTrue(b.testChecked(string.get(), number.get(), date.get()));
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
		Assertions.assertFalse(b.testChecked(string.get(), number.get(), date.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		{
			final Throwable thrown = new Throwable(UUID.randomUUID().toString());
			callers.clear();
			string.set(UUID.randomUUID().toString());
			number.set(Math.random());
			date.set(new Date());
			a = (s, n, d) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				Assertions.assertSame(date.get(), d);
				callers.add("a");
				throw thrown;
			};
			b = a.xor((s, n, d) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				Assertions.assertSame(date.get(), d);
				callers.add("b");
				return true;
			});
			try {
				b.testChecked(string.get(), number.get(), date.get());
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
				throw thrown;
			});
			try {
				b.testChecked(string.get(), number.get(), date.get());
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
			CheckedTriPredicate<String, Double, Date, Throwable> n = (x, y, z) -> false;
			Assertions.assertThrows(NullPointerException.class, () -> n.xor(null));
		}
	}
}
