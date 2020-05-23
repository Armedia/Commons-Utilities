package com.armedia.commons.utilities;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.slf4j.Logger;

public class CloseUtils {

	private static BiConsumer<String, Object[]> getConsumer(Logger log) {
		return (log != null ? log::error : null);
	}

	public static void closeQuietly(AutoCloseable... closeables) {
		CloseUtils.closeQuietly(CloseUtils.getConsumer(null), closeables);
	}

	public static void closeQuietly(Collection<AutoCloseable> closeables) {
		CloseUtils.closeQuietly(CloseUtils.getConsumer(null), closeables);
	}

	public static void closeQuietly(Logger log, AutoCloseable... closeables) {
		CloseUtils.closeQuietly(CloseUtils.getConsumer(log), closeables);
	}

	public static void closeQuietly(Logger log, Collection<AutoCloseable> closeables) {
		CloseUtils.closeQuietly(CloseUtils.getConsumer(log), closeables);
	}

	public static void closeQuietly(BiConsumer<String, Object[]> log, AutoCloseable... closeables) {
		CloseUtils.closeQuietly(log, (closeables != null ? Arrays.asList(closeables) : null));
	}

	public static void closeQuietly(BiConsumer<String, Object[]> log, Collection<AutoCloseable> closeables) {
		if ((closeables == null) || (closeables.isEmpty())) { return; }
		final Object[] args = (log != null ? new Object[2] : null);
		closeables.stream().filter(Objects::nonNull).forEach((c) -> {
			try {
				c.close();
			} catch (Exception e) {
				if (log != null) {
					args[0] = c.getClass();
					args[1] = e;
					log.accept("Exception caught while closing an AutoCloseable resource of {}", args);
				}
			}
		});
	}
}