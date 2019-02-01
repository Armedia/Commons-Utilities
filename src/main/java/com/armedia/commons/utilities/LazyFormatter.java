package com.armedia.commons.utilities;

import java.util.Objects;
import java.util.function.Supplier;

public final class LazyFormatter {

	private static final Object[] NO_ARGS = {};

	private final Supplier<String> supplier;
	private volatile String value = null;

	private LazyFormatter(Supplier<String> str) {
		this.supplier = Objects.requireNonNull(str, "Must provide a non-null String Supplier instance");
	}

	public static LazyFormatter lazyFormat(String format, Object... args) {
		Objects.requireNonNull(format, "Must provide a non-null formatter string");
		return LazyFormatter.lazyFormat(() -> String.format(format, Tools.coalesce(args, LazyFormatter.NO_ARGS)));
	}

	public static LazyFormatter lazyFormat(Supplier<String> str) {
		return new LazyFormatter(str);
	}

	@Override
	public String toString() {
		String localValue = this.value;
		if (localValue == null) {
			synchronized (this) {
				localValue = this.value;
				if (localValue == null) {
					this.value = localValue = this.supplier.get();
				}
			}
		}
		return localValue;
	}
}