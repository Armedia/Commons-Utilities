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
package com.armedia.commons.utilities.concurrent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ShareableSetTest {

	@Test
	public void testConstructors() {
		ShareableLockable sl = null;
		ReadWriteLock rwl = null;
		Set<Object> s = null;

		Assertions.assertThrows(NullPointerException.class, () -> new ShareableSet<>(s));
		new ShareableSet<>(new HashSet<>());

		Assertions.assertThrows(NullPointerException.class, () -> new ShareableSet<>(rwl, s));
		new ShareableSet<>(rwl, new HashSet<>());
		Assertions.assertThrows(NullPointerException.class, () -> new ShareableSet<>(new ReentrantReadWriteLock(), s));
		new ShareableSet<>(new ReentrantReadWriteLock(), new HashSet<>());

		Assertions.assertThrows(NullPointerException.class, () -> new ShareableSet<>(sl, s));
		Assertions.assertThrows(NullPointerException.class, () -> new ShareableSet<>(sl, new HashSet<>()));
		Assertions.assertThrows(NullPointerException.class, () -> new ShareableSet<>(new BaseShareableLockable(), s));
		new ShareableSet<>(new BaseShareableLockable(), new HashSet<>());
	}
}
