/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2019 Armedia, LLC
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
package com.armedia.commons.utilities.line;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.io.input.NullInputStream;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InputStreamLineSourceTest {

	@Test
	public void testConstructors() throws IOException {
		Assertions.assertThrows(NullPointerException.class, () -> new InputStreamLineSource(null, null));
		Assertions.assertThrows(IllegalArgumentException.class, () -> new InputStreamLineSource("", null));
		Assertions.assertThrows(NullPointerException.class, () -> new InputStreamLineSource("a", null));
		Assertions.assertThrows(NullPointerException.class,
			() -> new InputStreamLineSource(null, new NullInputStream(0)));

		Assertions.assertThrows(NullPointerException.class, () -> new InputStreamLineSource(null, null, false));
		Assertions.assertThrows(IllegalArgumentException.class, () -> new InputStreamLineSource("", null, false));
		Assertions.assertThrows(NullPointerException.class, () -> new InputStreamLineSource("a", null, false));
		Assertions.assertThrows(NullPointerException.class,
			() -> new InputStreamLineSource(null, new NullInputStream(0), false));

		Assertions.assertThrows(NullPointerException.class, () -> new InputStreamLineSource(null, null, true));
		Assertions.assertThrows(IllegalArgumentException.class, () -> new InputStreamLineSource("", null, true));
		Assertions.assertThrows(NullPointerException.class, () -> new InputStreamLineSource("a", null, true));
		Assertions.assertThrows(NullPointerException.class,
			() -> new InputStreamLineSource(null, new NullInputStream(0), true));

		Assertions.assertThrows(NullPointerException.class, () -> new InputStreamLineSource(null, null, null));
		Assertions.assertThrows(IllegalArgumentException.class, () -> new InputStreamLineSource("", null, null));
		Assertions.assertThrows(NullPointerException.class, () -> new InputStreamLineSource("a", null, null));
		Assertions.assertThrows(NullPointerException.class,
			() -> new InputStreamLineSource(null, new NullInputStream(0), null));

		Assertions.assertThrows(NullPointerException.class, () -> new InputStreamLineSource(null, null, null, false));
		Assertions.assertThrows(IllegalArgumentException.class, () -> new InputStreamLineSource("", null, null, false));
		Assertions.assertThrows(NullPointerException.class, () -> new InputStreamLineSource("a", null, null, false));
		Assertions.assertThrows(NullPointerException.class,
			() -> new InputStreamLineSource(null, new NullInputStream(0), null, false));

		Assertions.assertThrows(NullPointerException.class, () -> new InputStreamLineSource(null, null, null, true));
		Assertions.assertThrows(IllegalArgumentException.class, () -> new InputStreamLineSource("", null, null, true));
		Assertions.assertThrows(NullPointerException.class, () -> new InputStreamLineSource("a", null, null, true));
		Assertions.assertThrows(NullPointerException.class,
			() -> new InputStreamLineSource(null, new NullInputStream(0), null, true));

		try (InputStreamLineSource isls = new InputStreamLineSource("a", new NullInputStream(0))) {
		}
		try (InputStreamLineSource isls = new InputStreamLineSource("a", new NullInputStream(0), null)) {
		}
		try (InputStreamLineSource isls = new InputStreamLineSource("a", new NullInputStream(0), false)) {
		}
		try (InputStreamLineSource isls = new InputStreamLineSource("a", new NullInputStream(0), true)) {
		}
		try (InputStreamLineSource isls = new InputStreamLineSource("a", new NullInputStream(0), null, false)) {
		}
		try (InputStreamLineSource isls = new InputStreamLineSource("a", new NullInputStream(0), null, true)) {
		}
	}

	@Test
	public void testLoad() throws IOException, LineSourceException {
		InputStream in = EasyMock.createStrictMock(InputStream.class);
		EasyMock.reset(in);
		in.close();
		EasyMock.expectLastCall().once();
		EasyMock.replay(in);
		try (InputStreamLineSource isls = new InputStreamLineSource("a", in, true)) {
			Iterable<String> iterable = isls.load();
			Assertions.assertNotNull(iterable);
			Iterator<String> it = iterable.iterator();
			Assertions.assertNotNull(it);
		}
		EasyMock.verify(in);
	}

	@Test
	public void testClose() throws Exception {
		InputStream in = EasyMock.createStrictMock(InputStream.class);

		EasyMock.reset(in);
		in.close();
		EasyMock.expectLastCall().once();
		EasyMock.replay(in);
		try (InputStreamLineSource isls = new InputStreamLineSource("a", in, true)) {
		}
		EasyMock.verify(in);

		EasyMock.reset(in);
		EasyMock.replay(in);
		try (InputStreamLineSource isls = new InputStreamLineSource("a", in, false)) {
		}
		EasyMock.verify(in);
	}
}
