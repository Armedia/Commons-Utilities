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
package com.armedia.commons.utilities.line;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourceLineSourceFactoryTest {

	@Test
	public void testCalculateId() throws Exception {
		ResourceLineSourceFactory rlsf = new ResourceLineSourceFactory();

		Assertions.assertThrows(NullPointerException.class, () -> rlsf.calculateId(null));
		String id = null;
		URL url = null;

		id = "http://this.is.a/normal/url";
		url = new URL(id);
		Assertions.assertEquals(id, rlsf.calculateId(url));

		id = "http://this.is.a/normal/url- - /\\/@~$%";
		url = new URL(id);
		Assertions.assertEquals(id, rlsf.calculateId(url));
	}

	@Test
	public void testProcessException() throws LineSourceException {
		ResourceLineSourceFactory rlsf = new ResourceLineSourceFactory();
		Assertions.assertNull(rlsf.processException("some-source", "some-value", new FileNotFoundException()));
		Throwable cause = new Throwable();
		try {
			rlsf.processException("some-source", "some-value", cause);
		} catch (LineSourceException e) {
			Assertions.assertSame(cause, e.getCause());
		}
	}

	@Test
	public void testNewInstance() throws Exception {
		ResourceLineSourceFactory rlsf = new ResourceLineSourceFactory();

		Assertions.assertNull(rlsf.newInstance(null, null));
		Assertions.assertNull(rlsf.newInstance("", null));
		Assertions.assertNull(rlsf.newInstance("   ", null));

		Assertions.assertNotNull(rlsf.newInstance(ResourceLineSourceFactory.STDIN, null));

		LineSource lines_1 = rlsf.newInstance("classpath:/lines-1.test", null);
		Assertions.assertNotNull(lines_1);
		LineSource lines_2 = rlsf.newInstance("classpath:/lines-2.test", lines_1);
		Assertions.assertNotNull(lines_2);
		Assertions.assertNull(rlsf.newInstance("non-existent.test", lines_1));
		Assertions.assertNull(rlsf.newInstance("non-existent.test", null));

		rlsf = new ResourceLineSourceFactory() {
			@Override
			protected URL getResourceUrl(String resource, String relative) throws Exception {
				throw new MalformedURLException();
			}
		};
		Assertions.assertNull(rlsf.newInstance("non-existent.test", lines_1));
	}
}
