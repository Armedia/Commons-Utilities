package com.armedia.commons.utilities;

import java.nio.channels.ReadableByteChannel;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BoundedReadableByteChannelTest {

	@Test
	public void testBoundedReadableByteChannel() throws Exception {
		Assertions.assertThrows(NullPointerException.class, () -> new BoundedReadableByteChannel(null, 0));
		ReadableByteChannel c = EasyMock.createStrictMock(ReadableByteChannel.class);
		for (long i = -100; i < 100; i++) {
			EasyMock.reset(c);
			c.close();
			EasyMock.expectLastCall().once();
			EasyMock.replay(c);
			try (BoundedReadableByteChannel brbc = new BoundedReadableByteChannel(c, i)) {
				if (i <= 0) {
					Assertions.assertEquals(0, brbc.getLimit());
					Assertions.assertEquals(0, brbc.getRemaining());
				} else {
					Assertions.assertEquals(i, brbc.getLimit());
					Assertions.assertEquals(i, brbc.getRemaining());
				}
			}
			EasyMock.verify(c);
		}
	}

	@Test
	public void testIsOpen() {
		Assertions.fail("Not yet implemented");
	}

	@Test
	public void testRead() {
		Assertions.fail("Not yet implemented");
	}

	@Test
	public void testClose() {
		Assertions.fail("Not yet implemented");
	}

}