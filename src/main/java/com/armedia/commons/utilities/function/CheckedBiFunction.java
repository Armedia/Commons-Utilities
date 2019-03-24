package com.armedia.commons.utilities.function;

import java.util.Objects;
import java.util.function.BiFunction;

@FunctionalInterface
public interface CheckedBiFunction<T, U, R, EX extends Throwable> extends BiFunction<T, U, R> {

	public R applyChecked(T t, U u) throws EX;

	@Override
	public default R apply(T t, U u) {
		try {
			return applyChecked(t, u);
		} catch (Throwable thrown) {
			throw new RuntimeException(thrown.getMessage(), thrown);
		}
	}

	default <V> CheckedBiFunction<T, U, V, EX> andThen(CheckedFunction<? super R, ? extends V, ? extends EX> after) {
		Objects.requireNonNull(after);
		return (T t, U u) -> after.applyChecked(applyChecked(t, u));
	}

}