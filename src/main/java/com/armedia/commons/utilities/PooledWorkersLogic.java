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
