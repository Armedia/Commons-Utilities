package com.armedia.commons.utilities;

import java.lang.reflect.InvocationHandler;

public abstract class StatefulInvocationHandler<T> implements InvocationHandler {
	protected final T state;

	public StatefulInvocationHandler(T state) {
		this.state = state;
	}

	public final T getState() {
		return this.state;
	}
}