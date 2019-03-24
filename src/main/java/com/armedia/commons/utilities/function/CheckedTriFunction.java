package com.armedia.commons.utilities.function;

import java.util.Objects;

@FunctionalInterface
public interface CheckedTriFunction<T, U, V, R, EX extends Throwable> extends TriFunction<T, U, V, R> {

	public R applyChecked(T t, U u, V v) throws EX;

	@Override
	public default R apply(T t, U u, V v) {
		try {
			return applyChecked(t, u, v);
		} catch (Throwable thrown) {
			throw new RuntimeException(thrown.getMessage(), thrown);
		}
	}

	default <W> CheckedTriFunction<T, U, V, W, EX> andThen(
		CheckedFunction<? super R, ? extends W, ? extends EX> after) {
		Objects.requireNonNull(after);
		return (T t, U u, V v) -> after.applyChecked(applyChecked(t, u, v));
	}

}