package com.armedia.commons.utilities;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamTools {

	public static final int DEFAULT_STREAM_FLAGS = Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.ORDERED;
	public static final boolean DEFAULT_PARALLEL = false;

	public static <T> Stream<T> streamFromIterator(Iterator<T> it) {
		return StreamTools.streamFromIterator(it, StreamTools.DEFAULT_STREAM_FLAGS, StreamTools.DEFAULT_PARALLEL);
	}

	public static <T> Stream<T> streamFromIterator(Iterator<T> it, int flags) {
		return StreamTools.streamFromIterator(it, flags, StreamTools.DEFAULT_PARALLEL);
	}

	public static <T> Stream<T> streamFromIterator(Iterator<T> it, boolean parallel) {
		return StreamTools.streamFromIterator(it, StreamTools.DEFAULT_STREAM_FLAGS, parallel);
	}

	public static <T> Stream<T> streamFromIterator(Iterator<T> it, int flags, boolean parallel) {
		if (it == null) { return Stream.empty(); }
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, flags), parallel);
	}
}