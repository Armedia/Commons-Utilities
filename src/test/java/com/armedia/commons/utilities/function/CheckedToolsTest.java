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
package com.armedia.commons.utilities.function;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CheckedToolsTest {

	@Test
	public void testConstructor() {
		new CheckedTools();
	}

	@Test
	public void testCheckConsumer() {
		Assertions.assertThrows(NullPointerException.class, () -> CheckedTools.check((Consumer<String>) null));
		Consumer<String> unchecked = EasyMock.createStrictMock(Consumer.class);
		CheckedConsumer<String, RuntimeException> checked = CheckedTools.check(unchecked);
		final String value = UUID.randomUUID().toString();
		unchecked.accept(EasyMock.same(value));
		EasyMock.expectLastCall().once();
		EasyMock.replay(unchecked);
		checked.acceptChecked(value);
		EasyMock.verify(unchecked);
	}

	@Test
	public void testCheckFunction() {
		Assertions.assertThrows(NullPointerException.class, () -> CheckedTools.check((Function<String, Double>) null));
		Function<String, Double> unchecked = EasyMock.createStrictMock(Function.class);
		CheckedFunction<String, Double, RuntimeException> checked = CheckedTools.check(unchecked);
		final String value = UUID.randomUUID().toString();
		final Double result = Math.random();
		EasyMock.expect(unchecked.apply(EasyMock.same(value))).andReturn(result).once();
		EasyMock.replay(unchecked);
		Assertions.assertSame(result, checked.applyChecked(value));
		EasyMock.verify(unchecked);
	}

	@Test
	public void testCheckPredicate() {
		Assertions.assertThrows(NullPointerException.class, () -> CheckedTools.check((Predicate<String>) null));
		Predicate<String> unchecked = EasyMock.createStrictMock(Predicate.class);
		CheckedPredicate<String, RuntimeException> checked = CheckedTools.check(unchecked);
		final String value = UUID.randomUUID().toString();
		EasyMock.expect(unchecked.test(EasyMock.same(value))).andReturn(true).once();
		EasyMock.replay(unchecked);
		Assertions.assertTrue(checked.testChecked(value));
		EasyMock.verify(unchecked);
	}

	@Test
	public void testCheckRunnable() {
		Assertions.assertThrows(NullPointerException.class, () -> CheckedTools.check((Runnable) null));
		Runnable unchecked = EasyMock.createStrictMock(Runnable.class);
		CheckedRunnable<RuntimeException> checked = CheckedTools.check(unchecked);
		unchecked.run();
		EasyMock.expectLastCall().once();
		EasyMock.replay(unchecked);
		checked.runChecked();
		EasyMock.verify(unchecked);
	}

	@Test
	public void testCheckSupplier() {
		Assertions.assertThrows(NullPointerException.class, () -> CheckedTools.check((Supplier<Double>) null));
		Supplier<Double> unchecked = EasyMock.createStrictMock(Supplier.class);
		CheckedSupplier<Double, RuntimeException> checked = CheckedTools.check(unchecked);
		final Double result = Math.random();
		EasyMock.expect(unchecked.get()).andReturn(result).once();
		EasyMock.replay(unchecked);
		Assertions.assertSame(result, checked.getChecked());
		EasyMock.verify(unchecked);
	}

	@Test
	public void testCheckBiConsumer() {
		Assertions.assertThrows(NullPointerException.class,
			() -> CheckedTools.check((BiConsumer<String, Double>) null));
		BiConsumer<String, Double> unchecked = EasyMock.createStrictMock(BiConsumer.class);
		CheckedBiConsumer<String, Double, RuntimeException> checked = CheckedTools.check(unchecked);
		final String s = UUID.randomUUID().toString();
		final Double d = Math.random();
		unchecked.accept(EasyMock.same(s), EasyMock.same(d));
		EasyMock.expectLastCall().once();
		EasyMock.replay(unchecked);
		checked.acceptChecked(s, d);
		EasyMock.verify(unchecked);
	}

	@Test
	public void testCheckBiFunction() {
		Assertions.assertThrows(NullPointerException.class,
			() -> CheckedTools.check((BiFunction<String, UUID, Double>) null));
		BiFunction<String, UUID, Double> unchecked = EasyMock.createStrictMock(BiFunction.class);
		CheckedBiFunction<String, UUID, Double, RuntimeException> checked = CheckedTools.check(unchecked);
		final String s = UUID.randomUUID().toString();
		final UUID u = UUID.randomUUID();
		final Double d = Math.random();
		EasyMock.expect(unchecked.apply(EasyMock.same(s), EasyMock.same(u))).andReturn(d).once();
		EasyMock.replay(unchecked);
		Assertions.assertSame(d, checked.applyChecked(s, u));
		EasyMock.verify(unchecked);
	}

	@Test
	public void testCheckBiPredicate() {
		Assertions.assertThrows(NullPointerException.class,
			() -> CheckedTools.check((BiPredicate<String, Double>) null));
		BiPredicate<String, Double> unchecked = EasyMock.createStrictMock(BiPredicate.class);
		CheckedBiPredicate<String, Double, RuntimeException> checked = CheckedTools.check(unchecked);
		final String v = UUID.randomUUID().toString();
		final Double d = Math.random();
		EasyMock.expect(unchecked.test(EasyMock.same(v), EasyMock.same(d))).andReturn(true).once();
		EasyMock.replay(unchecked);
		Assertions.assertTrue(checked.testChecked(v, d));
		EasyMock.verify(unchecked);
	}

	@Test
	public void testCheckTriConsumer() {
		Assertions.assertThrows(NullPointerException.class,
			() -> CheckedTools.check((TriConsumer<String, Double, UUID>) null));
		TriConsumer<String, Double, UUID> unchecked = EasyMock.createStrictMock(TriConsumer.class);
		CheckedTriConsumer<String, Double, UUID, RuntimeException> checked = CheckedTools.check(unchecked);
		final String s = UUID.randomUUID().toString();
		final Double d = Math.random();
		final UUID u = UUID.randomUUID();
		unchecked.accept(EasyMock.same(s), EasyMock.same(d), EasyMock.same(u));
		EasyMock.expectLastCall().once();
		EasyMock.replay(unchecked);
		checked.acceptChecked(s, d, u);
		EasyMock.verify(unchecked);
	}

	@Test
	public void testCheckTriFunction() {
		Assertions.assertThrows(NullPointerException.class,
			() -> CheckedTools.check((TriFunction<String, UUID, Double, Long>) null));
		TriFunction<String, UUID, Double, Long> unchecked = EasyMock.createStrictMock(TriFunction.class);
		CheckedTriFunction<String, UUID, Double, Long, RuntimeException> checked = CheckedTools.check(unchecked);
		final String s = UUID.randomUUID().toString();
		final UUID u = UUID.randomUUID();
		final Double d = Math.random();
		final Long l = System.nanoTime();
		EasyMock.expect(unchecked.apply(EasyMock.same(s), EasyMock.same(u), EasyMock.same(d))).andReturn(l).once();
		EasyMock.replay(unchecked);
		Assertions.assertSame(l, checked.applyChecked(s, u, d));
		EasyMock.verify(unchecked);
	}

	@Test
	public void testCheckTriPredicate() {
		Assertions.assertThrows(NullPointerException.class,
			() -> CheckedTools.check((TriPredicate<String, Double, UUID>) null));
		TriPredicate<String, Double, UUID> unchecked = EasyMock.createStrictMock(TriPredicate.class);
		CheckedTriPredicate<String, Double, UUID, RuntimeException> checked = CheckedTools.check(unchecked);
		final String s = UUID.randomUUID().toString();
		final Double d = Math.random();
		final UUID u = UUID.randomUUID();
		EasyMock.expect(unchecked.test(EasyMock.same(s), EasyMock.same(d), EasyMock.same(u))).andReturn(true).once();
		EasyMock.replay(unchecked);
		Assertions.assertTrue(checked.testChecked(s, d, u));
		EasyMock.verify(unchecked);
	}
}
