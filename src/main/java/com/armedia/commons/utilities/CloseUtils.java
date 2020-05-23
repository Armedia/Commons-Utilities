package com.armedia.commons.utilities;

import java.util.function.BiConsumer;

import org.slf4j.Logger;

public class CloseUtils {
	private static final BiConsumer<String, Object[]> NULL = null;

	public static void closeQuietly(AutoCloseable... closeables) {
		CloseUtils.closeQuietly(CloseUtils.NULL, closeables);
	}

	public static void closeQuietly(Logger log, AutoCloseable... closeables) {
		BiConsumer<String, Object[]> l = (log != null ? log::error : null);
		CloseUtils.closeQuietly(l, closeables);
	}

	public static void closeQuietly(BiConsumer<String, Object[]> log, AutoCloseable... closeables) {
		if ((closeables == null) || (closeables.length == 0)) { return; }
		final Object[] args = (log != null ? new Object[2] : null);
		for (AutoCloseable c : closeables) {
			if (c != null) {
				try {
					c.close();
				} catch (Exception e) {
					if (log != null) {
						args[0] = c.getClass();
						args[1] = e;
						log.accept("Exception caught while closing an AutoCloseable resource of {}", args);
					}
				}
			}
		}
	}
}