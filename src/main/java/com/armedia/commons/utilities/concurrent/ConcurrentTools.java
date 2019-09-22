package com.armedia.commons.utilities.concurrent;

import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;

import com.armedia.commons.utilities.function.CheckedFunction;
import com.armedia.commons.utilities.function.CheckedTools;

public class ConcurrentTools {

	/**
	 * <p>
	 * Added to provide a simpler, checked version of
	 * {@link ConcurrentUtils#createIfAbsent(ConcurrentMap, Object, org.apache.commons.lang3.concurrent.ConcurrentInitializer)}
	 * which will simplify exception handling
	 * </p>
	 */
	public static <K, V, E extends Throwable> V createIfAbsent(final ConcurrentMap<K, V> map, final K key,
		final CheckedFunction<K, V, E> initializer) throws E {
		if ((map == null) || (initializer == null)) { return null; }

		final V value = map.get(key);
		if (value == null) { return ConcurrentUtils.putIfAbsent(map, key, initializer.apply(key)); }
		return value;
	}

	/**
	 * <p>
	 * Added to provide a simpler version of
	 * {@link ConcurrentUtils#createIfAbsent(ConcurrentMap, Object, org.apache.commons.lang3.concurrent.ConcurrentInitializer)}
	 * which will simplify exception handling by not requiring it where not needed
	 * </p>
	 */
	public static <K, V> V createIfAbsent(final ConcurrentMap<K, V> map, final K key,
		final Function<K, V> initializer) {
		return ConcurrentTools.createIfAbsent(map, key, CheckedTools.check(initializer));
	}
}