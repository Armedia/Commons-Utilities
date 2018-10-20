package com.armedia.commons.utilities;

@FunctionalInterface
public interface Converter<A, B> {

	public B convert(A a);

}
