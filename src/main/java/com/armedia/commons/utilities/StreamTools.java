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
package com.armedia.commons.utilities;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamTools {

	public static final int DEFAULT_CHARACTERISTICS = Spliterator.DISTINCT | Spliterator.IMMUTABLE
		| Spliterator.ORDERED;
	public static final boolean DEFAULT_PARALLEL = false;

	public static <T> Stream<T> of(Iterator<T> it) {
		return StreamTools.of(it, StreamTools.DEFAULT_CHARACTERISTICS, StreamTools.DEFAULT_PARALLEL);
	}

	public static <T> Stream<T> of(Iterator<T> it, int characteristics) {
		return StreamTools.of(it, characteristics, StreamTools.DEFAULT_PARALLEL);
	}

	public static <T> Stream<T> of(Iterator<T> it, boolean parallel) {
		return StreamTools.of(it, StreamTools.DEFAULT_CHARACTERISTICS, parallel);
	}

	public static <T> Stream<T> of(Iterator<T> it, int characteristics, boolean parallel) {
		if (it == null) { return Stream.empty(); }
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, characteristics), parallel);
	}

}
