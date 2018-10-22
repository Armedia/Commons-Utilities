package com.armedia.commons.utilities;

@FunctionalInterface
public interface Initializer<T> {

	public T initialize();

}