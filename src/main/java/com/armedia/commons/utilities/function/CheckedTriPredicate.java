package com.armedia.commons.utilities.function;

@FunctionalInterface
public interface CheckedTriPredicate<T, U, V, EX extends Throwable> extends TriPredicate<T, U, V> {
	public boolean testChecked(T t, U u, V v) throws EX;

	@Override
	public default boolean test(T t, U u, V v) {
		try {
			return testChecked(t, u, v);
		} catch (Throwable thrown) {
			RuntimeException re = new RuntimeException(thrown.getMessage(), thrown);
			for (Throwable s : thrown.getSuppressed()) {
				re.addSuppressed(s);
			}
			throw re;
		}
	}
}