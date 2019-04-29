package com.armedia.commons.utilities.line;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourceLineSourceFactoryTest {
	@Test
	public void testNewInstance() throws Exception {
		ResourceLineSourceFactory rlsf = new ResourceLineSourceFactory();

		Assertions.assertNull(rlsf.newInstance(null, null));
		Assertions.assertNull(rlsf.newInstance("", null));
		Assertions.assertNull(rlsf.newInstance("   ", null));

		Assertions.assertNotNull(rlsf.newInstance(ResourceLineSourceFactory.STDIN, null));

		LineSource lines_1 = rlsf.newInstance("classpath:/lines-1.test", null);
		Assertions.assertNotNull(lines_1);
		LineSource lines_2 = rlsf.newInstance("classpath:/lines-2.test", lines_1);
		Assertions.assertNotNull(lines_2);
		Assertions.assertNull(rlsf.newInstance("non-existent.test", lines_1));
		Assertions.assertNull(rlsf.newInstance("non-existent.test", null));
	}
}