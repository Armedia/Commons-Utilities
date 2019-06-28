/*******************************************************************************
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (c) 2010 - 2019 Armedia LLC
 * %%
 * This file is part of the Caliente software.
 *
 * If the software was purchased under a paid Caliente license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Caliente is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Caliente is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Caliente. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 *******************************************************************************/
package com.armedia.commons.utilities;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import com.armedia.commons.utilities.concurrent.BaseShareableLockable;
import com.armedia.commons.utilities.concurrent.MutexAutoLock;

public class KeyGenerator extends BaseShareableLockable implements Supplier<String> {

	public static interface KeyFactory extends Supplier<String> {

		public default boolean isCacheable() {
			return true;
		}

	}

	public static enum Factory implements KeyFactory {

		//
		TIME {
			private final String format = "%s#%016X";
			private final AtomicLong counter = new AtomicLong(0);
			private final ZoneId timeZone = ZoneOffset.UTC;
			private final String pattern = "yyyyMMddHHmmss";
			private final DateTimeFormatter formatter = DateTimeFormatter //
				.ofPattern(this.pattern) //
				.withZone(this.timeZone) //
			;

			@Override
			public String get() {
				return String.format(this.format, this.formatter.format(Instant.now()), this.counter.getAndIncrement());
			}

			@Override
			public boolean isCacheable() {
				return false;
			}
		}, //

		UUID {
			@Override
			public String get() {
				return java.util.UUID.randomUUID().toString();
			}
		}, //
			//
		;
	}

	public static final int CACHE_COUNT_MIN = 100;
	public static final int CACHE_COUNT_MAX = 100000;
	public static final int DEFAULT_CACHE_COUNT = 1000;
	public static final KeyFactory DEFAULT_FACTORY = Factory.UUID;

	private final KeyFactory keyFactory;
	private final Supplier<String> generator;

	private final BlockingQueue<String> cache = new LinkedBlockingQueue<>();
	private int cacheCount = KeyGenerator.DEFAULT_CACHE_COUNT;

	public KeyGenerator() {
		this(null);
	}

	public KeyGenerator(KeyFactory keyFactory) {
		this.keyFactory = Tools.coalesce(keyFactory, KeyGenerator.DEFAULT_FACTORY);
		this.generator = (this.keyFactory.isCacheable() ? this::generateCached : this.keyFactory);
	}

	public KeyFactory getKeyFactory() {
		return this.keyFactory;
	}

	protected String generateCached() {
		return shareLockedUpgradable(this.cache::poll, Objects::isNull, (e) -> {
			for (int i = 0; i <= this.cacheCount; i++) {
				this.cache.offer(this.keyFactory.get());
			}
			return this.cache.poll();
		});
	}

	public int getCacheCount() {
		return shareLocked(() -> this.cacheCount);
	}

	public KeyGenerator setCacheCount(final int cacheCount) {
		try (MutexAutoLock lock = autoMutexLock()) {
			this.cacheCount = Tools.ensureBetween(KeyGenerator.CACHE_COUNT_MIN, cacheCount,
				KeyGenerator.CACHE_COUNT_MAX);
			return this;
		}
	}

	@Override
	public String get() {
		return this.generator.get();
	}

}