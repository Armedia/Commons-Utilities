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
package com.armedia.commons.utilities;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StatefulInvocationHandlerTest {

	private static interface TestInterface {
		public UUID doSomething(String arg1, int arg2);

		public Object doAnotherThing(long l, UUID uuid, Date d);
	}

	@Test
	public void testStatefulInvocationHandler() throws Throwable {
		final AtomicReference<Object> stateRef = new AtomicReference<>(new Object());
		final AtomicReference<UUID> returnValue = new AtomicReference<>(null);
		final AtomicInteger paramCount = new AtomicInteger(0);
		final AtomicReference<Object> param1 = new AtomicReference<>(null);
		final AtomicReference<Object> param2 = new AtomicReference<>(null);
		final AtomicReference<Object> param3 = new AtomicReference<>(null);
		final AtomicReference<TestInterface> testProxy = new AtomicReference<>(null);
		final Method doSomething;
		{
			Class<?>[] args = {
				String.class, Integer.TYPE
			};
			doSomething = TestInterface.class.getMethod("doSomething", args);
		}
		final Method doAnotherThing;
		{
			Class<?>[] args = {
				Long.TYPE, UUID.class, Date.class
			};
			doAnotherThing = TestInterface.class.getMethod("doAnotherThing", args);
		}
		final AtomicReference<Method> testMethod = new AtomicReference<>();

		StatefulInvocationHandler<Object> handler = new StatefulInvocationHandler<Object>(stateRef.get()) {
			@Override
			protected Object invoke(Object state, Object proxy, Method method, Object[] args) throws Throwable {
				Assertions.assertSame(stateRef.get(), state);
				Assertions.assertSame(state, getState());
				Assertions.assertSame(testProxy.get(), proxy);
				Assertions.assertEquals(testMethod.get(), method);
				Assertions.assertEquals(paramCount.get(), args.length);
				if (paramCount.get() > 0) {
					Assertions.assertSame(param1.get(), args[0]);
				}
				if (paramCount.get() > 1) {
					Assertions.assertSame(param2.get(), args[1]);
				}
				if (paramCount.get() > 2) {
					Assertions.assertSame(param3.get(), args[2]);
				}
				return returnValue.get();
			}
		};
		Assertions.assertSame(stateRef.get(), handler.getState());

		Class<?>[] interfaces = {
			TestInterface.class
		};
		TestInterface ti = TestInterface.class
			.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, handler));
		testProxy.set(ti);
		testMethod.set(doSomething);
		paramCount.set(2);
		for (int i = -10; i < 10; i++) {
			returnValue.set(UUID.randomUUID());
			String str = UUID.randomUUID().toString();
			param1.set(str);
			param2.set(i);
			Assertions.assertSame(returnValue.get(), ti.doSomething(str, i));
		}

		testMethod.set(doAnotherThing);
		paramCount.set(3);
		for (long i = -10; i < 10; i++) {
			returnValue.set(UUID.randomUUID());
			UUID uuid = UUID.randomUUID();
			Date d = new Date();
			param1.set(i);
			param2.set(uuid);
			param3.set(d);
			Assertions.assertSame(returnValue.get(), ti.doAnotherThing(i, uuid, d));
		}
	}
}
