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
package com.armedia.commons.utilities.codec;

import java.util.function.Predicate;

import com.armedia.commons.utilities.concurrent.BaseShareableLockable;
import com.armedia.commons.utilities.concurrent.SharedAutoLock;
import com.armedia.commons.utilities.function.CheckedFunction;

public abstract class CodecBuilder<VALUE, ENCODING, EXCEPTION extends Exception, CODEC extends CheckedCodec<VALUE, ENCODING, EXCEPTION>>
	extends BaseShareableLockable {

	private CheckedFunction<VALUE, ENCODING, EXCEPTION> encoder = null;
	private VALUE nullValue = null;
	private Predicate<VALUE> nullValueChecker = null;

	private CheckedFunction<ENCODING, VALUE, EXCEPTION> decoder = null;
	private ENCODING nullEncoding = null;
	private Predicate<ENCODING> nullEncodingChecker = null;

	public final VALUE getNullValue() {
		return shareLocked(() -> this.nullValue);
	}

	public final CodecBuilder<VALUE, ENCODING, EXCEPTION, CODEC> setNullValue(VALUE nullValue) {
		mutexLocked(() -> this.nullValue = nullValue);
		return this;
	}

	public final Predicate<VALUE> getNullValueChecker() {
		return shareLocked(() -> this.nullValueChecker);
	}

	public final CodecBuilder<VALUE, ENCODING, EXCEPTION, CODEC> setNullValueChecker(
		Predicate<VALUE> nullValueChecker) {
		mutexLocked(() -> this.nullValueChecker = nullValueChecker);
		return this;
	}

	public final CheckedFunction<ENCODING, VALUE, EXCEPTION> getDecoder() {
		return shareLocked(() -> this.decoder);
	}

	public final CodecBuilder<VALUE, ENCODING, EXCEPTION, CODEC> setDecoder(
		CheckedFunction<ENCODING, VALUE, EXCEPTION> specialDecoder) {
		mutexLocked(() -> this.decoder = specialDecoder);
		return this;
	}

	public final ENCODING getNullEncoding() {
		return shareLocked(() -> this.nullEncoding);
	}

	public final CodecBuilder<VALUE, ENCODING, EXCEPTION, CODEC> setNullEncoding(ENCODING nullEncoding) {
		mutexLocked(() -> this.nullEncoding = nullEncoding);
		return this;
	}

	public final Predicate<ENCODING> getNullEncodingChecker() {
		return shareLocked(() -> this.nullEncodingChecker);
	}

	public final CodecBuilder<VALUE, ENCODING, EXCEPTION, CODEC> setNullEncodingChecker(
		Predicate<ENCODING> nullEncodingChecker) {
		mutexLocked(() -> this.nullEncodingChecker = nullEncodingChecker);
		return this;
	}

	public final CheckedFunction<VALUE, ENCODING, EXCEPTION> getEncoder() {
		return shareLocked(() -> this.encoder);
	}

	public final CodecBuilder<VALUE, ENCODING, EXCEPTION, CODEC> setEncoder(
		CheckedFunction<VALUE, ENCODING, EXCEPTION> specialEncoder) {
		mutexLocked(() -> this.encoder = specialEncoder);
		return this;
	}

	public final <C extends CODEC> C build() {
		try (SharedAutoLock lock = autoSharedLock()) {
			return newCodec();
		}
	}

	protected abstract <C extends CODEC> C newCodec();
}
