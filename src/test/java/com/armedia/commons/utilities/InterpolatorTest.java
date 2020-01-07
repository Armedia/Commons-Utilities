/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2020 Armedia, LLC
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.Interpolator.FailMode;

public class InterpolatorTest {

	@Test
	public void testInterpolator() {
		Interpolator i = new Interpolator();

		Assertions.assertEquals(Interpolator.DEFAULT_FAIL, i.getFailMode());
		Assertions.assertEquals(Interpolator.DEFAULT_PREFIX, i.getPrefix());
		Assertions.assertEquals(Interpolator.DEFAULT_SUFFIX, i.getSuffix());
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
					Assertions.assertEquals(failMode, i.getFailMode());
					Assertions.assertEquals(prefix, i.getPrefix());
					Assertions.assertEquals(suffix, i.getSuffix());
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
		Assertions.assertEquals(expected, actual);
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
