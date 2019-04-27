package com.armedia.commons.utilities.function;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CheckedBiPredicateTest {

	@Test
	public void testApplyChecked() throws Throwable {
		CheckedBiPredicate<String, Double, Throwable> p = null;

		final Random r = new Random(System.nanoTime());
		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Double> number = new AtomicReference<>(null);
		final AtomicReference<Boolean> bool = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		bool.set(r.nextBoolean());
		p = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			return bool.get();
		};
		Assertions.assertEquals(bool.get(), p.testChecked(string.get(), number.get()));

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		p = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			throw thrown;
		};
		try {
			p.testChecked(string.get(), number.get());
			Assertions.fail("Did not raise the cascaded exception");
		} catch (Throwable t) {
			Assertions.assertSame(thrown, t);
		}
	}

	@Test
	public void testApply() {
		CheckedBiPredicate<String, Double, Throwable> p = null;

		final Random r = new Random(System.nanoTime());
		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Double> number = new AtomicReference<>(null);
		final AtomicReference<Boolean> bool = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		bool.set(r.nextBoolean());
		p = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			return bool.get();
		};
		Assertions.assertSame(bool.get(), p.test(string.get(), number.get()));

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		bool.set(r.nextBoolean());
		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		p = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			throw thrown;
		};
		try {
			p.test(string.get(), number.get());
			Assertions.fail("Did not raise the cascaded exception");
		} catch (RuntimeException e) {
			Assertions.assertNotSame(thrown, e);
			Assertions.assertEquals(thrown.getMessage(), e.getMessage());
			Assertions.assertSame(thrown, e.getCause());
		}
	}

	@Test
	public void testAnd() throws Throwable {
		CheckedBiPredicate<String, Double, Throwable> a = null;
		CheckedBiPredicate<String, Double, Throwable> b = null;

		final List<String> callers = new ArrayList<>();
		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Double> number = new AtomicReference<>(null);

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		a = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("a");
			return true;
		};
		b = a.and((s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("b");
			return true;
		});
		Assertions.assertTrue(b.testChecked(string.get(), number.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		a = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("a");
			return false;
		};
		b = a.and((s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("b");
			return true;
		});
		Assertions.assertFalse(b.testChecked(string.get(), number.get()));
		Assertions.assertEquals(1, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		a = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("a");
			return true;
		};
		b = a.and((s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("b");
			return false;
		});
		Assertions.assertFalse(b.testChecked(string.get(), number.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		a = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("a");
			return false;
		};
		b = a.and((s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("b");
			return false;
		});
		Assertions.assertFalse(b.testChecked(string.get(), number.get()));
		Assertions.assertEquals(1, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a"
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
			b = a.and((s, n) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				callers.add("b");
				return true;
			});
			try {
				b.testChecked(string.get(), number.get());
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
				return true;
			};
			b = a.and((s, n) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				callers.add("b");
				throw thrown;
			});
			try {
				b.testChecked(string.get(), number.get());
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
			CheckedBiPredicate<String, Double, Throwable> n = (x, y) -> false;
			Assertions.assertThrows(NullPointerException.class, () -> n.and(null));
		}
	}

	@Test
	public void testOr() throws Throwable {
		CheckedBiPredicate<String, Double, Throwable> a = null;
		CheckedBiPredicate<String, Double, Throwable> b = null;

		final List<String> callers = new ArrayList<>();
		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Double> number = new AtomicReference<>(null);

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		a = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("a");
			return true;
		};
		b = a.or((s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("b");
			return true;
		});
		Assertions.assertTrue(b.testChecked(string.get(), number.get()));
		Assertions.assertEquals(1, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		a = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("a");
			return false;
		};
		b = a.or((s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("b");
			return true;
		});
		Assertions.assertTrue(b.testChecked(string.get(), number.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		a = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("a");
			return true;
		};
		b = a.or((s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("b");
			return false;
		});
		Assertions.assertTrue(b.testChecked(string.get(), number.get()));
		Assertions.assertEquals(1, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		a = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("a");
			return false;
		};
		b = a.or((s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("b");
			return false;
		});
		Assertions.assertFalse(b.testChecked(string.get(), number.get()));
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
			b = a.or((s, n) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				callers.add("b");
				return true;
			});
			try {
				b.testChecked(string.get(), number.get());
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
				return false;
			};
			b = a.or((s, n) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				callers.add("b");
				throw thrown;
			});
			try {
				b.testChecked(string.get(), number.get());
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
			CheckedBiPredicate<String, Double, Throwable> n = (x, y) -> false;
			Assertions.assertThrows(NullPointerException.class, () -> n.or(null));
		}
	}

	@Test
	public void testNegate() throws Throwable {
		CheckedBiPredicate<String, Double, Throwable> a = null;

		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Double> number = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		a = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			return true;
		};
		Assertions.assertFalse(a.negate().test(string.get(), number.get()));

		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		a = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			return false;
		};
		Assertions.assertTrue(a.negate().test(string.get(), number.get()));

		{
			final Throwable thrown = new Throwable(UUID.randomUUID().toString());
			string.set(UUID.randomUUID().toString());
			number.set(Math.random());
			a = (s, n) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				throw thrown;
			};
			try {
				a.negate().testChecked(string.get(), number.get());
				Assertions.fail("Did not raise the chained exception");
			} catch (Throwable t) {
				Assertions.assertSame(thrown, t);
			}
		}
	}

	@Test
	public void testXor() throws Throwable {
		CheckedBiPredicate<String, Double, Throwable> a = null;
		CheckedBiPredicate<String, Double, Throwable> b = null;

		final List<String> callers = new ArrayList<>();
		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Double> number = new AtomicReference<>(null);

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		a = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("a");
			return true;
		};
		b = a.xor((s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("b");
			return true;
		});
		Assertions.assertFalse(b.testChecked(string.get(), number.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		a = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("a");
			return false;
		};
		b = a.xor((s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("b");
			return true;
		});
		Assertions.assertTrue(b.testChecked(string.get(), number.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		a = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("a");
			return true;
		};
		b = a.xor((s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("b");
			return false;
		});
		Assertions.assertTrue(b.testChecked(string.get(), number.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		number.set(Math.random());
		a = (s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("a");
			return false;
		};
		b = a.xor((s, n) -> {
			Assertions.assertSame(string.get(), s);
			Assertions.assertSame(number.get(), n);
			callers.add("b");
			return false;
		});
		Assertions.assertFalse(b.testChecked(string.get(), number.get()));
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
			b = a.xor((s, n) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				callers.add("b");
				return true;
			});
			try {
				b.testChecked(string.get(), number.get());
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
				return true;
			};
			b = a.xor((s, n) -> {
				Assertions.assertSame(string.get(), s);
				Assertions.assertSame(number.get(), n);
				callers.add("b");
				throw thrown;
			});
			try {
				b.testChecked(string.get(), number.get());
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
			CheckedBiPredicate<String, Double, Throwable> n = (x, y) -> false;
			Assertions.assertThrows(NullPointerException.class, () -> n.xor(null));
		}
	}
}
