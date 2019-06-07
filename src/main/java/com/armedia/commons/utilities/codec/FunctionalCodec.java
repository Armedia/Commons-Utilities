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