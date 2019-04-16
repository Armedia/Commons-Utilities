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
		config = new LineIteratorConfig(config);

		return new LineIterator(getSourceFactories(), config, sourceSpecs);
	}

}