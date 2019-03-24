package com.armedia.commons.utilities;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.KeyGenerator;

class KeyGeneratorTest {
	@Test
	void testGet() throws Exception {
		KeyGenerator gen = new KeyGenerator();

		System.out.printf("=== START ===%n");
		for (int i = 1; i <= 100; i++) {
			System.out.printf("ID=[%s]%n", gen.get());
			if ((i % 10) == 0) {
				System.out.printf("==== PAUSE ====%n");
				Thread.sleep(TimeUnit.SECONDS.toMillis(1));
			}
		}
		System.out.printf("==== END ====%n");
	}
}