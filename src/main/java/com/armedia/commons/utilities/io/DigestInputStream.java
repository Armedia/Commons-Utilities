/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2025 Armedia, LLC
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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;

import com.armedia.commons.utilities.DigestHashCollector;

public class DigestInputStream extends FilterInputStream implements DigestHashCollector {

	private final MessageDigest digest;
	private final byte[] byteBuf = new byte[1];
	private long length = 0;

	public DigestInputStream(InputStream in, String digest) throws NoSuchAlgorithmException {
		this( //
			Objects.requireNonNull(in, "Must provide a non-null InputStream to wrap around"), //
			MessageDigest.getInstance( //
				Objects.requireNonNull(digest, "Must provide a non-null digest name") //
			) //
		);
	}

	public DigestInputStream(InputStream in, MessageDigest digest) {
		super(Objects.requireNonNull(in, "Must provide a non-null InputStream to wrap around"));
		this.digest = Objects.requireNonNull(digest, "Must provide a non-null digest instance");
	}

	@Override
	public MessageDigest getDigest() {
		return this.digest;
	}

	@Override
	public Pair<Long, byte[]> collectHash() {
		Pair<Long, byte[]> ret = Pair.of(this.length, this.digest.digest());
		this.length = 0;
		return ret;
	}

	@Override
	public void resetHash() {
		this.digest.reset();
		this.length = 0;
	}

	@Override
	public int read() throws IOException {
		if (read(this.byteBuf) >= 0) { return this.byteBuf[0]; }
		return -1;
	}

	@Override
	public int read(byte[] buf, int off, int len) throws IOException {
		int read = super.read(buf, off, len);
		this.digest.update(buf, off, len);
		this.length += len;
		return read;
	}
}
