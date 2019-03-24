package com.armedia.commons.utilities.function;

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

}