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
package com.armedia.commons.utilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BucketHasherTest {

	@Test
	public void testHash() throws IOException {
		final byte[] nullB = null;
		final CharSequence nullStr = null;
		final ByteBuffer nullBuffer = null;
		final InputStream nullIn = null;

		Assertions.assertEquals(-1L, BucketHasher.hash(nullB));
		Assertions.assertEquals(-1L, BucketHasher.hash(nullB, 0));
		Assertions.assertEquals(-1L, BucketHasher.hash(nullB, 0, 0));
		Assertions.assertEquals(-1L, BucketHasher.hash(nullStr));
		Assertions.assertEquals(-1L, BucketHasher.hash(nullStr, 0));
		Assertions.assertEquals(-1L, BucketHasher.hash(nullStr, 0, 0));
		Assertions.assertEquals(-1L, BucketHasher.hash(nullBuffer));
		Assertions.assertEquals(-1L, BucketHasher.hash(nullBuffer, 0));
		Assertions.assertEquals(-1L, BucketHasher.hash(nullBuffer, 0, 0));
		Assertions.assertEquals(-1L, BucketHasher.hash(nullIn));
		Assertions.assertEquals(-1L, BucketHasher.hash(nullIn, 0));
		Assertions.assertEquals(-1L, BucketHasher.hash(nullIn, 0, 0));

		byte[] b = new byte[4];
		CharSequence str = "";
		ByteBuffer buf = ByteBuffer.allocate(4);
		InputStream in = new ByteArrayInputStream(b);

		Assertions.assertEquals(0L, BucketHasher.hash(b, 0));
		Assertions.assertEquals(0L, BucketHasher.hash(b, 0, 0));
		Assertions.assertEquals(0L, BucketHasher.hash(str, 0));
		Assertions.assertEquals(0L, BucketHasher.hash(str, 0, 0));
		Assertions.assertEquals(0L, BucketHasher.hash(buf, 0));
		Assertions.assertEquals(0L, BucketHasher.hash(buf, 0, 0));
		Assertions.assertEquals(0L, BucketHasher.hash(in, 0));
		Assertions.assertEquals(0L, BucketHasher.hash(in, 0, 0));

		Assertions.assertThrows(IllegalArgumentException.class, () -> BucketHasher.hash(1, Long.MIN_VALUE));
		Assertions.assertThrows(IllegalArgumentException.class, () -> BucketHasher.hash(1, -1L));
		Assertions.assertThrows(IllegalArgumentException.class, () -> BucketHasher.hash(1, 4294967296L));
		Assertions.assertThrows(IllegalArgumentException.class, () -> BucketHasher.hash(1, Long.MAX_VALUE));

		Assertions.assertThrows(IllegalArgumentException.class,
			() -> BucketHasher.hash(1, BucketHasher.DEF_BUCKET, Long.MIN_VALUE));
		Assertions.assertThrows(IllegalArgumentException.class,
			() -> BucketHasher.hash(1, BucketHasher.DEF_BUCKET, -1L));
		Assertions.assertThrows(IllegalArgumentException.class,
			() -> BucketHasher.hash(1, BucketHasher.DEF_BUCKET, 4294967296L));
		Assertions.assertThrows(IllegalArgumentException.class,
			() -> BucketHasher.hash(1, BucketHasher.DEF_BUCKET, Long.MAX_VALUE));
		Assertions.assertEquals(3509391659L, BucketHasher.hash(1, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED));
		Assertions.assertEquals(2441322222L, BucketHasher.hash(1, BucketHasher.DEF_BUCKET, 10L));
		Assertions.assertEquals(3501171530L, BucketHasher.hash(1, BucketHasher.DEF_BUCKET, BucketHasher.MAX_SEED));

		Collection<Triple<CharSequence, Long, Class<Exception>>> c = new LinkedList<>();
		c.add(Triple.of("010203", 2665938722L, null));
		c.add(Triple.of("abc", 1191608682L, null));
		for (Triple<CharSequence, Long, Class<Exception>> r : c) {
			Class<Exception> t = r.getRight();
			if (t != null) {
				Assertions.assertThrows(t, () -> BucketHasher.hash(r.getLeft()));
				continue;
			} else {
				Assertions.assertEquals(r.getMiddle(), BucketHasher.hash(r.getLeft()), r.getLeft().toString());
			}
		}
		b = new byte[3];
		b[0] = 1;
		b[1] = 2;
		b[2] = 3;
		Assertions.assertEquals(2562861693L, BucketHasher.hash(b));
	}

}
