package com.armedia.commons.utilities.concurrent;

public interface Lockable<L> {

	/**
	 * <p>
	 * Returns the lock instance that backs all the functionality.
	 * </p>
	 *
	 * @return the lock instance that backs all the functionality.
	 */
	public L getLock();

}