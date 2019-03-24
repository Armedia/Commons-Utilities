package com.armedia.commons.utilities.function;

import java.util.Objects;

@FunctionalInterface
public interface CheckedTriConsumer<T, U, V, EX extends Throwable> extends TriConsumer<T, U, V> {

	public void acceptChecked(T t, U u, V v) throws EX;

	@Override
	public default void accept(T t, U u, V v) {
		try {
			acceptChecked(t, u, v);
		} catch (Throwable thrown) {
			throw new RuntimeException(thrown.getMessage(), thrown);
		}
	}

	default CheckedTriConsumer<T, U, V, EX> andThen(
		CheckedTriConsumer<? super T, ? super U, ? super V, ? extends EX> after) {
		Objects.requireNonNull(after);
		return (l, m, r) -> {
			acceptChecked(l, m, r);
			after.acceptChecked(l, m, r);
		};
	}

}