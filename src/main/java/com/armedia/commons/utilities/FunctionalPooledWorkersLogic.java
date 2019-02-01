package com.armedia.commons.utilities;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.armedia.commons.utilities.function.TriConsumer;

/**
 * <p>
 * This implementation of {@link PooledWorkersLogic} serves as a simple gateway to functional
 * interfaces and lambda expressions, to facilitate programming.
 * </p>
 * 
 * @author diego
 *
 * @param <STATE>
 * @param <ITEM>
 */
public final class FunctionalPooledWorkersLogic<STATE, ITEM> implements PooledWorkersLogic<STATE, ITEM> {

	private final Supplier<STATE> initializer;
	private final BiConsumer<STATE, ITEM> processor;
	private final TriConsumer<STATE, ITEM, Exception> failureHandler;
	private final Consumer<STATE> cleanup;

	public FunctionalPooledWorkersLogic(BiConsumer<STATE, ITEM> processor) {
		this(null, processor, null, null);
	}

	public FunctionalPooledWorkersLogic(Supplier<STATE> initializer, BiConsumer<STATE, ITEM> processor) {
		this(initializer, processor, null, null);
	}

	public FunctionalPooledWorkersLogic(BiConsumer<STATE, ITEM> processor, Consumer<STATE> cleanup) {
		this(null, processor, null, cleanup);
	}

	public FunctionalPooledWorkersLogic(Supplier<STATE> initializer, BiConsumer<STATE, ITEM> processor,
		Consumer<STATE> cleanup) {
		this(initializer, processor, null, cleanup);
	}

	public FunctionalPooledWorkersLogic(BiConsumer<STATE, ITEM> processor,
		TriConsumer<STATE, ITEM, Exception> failureHandler) {
		this(null, processor, failureHandler, null);
	}

	public FunctionalPooledWorkersLogic(Supplier<STATE> initializer, BiConsumer<STATE, ITEM> processor,
		TriConsumer<STATE, ITEM, Exception> failureHandler) {
		this(initializer, processor, failureHandler, null);
	}

	public FunctionalPooledWorkersLogic(BiConsumer<STATE, ITEM> processor,
		TriConsumer<STATE, ITEM, Exception> failureHandler, Consumer<STATE> cleanup) {
		this(null, processor, failureHandler, cleanup);
	}

	public FunctionalPooledWorkersLogic(Supplier<STATE> initializer, BiConsumer<STATE, ITEM> processor,
		TriConsumer<STATE, ITEM, Exception> failureHandler, Consumer<STATE> cleanup) {
		this.processor = Objects.requireNonNull(processor, "Must provide a non-null BiConsumer to process the items");
		this.initializer = Tools.coalesce(initializer, this::nullInitialize);
		this.failureHandler = Tools.coalesce(failureHandler, this::nullHandleFailure);
		this.cleanup = Tools.coalesce(cleanup, this::nullCleanup);
	}

	private STATE nullInitialize() {
		// Do nothing
		return null;
	}

	private void nullHandleFailure(STATE state, ITEM item, Exception raised) {
		// Do nothing
	}

	private void nullCleanup(STATE state) {
		// Do nothing
	}

	@Override
	public STATE initialize() throws Exception {
		return this.initializer.get();
	}

	@Override
	public void process(STATE state, ITEM item) throws Exception {
		this.processor.accept(state, item);
	}

	@Override
	public void handleFailure(STATE state, ITEM item, Exception raised) {
		this.failureHandler.accept(state, item, raised);
	}

	@Override
	public void cleanup(STATE state) {
		this.cleanup.accept(state);
	}
}
