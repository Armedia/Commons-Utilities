package com.armedia.commons.utilities.line;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import org.apache.commons.io.input.NullReader;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReaderLineSourceTest {

	@Test
	public void testConstructors() throws IOException {
		Assertions.assertThrows(NullPointerException.class, () -> new ReaderLineSource(null, null));
		Assertions.assertThrows(IllegalArgumentException.class, () -> new ReaderLineSource("", null));
		Assertions.assertThrows(NullPointerException.class, () -> new ReaderLineSource("a", null));
		Assertions.assertThrows(NullPointerException.class, () -> new ReaderLineSource(null, new NullReader(0)));

		Assertions.assertThrows(NullPointerException.class, () -> new ReaderLineSource(null, null, false));
		Assertions.assertThrows(IllegalArgumentException.class, () -> new ReaderLineSource("", null, false));
		Assertions.assertThrows(NullPointerException.class, () -> new ReaderLineSource("a", null, false));
		Assertions.assertThrows(NullPointerException.class, () -> new ReaderLineSource(null, new NullReader(0), false));

		Assertions.assertThrows(NullPointerException.class, () -> new ReaderLineSource(null, null, true));
		Assertions.assertThrows(IllegalArgumentException.class, () -> new ReaderLineSource("", null, true));
		Assertions.assertThrows(NullPointerException.class, () -> new ReaderLineSource("a", null, true));
		Assertions.assertThrows(NullPointerException.class, () -> new ReaderLineSource(null, new NullReader(0), true));

		try (ReaderLineSource isls = new ReaderLineSource("a", new NullReader(0))) {
		}
		try (ReaderLineSource isls = new ReaderLineSource("a", new NullReader(0), false)) {
		}
		try (ReaderLineSource isls = new ReaderLineSource("a", new NullReader(0), true)) {
		}
	}

	@Test
	public void testLoad() throws IOException, LineSourceException {
		Reader in = EasyMock.createStrictMock(Reader.class);
		EasyMock.reset(in);
		in.close();
		EasyMock.expectLastCall().once();
		EasyMock.replay(in);
		try (ReaderLineSource isls = new ReaderLineSource("a", in, true)) {
			Iterable<String> iterable = isls.load();
			Assertions.assertNotNull(iterable);
			Iterator<String> it = iterable.iterator();
			Assertions.assertNotNull(it);
		}
		EasyMock.verify(in);
	}

	@Test
	public void testClose() throws Exception {
		Reader in = EasyMock.createStrictMock(Reader.class);

		EasyMock.reset(in);
		in.close();
		EasyMock.expectLastCall().once();
		EasyMock.replay(in);
		try (ReaderLineSource isls = new ReaderLineSource("a", in, true)) {
		}
		EasyMock.verify(in);

		EasyMock.reset(in);
		EasyMock.replay(in);
		try (ReaderLineSource isls = new ReaderLineSource("a", in, false)) {
		}
		EasyMock.verify(in);
	}
}