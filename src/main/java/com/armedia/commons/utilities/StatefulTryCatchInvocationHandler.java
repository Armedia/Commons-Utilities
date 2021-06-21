/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2021 Armedia, LLC
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
package com.armedia.commons.utilities;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public abstract class StatefulTryCatchInvocationHandler<T> extends StatefulInvocationHandler<T> {

	public StatefulTryCatchInvocationHandler(T state) {
		super(state);
	}

	/**
	 * <p>
	 * Implementation of {@link InvocationHandler#invoke(Object, Method, Object[])} which uses a
	 * <b><code>try-catch-finally</code></b> block with callbacks for each execution branch, for
	 * more flexible proxying implementations, but without having to turn to AOP frameworks.
	 * </p>
	 */
	@Override
	protected final Object invoke(T state, Object proxy, Method method, Object[] args) throws Throwable {
		Object returnValue = null;
		Throwable thrown = null;
		onTry(state, proxy, method, args);
		try {
			returnValue = onReturn(state, proxy, method, args, doInvoke(state, proxy, method, args));
		} catch (final Throwable t) {
			thrown = t;
			try {
				returnValue = onCatch(state, proxy, method, args, t, returnValue);
				thrown = null;
			} catch (final Throwable t2) {
				thrown = t2;
				throw t2;
			}
		} finally {
			returnValue = onFinally(state, proxy, method, args, returnValue, thrown);
		}
		return returnValue;
	}

	/**
	 * <p>
	 * This method is invoked at the very beginning of the <b><code>try-catch-finally</code></b>
	 * block, immediately before the <b><code>try</code></b> block is entered.
	 * </p>
	 *
	 * @param state
	 * @param proxy
	 * @param method
	 * @param args
	 * @throws Throwable
	 */
	protected void onTry(T state, Object proxy, Method method, Object[] args) throws Throwable {
		// By default, do nothing
	}

	/**
	 * <p>
	 * Replacement for {@link InvocationHandler#invoke(Object, Method, Object[])}. Implement your
	 * core proxy code here. This method is invoked immediately upon entering the
	 * <b><code>try</code></b> block of the <b><code>try-catch-finally</code></b>.
	 * </p>
	 *
	 * @param state
	 * @param proxy
	 * @param method
	 * @param args
	 * @throws Throwable
	 */
	protected abstract Object doInvoke(T state, Object proxy, Method method, Object[] args) throws Throwable;

	/**
	 * <p>
	 * Invoked immediately after {@link #doInvoke(Object, Object, Method, Object[])}, which means it
	 * will only be invoked if and only if {@link #doInvoke(Object, Object, Method, Object[])} did
	 * not raise an exception. The actual return value is provided as a parameter. This would be the
	 * last chance to do anything with that return value before it's actually returned to the
	 * caller, or raise an exception. Whatever this method ends up returning will be the actual
	 * return value for the overarching {@link #invoke(Object, Method, Object[])} invocation.
	 * </p>
	 *
	 * @param state
	 * @param proxy
	 * @param method
	 * @param args
	 * @param returnValue
	 * @throws Throwable
	 */
	protected Object onReturn(T state, Object proxy, Method method, Object[] args, Object returnValue)
		throws Throwable {
		// By default, do nothing
		return returnValue;
	}

	/**
	 * <p>
	 * Invoked on the <b><code>catch</code></b> phase of the <b><code>try-catch-finally</code></b>
	 * block. The actual exception raised is provided as an argument. The exception is the actual
	 * exception that the overarching {@link #invoke(Object, Method, Object[])} invocation will end
	 * up raising. However, the method can return another {@link Throwable} instance (the same
	 * typing restrictions apply as for {@link InvocationHandler#invoke(Object, Method, Object[])})
	 * to be raised instead of the exception (i.e. unwrapped contained exceptions, replace them,
	 * etc).
	 * </p>
	 * <p>
	 * If the method returns <code>null</code>, the original exception is raised.
	 * </p>
	 *
	 * @param state
	 * @param proxy
	 * @param method
	 * @param args
	 * @param thrown
	 * @param returnValue
	 * @throws Throwable
	 */
	protected Object onCatch(T state, Object proxy, Method method, Object[] args, Throwable thrown, Object returnValue)
		throws Throwable {
		// By default just rethrow the original exception
		throw thrown;
	}

	/**
	 * <p>
	 * Invoked during the <b><code>finally</code></b> phase of the
	 * <b><code>try-catch-finally</code></b> block.
	 * </p>
	 *
	 * @param state
	 * @param returnValue
	 * @param thrown
	 *            Will only be non-{@code null} if
	 *            {@link #onCatch(Object, Object, Method, Object[], Throwable, Object)} was also
	 *            invoked
	 * @param proxy
	 * @param method
	 * @param args
	 * @throws Throwable
	 */
	protected Object onFinally(T state, Object proxy, Method method, Object[] args, Object returnValue,
		Throwable thrown) throws Throwable {
		return returnValue;
	}
}
