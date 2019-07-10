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
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;

import com.armedia.commons.utilities.concurrent.BaseShareableLockable;
import com.armedia.commons.utilities.concurrent.MutexAutoLock;

public class DigestWritableByteChannel extends BaseShareableLockable
	implements WritableByteChannel, DigestHashCollector {

	private final WritableByteChannel channel;
	private final MessageDigest digest;
	private long length = 0;

	public DigestWritableByteChannel(WritableByteChannel channel, String digest) throws NoSuchAlgorithmException {
		this( //
			Objects.requireNonNull(channel, "Must provide a non-null WritableByteChannel instance"), //
			MessageDigest.getInstance( //
				Objects.requireNonNull(digest, "Must provide a non-null digest name") //
			) //
		);
	}

	public DigestWritableByteChannel(WritableByteChannel channel, MessageDigest digest) {
		this.channel = Objects.requireNonNull(channel, "Must provide a non-null WritableByteChannel instance");
		this.digest = Objects.requireNonNull(digest, "Must provide a non-null MessageDigest instance");
	}

	@Override
	public MessageDigest getDigest() {
		return this.digest;
	}

	@Override
	public Pair<Long, byte[]> collectHash() {
		try (MutexAutoLock lock = autoMutexLock()) {
			Pair<Long, byte[]> ret = Pair.of(this.length, this.digest.digest());
			this.length = 0;
			return ret;
		}
	}

	@Override
	public void resetHash() {
		try (MutexAutoLock lock = autoMutexLock()) {
			this.digest.reset();
			this.length = 0;
		}
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		try (MutexAutoLock lock = autoMutexLock()) {
			ByteBuffer slice = src.slice();
			int ret = this.channel.write(slice);
			this.digest.update(src);
			this.length += slice.capacity();
			return ret;
		}
	}

	@Override
	public boolean isOpen() {
		return shareLocked(this.channel::isOpen);
	}

	@Override
	public void close() throws IOException {
		mutexLocked(this.channel::close);
	}
}
