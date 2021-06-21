/*-
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (C) 2013 - 2021 Armedia, LLC
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
 */
package com.armedia.commons.utilities.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.xml.bind.JAXBContext;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.LazyFormatter;

public class XmlToolsTest {

	@Test
	public void testGetContext() throws Exception {
		Class<?>[] c = {
			Object.class, String.class, Number.class, Long.class, Short.class, Integer.class, Double.class, Float.class,
			Byte.class, Character.class, null, Object.class, String.class, Number.class, Long.class, Short.class,
			Integer.class, Double.class, Float.class, Byte.class, Character.class, null
		};

		List<Class<?>> l = new ArrayList<>();
		for (Class<?> C : c) {
			l.add(C);
		}

		Class<?>[] origClasses = null;
		JAXBContext origCtx = null;
		boolean shuffled = false;
		for (int i = 0; i < 10; i++) {
			final Class<?>[] oldClasses = origClasses;
			final Class<?>[] newClasses = l.toArray(ArrayUtils.EMPTY_CLASS_ARRAY);
			final JAXBContext newCtx = XmlTools.getContext(newClasses);
			Assertions.assertNotNull(newCtx);
			if (origCtx == null) {
				origCtx = newCtx;
				origClasses = newClasses;
				continue;
			}
			Supplier<String> oldMsg = () -> Arrays.toString(oldClasses);
			Supplier<String> newMsg = () -> Arrays.toString(newClasses);
			Assertions.assertNotSame(oldClasses, newClasses);
			if (shuffled) {
				Assertions.assertFalse(Arrays.deepEquals(oldClasses, newClasses));
			}
			Assertions.assertSame(origCtx, newCtx,
				LazyFormatter.of("Failed to get the same context for %s and %s", oldMsg, newMsg));
			Collections.shuffle(l);
			shuffled = true;
		}
		origClasses = null;
		Assertions.assertSame(XmlTools.getContext(), XmlTools.getContext());
		Assertions.assertSame(XmlTools.getContext(), XmlTools.getContext(origClasses));
		Assertions.assertSame(XmlTools.getContext(), XmlTools.getContext((Class<?>) null));
	}

	@Test
	public void testLoadSchemaURL() {
	}

}
