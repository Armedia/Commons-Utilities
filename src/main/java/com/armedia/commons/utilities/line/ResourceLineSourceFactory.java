/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2020 Armedia, LLC
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
package com.armedia.commons.utilities.line;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.armedia.commons.utilities.ResourceLoader;

public class ResourceLineSourceFactory implements LineSourceFactory {

	public static final String STDIN = "@-";
	private static final String STDIN_ID = "<STDIN>";

	protected String calculateId(URL url) {
		Objects.requireNonNull(url, "Must provide a URL to calcuate the ID for");
		try {
			return url.toURI().normalize().toString();
		} catch (URISyntaxException e) {
			// If this isn't valid as a URI, then we simply fall back and just use the URL
			// itself
			return url.toString();
		}
	}

	protected URL getResourceUrl(String resource, String relative) throws Exception {
		return ResourceLoader.getResourceOrFile(resource, relative);
	}

	protected LineSource processException(String resource, String relative, Throwable t) throws LineSourceException {
		if (FileNotFoundException.class.isInstance(t)) { return null; }
		throw new LineSourceException(
			String.format("Couldn't read the resource at [%s] (relative to [%s]", resource, relative), t);
	}

	@Override
	public LineSource newInstance(String resource, LineSource relativeTo) throws LineSourceException {
		if (StringUtils.isBlank(resource)) { return null; }

		Charset charset = null;

		String id = null;
		boolean close = true;
		InputStream in = null;
		if (StringUtils.equalsIgnoreCase(ResourceLineSourceFactory.STDIN, resource)) {
			// Read from Standard in, but don't close
			id = ResourceLineSourceFactory.STDIN_ID;
			in = System.in;
			close = false;
		} else {
			String relative = (relativeTo != null ? relativeTo.getId() : null);
			URL url = null;
			try {
				url = getResourceUrl(resource, relative);
				if (url == null) { return null; }
			} catch (Exception e) {
				return null;
			}

			try {
				in = url.openStream();
				id = calculateId(url);
			} catch (Exception e) {
				return processException(resource, relative, e);
			}
		}
		return new InputStreamLineSource(id, in, charset, close);
	}

}
