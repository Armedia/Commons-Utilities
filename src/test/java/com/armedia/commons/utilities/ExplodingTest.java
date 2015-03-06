package com.armedia.commons.utilities;

import java.util.UUID;

public class ExplodingTest implements BadServiceTest {

	static final String ERROR_STR = UUID.randomUUID().toString();

	public ExplodingTest() {
		throw new RuntimeException(ExplodingTest.ERROR_STR);
	}
}