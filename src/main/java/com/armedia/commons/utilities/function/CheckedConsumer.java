package com.armedia.commons.utilities.function;

import java.util.Objects;
import java.util.function.Consumer;

@FunctionalInterface
public interface CheckedConsumer<T, EX extends Throwable> extends Consumer<T> {

	public void acceptChecked(T t) throws EX;

	@Override
	public default void accept(T t) {
		try {
			acceptChecked(t);
		} catch (Throwable thrown) {
			throw new RuntimeException(thrown.getMessage(), thrown);
		}
	}

	default CheckedConsumer<T, EX> andThen(CheckedConsumer<? super T, ? extends EX> after) {
		Objects.requireNonNull(after);
		return (T t) -> {
			acceptChecked(t);
			after.acceptChecked(t);
		};
	}

}