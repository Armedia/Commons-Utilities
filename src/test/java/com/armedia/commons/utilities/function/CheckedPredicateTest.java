package com.armedia.commons.utilities.function;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CheckedPredicateTest {

	@Test
	public void testApplyChecked() throws Throwable {
		CheckedPredicate<String, Throwable> p = null;

		final Random r = new Random(System.nanoTime());
		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Boolean> bool = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		bool.set(r.nextBoolean());
		p = (s) -> {
			Assertions.assertSame(string.get(), s);
			return bool.get();
		};
		Assertions.assertEquals(bool.get(), p.testChecked(string.get()));

		string.set(UUID.randomUUID().toString());
		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		p = (s) -> {
			Assertions.assertSame(string.get(), s);
			throw thrown;
		};
		try {
			p.testChecked(string.get());
			Assertions.fail("Did not raise the cascaded exception");
		} catch (Throwable t) {
			Assertions.assertSame(thrown, t);
		}
	}

	@Test
	public void testApply() {
		CheckedPredicate<String, Throwable> p = null;

		final Random r = new Random(System.nanoTime());
		final AtomicReference<String> string = new AtomicReference<>(null);
		final AtomicReference<Boolean> bool = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		bool.set(r.nextBoolean());
		p = (s) -> {
			Assertions.assertSame(string.get(), s);
			return bool.get();
		};
		Assertions.assertSame(bool.get(), p.test(string.get()));

		string.set(UUID.randomUUID().toString());
		bool.set(r.nextBoolean());
		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		p = (s) -> {
			Assertions.assertSame(string.get(), s);
			throw thrown;
		};
		try {
			p.test(string.get());
			Assertions.fail("Did not raise the cascaded exception");
		} catch (RuntimeException e) {
			Assertions.assertNotSame(thrown, e);
			Assertions.assertEquals(thrown.getMessage(), e.getMessage());
			Assertions.assertSame(thrown, e.getCause());
		}
	}

	@Test
	public void testAnd() throws Throwable {
		CheckedPredicate<String, Throwable> a = null;
		CheckedPredicate<String, Throwable> b = null;

		final List<String> callers = new ArrayList<>();
		final AtomicReference<String> string = new AtomicReference<>(null);

		callers.clear();
		string.set(UUID.randomUUID().toString());
		a = (s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("a");
			return true;
		};
		b = a.and((s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("b");
			return true;
		});
		Assertions.assertTrue(b.testChecked(string.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		a = (s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("a");
			return false;
		};
		b = a.and((s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("b");
			return true;
		});
		Assertions.assertFalse(b.testChecked(string.get()));
		Assertions.assertEquals(1, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		a = (s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("a");
			return true;
		};
		b = a.and((s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("b");
			return false;
		});
		Assertions.assertFalse(b.testChecked(string.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		a = (s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("a");
			return false;
		};
		b = a.and((s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("b");
			return false;
		});
		Assertions.assertFalse(b.testChecked(string.get()));
		Assertions.assertEquals(1, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a"
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
			b = a.and((s) -> {
				Assertions.assertSame(string.get(), s);
				callers.add("b");
				return true;
			});
			try {
				b.testChecked(string.get());
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
				return true;
			};
			b = a.and((s) -> {
				Assertions.assertSame(string.get(), s);
				callers.add("b");
				throw thrown;
			});
			try {
				b.testChecked(string.get());
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
			CheckedPredicate<String, Throwable> n = (x) -> false;
			Assertions.assertThrows(NullPointerException.class, () -> n.and(null));
		}
	}

	@Test
	public void testOr() throws Throwable {
		CheckedPredicate<String, Throwable> a = null;
		CheckedPredicate<String, Throwable> b = null;

		final List<String> callers = new ArrayList<>();
		final AtomicReference<String> string = new AtomicReference<>(null);

		callers.clear();
		string.set(UUID.randomUUID().toString());
		a = (s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("a");
			return true;
		};
		b = a.or((s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("b");
			return true;
		});
		Assertions.assertTrue(b.testChecked(string.get()));
		Assertions.assertEquals(1, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		a = (s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("a");
			return false;
		};
		b = a.or((s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("b");
			return true;
		});
		Assertions.assertTrue(b.testChecked(string.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		a = (s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("a");
			return true;
		};
		b = a.or((s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("b");
			return false;
		});
		Assertions.assertTrue(b.testChecked(string.get()));
		Assertions.assertEquals(1, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		a = (s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("a");
			return false;
		};
		b = a.or((s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("b");
			return false;
		});
		Assertions.assertFalse(b.testChecked(string.get()));
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
			b = a.or((s) -> {
				Assertions.assertSame(string.get(), s);
				callers.add("b");
				return true;
			});
			try {
				b.testChecked(string.get());
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
				return false;
			};
			b = a.or((s) -> {
				Assertions.assertSame(string.get(), s);
				callers.add("b");
				throw thrown;
			});
			try {
				b.testChecked(string.get());
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
			CheckedPredicate<String, Throwable> n = (x) -> false;
			Assertions.assertThrows(NullPointerException.class, () -> n.or(null));
		}
	}

	@Test
	public void testNegate() throws Throwable {
		CheckedPredicate<String, Throwable> a = null;

		final AtomicReference<String> string = new AtomicReference<>(null);

		string.set(UUID.randomUUID().toString());
		a = (s) -> {
			Assertions.assertSame(string.get(), s);
			return true;
		};
		Assertions.assertFalse(a.negate().test(string.get()));

		string.set(UUID.randomUUID().toString());
		a = (s) -> {
			Assertions.assertSame(string.get(), s);
			return false;
		};
		Assertions.assertTrue(a.negate().test(string.get()));

		{
			final Throwable thrown = new Throwable(UUID.randomUUID().toString());
			string.set(UUID.randomUUID().toString());
			a = (s) -> {
				Assertions.assertSame(string.get(), s);
				throw thrown;
			};
			try {
				a.negate().testChecked(string.get());
				Assertions.fail("Did not raise the chained exception");
			} catch (Throwable t) {
				Assertions.assertSame(thrown, t);
			}
		}
	}

	@Test
	public void testXor() throws Throwable {
		CheckedPredicate<String, Throwable> a = null;
		CheckedPredicate<String, Throwable> b = null;

		final List<String> callers = new ArrayList<>();
		final AtomicReference<String> string = new AtomicReference<>(null);

		callers.clear();
		string.set(UUID.randomUUID().toString());
		a = (s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("a");
			return true;
		};
		b = a.xor((s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("b");
			return true;
		});
		Assertions.assertFalse(b.testChecked(string.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		a = (s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("a");
			return false;
		};
		b = a.xor((s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("b");
			return true;
		});
		Assertions.assertTrue(b.testChecked(string.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		a = (s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("a");
			return true;
		};
		b = a.xor((s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("b");
			return false;
		});
		Assertions.assertTrue(b.testChecked(string.get()));
		Assertions.assertEquals(2, callers.size());
		Assertions.assertArrayEquals(new String[] {
			"a", "b"
		}, callers.toArray());

		callers.clear();
		string.set(UUID.randomUUID().toString());
		a = (s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("a");
			return false;
		};
		b = a.xor((s) -> {
			Assertions.assertSame(string.get(), s);
			callers.add("b");
			return false;
		});
		Assertions.assertFalse(b.testChecked(string.get()));
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
			b = a.xor((s) -> {
				Assertions.assertSame(string.get(), s);
				callers.add("b");
				return true;
			});
			try {
				b.testChecked(string.get());
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
				return true;
			};
			b = a.xor((s) -> {
				Assertions.assertSame(string.get(), s);
				callers.add("b");
				throw thrown;
			});
			try {
				b.testChecked(string.get());
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
			CheckedPredicate<String, Throwable> n = (x) -> false;
			Assertions.assertThrows(NullPointerException.class, () -> n.xor(null));
		}
	}
}
