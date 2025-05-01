/*******************************************************************************
 * #%L
 * Armedia Commons Utilities
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
package com.armedia.commons.utilities.io;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
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
	public void testGetChannel() throws Exception {
		ReadableByteChannel c = EasyMock.createStrictMock(ReadableByteChannel.class);
		EasyMock.reset(c);
		c.close();
		EasyMock.expectLastCall().once();
		EasyMock.replay(c);
		try (BoundedReadableByteChannel brbc = new BoundedReadableByteChannel(c, 100)) {
			Assertions.assertSame(c, brbc.getWrapped());
		}
		EasyMock.verify(c);
	}

	@Test
	public void testGetLimit() throws Exception {
		ReadableByteChannel c = EasyMock.createStrictMock(ReadableByteChannel.class);
		for (long i = 0; i < 1000; i++) {
			EasyMock.reset(c);
			c.close();
			EasyMock.expectLastCall().once();
			EasyMock.replay(c);
			try (BoundedReadableByteChannel brbc = new BoundedReadableByteChannel(c, i)) {
				if (i < 0) {
					Assertions.assertEquals(0, brbc.getLimit());
				} else {
					Assertions.assertEquals(i, brbc.getLimit());
				}
			}
			EasyMock.verify(c);
		}
	}

	@Test
	public void testGetRemaining() throws Exception {
		byte[] data = new byte[256];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) i;
		}

		ByteBuffer buf = ByteBuffer.allocate(1);
		try (ReadableByteChannel c = Channels.newChannel(new ByteArrayInputStream(data))) {
			try (BoundedReadableByteChannel brbc = new BoundedReadableByteChannel(c, data.length)) {
				long remaining = data.length;
				while (true) {
					buf.clear();
					Assertions.assertEquals(remaining, brbc.getRemaining());
					int r = brbc.read(buf);
					if (r < 0) {
						break;
					}
					remaining -= r;
					Assertions.assertEquals(remaining, brbc.getRemaining());
				}
			}
		}
	}

	@Test
	public void testIsOpen() throws Exception {
		ReadableByteChannel c = EasyMock.createStrictMock(ReadableByteChannel.class);
		EasyMock.reset(c);
		EasyMock.expect(c.isOpen()).andReturn(true).once();
		c.close();
		EasyMock.expectLastCall().once();
		EasyMock.replay(c);
		try (BoundedReadableByteChannel brbc = new BoundedReadableByteChannel(c, 100)) {
			Assertions.assertTrue(brbc.isOpen());
		}
		EasyMock.verify(c);

		EasyMock.reset(c);
		EasyMock.expect(c.isOpen()).andReturn(false).once();
		c.close();
		EasyMock.expectLastCall().once();
		EasyMock.replay(c);
		try (BoundedReadableByteChannel brbc = new BoundedReadableByteChannel(c, 100)) {
			Assertions.assertFalse(brbc.isOpen());
		}
		EasyMock.verify(c);
	}

	@Test
	public void testRead() throws Exception {
		byte[] data = new byte[256];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) i;
		}

		ByteBuffer buf = ByteBuffer.allocate(64);
		try (ReadableByteChannel c = Channels.newChannel(new ByteArrayInputStream(data))) {
			try (BoundedReadableByteChannel brbc = new BoundedReadableByteChannel(c, 100)) {
				buf.clear();
				buf.limit(0);
				Assertions.assertEquals(0, brbc.read(buf));
			}
		}

		for (long limit = 0; limit < data.length; limit++) {
			try (ReadableByteChannel c = Channels.newChannel(new ByteArrayInputStream(data))) {
				try (BoundedReadableByteChannel brbc = new BoundedReadableByteChannel(c, limit)) {
					System.out.printf("Testing with limit = %d%n", limit);
					long totalRead = 0;
					int pos = 0;
					while (true) {
						buf.clear();
						int r = brbc.read(buf);
						if (r < 0) {
							break;
						}
						totalRead += r;
						// Ensure the bytes read were the correct bytes
						buf.flip();
						for (int i = 0; i < r; i++) {
							Assertions.assertEquals(data[pos], buf.get(),
								String.format("Mismatch at position %d (limit = %d)", pos, limit));
							pos++;
						}
					}
					Assertions.assertEquals(totalRead, limit);
				}
			}
		}
	}

	@Test
	public void testClose() throws Exception {
		ReadableByteChannel c = EasyMock.createStrictMock(ReadableByteChannel.class);
		EasyMock.reset(c);
		c.close();
		EasyMock.expectLastCall().once();
		EasyMock.replay(c);
		try (BoundedReadableByteChannel brbc = new BoundedReadableByteChannel(c, 100)) {
		}
		EasyMock.verify(c);
	}

}
