package com.armedia.commons.utilities;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamTools {

	public static <T> Stream<T> streamFromIterator(Iterator<T> it) {
		return StreamTools.streamFromIterator(it, Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.ORDERED);
	}

	public static <T> Stream<T> streamFromIterator(Iterator<T> it, int flags) {
		return StreamTools.streamFromIterator(it, flags, false);
	}

	public static <T> Stream<T> streamFromIterator(Iterator<T> it, int flags, boolean parallel) {
		if (it == null) { return Stream.empty(); }
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, flags), parallel);
	}
}