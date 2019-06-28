/*******************************************************************************
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (c) 2010 - 2019 Armedia LLC
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author drivera@armedia.com
 *
 */
public class FileNameToolsTest implements GoodService {

	@Test
	public void testConstructor() {
		new FileNameTools();
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.FileNameTools#removeEdgeSeparators(java.lang.String, char)}
	 * .
	 */
	@Test
	public void testRemoveEdgeSeparators() {
		String[][] testData = {
			{
				"|", "|", ""
			}, {
				"|", "asdf|fdas", "asdf|fdas"
			}, {
				"|", "|asdf|fdas", "asdf|fdas"
			}, {
				"|", "asdf|fdas|", "asdf|fdas"
			}, {
				"|", "|asdf|fdas|", "asdf|fdas"
			}, {
				"|", "|||||||||", ""
			}, {
				"|", "asdf|||||||||fdas", "asdf|||||||||fdas"
			}, {
				"|", "|||||||||asdf|||||||||fdas", "asdf|||||||||fdas"
			}, {
				"|", "asdf|||||||||fdas|||||||||", "asdf|||||||||fdas"
			}, {
				"|", "|||||||||asdf|||||||||fdas|||||||||", "asdf|||||||||fdas"
			},
		};
		for (String[] values : testData) {
			char sep = values[0].charAt(0);
			Assertions.assertEquals(values[2], FileNameTools.removeEdgeSeparators(values[1], sep));
			String a = values[1].replace('|', File.separatorChar);
			String b = values[2].replace('|', File.separatorChar);
			Assertions.assertEquals(b, FileNameTools.removeEdgeSeparators(a));
		}
		Assertions.assertNull(FileNameTools.removeEdgeSeparators(null));
		Assertions.assertNull(FileNameTools.removeEdgeSeparators(null, '|'));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.FileNameTools#removeLeadingSeparators(java.lang.String, char)}
	 * .
	 */
	@Test
	public void testRemoveLeadingSeparators() {
		String[][] testData = {
			{
				"|", "|", ""
			}, {
				"|", "asdf|fdas", "asdf|fdas"
			}, {
				"|", "|asdf|fdas", "asdf|fdas"
			}, {
				"|", "asdf|fdas|", "asdf|fdas|"
			}, {
				"|", "|asdf|fdas|", "asdf|fdas|"
			}, {
				"|", "|||||||||", ""
			}, {
				"|", "asdf|||||||||fdas", "asdf|||||||||fdas"
			}, {
				"|", "|||||||||asdf|||||||||fdas", "asdf|||||||||fdas"
			}, {
				"|", "asdf|||||||||fdas|||||||||", "asdf|||||||||fdas|||||||||"
			}, {
				"|", "|||||||||asdf|||||||||fdas|||||||||", "asdf|||||||||fdas|||||||||"
			},
		};
		for (String[] values : testData) {
			char sep = values[0].charAt(0);
			Assertions.assertEquals(values[2], FileNameTools.removeLeadingSeparators(values[1], sep));
			String a = values[1].replace('|', File.separatorChar);
			String b = values[2].replace('|', File.separatorChar);
			Assertions.assertEquals(b, FileNameTools.removeLeadingSeparators(a));
		}
		Assertions.assertNull(FileNameTools.removeLeadingSeparators(null));
		Assertions.assertNull(FileNameTools.removeLeadingSeparators(null, '|'));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.FileNameTools#removeTrailingSeparators(java.lang.String, char)}
	 * .
	 */
	@Test
	public void testRemoveTrailingSeparators() {
		String[][] testData = {
			{
				"|", "|", ""
			}, {
				"|", "asdf|fdas", "asdf|fdas"
			}, {
				"|", "|asdf|fdas", "|asdf|fdas"
			}, {
				"|", "asdf|fdas|", "asdf|fdas"
			}, {
				"|", "|asdf|fdas|", "|asdf|fdas"
			}, {
				"|", "|||||||||", ""
			}, {
				"|", "asdf|||||||||fdas", "asdf|||||||||fdas"
			}, {
				"|", "|||||||||asdf|||||||||fdas", "|||||||||asdf|||||||||fdas"
			}, {
				"|", "asdf|||||||||fdas|||||||||", "asdf|||||||||fdas"
			}, {
				"|", "|||||||||asdf|||||||||fdas|||||||||", "|||||||||asdf|||||||||fdas"
			},
		};
		for (String[] values : testData) {
			char sep = values[0].charAt(0);
			Assertions.assertEquals(values[2], FileNameTools.removeTrailingSeparators(values[1], sep));
			String a = values[1].replace('|', File.separatorChar);
			String b = values[2].replace('|', File.separatorChar);
			Assertions.assertEquals(b, FileNameTools.removeTrailingSeparators(a));
		}
		Assertions.assertNull(FileNameTools.removeTrailingSeparators(null));
		Assertions.assertNull(FileNameTools.removeTrailingSeparators(null, '|'));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.FileNameTools#singleSeparators(java.lang.String)}.
	 */
	@Test
	public void testSingleSeparators() {
		String[][] testData = {
			{
				"|", "|", "|"
			}, {
				"|", "asdf|fdas", "asdf|fdas"
			}, {
				"|", "|asdf|fdas", "|asdf|fdas"
			}, {
				"|", "asdf|fdas|", "asdf|fdas|"
			}, {
				"|", "|asdf|fdas|", "|asdf|fdas|"
			}, {
				"|", "|||||||||", "|"
			}, {
				"|", "asdf|||||||||fdas", "asdf|fdas"
			}, {
				"|", "|||||||||asdf|||||||||fdas", "|asdf|fdas"
			}, {
				"|", "asdf|||||||||fdas|||||||||", "asdf|fdas|"
			}, {
				"|", "|||||||||asdf|||||||||fdas|||||||||", "|asdf|fdas|"
			},
		};
		for (String[] values : testData) {
			char sep = values[0].charAt(0);
			Assertions.assertEquals(values[2], FileNameTools.singleSeparators(values[1], sep));
			String a = values[1].replace('|', File.separatorChar);
			String b = values[2].replace('|', File.separatorChar);
			Assertions.assertEquals(b, FileNameTools.singleSeparators(a));
		}
		Assertions.assertNull(FileNameTools.singleSeparators(null));
		Assertions.assertNull(FileNameTools.singleSeparators(null, '|'));
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.FileNameTools#basename(java.lang.String, char)}.
	 */
	@Test
	public void testBasename() {
		String[][] testData = {
			{
				"|", "|", "|"
			}, {
				"|", "||", "|"
			}, {
				"|", "asdf", "asdf"
			}, {
				"|", "asdf|fdas", "fdas"
			}, {
				"|", "|asdf|fdas", "fdas"
			}, {
				"|", "asdf|fdas|", "fdas"
			}, {
				"|", "|asdf|fdas|", "fdas"
			}, {
				"|", "|||||||||", "|"
			}, {
				"|", "asdf|||||||||fdas", "fdas"
			}, {
				"|", "|||||||||asdf|||||||||fdas", "fdas"
			}, {
				"|", "asdf|||||||||fdas|||||||||", "fdas"
			}, {
				"|", "|||||||||asdf|||||||||fdas|||||||||", "fdas"
			},
		};
		for (String[] values : testData) {
			char sep = values[0].charAt(0);
			Assertions.assertEquals(values[2], FileNameTools.basename(values[1], sep));
			String a = values[1].replace('|', File.separatorChar);
			String b = values[2].replace('|', File.separatorChar);
			Assertions.assertEquals(b, FileNameTools.basename(a));
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.FileNameTools#dirname(java.lang.String, char)}.
	 */
	@Test
	public void testDirname() {
		String[][] testData = {
			{
				"|", "|", "|"
			}, {
				"|", "asdf", "."
			}, {
				"|", "|asdf", "|"
			}, {
				"|", "asdf|", "."
			}, {
				"|", "|asdf|", "|"
			}, {
				"|", "asdf|fdas", "asdf"
			}, {
				"|", "|asdf|fdas", "|asdf"
			}, {
				"|", "asdf|fdas|", "asdf"
			}, {
				"|", "|asdf|fdas|", "|asdf"
			}, {
				"|", "|asdf|fdas|qwer", "|asdf|fdas"
			}, {
				"|", "|||||||||", "|"
			}, {
				"|", "asdf|||||||||fdas", "asdf"
			}, {
				"|", "|||||||||asdf|||||||||fdas", "|||||||||asdf"
			}, {
				"|", "asdf|||||||||fdas|||||||||", "asdf"
			}, {
				"|", "|||||||||asdf|||||||||fdas|||||||||", "|||||||||asdf"
			},
		};
		for (String[] values : testData) {
			char sep = values[0].charAt(0);
			String msg = String.format("While testing %s", Arrays.toString(values));
			Assertions.assertEquals(values[2], FileNameTools.dirname(values[1], sep), msg);
			String a = values[1].replace('|', File.separatorChar);
			String b = values[2].replace('|', File.separatorChar);
			Assertions.assertEquals(b, FileNameTools.dirname(a), msg);
		}
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.FileNameTools#tokenize(java.lang.String, char)}.
	 */
	@Test
	public void testTokenize() {
		final String[] empty = new String[0];
		Object[][] testData = {
			{
				"|", "", empty
			}, {
				"|", "|", empty
			}, {
				"|", "asdf|fdas", "asdf,fdas".split(",")
			}, {
				"|", "|asdf|fdas", "asdf,fdas".split(",")
			}, {
				"|", "asdf|fdas|", "asdf,fdas".split(",")
			}, {
				"|", "|asdf|fdas|", "asdf,fdas".split(",")
			}, {
				"|", "|||||||||", empty
			}, {
				"|", "asdf|||||||||fdas", "asdf,fdas".split(",")
			}, {
				"|", "|||||||||asdf|||||||||fdas", "asdf,fdas".split(",")
			}, {
				"|", "asdf|||||||||fdas|||||||||", "asdf,fdas".split(",")
			}, {
				"|", "|||||||||asdf|||||||||fdas|||||||||", "asdf,fdas".split(",")
			}, {
				"|", "|asdf|.|poiuy|..|fdas|", "asdf,.,poiuy,..,fdas".split(",")
			}, {
				"|", "|asdf|..|..|fdas|", "asdf,..,..,fdas".split(",")
			}, {
				"|", "|asdf|.|.|fdas|", "asdf,.,.,fdas".split(",")
			},
		};
		for (Object[] values : testData) {
			char sep = String.class.cast(values[0]).charAt(0);
			final String str = String.valueOf(values[1]);
			String[] expected = (String[]) values[2];
			List<String> l = null;

			l = FileNameTools.tokenize(str, sep);
			Assertions.assertArrayEquals(expected, l.toArray(empty));

			l = FileNameTools.tokenize(str.replace(sep, File.separatorChar));
			Assertions.assertArrayEquals(expected, l.toArray(empty));

			l = new ArrayList<>();
			FileNameTools.tokenize(l, str, sep);
			Assertions.assertArrayEquals(expected, l.toArray(empty));

			l = new ArrayList<>();
			FileNameTools.tokenize(l, str.replace(sep, File.separatorChar));
			Assertions.assertArrayEquals(expected, l.toArray(empty));
		}
		Assertions.assertThrows(IllegalArgumentException.class, () -> FileNameTools.tokenize(null));
		Assertions.assertThrows(IllegalArgumentException.class,
			() -> FileNameTools.tokenize(new ArrayList<String>(), null));
		Assertions.assertThrows(IllegalArgumentException.class, () -> FileNameTools.tokenize(null, File.separatorChar));
		Assertions.assertThrows(IllegalArgumentException.class,
			() -> FileNameTools.tokenize(new ArrayList<String>(), null, File.separatorChar));
		Assertions.assertThrows(IllegalArgumentException.class, () -> FileNameTools.tokenize(null, "/a/b/c"));
		Assertions.assertThrows(IllegalArgumentException.class,
			() -> FileNameTools.tokenize(null, "/a/b/c", File.separatorChar));
	}

	private String[] splitEmptyAsNull(String str, String splitters) {
		String[] ret = str.split(",");
		for (int i = 0; i < ret.length; i++) {
			if (ret[i].length() == 0) {
				ret[i] = null;
			}
		}
		return ret;
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.FileNameTools#reconstitute(java.util.Collection, boolean, boolean)}
	 *
	 */
	@Test
	public void testReconstitute() {
		final String[] empty = new String[0];
		Object[][] testData = {
			{
				"|", empty, ""
			}, {
				"|", empty, "|"
			}, {
				"|", "asdf,fdas".split(","), "asdf|fdas"
			}, {
				"|", "asdf,fdas".split(","), "|asdf|fdas"
			}, {
				"|", "asdf,fdas".split(","), "asdf|fdas|"
			}, {
				"|", "asdf,fdas".split(","), "|asdf|fdas|"
			}, {
				"|", "asdf,,fdas".split(","), "asdf|fdas"
			}, {
				"|", "asdf,,fdas".split(","), "|asdf|fdas"
			}, {
				"|", "asdf,,fdas".split(","), "asdf|fdas|"
			}, {
				"|", "asdf,,fdas".split(","), "|asdf|fdas|"
			}, {
				"|", "asdf,,,fdas".split(","), "asdf|fdas"
			}, {
				"|", "asdf,,,fdas".split(","), "|asdf|fdas"
			}, {
				"|", "asdf,,,fdas".split(","), "asdf|fdas|"
			}, {
				"|", "asdf,,,fdas".split(","), "|asdf|fdas|"
			}, {
				"|", splitEmptyAsNull("asdf,,fdas", ","), "asdf|fdas"
			}, {
				"|", splitEmptyAsNull("asdf,,fdas", ","), "|asdf|fdas"
			}, {
				"|", splitEmptyAsNull("asdf,,fdas", ","), "asdf|fdas|"
			}, {
				"|", splitEmptyAsNull("asdf,,fdas", ","), "|asdf|fdas|"
			}, {
				"|", splitEmptyAsNull("asdf,,,fdas", ","), "asdf|fdas"
			}, {
				"|", splitEmptyAsNull("asdf,,,fdas", ","), "|asdf|fdas"
			}, {
				"|", splitEmptyAsNull("asdf,,,fdas", ","), "asdf|fdas|"
			}, {
				"|", splitEmptyAsNull("asdf,,,fdas", ","), "|asdf|fdas|"
			}, {
				"|", "asdf,.,poiuy,..,fdas".split(","), "|asdf|.|poiuy|..|fdas|"
			}, {
				"|", "asdf,..,..,fdas".split(","), "|asdf|..|..|fdas|"
			}, {
				"|", "asdf,.,.,fdas".split(","), "|asdf|.|.|fdas|"
			},
		};
		for (Object[] values : testData) {
			char sep = String.class.cast(values[0]).charAt(0);
			String[] arr = (String[]) values[1];
			final String expected = String.valueOf(values[2]);
			final boolean leading = expected.startsWith(String.valueOf(sep));
			final boolean trailing = expected.endsWith(String.valueOf(sep));
			List<String> tokens = Arrays.asList(arr);
			Assertions.assertEquals(expected, FileNameTools.reconstitute(tokens, leading, trailing, sep));
			Assertions.assertEquals(expected.replace(sep, File.separatorChar),
				FileNameTools.reconstitute(tokens, leading, trailing));
		}
		Assertions.assertThrows(IllegalArgumentException.class, () -> FileNameTools.reconstitute(null, true, false));
		Assertions.assertThrows(IllegalArgumentException.class,
			() -> FileNameTools.reconstitute(null, true, false, File.separatorChar));
	}
}