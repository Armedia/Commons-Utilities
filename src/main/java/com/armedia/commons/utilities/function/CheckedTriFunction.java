package com.armedia.commons.utilities.function;

@FunctionalInterface
public interface CheckedTriFunction<T, U, V, R, EX extends Throwable> extends TriFunction<T, U, V, R> {
	public R applyChecked(T t, U u, V v) throws EX;

	@Override
	public default R apply(T t, U u, V v) {
		try {
			return applyChecked(t, u, v);
		} catch (Throwable thrown) {
			RuntimeException re = new RuntimeException(thrown.getMessage(), thrown);
			for (Throwable s : thrown.getSuppressed()) {
				re.addSuppressed(s);
			}
			throw re;
		}
	}
}