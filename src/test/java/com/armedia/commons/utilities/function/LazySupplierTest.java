package com.armedia.commons.utilities.function;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

public class LazySupplierTest {

	private static final CheckedSupplier<String> FAIL_SUPP = () -> {
		Assert.fail("This supplier should not have been invoked");
		return null;
	};

	@Test
	public void testLazySupplier() {
		LazySupplier<String> supplier = new LazySupplier<>();
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertNull(supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testLazySupplierCheckedSupplierOfT() {
		final String uuid = UUID.randomUUID().toString();
		final CheckedSupplier<String> uuidSupplier = () -> uuid;
		LazySupplier<String> supplier = null;

		supplier = new LazySupplier<>(uuidSupplier);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>((CheckedSupplier<String>) null);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertNull(supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testLazySupplierT() {
		final String uuid = UUID.randomUUID().toString();
		LazySupplier<String> supplier = null;

		supplier = new LazySupplier<>(uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>((String) null);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertNull(supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testLazySupplierCheckedSupplierOfTT() {
		String uuid = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		CheckedSupplier<String> uuidSupplier = () -> uuid2;
		LazySupplier<String> supplier = null;

		supplier = new LazySupplier<>(null, null);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertNull(supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(uuidSupplier, null);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(null, uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(uuidSupplier, uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get(LazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testGet() {
		String uuid = UUID.randomUUID().toString();
		LazySupplier<String> supplier = null;

		supplier = new LazySupplier<>();
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertNull(supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(() -> uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(() -> {
			throw new Exception(uuid);
		});
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		try {
			supplier.get();
			Assert.fail("Did not raise an explicit exception");
		} catch (Exception e) {
			Assert.assertSame(uuid, e.getMessage());
		}
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		AtomicBoolean called = new AtomicBoolean(false);
		Assert.assertSame(uuid, supplier.get(() -> {
			called.set(true);
			return uuid;
		}));
		Assert.assertTrue(called.get());
	}

	@Test
	public void testGetSupplierOfT() {
		String uuid = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		LazySupplier<String> supplier = null;

		supplier = new LazySupplier<>();
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(() -> uuid));
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get(() -> uuid2));
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(null));
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>();
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		try {
			supplier.get(() -> {
				throw new RuntimeException(uuid);
			});
			Assert.fail("Did not raise an explicit exception");
		} catch (Exception e) {
			Assert.assertSame(uuid, e.getMessage());
		}
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		AtomicBoolean called = new AtomicBoolean(false);
		Assert.assertSame(uuid, supplier.get(() -> {
			called.set(true);
			return uuid;
		}));
		Assert.assertTrue(called.get());
	}

	@Test
	public void testGetChecked() throws Exception {
		String uuid = UUID.randomUUID().toString();
		LazySupplier<String> supplier = null;

		supplier = new LazySupplier<>();
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertNull(supplier.getChecked());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.getChecked());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(() -> uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.getChecked());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(() -> {
			throw new Exception(uuid);
		});
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		try {
			supplier.getChecked();
			Assert.fail("Did not raise an explicit exception");
		} catch (Exception e) {
			Assert.assertSame(uuid, e.getMessage());
		}
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		AtomicBoolean called = new AtomicBoolean(false);
		Assert.assertSame(uuid, supplier.get(() -> {
			called.set(true);
			return uuid;
		}));
		Assert.assertTrue(called.get());
	}

	@Test
	public void testGetCheckedCheckedSupplierOfT() throws Exception {
		String uuid = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		LazySupplier<String> supplier = null;

		supplier = new LazySupplier<>();
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(() -> uuid));
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get(() -> uuid2));
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>(uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(null));
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = new LazySupplier<>();
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		try {
			supplier.getChecked(() -> {
				throw new Exception(uuid);
			});
			Assert.fail("Did not raise an explicit exception");
		} catch (Exception e) {
			Assert.assertSame(uuid, e.getMessage());
		}
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		AtomicBoolean called = new AtomicBoolean(false);
		Assert.assertSame(uuid, supplier.get(() -> {
			called.set(true);
			return uuid;
		}));
		Assert.assertTrue(called.get());
	}

	@Test
	public void testFromSupplierSupplierOfT() {
		final String uuid = UUID.randomUUID().toString();
		final Supplier<String> uuidSupplier = () -> uuid;
		LazySupplier<String> supplier = null;

		supplier = LazySupplier.fromSupplier(uuidSupplier);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = LazySupplier.fromSupplier(null);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertNull(supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testFromSupplierSupplierOfTT() {
		String uuid = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		Supplier<String> uuidSupplier = () -> uuid2;
		LazySupplier<String> supplier = null;

		supplier = LazySupplier.fromSupplier(null, null);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertNull(supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertNull(supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = LazySupplier.fromSupplier(uuidSupplier, null);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = LazySupplier.fromSupplier(null, uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertTrue(supplier.isDefaulted());
		Assert.assertSame(uuid, supplier.get(LazySupplierTest.FAIL_SUPP));

		supplier = LazySupplier.fromSupplier(uuidSupplier, uuid);
		Assert.assertFalse(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get());
		Assert.assertTrue(supplier.isInitialized());
		Assert.assertFalse(supplier.isDefaulted());
		Assert.assertSame(uuid2, supplier.get(LazySupplierTest.FAIL_SUPP));
	}

	@Test
	public void testAwait() {
	}

	@Test
	public void testAwaitUninterruptibly() {
	}

	@Test
	public void testAwaitNanos() {
	}

	@Test
	public void testAwaitLongTimeUnit() {
	}

	@Test
	public void testAwaitUntil() {
	}
}