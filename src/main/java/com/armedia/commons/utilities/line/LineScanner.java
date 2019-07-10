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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.armedia.commons.utilities.Tools;
import com.armedia.commons.utilities.concurrent.BaseShareableLockable;
import com.armedia.commons.utilities.concurrent.MutexAutoLock;
import com.armedia.commons.utilities.concurrent.SharedAutoLock;

public class LineScanner extends BaseShareableLockable {

	public static final Map<Integer, LineSourceFactory> DEFAULT_FACTORIES;
	static {
		Map<Integer, LineSourceFactory> defaultFactories = new LinkedHashMap<>();
		// Add the default factory handlers...

		// First things first: the resource handler
		LineSourceFactory factory = new ResourceLineSourceFactory();
		defaultFactories.put(System.identityHashCode(factory), factory);

		DEFAULT_FACTORIES = Tools.freezeMap(defaultFactories);
	}

	private final Map<Integer, LineSourceFactory> factories = new LinkedHashMap<>();

	public LineScanner() {
	}

	public final Collection<LineSourceFactory> getSourceFactories() {
		try (SharedAutoLock lock = autoSharedLock()) {
			Collection<LineSourceFactory> ret = new ArrayList<>(this.factories.values());
			ret.addAll(LineScanner.DEFAULT_FACTORIES.values()); // Append the defaults
			return ret;
		}
	}

	public final LineScanner addSourceFactory(LineSourceFactory factory) {
		if (factory == null) { return this; }
		return addSourceFactories(factory);
	}

	public final LineScanner addSourceFactories(LineSourceFactory... factories) {
		if ((factories == null) || (factories.length < 1)) { return this; }
		return addSourceFactories(Arrays.asList(factories));
	}

	public final LineScanner addSourceFactories(Collection<LineSourceFactory> factories) {
		if ((factories == null) || factories.isEmpty()) { return this; }
		// Add the factories, avoiding duplicates... we need to do it sequentially
		// because we need to preserve the order in which factories are added
		try (MutexAutoLock lock = autoMutexLock()) {
			factories.stream().filter(Objects::nonNull).forEach(f -> this.factories.put(System.identityHashCode(f), f));
			return this;
		}
	}

	public final LineScanner removeSourceFactory(LineSourceFactory factory) {
		if (factory == null) { return this; }
		return removeSourceFactories(factory);
	}

	public final LineScanner removeSourceFactories(LineSourceFactory... factories) {
		if ((factories == null) || (factories.length < 1)) { return this; }
		return removeSourceFactories(Arrays.asList(factories));
	}

	public final LineScanner removeSourceFactories(Collection<LineSourceFactory> factories) {
		if ((factories == null) || factories.isEmpty()) { return this; }
		// Add the factories, avoiding duplicates... we need to do it sequentially
		// because we need to preserve the order in which factories are added
		try (MutexAutoLock lock = autoMutexLock()) {
			factories.stream().filter(Objects::nonNull)
				.forEach(f -> this.factories.remove(System.identityHashCode(f), f));
			return this;
		}
	}

	public final boolean hasSourceFactory(LineSourceFactory factory) {
		if (factory == null) { return false; }
		return shareLocked(() -> this.factories.containsKey(System.identityHashCode(factory)));
	}

	public LineIterator iterator(String... sourceSpecs) {
		return iterator(null, sourceSpecs);
	}

	public LineIterator iterator(LineIteratorConfig config, String... sourceSpecs) {
		return iterator(config,
			(sourceSpecs != null) && (sourceSpecs.length > 0) ? Arrays.asList(sourceSpecs) : Collections.emptyList());
	}

	public LineIterator iterator(Collection<String> sourceSpecs) {
		return iterator(null, sourceSpecs);
	}

	public LineIterator iterator(LineIteratorConfig config, Collection<String> sourceSpecs) {
		if (sourceSpecs == null) {
			sourceSpecs = Collections.emptyList();
		}
		Iterator<String> it = sourceSpecs.iterator();
		if (!it.hasNext()) { return LineIterator.NULL_ITERATOR; }
		return new LineIterator(getSourceFactories(), config, sourceSpecs);
	}

}
