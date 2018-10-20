package com.armedia.commons.utilities;

import java.util.Iterator;

public class ConvertingIterable<A, B> implements Iterable<B> {

	protected final Iterable<A> iterable;
	protected final Converter<A, B> converter;

	public ConvertingIterable(Iterable<A> iterable, Converter<A, B> converter) {
		if (iterable == null) { throw new IllegalArgumentException("Must provide a non-null Iterable instance"); }
		this.iterable = iterable;
		this.converter = converter;
	}

	@Override
	public Iterator<B> iterator() {
		return new ConvertingIterator<>(this.iterable.iterator(), this.converter);
	}
}