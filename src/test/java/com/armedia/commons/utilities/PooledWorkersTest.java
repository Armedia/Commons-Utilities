/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2022 Armedia, LLC
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

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PooledWorkersTest {

	@Test
	public void testBlocking() throws Exception {
		final AtomicLong data = new AtomicLong(0);
		PooledWorkersLogic<Object, String, Exception> logic = (o, s) -> {
			data.incrementAndGet();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				Assertions.fail("Unexpected InterruptedException caught");
			}
		};

		PooledWorkers.Builder<Object, String, Exception> pwb = new PooledWorkers.Builder<Object, String, Exception>() //
			.logic(logic) //
			.threads(4) //
			.name("Blocking") //
			.waitForWork(true) //
		;
		final int testCount = 100;
		PooledWorkers<Object, String> pw = pwb.start();
		Assertions.assertEquals(0, data.get());
		for (int i = 1; i <= testCount; i++) {
			pw.addWorkItem(String.format("%08x", i));
		}
		pw.waitForCompletion();
		Assertions.assertEquals(testCount, data.get());
	}

	@Test
	public void testInterrupt() throws Exception {
		final AtomicLong data = new AtomicLong(0);
		PooledWorkersLogic<Object, String, Exception> logic = (o, s) -> {
			data.incrementAndGet();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				Assertions.fail("Unexpected InterruptedException caught");
			}
		};

		PooledWorkers.Builder<Object, String, Exception> pwb = new PooledWorkers.Builder<Object, String, Exception>() //
			.logic(logic) //
			.threads(4) //
			.name("Interrupt") //
			.waitForWork(true) //
		;
		final int testCount = 100;
		PooledWorkers<Object, String> pw = pwb.start();
		Assertions.assertEquals(0, data.get());
		for (int i = 1; i <= testCount; i++) {
			pw.addWorkItem(String.format("%08x", i));
		}
		while (data.get() < 100) {
			Thread.sleep(100);
		}
		pw.waitForCompletion();
		Assertions.assertEquals(testCount, data.get());
	}

	@Test
	public void testNonBlocking() throws Exception {
		final AtomicLong data = new AtomicLong(0);
		PooledWorkersLogic<Object, String, Exception> logic = (o, s) -> {
			data.incrementAndGet();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				Assertions.fail("Unexpected InterruptedException caught");
			}
		};

		final int testCount = 100;
		Collection<String> items = new LinkedList<>();
		for (int i = 1; i <= testCount; i++) {
			items.add(String.format("%08x", i));
		}
		Assertions.assertEquals(0, data.get());
		PooledWorkers.Builder<Object, String, Exception> pwb = new PooledWorkers.Builder<Object, String, Exception>() //
			.logic(logic) //
			.threads(4) //
			.name("NonBlocking") //
			.waitForWork(false) //
			.items(items) //
		;
		PooledWorkers<Object, String> pw = pwb.start();
		pw.waitForCompletion();
		Assertions.assertEquals(testCount, data.get());
	}
}
