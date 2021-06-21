/*******************************************************************************
 * #%L
 * Armedia Caliente
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
 *******************************************************************************/
package com.armedia.commons.utilities.io;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.slf4j.Logger;

public class CloseUtils {

	private static BiConsumer<String, Object[]> getConsumer(Logger log) {
		return (log != null ? log::error : null);
	}

	public static void closeQuietly(AutoCloseable... closeables) {
		CloseUtils.closeQuietly(CloseUtils.getConsumer(null), closeables);
	}

	public static void closeQuietly(Collection<AutoCloseable> closeables) {
		CloseUtils.closeQuietly(CloseUtils.getConsumer(null), closeables);
	}

	public static void closeQuietly(Logger log, AutoCloseable... closeables) {
		CloseUtils.closeQuietly(CloseUtils.getConsumer(log), closeables);
	}

	public static void closeQuietly(Logger log, Collection<AutoCloseable> closeables) {
		CloseUtils.closeQuietly(CloseUtils.getConsumer(log), closeables);
	}

	public static void closeQuietly(BiConsumer<String, Object[]> log, AutoCloseable... closeables) {
		CloseUtils.closeQuietly(log, (closeables != null ? Arrays.asList(closeables) : null));
	}

	public static void closeQuietly(BiConsumer<String, Object[]> log, Collection<AutoCloseable> closeables) {
		if ((closeables == null) || (closeables.isEmpty())) { return; }
		final Object[] args = (log != null ? new Object[2] : null);
		closeables.stream().filter(Objects::nonNull).forEach((c) -> {
			try {
				c.close();
			} catch (Exception e) {
				if (log != null) {
					args[0] = c.getClass();
					args[1] = e;
					log.accept("Exception caught while closing an AutoCloseable resource of {}", args);
				}
			}
		});
	}
}
