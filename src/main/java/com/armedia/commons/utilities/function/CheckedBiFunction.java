package com.armedia.commons.utilities.function;

import java.util.function.BiFunction;

@FunctionalInterface
public interface CheckedBiFunction<T, U, R, EX extends Throwable> extends BiFunction<T, U, R> {
	public R applyChecked(T t, U u) throws EX;

	@Override
	public default R apply(T t, U u) {
		try {
			return applyChecked(t, u);
		} catch (Throwable thrown) {
			RuntimeException re = new RuntimeException(thrown.getMessage(), thrown);
			for (Throwable s : thrown.getSuppressed()) {
				re.addSuppressed(s);
			}
			throw re;
		}
	}
}