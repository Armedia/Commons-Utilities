/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2025 Armedia, LLC
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
package com.armedia.commons.utilities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourceLoaderExceptionTest {

	@Test
	public void testResourceLoaderException() {
		new ResourceLoaderException();
	}

	@Test
	public void testResourceLoaderExceptionString() {
		for (int i = 0; i < 10; i++) {
			String msg = String.format("%02d", i);
			ResourceLoaderException ex = new ResourceLoaderException(msg);
			Assertions.assertEquals(msg, ex.getMessage());
		}
	}

	@Test
	public void testResourceLoaderExceptionThrowable() {
		Throwable cause = new Throwable();
		ResourceLoaderException ex = new ResourceLoaderException(cause);
		Assertions.assertSame(cause, ex.getCause());
	}

	@Test
	public void testResourceLoaderExceptionStringThrowable() {
		Throwable cause = new Throwable();
		for (int i = 0; i < 10; i++) {
			String msg = String.format("%02d", i);
			ResourceLoaderException ex = new ResourceLoaderException(msg, cause);
			Assertions.assertEquals(msg, ex.getMessage());
			Assertions.assertSame(cause, ex.getCause());
		}
	}

	@Test
	public void testResourceLoaderExceptionStringThrowableBooleanBoolean() {
		Throwable cause = new Throwable();
		ResourceLoaderException ex = null;
		for (int i = 0; i < 10; i++) {
			String msg = String.format("%02d", i);

			ex = new ResourceLoaderException(msg, cause, false, false);
			Assertions.assertEquals(msg, ex.getMessage());
			Assertions.assertSame(cause, ex.getCause());

			ex = new ResourceLoaderException(msg, cause, false, true);
			Assertions.assertEquals(msg, ex.getMessage());
			Assertions.assertSame(cause, ex.getCause());

			ex = new ResourceLoaderException(msg, cause, true, false);
			Assertions.assertEquals(msg, ex.getMessage());
			Assertions.assertSame(cause, ex.getCause());

			ex = new ResourceLoaderException(msg, cause, false, true);
			Assertions.assertEquals(msg, ex.getMessage());
			Assertions.assertSame(cause, ex.getCause());
		}
	}
}
