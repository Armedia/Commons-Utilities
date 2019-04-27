package com.armedia.commons.utilities.function;

import java.util.Objects;

@FunctionalInterface
public interface CheckedRunnable<EX extends Throwable> extends Runnable {

	public void runChecked() throws EX;

	@Override
	public default void run() {
		try {
			runChecked();
		} catch (Throwable t) {
			throw new RuntimeException(t.getMessage(), t);
		}
	}

	public default CheckedRunnable<EX> andThen(CheckedRunnable<EX> after) {
		Objects.requireNonNull(after);
		return () -> {
			runChecked();
			after.runChecked();
		};
	}

	public default CheckedRunnable<EX> andThen(Runnable after) {
		Objects.requireNonNull(after);
		return () -> {
			runChecked();
			after.run();
		};
	}
}