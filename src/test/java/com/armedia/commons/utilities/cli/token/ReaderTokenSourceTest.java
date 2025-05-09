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
package com.armedia.commons.utilities.cli.token;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReaderTokenSourceTest {

	@Test
	public void testTokenize() throws Exception {
		String str = "abc c d asd fa sdf  fa\\\\ sdf\\ asdf\\r\\n\\r\\n\\t\\f\\\" rest					 of the         \\t                      stuff ' single \"quote   ' \"quoted ' stuff\"";
		List<String> expected = Arrays.asList(new String[] {
			"abc", //
			"c", //
			"d", //
			"asd", //
			"fa", //
			"sdf", //
			"fa\\", //
			"sdf asdf\r\n\r\n\t\f\"", //
			"rest", //
			"of", //
			"the", //
			"\t", //
			"stuff", //
			" single \"quote   ", //
			"quoted ' stuff", //
		});
		ReaderTokenSource source = new CharacterSequenceTokenSource(str);
		List<String> actual = source.getTokenStrings();
		Assertions.assertEquals(expected.size(), actual.size(), "Token counts");
		for (int i = 0; i < actual.size(); i++) {
			Assertions.assertEquals(expected.get(i), actual.get(i), String.format("Mismatch found at token %d", i));
		}
	}

	@Test
	public void testInterpolated() throws Exception {
		final long nanoTime = System.nanoTime();
		String singlePropName = "test.single.property." + nanoTime;
		String dualPropName = "test.dual.property." + nanoTime;
		try {
			String singlePropValue = UUID.randomUUID().toString();
			System.setProperty(singlePropName, singlePropValue);

			String dualPropValue = UUID.randomUUID().toString() + " " + nanoTime;
			System.setProperty(dualPropName, dualPropValue);

			String path = System.getenv("PATH");
			String str = "abc ${PATH} ${" + singlePropName + "} ${NOTHING} ${" + dualPropName + "}";
			List<String> expected = Arrays.asList(new String[] {
				"abc", //
				path, //
				singlePropValue, //
				"${NOTHING}", //
				dualPropValue, //
			});
			ReaderTokenSource source = new CharacterSequenceTokenSource(str);
			List<String> actual = source.getTokenStrings();
			Assertions.assertEquals(expected.size(), actual.size(), "Token counts");
			for (int i = 0; i < actual.size(); i++) {
				Assertions.assertEquals(expected.get(i), actual.get(i), String.format("Mismatch found at token %d", i));
			}
		} finally {
			System.clearProperty(dualPropName);
			System.clearProperty(singlePropName);
		}
	}

	@Test
	public void testReadQuoted() throws Exception {
		String str = "abc c d asd fa sdf  fa\\\\ sdf\\ asdf\\r\\n\\r\\n\\t\\f\\\" rest of the stuff ' ' ' \"";
		String expected = "abc c d asd fa sdf  fa\\ sdf\\ asdf\r\n\r\n\t\f\" rest of the stuff ' ' ' ";
		ReaderTokenSource source = new CharacterSequenceTokenSource(str);
		String actual = source.readQuoted(new StringReader(str), '"');
		Assertions.assertEquals(expected, actual);
	}
}
