package com.armedia.commons.utilities.function;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CheckedSupplierTest {

	@Test
	public void testGetChecked() throws Throwable {
		CheckedSupplier<UUID, Throwable> f = null;

		final AtomicReference<UUID> uuid = new AtomicReference<>(null);

		uuid.set(UUID.randomUUID());
		f = () -> uuid.get();
		Assertions.assertSame(uuid.get(), f.getChecked());

		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		f = () -> {
			throw thrown;
		};
		try {
			f.getChecked();
			Assertions.fail("Did not raise the cascaded exception");
		} catch (Throwable t) {
			Assertions.assertSame(thrown, t);
		}
	}

	@Test
	public void testGet() {
		CheckedSupplier<UUID, Throwable> f = null;

		final AtomicReference<UUID> uuid = new AtomicReference<>(null);

		uuid.set(UUID.randomUUID());
		f = () -> uuid.get();
		Assertions.assertSame(uuid.get(), f.get());

		uuid.set(UUID.randomUUID());
		final Throwable thrown = new Throwable(UUID.randomUUID().toString());
		f = () -> {
			throw thrown;
		};
		try {
			f.get();
			Assertions.fail("Did not raise the cascaded exception");
		} catch (RuntimeException e) {
			Assertions.assertNotSame(thrown, e);
			Assertions.assertEquals(thrown.getMessage(), e.getMessage());
			Assertions.assertSame(thrown, e.getCause());
		}
	}
}