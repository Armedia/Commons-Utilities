package com.armedia.commons.utilities.line;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourceLineSourceFactoryTest {

	@Test
	public void testCalculateId() throws Exception {
		ResourceLineSourceFactory rlsf = new ResourceLineSourceFactory();

		Assertions.assertThrows(NullPointerException.class, () -> rlsf.calculateId(null));
		String id = null;
		URL url = null;

		id = "http://this.is.a/normal/url";
		url = new URL(id);
		Assertions.assertEquals(id, rlsf.calculateId(url));

		id = "http://this.is.a/normal/url- - /\\/@~$%";
		url = new URL(id);
		Assertions.assertEquals(id, rlsf.calculateId(url));
	}

	@Test
	public void testProcessException() throws LineSourceException {
		ResourceLineSourceFactory rlsf = new ResourceLineSourceFactory();
		Assertions.assertNull(rlsf.processException("some-source", "some-value", new FileNotFoundException()));
		Throwable cause = new Throwable();
		try {
			rlsf.processException("some-source", "some-value", cause);
		} catch (LineSourceException e) {
			Assertions.assertSame(cause, e.getCause());
		}
	}

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

		rlsf = new ResourceLineSourceFactory() {
			@Override
			protected URL getResourceUrl(String resource, String relative) throws Exception {
				throw new MalformedURLException();
			}
		};
		Assertions.assertNull(rlsf.newInstance("non-existent.test", lines_1));
	}
}