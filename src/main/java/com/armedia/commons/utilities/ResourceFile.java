/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2019 Armedia, LLC
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

/**
 *
 *
 */
public class ResourceFile {

	// private final Logger log = Logger.getLogger(ResourceFile.class);

	private final ClassLoader cl;
	@SuppressWarnings("unused")
	private final String description;
	private final String baseName;
	private boolean resolved = false;
	private URL url = null;

	public ResourceFile(String baseName) {
		this(baseName, null);
	}

	public ResourceFile(String baseName, String description) {
		this.cl = Thread.currentThread().getContextClassLoader();
		this.baseName = Tools.toString(baseName, true);
		if (this.baseName == null) { throw new IllegalArgumentException("Empty names are not allowed"); }
		description = Tools.toTrimmedString(description, true);
		if (description == null) {
			description = String.format("resource [%s]", baseName);
		}
		this.description = description;
	}

	protected final synchronized void resolve() {
		if (this.resolved) { return; }
		URL url = this.cl.getResource(this.baseName);
		int attempt = 0;
		while (url == null) {
			String fbext = getFallbackExtension(++attempt);
			if (fbext == null) {
				/*
				this.log.debug("Resource {} not found after {} attempts", this.description, attempt);
				*/
				this.url = null;
				this.resolved = true;
				return;
			}
			fbext = fbext.replace('/', '_');

			String newName = String.format("%s.%s", this.baseName, fbext);
			/*
			this.log.debug("Searching for {} as [{}] (fallback attempt #{})", this.description, newName, attempt);
			*/
			url = this.cl.getResource(newName);
		}

		/*
		if (attempt == 0) {
			this.log.debug("Resource {} at: {}", this.description, url);
		} else {
			this.log.debug("Resource {} found on fallback attempt {} at: {}", this.description, attempt, url);
		}
		*/
		this.url = url;
		this.resolved = true;
	}

	/**
	 * Returns the resource to attempt for the given fallback attempt, or {@code null} if no further
	 * attempts should be undertaken. The first attempt is always for the base name given with the
	 * constructor, while all subsequent attempts can change the resource name completely.
	 *
	 * @param fallbackAttempt
	 * @return the new name for the resource that should be attempted
	 */
	protected String getFallbackExtension(int fallbackAttempt) {
		return String.format("%d", fallbackAttempt);
	}

	public final InputStream openInputStream() throws IOException {
		if (!exists()) { throw new IOException(String.format("Resource [%s] doesn't exist", this.baseName)); }
		/*
		this.log.debug("Opening an inputstream for resource [{}] from: {}", this.baseName, this.url);
		*/
		return this.url.openStream();
	}

	public final String getString() throws IOException {
		return getString((Charset) null);
	}

	public final String getString(String encoding) throws IOException {
		if (encoding == null) {
			encoding = Charset.defaultCharset().name();
		}
		return getString(Charset.forName(encoding));
	}

	public final String getString(Charset encoding) throws IOException {
		if (encoding == null) {
			encoding = Charset.defaultCharset();
		}
		String str = new String(getContents(), encoding);
		/*
		this.log.debug("Returning the string contents for resource [{}], encoding {}:{}{}", this.baseName,
			encoding.name(), Tools.NL, str);
		*/
		return str;
	}

	public final byte[] getContents() throws IOException {
		try (InputStream in = openInputStream()) {
			byte[] data = IOUtils.toByteArray(in);
			/*
			this.log.debug("Loaded {} bytes for resource [{}] from: {}", data.length, this.baseName, this.url);
			*/
			return data;
		}
	}

	public final boolean exists() {
		resolve();
		return (this.url != null);
	}
}
