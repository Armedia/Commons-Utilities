/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (c) 2010 - 2019 Armedia LLC
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

import java.util.function.Function;
import java.util.function.Predicate;

import com.armedia.commons.utilities.function.CheckedTools;

public class FunctionalCodec<VALUE, ENCODING> extends FunctionalCheckedCodec<VALUE, ENCODING, RuntimeException>
	implements Codec<VALUE, ENCODING> {

	public static class Builder<VALUE, ENCODING>
		extends CodecBuilder<VALUE, ENCODING, RuntimeException, FunctionalCodec<VALUE, ENCODING>> {
		@Override
		@SuppressWarnings("unchecked")
		protected FunctionalCodec<VALUE, ENCODING> newCodec() throws RuntimeException {
			return new FunctionalCodec<>(this);
		}
	}

	protected FunctionalCodec(Builder<VALUE, ENCODING> builder) {
		this(builder.getEncoder(), builder.getNullValue(), builder.getNullValueChecker(), builder.getDecoder(),
			builder.getNullEncoding(), builder.getNullEncodingChecker());
	}

	public FunctionalCodec(Function<VALUE, ENCODING> encoder, VALUE nullValue, Predicate<VALUE> nullValueChecker,
		Function<ENCODING, VALUE> decoder, ENCODING nullEncoding, Predicate<ENCODING> nullEncodingChecker) {
		super(CheckedTools.check(encoder), nullValue, nullValueChecker, CheckedTools.check(decoder), nullEncoding,
			nullEncodingChecker);
	}
}
