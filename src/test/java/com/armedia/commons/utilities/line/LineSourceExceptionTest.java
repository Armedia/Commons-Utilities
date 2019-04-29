package com.armedia.commons.utilities.line;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LineSourceExceptionTest {

	@Test
	public void testLineSourceException() {
		new LineSourceException();
	}

	@Test
	public void testLineSourceExceptionString() {
		for (int i = 0; i < 10; i++) {
			String msg = String.format("%02d", i);
			LineSourceException ex = new LineSourceException(msg);
			Assertions.assertEquals(msg, ex.getMessage());
		}
	}

	@Test
	public void testLineSourceExceptionThrowable() {
		Throwable cause = new Throwable();
		LineSourceException ex = new LineSourceException(cause);
		Assertions.assertSame(cause, ex.getCause());
	}

	@Test
	public void testLineSourceExceptionStringThrowable() {
		Throwable cause = new Throwable();
		for (int i = 0; i < 10; i++) {
			String msg = String.format("%02d", i);
			LineSourceException ex = new LineSourceException(msg, cause);
			Assertions.assertEquals(msg, ex.getMessage());
			Assertions.assertSame(cause, ex.getCause());
		}
	}

	@Test
	public void testLineSourceExceptionStringThrowableBooleanBoolean() {
		Throwable cause = new Throwable();
		LineSourceException ex = null;
		for (int i = 0; i < 10; i++) {
			String msg = String.format("%02d", i);

			ex = new LineSourceException(msg, cause, false, false);
			Assertions.assertEquals(msg, ex.getMessage());
			Assertions.assertSame(cause, ex.getCause());

			ex = new LineSourceException(msg, cause, false, true);
			Assertions.assertEquals(msg, ex.getMessage());
			Assertions.assertSame(cause, ex.getCause());

			ex = new LineSourceException(msg, cause, true, false);
			Assertions.assertEquals(msg, ex.getMessage());
			Assertions.assertSame(cause, ex.getCause());

			ex = new LineSourceException(msg, cause, false, true);
			Assertions.assertEquals(msg, ex.getMessage());
			Assertions.assertSame(cause, ex.getCause());
		}
	}
}