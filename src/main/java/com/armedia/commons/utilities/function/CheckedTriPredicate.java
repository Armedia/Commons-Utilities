package com.armedia.commons.utilities.function;

import java.util.Objects;

@FunctionalInterface
public interface CheckedTriPredicate<T, U, V, EX extends Throwable> extends TriPredicate<T, U, V> {

	public boolean testChecked(T t, U u, V v) throws EX;

	@Override
	public default boolean test(T t, U u, V v) {
		try {
			return testChecked(t, u, v);
		} catch (Throwable thrown) {
			throw new RuntimeException(thrown.getMessage(), thrown);
		}
	}

	default CheckedTriPredicate<T, U, V, EX> and(
		CheckedTriPredicate<? super T, ? super U, ? super V, ? extends EX> other) {
		Objects.requireNonNull(other);
		return (T t, U u, V v) -> testChecked(t, u, v) && other.testChecked(t, u, v);
	}

	@Override
	default CheckedTriPredicate<T, U, V, EX> negate() {
		return (T t, U u, V v) -> !testChecked(t, u, v);
	}

	default CheckedTriPredicate<T, U, V, EX> or(
		CheckedTriPredicate<? super T, ? super U, ? super V, ? extends EX> other) {
		Objects.requireNonNull(other);
		return (T t, U u, V v) -> testChecked(t, u, v) || other.testChecked(t, u, v);
	}

	default CheckedTriPredicate<T, U, V, EX> xor(
		CheckedTriPredicate<? super T, ? super U, ? super V, ? extends EX> other) {
		Objects.requireNonNull(other);
		return (T t, U u, V v) -> testChecked(t, u, v) != other.testChecked(t, u, v);
	}

}