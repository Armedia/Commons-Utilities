package com.armedia.commons.utilities;

import java.util.Iterator;

public class ConvertingIterator<A, B> implements Iterator<B> {

	protected final Iterator<A> iterator;
	protected final Converter<A, B> converter;

	public ConvertingIterator(Iterator<A> iterator, Converter<A, B> converter) {
		if (iterator == null) { throw new IllegalArgumentException("Must provide a non-null Iterator instance"); }
		if (converter == null) { throw new IllegalArgumentException("Must provide a non-null Converter instance"); }
		this.iterator = iterator;
		this.converter = converter;
	}

	@Override
	public final boolean hasNext() {
		return this.iterator.hasNext();
	}

	@Override
	public final B next() {
		A a = this.iterator.next();
		if (a == null) { return null; }
		return this.converter.convert(a);
	}

	@Override
	public final void remove() {
		this.iterator.remove();
	}
}