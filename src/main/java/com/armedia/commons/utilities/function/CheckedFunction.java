package com.armedia.commons.utilities.function;

import java.util.function.Function;

@FunctionalInterface
public interface CheckedFunction<T, R, EX extends Throwable> extends Function<T, R> {
	public R applyChecked(T t) throws EX;

	@Override
	public default R apply(T t) {
		try {
			return applyChecked(t);
		} catch (Throwable thrown) {
			RuntimeException re = new RuntimeException(thrown.getMessage(), thrown);
			for (Throwable s : thrown.getSuppressed()) {
				re.addSuppressed(s);
			}
			throw re;
		}
	}
}