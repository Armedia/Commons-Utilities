package com.armedia.commons.utilities.codec;

import java.util.Objects;
import java.util.function.Predicate;

import com.armedia.commons.utilities.Tools;
import com.armedia.commons.utilities.function.CheckedFunction;

public class FunctionalCheckedCodec<VALUE, ENCODING, EXCEPTION extends Exception>
	implements CheckedCodec<VALUE, ENCODING, EXCEPTION> {

	public static class Builder<VALUE, ENCODING, EXCEPTION extends Exception>
		extends CodecBuilder<VALUE, ENCODING, EXCEPTION, FunctionalCheckedCodec<VALUE, ENCODING, EXCEPTION>> {
		@Override
		@SuppressWarnings("unchecked")
		protected FunctionalCheckedCodec<VALUE, ENCODING, EXCEPTION> newCodec() {
			return new FunctionalCheckedCodec<>(this);
		}
	}

	private final VALUE nullValue;
	private final Predicate<VALUE> nullValueCheck;
	private final CheckedFunction<ENCODING, VALUE, EXCEPTION> decoder;
	private final ENCODING nullEncoding;
	private final Predicate<ENCODING> nullEncodingCheck;
	private final CheckedFunction<VALUE, ENCODING, EXCEPTION> encoder;

	protected FunctionalCheckedCodec(Builder<VALUE, ENCODING, EXCEPTION> builder) {
		this(builder.getEncoder(), builder.getNullValue(), builder.getNullValueChecker(), builder.getDecoder(),
			builder.getNullEncoding(), builder.getNullEncodingChecker());
	}

	public FunctionalCheckedCodec(CheckedFunction<VALUE, ENCODING, EXCEPTION> encoder, VALUE nullValue,
		Predicate<VALUE> nullValueCheck, CheckedFunction<ENCODING, VALUE, EXCEPTION> decoder, ENCODING nullEncoding,
		Predicate<ENCODING> nullEncodingCheck) {
		this.encoder = Objects.requireNonNull(encoder, "Must provide an encoder function");
		this.decoder = Objects.requireNonNull(decoder, "Must provide a decoder function");

		this.nullValue = nullValue;
		Predicate<VALUE> predValue = Objects::isNull;
		if (nullValueCheck == null) {
			nullValueCheck = (e) -> Tools.equals(e, this.nullValue);
		}
		predValue = predValue.or(nullValueCheck);
		if (!predValue.test(nullValue)) {
			throw new IllegalArgumentException(String
				.format("The given null-value-check predicate doesn't match the given null-value [%s]", nullValue));
		}
		this.nullValueCheck = predValue;

		this.nullEncoding = nullEncoding;
		Predicate<ENCODING> predEncoding = Objects::isNull;
		if (nullEncodingCheck == null) {
			nullEncodingCheck = (e) -> Tools.equals(e, this.nullEncoding);
		}
		predEncoding = predEncoding.or(nullEncodingCheck);
		if (!predEncoding.test(nullEncoding)) {
			throw new IllegalArgumentException(String.format(
				"The given null-encoding-check predicate doesn't match the given null-encoding [%s]", nullEncoding));
		}
		this.nullEncodingCheck = predEncoding;
	}

	@Override
	public ENCODING encode(VALUE v) throws EXCEPTION {
		if (isNullValue(v)) { return this.nullEncoding; }
		return this.encoder.applyChecked(v);
	}

	public final CheckedFunction<VALUE, ENCODING, EXCEPTION> getEncoder() {
		return this.encoder;
	}

	@Override
	public final VALUE getNullValue() {
		return this.nullValue;
	}

	@Override
	public final boolean isNullValue(VALUE v) {
		return this.nullValueCheck.test(v);
	}

	@Override
	public VALUE decode(ENCODING e) throws EXCEPTION {
		if (isNullEncoding(e)) { return this.nullValue; }
		return this.decoder.applyChecked(e);
	}

	public final CheckedFunction<ENCODING, VALUE, EXCEPTION> getDecoder() {
		return this.decoder;
	}

	@Override
	public final ENCODING getNullEncoding() {
		return this.nullEncoding;
	}

	@Override
	public final boolean isNullEncoding(ENCODING e) {
		return this.nullEncodingCheck.test(e);
	}
}