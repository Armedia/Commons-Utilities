package com.armedia.commons.utilities.function;

@FunctionalInterface
public interface CheckedTriConsumer<T, U, V, EX extends Throwable> extends TriConsumer<T, U, V> {
	public void acceptChecked(T t, U u, V v) throws EX;

	@Override
	public default void accept(T t, U u, V v) {
		try {
			acceptChecked(t, u, v);
		} catch (Throwable thrown) {
			RuntimeException re = new RuntimeException(thrown.getMessage(), thrown);
			for (Throwable s : thrown.getSuppressed()) {
				re.addSuppressed(s);
			}
			throw re;
		}
	}
}