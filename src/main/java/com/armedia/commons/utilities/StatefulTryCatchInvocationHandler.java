package com.armedia.commons.utilities;

public abstract class StatefulTryCatchInvocationHandler<T> extends TryCatchInvocationHandler {
	protected final T state;

	public StatefulTryCatchInvocationHandler(T state) {
		this.state = state;
	}

	public final T getState() {
		return this.state;
	}
}