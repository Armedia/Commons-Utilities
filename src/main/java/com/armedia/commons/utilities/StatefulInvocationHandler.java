package com.armedia.commons.utilities;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public abstract class StatefulInvocationHandler<T> implements InvocationHandler {
	protected final T state;

	public StatefulInvocationHandler(T state) {
		this.state = state;
	}

	public final T getState() {
		return this.state;
	}

	@Override
	public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return invoke(getState(), proxy, method, args);
	}

	protected abstract Object invoke(T state, Object proxy, Method method, Object[] args) throws Throwable;
}