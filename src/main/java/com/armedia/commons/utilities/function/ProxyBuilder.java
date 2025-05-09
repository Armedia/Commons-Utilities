/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2025 Armedia, LLC
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
package com.armedia.commons.utilities.function;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.apache.commons.lang3.ClassUtils;

import com.armedia.commons.utilities.concurrent.BaseShareableLockable;
import com.armedia.commons.utilities.concurrent.MutexAutoLock;
import com.armedia.commons.utilities.concurrent.SharedAutoLock;

public final class ProxyBuilder {
	private static final Class<?>[] NO_CLASSES = {};

	@FunctionalInterface
	public static interface ArgumentProcessor {
		public Object[] processArguments(Object target, Method method, Object[] args);
	}

	@FunctionalInterface
	public static interface ArgumentProcessorSelector {
		public ArgumentProcessor selectArgumentProcessor(Method method, Object[] args);
	}

	@FunctionalInterface
	public static interface MethodSubstitute {
		public Object invoke(Object target, Method method, Object[] args) throws Exception;
	}

	@FunctionalInterface
	public static interface MethodSubstituteSelector {
		public MethodSubstitute selectMethodSubstitute(Method method, Object[] args);
	}

	@FunctionalInterface
	public static interface ResultProcessor {
		public Object processResult(Method method, Object[] args, Object result, Exception thrown);
	}

	@FunctionalInterface
	public static interface ResultProcessorSelector {
		public ResultProcessor selectResultProcessor(Method method, Object[] args);
	}

	@FunctionalInterface
	public static interface ExceptionHandler {
		public Object handleException(Object target, Method method, Object[] args, Exception thrown) throws Exception;
	}

	@FunctionalInterface
	public static interface ExceptionHandlerSelector {
		public ExceptionHandler selectExceptionHandler(Method method, Object[] args);
	}

	@FunctionalInterface
	public static interface FinallyProcessor {
		public void processFinally(Method method, Object[] args, Object result, Exception thrown);
	}

	private final BaseShareableLockable lock = new BaseShareableLockable();
	private ArgumentProcessorSelector argumentProcessorSelector = null;
	private MethodSubstituteSelector methodSubstituteSelector = null;
	private ResultProcessorSelector resultProcessorSelector = null;
	private ExceptionHandlerSelector exceptionHandlerSelector = null;
	private FinallyProcessor finallyProcessor = null;

	public ProxyBuilder() {
		this(null);
	}

	public ProxyBuilder(ProxyBuilder other) {
		if (other != null) {
			this.argumentProcessorSelector = other.argumentProcessorSelector;
			this.methodSubstituteSelector = other.methodSubstituteSelector;
			this.resultProcessorSelector = other.resultProcessorSelector;
			this.exceptionHandlerSelector = other.exceptionHandlerSelector;
			this.finallyProcessor = other.finallyProcessor;
		}
	}

	public ProxyBuilder withArgumentProcessorSelector(ArgumentProcessorSelector argumentProcessorSelector) {
		try (MutexAutoLock mutex = this.lock.mutexAutoLock()) {
			this.argumentProcessorSelector = argumentProcessorSelector;
		}
		return this;
	}

	public ArgumentProcessorSelector argumentProcessorSelector() {
		return this.lock.shareLocked(() -> this.argumentProcessorSelector);
	}

	public ProxyBuilder withMethodSubstituteSelector(MethodSubstituteSelector methodSubstituteSelector) {
		try (MutexAutoLock mutex = this.lock.mutexAutoLock()) {
			this.methodSubstituteSelector = methodSubstituteSelector;
		}
		return this;
	}

	public MethodSubstituteSelector methodSubstituteSelector() {
		return this.lock.shareLocked(() -> this.methodSubstituteSelector);
	}

	public ProxyBuilder withResultProcessorSelector(ResultProcessorSelector resultProcessorSelector) {
		try (MutexAutoLock mutex = this.lock.mutexAutoLock()) {
			this.resultProcessorSelector = resultProcessorSelector;
		}
		return this;
	}

	public ResultProcessorSelector resultProcessorSelector() {
		return this.lock.shareLocked(() -> this.resultProcessorSelector);
	}

	public ProxyBuilder withExceptionHandlerSelector(ExceptionHandlerSelector exceptionHandlerSelector) {
		try (MutexAutoLock mutex = this.lock.mutexAutoLock()) {
			this.exceptionHandlerSelector = exceptionHandlerSelector;
		}
		return this;
	}

	public ExceptionHandlerSelector exceptionHandlerSelector() {
		return this.lock.shareLocked(() -> this.exceptionHandlerSelector);
	}

	public ProxyBuilder withFinallyProcessor(FinallyProcessor finallyProcessor) {
		try (MutexAutoLock mutex = this.lock.mutexAutoLock()) {
			this.finallyProcessor = finallyProcessor;
		}
		return this;
	}

	public FinallyProcessor finallyProcessor() {
		return this.lock.shareLocked(() -> this.finallyProcessor);
	}

	public <T> T proxy(T target) {
		return proxy(null, target);
	}

	public <T> T proxy(ClassLoader classLoader, T target) {
		Interceptor interceptor = null;
		try (SharedAutoLock shared = this.lock.sharedAutoLock()) {
			interceptor = new Interceptor( //
				target, //
				this.argumentProcessorSelector, //
				this.methodSubstituteSelector, //
				this.resultProcessorSelector, //
				this.exceptionHandlerSelector, //
				this.finallyProcessor //
			);
		}
		if (classLoader == null) {
			classLoader = Thread.currentThread().getContextClassLoader();
		}

		List<Class<?>> interfaces = ClassUtils.getAllInterfaces(target.getClass());
		@SuppressWarnings("unchecked")
		T proxy = (T) Proxy.newProxyInstance(classLoader, interfaces.toArray(ProxyBuilder.NO_CLASSES), interceptor);
		return proxy;
	}

	public static boolean isIntercepted(Object o) {
		if ((o != null) && Proxy.isProxyClass(o.getClass())) {
			Object handler = Proxy.getInvocationHandler(o);
			if (handler.getClass() == Interceptor.class) { return true; }
		}
		return false;
	}

	private static final class Interceptor implements InvocationHandler {
		private final Object target;
		private final ArgumentProcessorSelector argumentProcessorSelector;
		private final MethodSubstituteSelector methodSubstituteSelector;
		private final ResultProcessorSelector resultProcessorSelector;
		private final ExceptionHandlerSelector exceptionHandlerSelector;
		private final FinallyProcessor finallyProcessor;

		private Interceptor(Object target, ArgumentProcessorSelector argumentProcessorSelector,
			MethodSubstituteSelector methodSubstituteSelector, ResultProcessorSelector resultProcessorSelector,
			ExceptionHandlerSelector exceptionHandlerSelector, FinallyProcessor finallyProcessor) {
			this.target = target;
			this.argumentProcessorSelector = argumentProcessorSelector;
			this.methodSubstituteSelector = methodSubstituteSelector;
			this.resultProcessorSelector = resultProcessorSelector;
			this.exceptionHandlerSelector = exceptionHandlerSelector;
			this.finallyProcessor = finallyProcessor;
		}

		private Object[] processArguments(Method method, Object[] args) {
			if (this.argumentProcessorSelector != null) {
				ArgumentProcessor argumentProcessor = this.argumentProcessorSelector.selectArgumentProcessor(method,
					args);
				if (argumentProcessor != null) { return argumentProcessor.processArguments(this.target, method, args); }
			}
			return args;
		}

		private Object substituteMethod(Method method, Object[] args) throws Exception {
			if (this.methodSubstituteSelector != null) {
				MethodSubstitute methodSubstitute = this.methodSubstituteSelector.selectMethodSubstitute(method, args);
				if (methodSubstitute != null) { return methodSubstitute.invoke(this.target, method, args); }
			}
			return method.invoke(this.target, args);
		}

		private Object processResult(Object result, Method method, Object[] args, Exception thrown) {
			if (this.resultProcessorSelector != null) {
				ResultProcessor resultProcessor = this.resultProcessorSelector.selectResultProcessor(method, args);
				if (resultProcessor != null) { return resultProcessor.processResult(method, args, result, thrown); }
			}
			return result;
		}

		private Object handleException(Exception thrown, Method method, Object[] args) throws Exception {
			if (this.exceptionHandlerSelector != null) {
				ExceptionHandler exceptionHandler = this.exceptionHandlerSelector.selectExceptionHandler(method, args);
				if (exceptionHandler != null) {
					return exceptionHandler.handleException(this.target, method, args, thrown);
				}
			}
			throw thrown;
		}

		private void processFinally(Method method, Object[] args, Object result, Exception thrown) {
			// By default, do nothing...
			if (this.finallyProcessor != null) {
				this.finallyProcessor.processFinally(method, args, result, thrown);
			}
		}

		@Override
		public final Object invoke(Object proxy, Method method, Object[] args) throws Exception {
			Object returned = null;
			Exception thrown = null;
			try {
				// Adjust the arguments
				args = processArguments(method, args);

				// Do the interception/invocation
				Object result = substituteMethod(method, args);

				// Process the results to be returned
				result = processResult(result, method, args, null);

				// Return the results
				return (returned = result);
			} catch (Exception t) {
				// Handle the exception, in case we want to swallow it or turn it into something
				// else
				Object result = handleException(thrown = t, method, args);

				// Process the result
				result = processResult(result, method, args, thrown);

				// Return the results
				return (returned = result);
			} finally {
				// Process after everything, in case we want to document...
				processFinally(method, args, returned, thrown);
			}
		}
	}
}
