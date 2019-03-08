package com.armedia.commons.utilities.function;

import java.util.function.Consumer;

@FunctionalInterface
public interface CheckedConsumer<T, EX extends Throwable> extends Consumer<T> {
	public void acceptChecked(T t) throws EX;

	@Override
	public default void accept(T t) {
		try {
			acceptChecked(t);
		} catch (Throwable thrown) {
			RuntimeException re = new RuntimeException(thrown.getMessage(), thrown);
			for (Throwable s : thrown.getSuppressed()) {
				re.addSuppressed(s);
			}
			throw re;
		}
	}
}