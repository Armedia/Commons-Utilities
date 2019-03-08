package com.armedia.commons.utilities.function;

@FunctionalInterface
public interface CheckedRunnable<EX extends Throwable> extends Runnable {
	@Override
	public default void run() {
		try {
			runChecked();
		} catch (Throwable t) {
			RuntimeException re = new RuntimeException(t.getMessage(), t);
			for (Throwable s : t.getSuppressed()) {
				re.addSuppressed(s);
			}
			throw re;
		}
	}

	public void runChecked() throws EX;
}
