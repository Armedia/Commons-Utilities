package com.armedia.commons.utilities;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class BaseCodec<VALUE, ENCODING> implements Codec<VALUE, ENCODING> {

	private final VALUE nullValue;
	private final Function<ENCODING, VALUE> decode;
	private final Predicate<VALUE> nullValueCheck;

	private final ENCODING nullEncoding;
	private final Function<VALUE, ENCODING> encode;
	private final Predicate<ENCODING> nullEncodingCheck;

	public BaseCodec(Function<VALUE, ENCODING> encoder, Function<ENCODING, VALUE> decoder) {
		this(encoder, null, null, decoder, null, null);
	}

	public BaseCodec(Function<VALUE, ENCODING> encoder, Function<ENCODING, VALUE> decoder, VALUE nullValue) {
		this(encoder, null, null, decoder, nullValue, null);
	}

	public BaseCodec(Function<VALUE, ENCODING> encoder, Function<ENCODING, VALUE> decoder,
		Predicate<VALUE> nullValueCheck) {
		this(encoder, null, null, decoder, null, nullValueCheck);
	}

	public BaseCodec(Function<VALUE, ENCODING> encoder, Function<ENCODING, VALUE> decoder, VALUE nullValue,
		Predicate<VALUE> nullValueCheck) {
		this(encoder, null, null, decoder, nullValue, nullValueCheck);
	}

	public BaseCodec(Function<VALUE, ENCODING> encoder, ENCODING nullEncoding, Function<ENCODING, VALUE> decoder,
		VALUE nullValue) {
		this(encoder, nullEncoding, null, decoder, nullValue, null);
	}

	public BaseCodec(Function<VALUE, ENCODING> encoder, ENCODING nullEncoding, Function<ENCODING, VALUE> decoder,
		Predicate<VALUE> nullValueCheck) {
		this(encoder, nullEncoding, null, decoder, null, nullValueCheck);
	}

	public BaseCodec(Function<VALUE, ENCODING> encoder, ENCODING nullEncoding, Function<ENCODING, VALUE> decoder,
		VALUE nullValue, Predicate<VALUE> nullValueCheck) {
		this(encoder, nullEncoding, null, decoder, nullValue, nullValueCheck);
	}

	public BaseCodec(Function<VALUE, ENCODING> encoder, Predicate<ENCODING> nullEncodingCheck,
		Function<ENCODING, VALUE> decoder) {
		this(encoder, null, nullEncodingCheck, decoder, null, null);
	}

	public BaseCodec(Function<VALUE, ENCODING> encoder, Predicate<ENCODING> nullEncodingCheck,
		Function<ENCODING, VALUE> decoder, VALUE nullValue) {
		this(encoder, null, nullEncodingCheck, decoder, nullValue, null);
	}

	public BaseCodec(Function<VALUE, ENCODING> encoder, Predicate<ENCODING> nullEncodingCheck,
		Function<ENCODING, VALUE> decoder, Predicate<VALUE> nullValueCheck) {
		this(encoder, null, nullEncodingCheck, decoder, null, nullValueCheck);
	}

	public BaseCodec(Function<VALUE, ENCODING> encoder, Predicate<ENCODING> nullEncodingCheck,
		Function<ENCODING, VALUE> decoder, VALUE nullValue, Predicate<VALUE> nullValueCheck) {
		this(encoder, null, nullEncodingCheck, decoder, nullValue, nullValueCheck);
	}

	public BaseCodec(Function<VALUE, ENCODING> encoder, ENCODING nullEncoding, Predicate<ENCODING> nullEncodingCheck,
		Function<ENCODING, VALUE> decoder) {
		this(encoder, nullEncoding, nullEncodingCheck, decoder, null, null);
	}

	public BaseCodec(Function<VALUE, ENCODING> encoder, ENCODING nullEncoding, Predicate<ENCODING> nullEncodingCheck,
		Function<ENCODING, VALUE> decoder, VALUE nullValue) {
		this(encoder, nullEncoding, nullEncodingCheck, decoder, nullValue, null);
	}

	public BaseCodec(Function<VALUE, ENCODING> encoder, ENCODING nullEncoding, Predicate<ENCODING> nullEncodingCheck,
		Function<ENCODING, VALUE> decoder, Predicate<VALUE> nullValueCheck) {
		this(encoder, nullEncoding, nullEncodingCheck, decoder, null, nullValueCheck);
	}

	public BaseCodec(Function<VALUE, ENCODING> encoder, ENCODING nullEncoding, Predicate<ENCODING> nullEncodingCheck,
		Function<ENCODING, VALUE> decoder, VALUE nullValue, Predicate<VALUE> nullValueCheck) {
		this.encode = Objects.requireNonNull(encoder, "Must provide an encoder function");
		this.decode = Objects.requireNonNull(decoder, "Must provide a decoder function");

		Predicate<VALUE> pt = Objects::isNull;
		if (nullValueCheck != null) {
			pt = pt.or(nullValueCheck);
		}
		if (!pt.test(nullValue)) {
			throw new IllegalArgumentException(
				String.format("The given null-value-check predicate doesn't consider [%s] as a null-value", nullValue));
		}
		this.nullValueCheck = pt;
		if (!this.nullValueCheck.test(nullValue)) {
			throw new IllegalArgumentException(
				String.format("The given predicate did not detect the given value [%s] as a null value", nullValue));
		}
		this.nullValue = nullValue;

		Predicate<ENCODING> ps = Objects::isNull;
		if (nullEncodingCheck != null) {
			ps = ps.or(nullEncodingCheck);
		}
		if (!ps.test(nullEncoding)) {
			throw new IllegalArgumentException(String
				.format("The given null-string-check predicate doesn't consider [%s] as a null-string", nullEncoding));
		}
		this.nullEncodingCheck = ps;
		if (!this.nullEncodingCheck.test(nullEncoding)) {
			throw new IllegalArgumentException(String
				.format("The given predicate did not detect the given encoding [%s] as a null encoding", nullEncoding));
		}
		this.nullEncoding = nullEncoding;
	}

	@Override
	public ENCODING encode(VALUE v) {
		if (isNullValue(v)) { return this.nullEncoding; }
		return this.encode.apply(v);
	}

	@Override
	public boolean isNullValue(VALUE v) {
		return this.nullValueCheck.test(v);
	}

	@Override
	public VALUE getNullValue() {
		return this.nullValue;
	}

	@Override
	public VALUE decode(ENCODING e) {
		if (isNullEncoding(e)) { return this.nullValue; }
		return this.decode.apply(e);
	}

	@Override
	public boolean isNullEncoding(ENCODING e) {
		return this.nullEncodingCheck.test(e);
	}

	@Override
	public ENCODING getNullEncoding() {
		return this.nullEncoding;
	}
}