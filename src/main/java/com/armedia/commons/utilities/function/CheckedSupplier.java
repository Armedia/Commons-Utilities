package com.armedia.commons.utilities.function;

import java.util.function.Supplier;

@FunctionalInterface
public interface CheckedSupplier<T, EX extends Throwable> extends Supplier<T> {

	public T getChecked() throws EX;

	@Override
	public default T get() {
		try {
			return getChecked();
		} catch (Throwable t) {
			throw new RuntimeException(t.getMessage(), t);
		}
	}

}