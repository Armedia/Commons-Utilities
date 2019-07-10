/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2019 Armedia, LLC
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
