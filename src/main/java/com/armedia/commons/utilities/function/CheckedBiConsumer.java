package com.armedia.commons.utilities.function;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface CheckedBiConsumer<T, U, EX extends Throwable> extends BiConsumer<T, U> {
	public void acceptChecked(T t, U u) throws EX;

	@Override
	public default void accept(T t, U u) {
		try {
			acceptChecked(t, u);
		} catch (Throwable thrown) {
			RuntimeException re = new RuntimeException(thrown.getMessage(), thrown);
			for (Throwable s : thrown.getSuppressed()) {
				re.addSuppressed(s);
			}
			throw re;
		}
	}
}