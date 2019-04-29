package com.armedia.commons.utilities.line;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LineSourceTest {

	private static class TestLineSource extends LineSource {
		public TestLineSource(String id, boolean supportsContinuation) {
			super(id, supportsContinuation);
		}

		public TestLineSource(String id) {
			super(id);
		}

		@Override
		public Iterable<String> load() throws LineSourceException {
			return Collections.emptyList();
		}
	}

	@Test
	public void testConstructors() throws Exception {
		Assertions.assertThrows(NullPointerException.class, () -> new TestLineSource(null));
		Assertions.assertThrows(IllegalArgumentException.class, () -> new TestLineSource(""));
		try (LineSource ls = new TestLineSource("a")) {
		}

		Assertions.assertThrows(NullPointerException.class, () -> new TestLineSource(null, false));
		Assertions.assertThrows(IllegalArgumentException.class, () -> new TestLineSource("", false));
		try (LineSource ls = new TestLineSource("a", false)) {
		}

		Assertions.assertThrows(NullPointerException.class, () -> new TestLineSource(null, true));
		Assertions.assertThrows(IllegalArgumentException.class, () -> new TestLineSource("", true));
		try (LineSource ls = new TestLineSource("a", true)) {
		}
	}

	@Test
	public void testGetId() throws Exception {
		for (int i = 0; i < 10; i++) {
			String id = String.format("%02d", i);
			try (LineSource ls = new TestLineSource(id)) {
				Assertions.assertEquals(id, ls.getId());
			}
		}
	}

	@Test
	public void testIsSupportsContinuation() throws Exception {
		try (LineSource ls = new TestLineSource("abc", true)) {
			Assertions.assertTrue(ls.isSupportsContinuation());
		}
		try (LineSource ls = new TestLineSource("abc", false)) {
			Assertions.assertFalse(ls.isSupportsContinuation());
		}
	}

	@Test
	public void testLoad() throws Exception {
		try (LineSource ls = new TestLineSource("a", true)) {
			Iterable<String> iterable = ls.load();
			Assertions.assertNotNull(iterable);
			Iterator<String> it = iterable.iterator();
			Assertions.assertNotNull(it);
		}
	}

	@Test
	public void testClose() throws Exception {
		try (LineSource isls = new TestLineSource("a", true)) {
		}
		try (LineSource isls = new TestLineSource("a", false)) {
		}
	}

	@Test
	public void testWrap() throws Exception {
		Collection<String> c = Collections.emptyList();

		Assertions.assertThrows(NullPointerException.class, () -> LineSource.wrap(null, null));
		Assertions.assertThrows(NullPointerException.class, () -> LineSource.wrap(null, c));
		Assertions.assertThrows(NullPointerException.class, () -> LineSource.wrap("", null));
		Assertions.assertThrows(IllegalArgumentException.class, () -> LineSource.wrap("", c));
		Assertions.assertThrows(NullPointerException.class, () -> LineSource.wrap("a", null));

		Assertions.assertThrows(NullPointerException.class, () -> LineSource.wrap(null, null, false));
		Assertions.assertThrows(NullPointerException.class, () -> LineSource.wrap(null, c, false));
		Assertions.assertThrows(NullPointerException.class, () -> LineSource.wrap("", null, false));
		Assertions.assertThrows(IllegalArgumentException.class, () -> LineSource.wrap("", c, false));
		Assertions.assertThrows(NullPointerException.class, () -> LineSource.wrap("a", null, false));

		Assertions.assertThrows(NullPointerException.class, () -> LineSource.wrap(null, null, true));
		Assertions.assertThrows(NullPointerException.class, () -> LineSource.wrap(null, c, true));
		Assertions.assertThrows(NullPointerException.class, () -> LineSource.wrap("", null, true));
		Assertions.assertThrows(IllegalArgumentException.class, () -> LineSource.wrap("", c, true));
		Assertions.assertThrows(NullPointerException.class, () -> LineSource.wrap("a", null, true));

		for (int i = 0; i < 10; i++) {
			String id = String.format("%02d", i);
			try (LineSource ls = LineSource.wrap(id, c)) {
				Assertions.assertEquals(id, ls.getId());
				Assertions.assertFalse(ls.isSupportsContinuation());
				Assertions.assertSame(c, ls.load());
			}
			try (LineSource ls = LineSource.wrap(id, c, false)) {
				Assertions.assertEquals(id, ls.getId());
				Assertions.assertFalse(ls.isSupportsContinuation());
				Assertions.assertSame(c, ls.load());
			}
			try (LineSource ls = LineSource.wrap(id, c, true)) {
				Assertions.assertEquals(id, ls.getId());
				Assertions.assertTrue(ls.isSupportsContinuation());
				Assertions.assertSame(c, ls.load());
			}
		}

	}
}