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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.armedia.commons.utilities.concurrent.BaseShareableLockable;

public class KeyGenerator extends BaseShareableLockable implements Supplier<String> {

	public static interface KeyFactory extends Supplier<String> {

		public default boolean isCacheable() {
			return true;
		}

	}

	public static enum Factories implements KeyFactory {

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
	public static final KeyFactory DEFAULT_PREFIX = Factories.UUID;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final KeyFactory keyFactory;
	private final Supplier<String> generator;

	private final BlockingQueue<String> cache = new LinkedBlockingQueue<>();
	private int cacheCount = KeyGenerator.DEFAULT_CACHE_COUNT;

	public KeyGenerator() {
		this(null);
	}

	public KeyGenerator(KeyFactory keyFactory) {
		this.keyFactory = Tools.coalesce(keyFactory, KeyGenerator.DEFAULT_PREFIX);
		this.generator = (this.keyFactory.isCacheable() ? this::generateCached : this.keyFactory);
	}

	public KeyFactory getKeyFactory() {
		return this.keyFactory;
	}

	protected String generateCached() {
		return shareLockedUpgradable(this.cache::poll, Objects::isNull, (e) -> {
			this.log.debug("Rendering {} new keys for the empty cache", this.cacheCount);
			for (int i = 0; i <= this.cacheCount; i++) {
				String newKey = this.keyFactory.get();
				if (!this.cache.offer(newKey)) {
					this.log.trace("Cached {} keys of the intended {}", i, this.cacheCount);
					if (i == 0) {
						// NOT EVEN ONE!!! Return the currently-rendered key
						return newKey;
					}
					break;
				}
			}
			return this.cache.poll();
		});
	}

	public int getCacheCount() {
		return shareLocked(() -> this.cacheCount);
	}

	public KeyGenerator setCacheCount(final int cacheCount) {
		return mutexLocked(() -> {
			this.cacheCount = Tools.ensureBetween(KeyGenerator.CACHE_COUNT_MAX, cacheCount,
				KeyGenerator.CACHE_COUNT_MIN);
			return this;
		});
	}

	@Override
	public String get() {
		return this.generator.get();
	}

}