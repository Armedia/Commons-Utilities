package com.armedia.commons.utilities;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamTools {

	public static final int DEFAULT_CHARACTERISTICS = Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.ORDERED;
	public static final boolean DEFAULT_PARALLEL = false;

	public static <T> Stream<T> fromIterator(Iterator<T> it) {
		return StreamTools.fromIterator(it, StreamTools.DEFAULT_CHARACTERISTICS, StreamTools.DEFAULT_PARALLEL);
	}

	public static <T> Stream<T> fromIterator(Iterator<T> it, int characteristics) {
		return StreamTools.fromIterator(it, characteristics, StreamTools.DEFAULT_PARALLEL);
	}

	public static <T> Stream<T> fromIterator(Iterator<T> it, boolean parallel) {
		return StreamTools.fromIterator(it, StreamTools.DEFAULT_CHARACTERISTICS, parallel);
	}

	public static <T> Stream<T> fromIterator(Iterator<T> it, int characteristics, boolean parallel) {
		if (it == null) { return Stream.empty(); }
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, characteristics), parallel);
	}
}