/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2022 Armedia, LLC
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

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GlobberTest implements GoodService {

	@Test
	public void testConstructor() {
		new Globber();
	}

	@Test
	public void testAsPattern() {
		String[][] data = {
			{
				"*.htm*", "index.html", "true"
			}, {
				"*.html", "my.index.html", "true"
			}, {
				"my.*.html", "my.index.html", "true"
			}, {
				"my.?.html", "my.index.html", "false"
			}, {
				"*.html", "index.html.2", "false"
			}, {
				"*.html", "index.htm", "false"
			}, {
				"*.html", "index.htmL", "false"
			}, {
				"*.htm?", "index.htmL", "true"
			}, {
				"*.htm?", "index.htm", "false"
			}, {
				"*.htm?", "index.htmx", "true"
			}, {
				"{one,two,three,fo{ur,rt}}.htm", "one.htm", "true"
			}, {
				"{one,two,three,fo{ur,rt}}.htm", "two.htm", "true"
			}, {
				"{one,two,three,fo{ur,rt}}.htm", "three.htm", "true"
			}, {
				"{one,two,three,fo{ur,rt}}.htm", "four.htm", "true"
			}, {
				"{one,two,three,fo{ur,rt}}.htm", "fort.htm", "true"
			}, {
				"{one,two,three,fo{ur,rt}}.htm", "1.htm", "false"
			}, {
				"{one,two,three,fo{ur,rt}}.htm", "fourt.htm", "false"
			}, {
				"{one,two,three,fo\\{ur\\,rt\\}}.htm", "one.htm", "true"
			}, {
				"{one,two,three,fo\\{ur\\,rt\\}}.htm", "two.htm", "true"
			}, {
				"{one,two,three,fo\\{ur\\,rt\\}}.htm", "three.htm", "true"
			}, {
				"{one,two,three,fo\\{ur\\,rt\\}}.htm", "four.htm", "false"
			}, {
				"{one,two,three,fo\\{ur\\,rt\\}}.htm", "fort.htm", "false"
			}, {
				"{one,two,three,fo\\{ur\\,rt\\}}.htm", "fo{ur,rt}.htm", "true"
			}, {
				"a\\*b", "a*b", "true"
			}, {
				"a\\*b", "aXYZb", "false"
			}, {
				"a\\?b", "a?b", "true"
			}, {
				"a\\?b", "aXb", "false"
			}, {
				"a\\\\b", "a\\b", "true"
			}, {
				"a\\{b,c\\}", "a{b,c}", "true"
			}, {
				"a\\{b,c\\}", "ab", "false"
			}, {
				"a\\{b,c\\}", "a{b,c}d", "false"
			}, {
				"a,b,c", "a,b,c", "true"
			}, {
				"a,b,c", "a", "false"
			}, {
				"a,b,c", "b", "false"
			}, {
				"a,b,c", "c", "false"
			}, {
				"a{aa\\,bb,cc}", "aaa,bb", "true"
			}, {
				"a{aa\\,bb,cc}", "acc", "true"
			}, {
				"a}b", "a}b", "true"
			},
		};
		for (String[] d : data) {
			final Pattern p = Globber.asPattern(d[0]);
			final Matcher m = p.matcher(d[1]);
			final boolean expected = Boolean.valueOf(d[2]).booleanValue();
			Assertions.assertEquals(expected, m.matches(),
				String.format("While matching %s (%s)", Arrays.toString(d), p.pattern()));
		}
	}

	@Test
	public void testErrors() {
		Assertions.assertThrows(PatternSyntaxException.class, () -> Globber.asPattern("abc{def"));
		Assertions.assertThrows(PatternSyntaxException.class, () -> Globber.asPattern("abcdef\\"));
	}
}
