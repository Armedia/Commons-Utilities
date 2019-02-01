package com.armedia.commons.utilities.function;

import java.util.function.Supplier;

@FunctionalInterface
public interface CheckedSupplier<T> extends Supplier<T> {
	public T getChecked() throws Exception;

	@Override
	public default T get() {
		try {
			return getChecked();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}