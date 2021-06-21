/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2021 Armedia, LLC
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourceLoaderTest {

	@Test
	public void testIsSupported() {
		Collection<Pair<String, Boolean>> urlData = new ArrayList<>();
		urlData.add(Pair.of("http://www.google.com", null));
		urlData.add(Pair.of("https://www.google.com", null));
		urlData.add(Pair.of("file:///www.google.com", null));
		urlData.add(Pair.of("classpath://www.google.com", true));
		urlData.add(Pair.of("cp://www.google.com", true));
		urlData.add(Pair.of("resource://www.google.com", true));
		urlData.add(Pair.of("res://www.google.com", true));
		urlData.add(Pair.of("ssh://www.google.com", null));
		urlData.add(Pair.of("jdbc:some:crap", null));
		urlData.add(Pair.of("some-weird-uri-syntax", null));

		urlData.forEach((p) -> {
			final String str = p.getLeft();
			Boolean expect = p.getRight();

			final URI uri;
			try {
				uri = new URI(str);
			} catch (URISyntaxException e) {
				// If we can't build a URI, we can't test the string...
				return;
			}

			if (expect == null) {
				try {
					uri.toURL();
					expect = true;
				} catch (Exception e) {
					expect = false;
				}
			}
			Assertions.assertEquals(expect, ResourceLoader.isSupported(uri));
		});
	}

	@Test
	public void testGetResourceURI() {
		Collection<Pair<String, Object>> urlData = new ArrayList<>();
		urlData.add(Pair.of("http://www.google.com", -1L));
		urlData.add(Pair.of("https://www.google.com", -1L));

		try {
			Assertions.assertNull(ResourceLoader.getResource((URI) null));
		} catch (Throwable t) {
			Assertions.fail("Failed to accept a null URI");
		}

		try {
			File f = File.createTempFile("testfile", "test");
			f.deleteOnExit();
			// Fill it with random data
			Random r = new Random(System.currentTimeMillis());
			byte[] data = new byte[r.nextInt(1024) + 1024];
			r.nextBytes(data);
			FileUtils.writeByteArrayToFile(f, data);
			urlData.add(Pair.of(f.toURI().toURL().toString(), f.length()));
		} catch (Exception e) {
			// Can't test this...
		}

		String[] cp = {
			"classpath", "cp", "resource", "res"
		};
		List<Pair<String, Long>> sums = new ArrayList<>();
		sums.add(Pair.of("6cc185511bf9ef6e765c1fbeace1ad443e6b1e62abd04370fc116eb1a1c7897b", 65536L));
		sums.add(Pair.of("15783f3468c564cf3bc35cc563107144e939bd01862c7aacbe85e2f155b3f625", 65536L));
		sums.add(Pair.of("302d19781b0f03bfcad9399ef6e97d3c24d2a16b0ad3fed400971483893b3c8e", 65536L));
		sums.add(Pair.of("e7b584d724d308a33f4c7a55eb4fa143358e3d8e32c95b211bea09549e6b86bf", 65536L));
		sums.add(Pair.of("11185d2bb878c66f18b52d67f67da46225b07f66de4fd8124e07c9cbfd931111", 65536L));
		Map<String, String> verifier = new HashMap<>();
		for (int i = 0; i <= (sums.size() + 1); i++) {
			for (int p = 0; p < cp.length; p++) {
				String url = String.format("%s:/resource-%d.dat", cp[p], i);
				if (i < sums.size()) {
					Pair<String, Long> checksum = sums.get(i);
					verifier.put(url, checksum.getLeft());
					urlData.add(Pair.of(url, checksum.getRight()));
				} else {
					urlData.add(Pair.of(url, null));
				}
			}
		}

		urlData.add(Pair.of("ssh://www.google.com", Throwable.class));
		urlData.add(Pair.of("jdbc:some:crap", Throwable.class));
		urlData.add(Pair.of("some-weird-uri-syntax", Throwable.class));
		urlData.add(Pair.of("!This is an illegal URI", Throwable.class));

		urlData.forEach((p) -> {
			final String str = p.getLeft();
			Object result = p.getRight();

			final URI uri;
			try {
				uri = new URI(str);
			} catch (URISyntaxException e) {
				// If we can't build a URI, we can't test the string...
				return;
			}

			URL url = null;
			Throwable raised = null;
			try {
				url = ResourceLoader.getResource(uri);
			} catch (Throwable t) {
				raised = t;
			}

			if (result == null) {
				// Supported, but should be missing
				Assertions.assertNull(url);
				Assertions.assertNull(raised);
				return;
			}

			if (Number.class.isInstance(result)) {
				// Supported, but may be unreachable (i.e. http:// or https:// on standalone
				// networkless systems)
				Number n = Number.class.cast(result);
				Assertions.assertNotNull(url, str);
				Assertions.assertNull(raised);

				byte[] data = null;
				String actualSum = null;

				try (InputStream in = url.openStream()) {
					data = IOUtils.toByteArray(in);
					actualSum = StringUtils.lowerCase(DigestUtils.sha256Hex(data));
				} catch (IOException e) {
					if (n.longValue() >= 0) {
						Assertions.fail(String.format("Failed to read from the URL [%s]: %s", str, e.getMessage()));
						return;
					}
				}

				// Only check the size if we expect to find it and know the size beforehand
				// a negative value means we expect to find it, but won't know the size beforehand
				if (n.longValue() >= 0) {
					Assertions.assertEquals(n.longValue(), data.length);
				}

				String expectedSum = StringUtils.lowerCase(verifier.get(str));
				if (expectedSum != null) {
					Assertions.assertEquals(expectedSum, actualSum);
				}
				return;
			}

			if (Throwable.class == result) {
				// Not supported, should have raised an exception
				Assertions.assertNotNull(raised,
					String.format("Did not raise an exception for known-bad URL [%s]", str));
			}

		});
	}

	@Test
	public void testGetResourceString() {
		Collection<Pair<String, Object>> urlData = new ArrayList<>();
		urlData.add(Pair.of("http://www.google.com", -1L));
		urlData.add(Pair.of("https://www.google.com", -1L));

		try {
			Assertions.assertNull(ResourceLoader.getResource((URI) null));
		} catch (Throwable t) {
			Assertions.fail("Failed to accept a null URI");
		}

		try {
			File f = File.createTempFile("testfile", "test");
			f.deleteOnExit();
			// Fill it with random data
			Random r = new Random(System.currentTimeMillis());
			byte[] data = new byte[r.nextInt(1024) + 1024];
			r.nextBytes(data);
			FileUtils.writeByteArrayToFile(f, data);
			urlData.add(Pair.of(f.toURI().toURL().toString(), f.length()));
		} catch (Exception e) {
			// Can't test this...
		}

		String[] cp = {
			"classpath", "cp", "resource", "res"
		};
		List<Pair<String, Long>> sums = new ArrayList<>();
		sums.add(Pair.of("6cc185511bf9ef6e765c1fbeace1ad443e6b1e62abd04370fc116eb1a1c7897b", 65536L));
		sums.add(Pair.of("15783f3468c564cf3bc35cc563107144e939bd01862c7aacbe85e2f155b3f625", 65536L));
		sums.add(Pair.of("302d19781b0f03bfcad9399ef6e97d3c24d2a16b0ad3fed400971483893b3c8e", 65536L));
		sums.add(Pair.of("e7b584d724d308a33f4c7a55eb4fa143358e3d8e32c95b211bea09549e6b86bf", 65536L));
		sums.add(Pair.of("11185d2bb878c66f18b52d67f67da46225b07f66de4fd8124e07c9cbfd931111", 65536L));
		Map<String, String> verifier = new HashMap<>();
		for (int i = 0; i <= (sums.size() + 1); i++) {
			for (int p = 0; p < cp.length; p++) {
				String url = String.format("%s:/resource-%d.dat", cp[p], i);
				if (i < sums.size()) {
					Pair<String, Long> checksum = sums.get(i);
					verifier.put(url, checksum.getLeft());
					urlData.add(Pair.of(url, checksum.getRight()));
				} else {
					urlData.add(Pair.of(url, null));
				}
			}
		}

		urlData.add(Pair.of("ssh://www.google.com", Throwable.class));
		urlData.add(Pair.of("jdbc:some:crap", Throwable.class));
		urlData.add(Pair.of("some-weird-uri-syntax", Throwable.class));
		urlData.add(Pair.of("!This is an illegal URI", Throwable.class));

		urlData.forEach((p) -> {
			final String uri = p.getLeft();
			Object result = p.getRight();

			try {
				new URI(uri);
			} catch (URISyntaxException e) {
				// If we can't build a URI, we shold expect the test to fail
				result = Throwable.class;
			}

			URL url = null;
			Throwable raised = null;
			try {
				url = ResourceLoader.getResource(uri);
			} catch (Throwable t) {
				raised = t;
			}

			if (result == null) {
				// Supported, but should be missing
				Assertions.assertNull(url);
				Assertions.assertNull(raised);
				return;
			}

			if (Number.class.isInstance(result)) {
				// Supported, but may be unreachable (i.e. http:// or https:// on standalone
				// networkless systems)
				Number n = Number.class.cast(result);
				Assertions.assertNotNull(url, uri);
				Assertions.assertNull(raised);

				byte[] data = null;
				String actualSum = null;

				try (InputStream in = url.openStream()) {
					data = IOUtils.toByteArray(in);
					actualSum = StringUtils.lowerCase(DigestUtils.sha256Hex(data));
				} catch (IOException e) {
					if (n.longValue() >= 0) {
						Assertions.fail(String.format("Failed to read from the URL [%s]: %s", uri, e.getMessage()));
						return;
					}
				}

				// Only check the size if we expect to find it and know the size beforehand
				// a negative value means we expect to find it, but won't know the size beforehand
				if (n.longValue() >= 0) {
					Assertions.assertEquals(n.longValue(), data.length);
				}

				String expectedSum = StringUtils.lowerCase(verifier.get(uri));
				if (expectedSum != null) {
					Assertions.assertEquals(expectedSum, actualSum);
				}
				return;
			}

			if (Throwable.class == result) {
				// Not supported, should have raised an exception
				Assertions.assertNotNull(raised,
					String.format("Did not raise an exception for known-bad URL [%s]", uri));
			}

		});
	}

}
