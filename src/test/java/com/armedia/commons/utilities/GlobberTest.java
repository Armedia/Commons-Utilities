/**********************************************************************
 * 
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS.
 * REPRODUCTION OF ANY PORTION OF THE SOURCE CODE, CONTAINED HEREIN,
 * OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE,
 * IS STRICTLY PROHIBITED.
 * 
 * Confidential Property of Armedia LLC.
 * (c) Copyright Armedia LLC 2011-2012.
 * All Rights reserved.
 * 
 *********************************************************************/
package com.armedia.commons.utilities;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class GlobberTest {

	@Test
	public void testAsPatternString() {
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
			Assert.assertEquals(String.format("While matching %s (%s)", Arrays.toString(d), p.pattern()), expected,
				m.matches());
		}
	}
}