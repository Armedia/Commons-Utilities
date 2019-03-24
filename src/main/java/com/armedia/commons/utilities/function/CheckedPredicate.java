package com.armedia.commons.utilities.function;

import java.util.Objects;
import java.util.function.Predicate;

@FunctionalInterface
public interface CheckedPredicate<T, EX extends Throwable> extends Predicate<T> {

	public boolean testChecked(T t) throws EX;

	@Override
	public default boolean test(T t) {
		try {
			return testChecked(t);
		} catch (Throwable thrown) {
			throw new RuntimeException(thrown.getMessage(), thrown);
		}
	}

	default CheckedPredicate<T, EX> and(CheckedPredicate<? super T, ? extends EX> other) {
		Objects.requireNonNull(other);
		return (t) -> testChecked(t) && other.testChecked(t);
	}

	@Override
	default CheckedPredicate<T, EX> negate() {
		return (t) -> !testChecked(t);
	}

	default CheckedPredicate<T, EX> or(CheckedPredicate<? super T, ? extends EX> other) {
		Objects.requireNonNull(other);
		return (t) -> testChecked(t) || other.testChecked(t);
	}

	default CheckedPredicate<T, EX> xor(CheckedPredicate<? super T, ? extends EX> other) {
		Objects.requireNonNull(other);
		return (t) -> testChecked(t) != other.testChecked(t);
	}

}