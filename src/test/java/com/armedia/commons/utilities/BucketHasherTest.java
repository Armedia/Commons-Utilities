package com.armedia.commons.utilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BucketHasherTest {

	@Test
	public void testHash() throws IOException {
		final byte[] nullB = null;
		final String nullStr = null;
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
		String str = "";
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
	}

}