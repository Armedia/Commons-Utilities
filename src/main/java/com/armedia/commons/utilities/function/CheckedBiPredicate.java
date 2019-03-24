package com.armedia.commons.utilities.function;

import java.util.Objects;
import java.util.function.BiPredicate;

@FunctionalInterface
public interface CheckedBiPredicate<T, U, EX extends Throwable> extends BiPredicate<T, U> {

	public boolean testChecked(T t, U u) throws EX;

	@Override
	public default boolean test(T t, U u) {
		try {
			return testChecked(t, u);
		} catch (Throwable thrown) {
			throw new RuntimeException(thrown.getMessage(), thrown);
		}
	}

	default CheckedBiPredicate<T, U, EX> and(CheckedBiPredicate<? super T, ? super U, ? extends EX> other) {
		Objects.requireNonNull(other);
		return (T t, U u) -> testChecked(t, u) && other.testChecked(t, u);
	}

	@Override
	default CheckedBiPredicate<T, U, EX> negate() {
		return (T t, U u) -> !testChecked(t, u);
	}

	default CheckedBiPredicate<T, U, EX> or(CheckedBiPredicate<? super T, ? super U, ? extends EX> other) {
		Objects.requireNonNull(other);
		return (T t, U u) -> testChecked(t, u) || other.testChecked(t, u);
	}

	default CheckedBiPredicate<T, U, EX> xor(CheckedBiPredicate<? super T, ? super U, ? extends EX> other) {
		Objects.requireNonNull(other);
		return (T t, U u) -> testChecked(t, u) != other.testChecked(t, u);
	}

}