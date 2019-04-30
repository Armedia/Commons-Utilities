package com.armedia.commons.utilities;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class KeyGeneratorTest {
	@Test
	public void testGet() throws Exception {
		KeyGenerator gen = new KeyGenerator();

		System.out.printf("=== START ===%n");
		int max = 100;
		for (int i = 1; i <= max; i++) {
			System.out.printf("ID=[%s]%n", gen.get());
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