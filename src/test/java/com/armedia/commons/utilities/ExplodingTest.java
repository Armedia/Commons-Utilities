package com.armedia.commons.utilities;

public class ExplodingTest implements BadServiceTest {

	public ExplodingTest() {
		throw new RuntimeException("KABOOM!");
	}
}