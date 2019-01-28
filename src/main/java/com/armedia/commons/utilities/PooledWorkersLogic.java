package com.armedia.commons.utilities;

@FunctionalInterface
public interface PooledWorkersLogic<STATE, ITEM> {

	public default STATE initialize() throws Exception {
		return null;
	}

	public void process(STATE state, ITEM item) throws Exception;

	public default void handleFailure(STATE state, ITEM item, Exception raised) {
		// Do nothing...
	}

	public default void cleanup(STATE state) {
	}

}