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

import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.armedia.commons.utilities.codec.CheckedCodec;

public final class EncodedString {

	private final byte[] data;
	private final int hashCode;
	private final byte[] hash;
	private final CheckedCodec<CharSequence, byte[], ? extends Exception> cipher;

	private final String string;

	private EncodedString(Optional<CharSequence> string, CheckedCodec<CharSequence, byte[], ? extends Exception> cipher)
		throws Exception {
		CharSequence str = string.orElse(StringUtils.EMPTY);
		this.data = cipher.encode(str);
		this.cipher = cipher;
		this.hashCode = Tools.hashTool(this, null, str);
		this.hash = DigestUtils.sha256(str.toString());
		this.string = StringUtils.lowerCase( //
			Base64.getEncoder().encodeToString(this.data) //
		);
	}

	@Override
	public String toString() {
		return this.string;
	}

	public CharSequence decode() throws Exception {
		return this.cipher.decode(this.data);
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (!Tools.baseEquals(this, obj)) { return false; }
		EncodedString other = EncodedString.class.cast(obj);
		if (this.hashCode != other.hashCode) { return false; }
		if (!Arrays.equals(this.hash, other.hash)) { return false; }
		return true;
	}

	public static EncodedString from(char[] value, CheckedCodec<CharSequence, byte[], ? extends Exception> cipher)
		throws Exception {
		Objects.requireNonNull(cipher, "Must provide a non-null cipher");
		CharSequence seq = null;
		if ((value != null) && (value.length > 0)) {
			seq = new String(value);
		}
		return EncodedString.from(seq, cipher);
	}

	public static EncodedString from(CharSequence value, CheckedCodec<CharSequence, byte[], ? extends Exception> cipher)
		throws Exception {
		return new EncodedString(Optional.ofNullable(value),
			Objects.requireNonNull(cipher, "Must provide a non-null cipher"));
	}
}