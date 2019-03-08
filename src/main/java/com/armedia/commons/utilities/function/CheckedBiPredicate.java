package com.armedia.commons.utilities.function;

import java.util.function.BiPredicate;

@FunctionalInterface
public interface CheckedBiPredicate<T, U, EX extends Throwable> extends BiPredicate<T, U> {
	public boolean testChecked(T t, U u) throws EX;

	@Override
	public default boolean test(T t, U u) {
		try {
			return testChecked(t, u);
		} catch (Throwable thrown) {
			RuntimeException re = new RuntimeException(thrown.getMessage(), thrown);
			for (Throwable s : thrown.getSuppressed()) {
				re.addSuppressed(s);
			}
			throw re;
		}
	}
}