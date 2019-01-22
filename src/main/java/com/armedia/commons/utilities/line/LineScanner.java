package com.armedia.commons.utilities.line;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

import com.armedia.commons.utilities.Tools;

public class LineScanner {

	public static final Map<Integer, LineSourceFactory> DEFAULT_FACTORIES;
	static {
		Map<Integer, LineSourceFactory> defaultFactories = new LinkedHashMap<>();
		// Add the default factory handlers...

		// First things first: the resource handler
		LineSourceFactory factory = new ResourceLineSourceFactory();
		defaultFactories.put(System.identityHashCode(factory), factory);

		DEFAULT_FACTORIES = Tools.freezeMap(defaultFactories);
	}

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final Map<Integer, LineSourceFactory> factories = new LinkedHashMap<>();

	public LineScanner() {
	}

	public final Collection<LineSourceFactory> getSourceFactories() {
		this.lock.readLock().lock();
		try {
			return new ArrayList<>(this.factories.values());
		} finally {
			this.lock.readLock().unlock();
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
		this.lock.writeLock().lock();
		try {
			factories.stream().filter(Objects::nonNull).sequential()
				.forEach(f -> this.factories.put(System.identityHashCode(f), f));
			return this;
		} finally {
			this.lock.writeLock().unlock();
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
		this.lock.writeLock().lock();
		try {
			factories.stream().filter(Objects::nonNull).sequential()
				.forEach(f -> this.factories.remove(System.identityHashCode(f), f));
			return this;
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	public final boolean hasSourceFactory(LineSourceFactory factory) {
		if (factory == null) { return false; }
		this.lock.readLock().lock();
		try {
			return this.factories.containsKey(System.identityHashCode(factory));
		} finally {
			this.lock.readLock().unlock();
		}
	}

	public Map<String, Long> scanLines(Function<String, Boolean> processor, String... sourceSpecs)
		throws IOException, LineSourceException, LineProcessorException {
		return scanLines(processor, null, sourceSpecs);
	}

	public Map<String, Long> scanLines(Function<String, Boolean> processor, LineScannerConfig lineScannerConfig,
		String... sourceSpecs) throws IOException, LineSourceException, LineProcessorException {
		return scanLines(processor, lineScannerConfig,
			(sourceSpecs != null) && (sourceSpecs.length > 0) ? Arrays.asList(sourceSpecs) : Collections.emptyList());
	}

	public Map<String, Long> scanLines(Function<String, Boolean> processor, Iterable<String> sourceSpecs)
		throws IOException, LineSourceException, LineProcessorException {
		return scanLines(processor, null, sourceSpecs);
	}

	public Map<String, Long> scanLines(Function<String, Boolean> processor, LineScannerConfig lineScannerConfig,
		Iterable<String> sourceSpecs) throws IOException, LineSourceException, LineProcessorException {
		Objects.requireNonNull(processor, "Must provide a non-null processor function");
		if (sourceSpecs == null) {
			sourceSpecs = Collections.emptyList();
		}
		Iterator<String> it = sourceSpecs.iterator();
		if (!it.hasNext()) { return Collections.emptyMap(); }
		lineScannerConfig = new LineScannerConfig(lineScannerConfig);

		RecursiveLineScanner rls = new RecursiveLineScanner(getSourceFactories(), lineScannerConfig);
		rls.process(processor, sourceSpecs);
		Map<String, AtomicLong> counters = rls.getCounters();
		Map<String, Long> result = new LinkedHashMap<>();
		counters.forEach((k, v) -> result.put(k, v.get()));
		return result;
	}

}