package com.armedia.commons.utilities.line;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;

import com.armedia.commons.utilities.ResourceLoader;
import com.armedia.commons.utilities.ResourceLoaderException;

public class ResourceLineSourceFactory implements LineSourceFactory {

	private static final String STDIN = "@-";
	private static final String STDIN_ID = "<STDIN>";

	@Override
	public LineSource newInstance(String resource, LineSource relativeTo) throws LineSourceException {
		if (StringUtils.isBlank(resource)) { return null; }
		resource = StringUtils.strip(resource);

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
			try {
				URL url = ResourceLoader.getResourceOrFile(resource, relative);
				if (url == null) { return null; }
				in = url.openStream();
				try {
					id = url.toURI().normalize().toString();
				} catch (URISyntaxException e) {
					// If this isn't valid as a URI, then we simply fall back and just use the URL
					// itself
					id = url.toString();
				}
			} catch (FileNotFoundException e) {
				return null;
			} catch (IOException e) {
				throw new LineSourceException(String.format("Couldn't read the resource at [%s]", resource), e);
			} catch (ResourceLoaderException e) {
				// Not a valid resource URI, so skip it
				return null;
			}
		}
		return new InputStreamLineSource(id, in, charset, close);
	}

}