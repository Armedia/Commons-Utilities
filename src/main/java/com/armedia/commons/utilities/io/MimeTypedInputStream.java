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
package com.armedia.commons.utilities.io;

import java.io.IOException;
import java.io.InputStream;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

/**
 *
 *
 */
public class MimeTypedInputStream extends InputStream {

	public static final String DEFAULT_MIME_STRING = "application/octet-stream";

	public static final MimeType DEFAULT_MIME_TYPE;
	public static final MimeType UNKNOWN;

	static {
		try {
			DEFAULT_MIME_TYPE = new MimeType(MimeTypedInputStream.DEFAULT_MIME_STRING);
		} catch (MimeTypeParseException e) {
			throw new RuntimeException(
				String.format("Mime type [%s] was not parsed properly", MimeTypedInputStream.DEFAULT_MIME_STRING), e);
		}
		UNKNOWN = MimeTypedInputStream.DEFAULT_MIME_TYPE;
	}

	private final MimeType contentType;
	private final InputStream data;

	public MimeTypedInputStream(InputStream data) {
		this(data, null);
	}

	public MimeTypedInputStream(InputStream data, MimeType contentType) {
		this.contentType = (contentType != null ? contentType : MimeTypedInputStream.DEFAULT_MIME_TYPE);
		this.data = data;
	}

	/**
	 * @return the contentType
	 */
	public MimeType getContentType() {
		return this.contentType;
	}

	@Override
	public int read() throws IOException {
		return this.data.read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return this.data.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return this.data.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		return this.data.skip(n);
	}

	@Override
	public int available() throws IOException {
		return this.data.available();
	}

	@Override
	public void close() throws IOException {
		this.data.close();
	}

	@Override
	public void mark(int readlimit) {
		this.data.mark(readlimit);
	}

	@Override
	public void reset() throws IOException {
		this.data.reset();
	}

	@Override
	public boolean markSupported() {
		return this.data.markSupported();
	}
}
