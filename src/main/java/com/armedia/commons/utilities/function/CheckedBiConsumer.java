package com.armedia.commons.utilities.function;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface CheckedBiConsumer<T, U, EX extends Throwable> extends BiConsumer<T, U> {

	public void acceptChecked(T t, U u) throws EX;

	@Override
	public default void accept(T t, U u) {
		try {
			acceptChecked(t, u);
		} catch (Throwable thrown) {
			throw new RuntimeException(thrown.getMessage(), thrown);
		}
	}

	default CheckedBiConsumer<T, U, EX> andThen(CheckedBiConsumer<? super T, ? super U, ? extends EX> after) {
		Objects.requireNonNull(after);

		return (l, r) -> {
			acceptChecked(l, r);
			after.acceptChecked(l, r);
		};
	}

}