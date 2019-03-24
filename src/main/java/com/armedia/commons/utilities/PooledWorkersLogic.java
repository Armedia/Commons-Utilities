package com.armedia.commons.utilities;

import java.util.function.BiConsumer;

import com.armedia.commons.utilities.function.CheckedBiConsumer;
import com.armedia.commons.utilities.function.CheckedTools;

@FunctionalInterface
public interface PooledWorkersLogic<STATE, ITEM, EX extends Throwable> {

	public default STATE initialize() throws EX {
		return null;
	}

	public void process(STATE state, ITEM item) throws EX;

	public default void handleFailure(STATE state, ITEM item, EX raised) {
		// Do nothing...
	}

	public default void cleanup(STATE state) {
	}

	public static <STATE, ITEM, EX extends Throwable> PooledWorkersLogic<STATE, ITEM, EX> of(
		BiConsumer<STATE, ITEM> processor) {
		return new FunctionalPooledWorkersLogic<>(CheckedTools.check(processor));
	}

	public static <STATE, ITEM, EX extends Throwable> PooledWorkersLogic<STATE, ITEM, EX> of(
		CheckedBiConsumer<STATE, ITEM, EX> processor) {
		return new FunctionalPooledWorkersLogic<>(processor);
	}
}