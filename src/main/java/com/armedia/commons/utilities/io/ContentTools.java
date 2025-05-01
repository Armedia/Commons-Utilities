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
package com.armedia.commons.utilities.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.function.Predicate;

import org.apache.commons.io.FileUtils;

public class ContentTools {
	public static final int MIN_BUFFER_SIZE = (4 * (int) FileUtils.ONE_KB);
	public static final int DEF_BUFFER_SIZE = (32 * (int) FileUtils.ONE_KB);

	public static long copy(ReadableByteChannel in, WritableByteChannel out) throws IOException {
		return ContentTools.copy(in, out, null);
	}

	public static long copy(ReadableByteChannel in, WritableByteChannel out, Predicate<ByteBuffer> writeFilter)
		throws IOException {
		return ContentTools.copy(in, out, ContentTools.DEF_BUFFER_SIZE, writeFilter);
	}

	public static long copy(ReadableByteChannel in, WritableByteChannel out, int bufSize) throws IOException {
		return ContentTools.copy(in, out, bufSize, null);
	}

	public static long copy(ReadableByteChannel in, WritableByteChannel out, int bufSize,
		Predicate<ByteBuffer> writeFilter) throws IOException {
		long ret = 0;
		final ByteBuffer buf = ByteBuffer.allocate(Math.max(1024, bufSize));
		while (true) {
			buf.clear();
			final int read = in.read(buf);
			if (read < 0) { return ret; }
			if (read > 0) {
				buf.flip();
				if ((writeFilter != null) && !writeFilter.test(buf.asReadOnlyBuffer())) { return ret; }
				int written = 0;
				// Is this the right way to do it? Maybe do a timeout?
				boolean yield = false;
				while (buf.hasRemaining()) {
					if (yield) {
						// Be a good citizen... but only after a "missed" write
						yield = false;
						Thread.yield();
					}
					final int w = out.write(buf);
					written += w;
					yield = (w == 0);
				}
				ret += written;
			}
		}
	}
}
