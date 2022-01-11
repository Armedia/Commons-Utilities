/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2022 Armedia, LLC
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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

public class ResourceLoader {

	private static final String[] CLASSPATH_SCHEME_STRINGS = {
		"classpath", "cp", "res", "resource"
	};
	private static final Set<String> CLASSPATH_SCHEMES;

	static {
		Set<String> set = new TreeSet<>();
		for (String s : ResourceLoader.CLASSPATH_SCHEME_STRINGS) {
			if (!StringUtils.isEmpty(s)) {
				set.add(StringUtils.lowerCase(s));
			}
		}
		CLASSPATH_SCHEMES = Tools.freezeSet(new LinkedHashSet<>(set));
	}

	private static boolean isClasspath(String scheme) {
		if (StringUtils.isEmpty(scheme)) { return false; }
		return ResourceLoader.CLASSPATH_SCHEMES.contains(StringUtils.lowerCase(scheme));
	}

	public static boolean isSupported(URI uri) {
		if (uri == null) { return false; }
		if (ResourceLoader.CLASSPATH_SCHEMES.contains(StringUtils.lowerCase(uri.getScheme()))) { return true; }
		try {
			uri.toURL();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static URL getResource(URI uri) throws ResourceLoaderException {
		return ResourceLoader.getResource(null, uri);
	}

	public static URL getResource(ClassLoader cl, URI uri) throws ResourceLoaderException {
		if (uri == null) { return null; }

		URI source = uri;
		if (!uri.isAbsolute()) {
			source = uri.normalize();
		}

		// First things first: is it a URL?
		try {
			return source.toURL();
		} catch (IllegalArgumentException e) {
			throw new ResourceLoaderException(String.format("The given URI [%s] is not absolute", uri), e);
		} catch (MalformedURLException e) {
			// Not supported.... maybe a classpath?
			if (!ResourceLoader.isClasspath(uri.getScheme())) {
				throw new ResourceLoaderException(
					String.format("The URI [%s] is not supported as a resource URI", uri));
			}
			// It's a classpath!! Resolve it...
		}

		String resource = source.getSchemeSpecificPart();
		if (StringUtils.isBlank(resource)) {
			throw new ResourceLoaderException(
				String.format("The URI [%s] is not a valid resource URI (no scheme-specific part!)", uri));
		}

		// Eliminate all leading slashes
		resource = resource.replaceAll("^/+", "");
		if (cl == null) {
			cl = Thread.currentThread().getContextClassLoader();
		}
		return cl.getResource(resource);
	}

	public static URL getResource(String uriStr) throws ResourceLoaderException {
		return ResourceLoader.getResource(null, uriStr);
	}

	public static URL getResource(ClassLoader cl, String uriStr) throws ResourceLoaderException {
		if (StringUtils.isEmpty(uriStr)) { return null; }
		try {
			return ResourceLoader.getResource(cl, new URI(uriStr));
		} catch (URISyntaxException e) {
			throw new ResourceLoaderException(String.format("The given URI [%s] is not in valid syntax", uriStr), e);
		}
	}

	public static InputStream getResourceAsStream(URI uri) throws ResourceLoaderException, IOException {
		return ResourceLoader.getResourceAsStream(null, uri);
	}

	public static InputStream getResourceAsStream(ClassLoader cl, URI uri) throws ResourceLoaderException, IOException {
		return ResourceLoader.getResource(cl, uri).openStream();
	}

	public static URL getResourceOrFile(String uriOrPath) throws ResourceLoaderException {
		return ResourceLoader.getResourceOrFile(null, uriOrPath, null);
	}

	public static URL getResourceOrFile(ClassLoader cl, String uriOrPath) throws ResourceLoaderException {
		return ResourceLoader.getResourceOrFile(cl, uriOrPath, null);
	}

	public static URL getResourceOrFile(String uriOrPath, String relativeTo) throws ResourceLoaderException {
		return ResourceLoader.getResourceOrFile(null, uriOrPath, relativeTo);
	}

	public static URL getResourceOrFile(ClassLoader cl, String uriOrPath, String relativeTo)
		throws ResourceLoaderException {
		// First: is it a file? If it's a file, let's use that first
		try {
			Path b = null;
			if (!StringUtils.isEmpty(relativeTo)) {
				b = Paths.get(relativeTo);
			}
			Path p = null;
			if (b != null) {
				p = b.resolve(uriOrPath);
			} else {
				p = Paths.get(uriOrPath);
			}

			try {
				p = p.toRealPath();
			} catch (IOException e) {
				throw new ResourceLoaderException(String.format("Failed to convert the path [%s] to its real path", p),
					e);
			}
			if (Files.isRegularFile(p)) { return p.toUri().toURL(); }
		} catch (Exception e) {
			// If there was an exception, then it's not a regular file...
		}

		URI baseUri = null;
		if (!StringUtils.isEmpty(relativeTo)) {
			try {
				baseUri = new URI(relativeTo).normalize();
			} catch (URISyntaxException e) {
				// Relative path is worthless as a URI...skip its use
				baseUri = null;
			}
		}

		URI sourceUri = null;
		try {
			if (baseUri != null) {
				try {
					sourceUri = baseUri.resolve(uriOrPath);
				} catch (IllegalArgumentException e) {
					throw new ResourceLoaderException(
						String.format("Can't build a URI for [%s] relative to [%s]", uriOrPath, baseUri), e);
				}
			} else {
				sourceUri = new URI(uriOrPath);
			}

			URL resource = ResourceLoader.getResource(cl, sourceUri);
			if (resource == null) { return null; }
			if (StringUtils.equals("file", resource.getProtocol())) {
				// Local file... treat it as such...
				return new File(resource.getPath()).toURI().toURL();
			}
			// Not a local file, use the URI
			return resource;
		} catch (Exception e) {
			// Not a URI either...
			throw new ResourceLoaderException(
				String.format("The string [%s] is neither a valid path nor a valid URI", uriOrPath), e);
		}
	}

	public static InputStream getResourceOrFileAsStream(String uriOrPath) throws ResourceLoaderException, IOException {
		return ResourceLoader.getResourceOrFileAsStream(null, uriOrPath, null);
	}

	public static InputStream getResourceOrFileAsStream(ClassLoader cl, String uriOrPath)
		throws ResourceLoaderException, IOException {
		return ResourceLoader.getResourceOrFileAsStream(cl, uriOrPath, null);
	}

	public static InputStream getResourceOrFileAsStream(String uriOrPath, String relativeTo)
		throws ResourceLoaderException, IOException {
		return ResourceLoader.getResourceOrFileAsStream(null, uriOrPath, relativeTo);
	}

	public static InputStream getResourceOrFileAsStream(ClassLoader cl, String uriOrPath, String relativeTo)
		throws ResourceLoaderException, IOException {
		URL url = ResourceLoader.getResourceOrFile(cl, uriOrPath, relativeTo);
		return (url != null ? url.openStream() : null);
	}
}
