/**
 * *******************************************************************
 *
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS. REPRODUCTION OF ANY PORTION
 * OF THE SOURCE CODE, CONTAINED HEREIN, OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE, IS
 * STRICTLY PROHIBITED.
 *
 * Confidential Property of Armedia LLC. (c) Copyright Armedia LLC 2011-2012. All Rights reserved.
 *
 * *******************************************************************
 */
package com.armedia.commons.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

/**
 * @author drivera@armedia.com
 *
 */
public abstract class ResourceFile {

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
				if (this.log.isDebugEnabled()) {
					this.log.debug(String.format("Resource %s not found after %d attempts", this.description, attempt));
				}
				*/
				this.url = null;
				this.resolved = true;
				return;
			}
			fbext = fbext.replace('/', '_');

			String newName = String.format("%s.%s", this.baseName, fbext);
			/*
			if (this.log.isDebugEnabled()) {
				this.log.debug(String.format("Searching for %s as [%s] (fallback attempt #%d)", this.description,
					newName, attempt));
			}
			*/
			url = this.cl.getResource(newName);
		}

		/*
		if (this.log.isDebugEnabled()) {
			if (attempt == 0) {
				this.log.debug(String.format("Resource %s at: %s", this.description, url.toString()));
			} else {
				this.log.debug(String.format("Resource %s found on fallback attempt %d at: %s", this.description,
					attempt, url.toString()));
			}
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
		if (this.log.isDebugEnabled()) {
			this.log.debug(String.format("Opening an inputstream for resource [%s] from: %s", this.baseName,
				this.url.toString()));
		}
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
		if (this.log.isDebugEnabled()) {
			this.log.debug(String.format("Returning the string contents for resource [%s], encoding %s:%n%s",
				this.baseName, encoding.name(), str));
		}
		*/
		return str;
	}

	public final byte[] getContents() throws IOException {
		try (InputStream in = openInputStream()) {
			byte[] data = IOUtils.toByteArray(in);
			/*
			if (this.log.isDebugEnabled()) {
				this.log.debug(String.format("Loaded %d bytes for resource [%s] from: %s", data.length, this.baseName,
					this.url.toString()));
			}
			*/
			return data;
		}
	}

	public final boolean exists() {
		resolve();
		return (this.url != null);
	}
}