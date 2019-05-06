package com.armedia.commons.utilities;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.KeyGenerator.Factory;

public class KeyGeneratorTest {
	@Test
	public void testGet() throws Exception {
		Set<String> keys = new LinkedHashSet<>();
		for (Factory f : Factory.values()) {
			KeyGenerator gen = new KeyGenerator(f);
			Assertions.assertSame(f, gen.getKeyFactory());
			Assertions.assertEquals(KeyGenerator.DEFAULT_CACHE_COUNT, gen.getCacheCount());
			System.out.printf("=== START ===%n");
			int max = 100;
			for (int i = 1; i <= max; i++) {
				String key = gen.get();
				Assertions.assertTrue(keys.add(key), String.format("Key [%s] is a duplicate", key));
				System.out.printf("ID=[%s]%n", key);
				if ((i % 10) == 0) {
					System.out.printf("==== PAUSE ====%n");
					if (i < max) {
						Thread.sleep(TimeUnit.MILLISECONDS.toMillis(100));
					}
				}
			}
			System.out.printf("==== END ====%n");
		}
	}

	@Test
	public void testDefaultFactory() throws Exception {
		Set<String> keys = new LinkedHashSet<>();
		KeyGenerator gen = new KeyGenerator();
		Assertions.assertSame(KeyGenerator.DEFAULT_FACTORY, gen.getKeyFactory());
		Assertions.assertEquals(KeyGenerator.DEFAULT_CACHE_COUNT, gen.getCacheCount());
		System.out.printf("=== START ===%n");
		int max = 100;
		for (int i = 1; i <= max; i++) {
			String key = gen.get();
			Assertions.assertTrue(keys.add(key), String.format("Key [%s] is a duplicate", key));
			System.out.printf("ID=[%s]%n", key);
			if ((i % 10) == 0) {
				System.out.printf("==== PAUSE ====%n");
				if (i < max) {
					Thread.sleep(TimeUnit.MILLISECONDS.toMillis(100));
				}
			}
		}
		System.out.printf("==== END ====%n");
	}

	@Test
	public void testSetCacheCount() throws Exception {
		KeyGenerator gen = new KeyGenerator();
		Assertions.assertEquals(KeyGenerator.DEFAULT_CACHE_COUNT, gen.getCacheCount());
		for (int i = 0; i < 1000000; i++) {
			gen.setCacheCount(i);
			if (i < KeyGenerator.CACHE_COUNT_MIN) {
				Assertions.assertEquals(KeyGenerator.CACHE_COUNT_MIN, gen.getCacheCount());
			} else if (i > KeyGenerator.CACHE_COUNT_MAX) {
				Assertions.assertEquals(KeyGenerator.CACHE_COUNT_MAX, gen.getCacheCount());
			} else {
				Assertions.assertEquals(i, gen.getCacheCount());
			}
		}
	}
}