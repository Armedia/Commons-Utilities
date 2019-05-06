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