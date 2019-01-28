package com.armedia.commons.utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Test;

import com.armedia.commons.utilities.Interpolator.FailMode;

public class InterpolatorTest {

	@Test
	public void testInterpolator() {
		Interpolator i = new Interpolator();

		Assert.assertEquals(Interpolator.DEFAULT_FAIL, i.getFailMode());
		Assert.assertEquals(Interpolator.DEFAULT_PREFIX, i.getPrefix());
		Assert.assertEquals(Interpolator.DEFAULT_SUFFIX, i.getSuffix());
	}

	@Test
	public void testInterpolatorFailModeStringString() {
		String[] prefixes = {
			UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(),
			UUID.randomUUID().toString()
		};
		String[] suffixes = {
			UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(),
			UUID.randomUUID().toString()
		};

		for (String prefix : prefixes) {
			for (String suffix : suffixes) {
				for (FailMode failMode : FailMode.values()) {
					Interpolator i = new Interpolator(failMode, prefix, suffix);
					Assert.assertEquals(failMode, i.getFailMode());
					Assert.assertEquals(prefix, i.getPrefix());
					Assert.assertEquals(suffix, i.getSuffix());
				}
			}
		}
	}

	@Test
	public void testInterpolateString() {
		final Map<String, String> values = new HashMap<>();
		String[] names = {
			"alpha", "bravo", "charlie", "delta", "echo", "foxtrot", "golf", "hotel", "india", "juliett", "kilo",
			"lima", "mike", "november", "oscar", "papa", "romeo", "sierra", "tango", "uniform", "victor", "whiskey",
			"xray", "yankee", "zulu", "zero", "one", "two", "three", "four", "fiver", "sixer", "seven", "eight",
			"niner", "hundred", "thousand"
		};

		for (String n : names) {
			values.put(n, DigestUtils.md5Hex(n));
		}
		Interpolator i = new Interpolator();

		String source = "The quick brown @@[alpha]@@ jumped over the crazy @@[seven]@@ @@[crap]@@ end of line";
		String expected = "The quick brown 2c1743a391305fbf367df8e4f069f9f9 jumped over the crazy bb3aec0fdcdbc2974890f805c585d432 @@[crap]@@ end of line";
		String actual = i.interpolate((s) -> values.get(s), source);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testInterpolateInputStreamOutputStream() {
	}

	@Test
	public void testInterpolateInputStreamOutputStreamString() {
	}

	@Test
	public void testInterpolateInputStreamOutputStreamCharset() {
	}

	@Test
	public void testInterpolateReaderWriter() {
	}

}