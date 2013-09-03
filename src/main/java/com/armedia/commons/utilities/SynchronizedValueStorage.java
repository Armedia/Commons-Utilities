/**
 * *******************************************************************
 * 
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS.
 * REPRODUCTION OF ANY PORTION OF THE SOURCE CODE, CONTAINED HEREIN,
 * OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE,
 * IS STRICTLY PROHIBITED.
 * 
 * Confidential Property of Armedia LLC.
 * (c) Copyright Armedia LLC 2011.
 * All Rights reserved.
 * 
 * *******************************************************************
 */
package com.armedia.commons.utilities;

import java.util.Set;

/**
 * @author drivera@armedia.com
 * 
 */
public class SynchronizedValueStorage<T> extends SimpleValueStorage<T> {

	public SynchronizedValueStorage() {
		super();
	}

	public SynchronizedValueStorage(boolean ordered) {
		super(ordered);
	}

	@Override
	public synchronized T setValue(String name, T value) {
		return super.setValue(name, value);
	}

	@Override
	public synchronized T getValue(String name) {
		return super.getValue(name);
	}

	@Override
	public synchronized boolean hasValue(String name) {
		return super.hasValue(name);
	}

	@Override
	public synchronized Set<String> getValueNames() {
		return super.getValueNames();
	}

	@Override
	public synchronized T clearValue(String name) {
		return super.clearValue(name);
	}

	@Override
	public synchronized void clearAllValues() {
		super.clearAllValues();
	}
}