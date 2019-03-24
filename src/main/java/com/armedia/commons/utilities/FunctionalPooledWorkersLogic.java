package com.armedia.commons.utilities;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.armedia.commons.utilities.function.CheckedBiConsumer;
import com.armedia.commons.utilities.function.CheckedSupplier;
import com.armedia.commons.utilities.function.CheckedTools;
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
public final class FunctionalPooledWorkersLogic<STATE, ITEM, EX extends Throwable>
	implements PooledWorkersLogic<STATE, ITEM, EX> {

	private final CheckedSupplier<STATE, EX> initializer;
	private final CheckedBiConsumer<STATE, ITEM, EX> processor;
	private final TriConsumer<STATE, ITEM, EX> failureHandler;
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
		TriConsumer<STATE, ITEM, EX> failureHandler) {
		this(null, processor, failureHandler, null);
	}

	public FunctionalPooledWorkersLogic(Supplier<STATE> initializer, BiConsumer<STATE, ITEM> processor,
		TriConsumer<STATE, ITEM, EX> failureHandler) {
		this(initializer, processor, failureHandler, null);
	}

	public FunctionalPooledWorkersLogic(BiConsumer<STATE, ITEM> processor, TriConsumer<STATE, ITEM, EX> failureHandler,
		Consumer<STATE> cleanup) {
		this(null, processor, failureHandler, cleanup);
	}

	public FunctionalPooledWorkersLogic(Supplier<STATE> initializer, BiConsumer<STATE, ITEM> processor,
		TriConsumer<STATE, ITEM, EX> failureHandler, Consumer<STATE> cleanup) {
		this(CheckedTools.check(initializer), CheckedTools.check(processor), failureHandler, cleanup);
	}

	public FunctionalPooledWorkersLogic(CheckedBiConsumer<STATE, ITEM, EX> processor) {
		this(null, processor, null, null);
	}

	public FunctionalPooledWorkersLogic(CheckedSupplier<STATE, EX> initializer,
		CheckedBiConsumer<STATE, ITEM, EX> processor) {
		this(initializer, processor, null, null);
	}

	public FunctionalPooledWorkersLogic(CheckedBiConsumer<STATE, ITEM, EX> processor, Consumer<STATE> cleanup) {
		this(null, processor, null, cleanup);
	}

	public FunctionalPooledWorkersLogic(CheckedSupplier<STATE, EX> initializer,
		CheckedBiConsumer<STATE, ITEM, EX> processor, Consumer<STATE> cleanup) {
		this(initializer, processor, null, cleanup);
	}

	public FunctionalPooledWorkersLogic(CheckedBiConsumer<STATE, ITEM, EX> processor,
		TriConsumer<STATE, ITEM, EX> failureHandler) {
		this(null, processor, failureHandler, null);
	}

	public FunctionalPooledWorkersLogic(CheckedSupplier<STATE, EX> initializer,
		CheckedBiConsumer<STATE, ITEM, EX> processor, TriConsumer<STATE, ITEM, EX> failureHandler) {
		this(initializer, processor, failureHandler, null);
	}

	public FunctionalPooledWorkersLogic(CheckedBiConsumer<STATE, ITEM, EX> processor,
		TriConsumer<STATE, ITEM, EX> failureHandler, Consumer<STATE> cleanup) {
		this(null, processor, failureHandler, cleanup);
	}

	public FunctionalPooledWorkersLogic(CheckedSupplier<STATE, EX> initializer,
		CheckedBiConsumer<STATE, ITEM, EX> processor, TriConsumer<STATE, ITEM, EX> failureHandler,
		Consumer<STATE> cleanup) {
		this.processor = Objects.requireNonNull(processor, "Must provide a non-null BiConsumer to process the items");
		this.initializer = Tools.coalesce(initializer, this::nullInitialize);
		this.failureHandler = Tools.coalesce(failureHandler, this::nullHandleFailure);
		this.cleanup = Tools.coalesce(cleanup, this::nullCleanup);
	}

	private STATE nullInitialize() throws EX {
		// Do nothing
		return null;
	}

	private void nullHandleFailure(STATE state, ITEM item, EX raised) {
		// Do nothing
	}

	private void nullCleanup(STATE state) {
		// Do nothing
	}

	@Override
	public STATE initialize() throws EX {
		return this.initializer.get();
	}

	@Override
	public void process(STATE state, ITEM item) throws EX {
		this.processor.accept(state, item);
	}

	@Override
	public void handleFailure(STATE state, ITEM item, EX raised) {
		this.failureHandler.accept(state, item, raised);
	}

	@Override
	public void cleanup(STATE state) {
		this.cleanup.accept(state);
	}
}
