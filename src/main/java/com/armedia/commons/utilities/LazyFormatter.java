package com.armedia.commons.utilities;

import java.util.Objects;
import java.util.function.Supplier;

public final class LazyFormatter implements Supplier<String> {

	private final Supplier<String> supplier;
	private volatile String value = null;

	private LazyFormatter(Supplier<String> str) {
		this.supplier = Objects.requireNonNull(str, "Must provide a non-null String Supplier instance");
	}

	private LazyFormatter(String str) {
		this.value = Objects.requireNonNull(str, "Must provide a non-null String instance");
		this.supplier = null;
	}

	public static LazyFormatter lazyFormat(String format, Object... args) {
		Objects.requireNonNull(format, "Must provide a non-null formatter string");
		if ((args == null) || (args.length == 0)) { return LazyFormatter.lazyFormat(format); }
		return LazyFormatter.lazyFormat(() -> String.format(format, args));
	}

	public static LazyFormatter lazyFormat(Supplier<String> str) {
		return new LazyFormatter(str);
	}

	@Override
	public String get() {
		return toString();
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