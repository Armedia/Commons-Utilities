package com.armedia.commons.utilities;

import java.util.UUID;

public class ClassNotFoundTest implements BadServiceTest {
	public ClassNotFoundTest() throws Throwable {
		Class.forName(UUID.randomUUID().toString());
	}
}