/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2021 Armedia, LLC
 * %%
 * This file is part of the Caliente software.
 * 
 * If the software was purchased under a paid Caliente license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * 
 * Caliente is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Caliente is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Caliente. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 *******************************************************************************/
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
