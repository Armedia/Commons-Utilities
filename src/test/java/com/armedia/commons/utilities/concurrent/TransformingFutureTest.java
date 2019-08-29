/*-
 * #%L
 * Armedia Commons Utilities
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
 */
package com.armedia.commons.utilities.concurrent;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TransformingFutureTest {

	@Test
	public void testTransformingFuture() {
		Future<Object> f = EasyMock.createStrictMock(Future.class);
		Assertions.assertThrows(NullPointerException.class, () -> new TransformingFuture<>(null, null));
		Assertions.assertThrows(NullPointerException.class, () -> new TransformingFuture<>(null, Objects::toString));
		Assertions.assertThrows(NullPointerException.class, () -> new TransformingFuture<>(f, null));
		new TransformingFuture<>(f, Objects::toString);
	}

	@Test
	public void testCancel() throws Exception {
		Future<Object> f = EasyMock.createStrictMock(Future.class);
		TransformingFuture<Object, String> tf = new TransformingFuture<>(f, Object::toString);

		EasyMock.reset(f);
		EasyMock.expect(f.cancel(EasyMock.eq(false))).andReturn(false).once();
		EasyMock.replay(f);
		Assertions.assertFalse(tf.cancel(false));
		EasyMock.verify(f);

		EasyMock.reset(f);
		EasyMock.expect(f.cancel(EasyMock.eq(false))).andReturn(true).once();
		EasyMock.replay(f);
		Assertions.assertTrue(tf.cancel(false));
		EasyMock.verify(f);

		EasyMock.reset(f);
		EasyMock.expect(f.cancel(EasyMock.eq(true))).andReturn(false).once();
		EasyMock.replay(f);
		Assertions.assertFalse(tf.cancel(true));
		EasyMock.verify(f);

		EasyMock.reset(f);
		EasyMock.expect(f.cancel(EasyMock.eq(true))).andReturn(true).once();
		EasyMock.replay(f);
		Assertions.assertTrue(tf.cancel(true));
		EasyMock.verify(f);
	}

	@Test
	public void testIsCancelled() {
		Future<Object> f = EasyMock.createStrictMock(Future.class);
		TransformingFuture<Object, String> tf = new TransformingFuture<>(f, Object::toString);

		EasyMock.reset(f);
		EasyMock.expect(f.isCancelled()).andReturn(false).once();
		EasyMock.replay(f);
		Assertions.assertFalse(tf.isCancelled());
		EasyMock.verify(f);

		EasyMock.reset(f);
		EasyMock.expect(f.isCancelled()).andReturn(true).once();
		EasyMock.replay(f);
		Assertions.assertTrue(tf.isCancelled());
		EasyMock.verify(f);
	}

	@Test
	public void testIsDone() {
		Future<Object> f = EasyMock.createStrictMock(Future.class);
		TransformingFuture<Object, String> tf = new TransformingFuture<>(f, Object::toString);

		EasyMock.reset(f);
		EasyMock.expect(f.isDone()).andReturn(false).once();
		EasyMock.replay(f);
		Assertions.assertFalse(tf.isDone());
		EasyMock.verify(f);

		EasyMock.reset(f);
		EasyMock.expect(f.isDone()).andReturn(true).once();
		EasyMock.replay(f);
		Assertions.assertTrue(tf.isDone());
		EasyMock.verify(f);
	}

	@Test
	public void testGet() throws InterruptedException, ExecutionException {
		final Future<Long> f = EasyMock.createStrictMock(Future.class);
		final Exception e = new Exception(UUID.randomUUID().toString());
		final Function<Long, String> transformer = Object::toString;
		final TransformingFuture<Long, String> happy = new TransformingFuture<>(f, transformer);
		final TransformingFuture<Long, String> unhappy = new TransformingFuture<>(f, (l) -> {
			throw e;
		});

		// Happy path
		for (long i = -100; i < 100; i++) {
			EasyMock.reset(f);
			EasyMock.expect(f.get()).andReturn(i).once();
			EasyMock.replay(f);
			String s = String.valueOf(i);
			Assertions.assertEquals(s, happy.get());
			EasyMock.verify(f);
		}

		// Unhappy path
		EasyMock.reset(f);
		EasyMock.expect(f.get()).andReturn(100L).once();
		EasyMock.replay(f);
		try {
			unhappy.get();
			Assertions.fail("Did not raise an expected exception");
		} catch (ExecutionException e2) {
			Assertions.assertSame(e, e2.getCause());
		}
		EasyMock.verify(f);
	}

	@Test
	public void testGetLongTimeUnit() throws Exception {
		final Future<Long> f = EasyMock.createStrictMock(Future.class);
		final Exception e = new Exception(UUID.randomUUID().toString());
		final Function<Long, String> transformer = Object::toString;
		final TransformingFuture<Long, String> happy = new TransformingFuture<>(f, transformer);
		final TransformingFuture<Long, String> unhappy = new TransformingFuture<>(f, (l) -> {
			throw e;
		});

		for (long d = 0; d < 10; d++) {
			for (TimeUnit tu : TimeUnit.values()) {
				// Happy path
				for (long i = -100; i < 100; i++) {
					EasyMock.reset(f);
					EasyMock.expect(f.get(EasyMock.eq(d), EasyMock.same(tu))).andReturn(i).once();
					EasyMock.replay(f);
					String s = String.valueOf(i);
					Assertions.assertEquals(s, happy.get(d, tu));
					EasyMock.verify(f);
				}

				// Unhappy path
				EasyMock.reset(f);
				EasyMock.expect(f.get(EasyMock.eq(d), EasyMock.same(tu))).andReturn(100L).once();
				EasyMock.replay(f);
				try {
					unhappy.get(d, tu);
					Assertions.fail("Did not raise an expected exception");
				} catch (ExecutionException e2) {
					Assertions.assertSame(e, e2.getCause());
				}
				EasyMock.verify(f);
			}
		}
	}

}
