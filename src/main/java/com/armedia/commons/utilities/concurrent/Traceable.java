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
package com.armedia.commons.utilities.concurrent;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;

import com.armedia.commons.utilities.function.CheckedRunnable;
import com.armedia.commons.utilities.function.CheckedSupplier;

public interface Traceable {

	public Logger getLog();

	public Serializable getId();

	public String getName();

	public static String formatArgs(Object... args) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append("{}");
		}
		return Traceable.format(sb.toString(), args);
	}

	public static String format(String format, Object... args) {
		return MessageFormatter.arrayFormat(format, args).getMessage();
	}

	public default <E extends Throwable> void invoke(CheckedRunnable<E> r, String method, Object... args) throws E {
		invoke(() -> {
			r.runChecked();
			return null;
		}, method, args);
	}

	public default <V, E extends Throwable> V invoke(CheckedSupplier<V, E> s, String method, Object... args) throws E {
		final String argStr = Traceable.formatArgs(args);
		final Logger log = getLog();
		log.trace("{}.{}({})", getName(), method, argStr);
		boolean ok = false;
		V ret = null;
		try {
			ret = s.getChecked();
			ok = true;
			return ret;
		} finally {
			log.trace("{}.{}({}) {} (returning {})", getName(), method, argStr, ok ? "completed" : "FAILED", ret);
		}
	}
}