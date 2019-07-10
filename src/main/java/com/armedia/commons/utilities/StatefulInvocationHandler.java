/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2019 Armedia, LLC
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
