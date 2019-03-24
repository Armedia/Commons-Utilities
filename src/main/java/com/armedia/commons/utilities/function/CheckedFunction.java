package com.armedia.commons.utilities.function;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface CheckedFunction<T, R, EX extends Throwable> extends Function<T, R> {

	public R applyChecked(T t) throws EX;

	@Override
	public default R apply(T t) {
		try {
			return applyChecked(t);
		} catch (Throwable thrown) {
			throw new RuntimeException(thrown.getMessage(), thrown);
		}
	}

	default <V> CheckedFunction<V, R, EX> compose(CheckedFunction<? super V, ? extends T, ? extends EX> before) {
		Objects.requireNonNull(before);
		return (V v) -> applyChecked(before.applyChecked(v));
	}

	default <V> CheckedFunction<T, V, EX> andThen(CheckedFunction<? super R, ? extends V, ? extends EX> after) {
		Objects.requireNonNull(after);
		return (T t) -> after.applyChecked(applyChecked(t));
	}

}