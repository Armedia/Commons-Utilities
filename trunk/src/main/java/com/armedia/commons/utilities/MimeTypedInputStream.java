/**
 * *******************************************************************
 * 
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS. REPRODUCTION OF ANY PORTION
 * OF THE SOURCE CODE, CONTAINED HEREIN, OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE, IS
 * STRICTLY PROHIBITED.
 * 
 * Confidential Property of Armedia LLC. (c) Copyright Armedia LLC 2011-2011. All Rights reserved.
 * 
 * *******************************************************************
 */
package com.armedia.commons.utilities;

import java.io.IOException;
import java.io.InputStream;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

/**
 * @author drivera@armedia.com
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
			throw new RuntimeException(String.format("Mime type [%s] was not parsed properly",
				MimeTypedInputStream.DEFAULT_MIME_STRING), e);
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