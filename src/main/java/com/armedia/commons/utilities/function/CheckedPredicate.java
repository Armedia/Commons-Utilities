package com.armedia.commons.utilities.function;

import java.util.function.Predicate;

@FunctionalInterface
public interface CheckedPredicate<T, EX extends Throwable> extends Predicate<T> {
	public boolean testChecked(T t) throws EX;

	@Override
	public default boolean test(T t) {
		try {
			return testChecked(t);
		} catch (Throwable thrown) {
			RuntimeException re = new RuntimeException(thrown.getMessage(), thrown);
			for (Throwable s : thrown.getSuppressed()) {
				re.addSuppressed(s);
			}
			throw re;
		}
	}
}