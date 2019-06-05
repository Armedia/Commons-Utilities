package com.armedia.commons.utilities.function;

import java.util.function.Predicate;

import com.armedia.commons.utilities.CheckedCodec;

public class FunctionalCheckedCodec<VALUE, ENCODING, EXCEPTION extends Exception>
	implements CheckedCodec<VALUE, ENCODING, EXCEPTION> {

	private final VALUE nullValue;
	private final ENCODING nullEncoding;
	private final Predicate<VALUE> nullValueChecker;
	private final CheckedFunction<VALUE, ENCODING, EXCEPTION> specialEncoder;
	private final Predicate<ENCODING> nullEncodingChecker;
	private final CheckedFunction<ENCODING, VALUE, EXCEPTION> specialDecoder;

	public FunctionalCheckedCodec(VALUE nullValue, Predicate<ENCODING> nullEncodingChecker,
		CheckedFunction<VALUE, ENCODING, EXCEPTION> specialEncoder, ENCODING nullEncoding,
		Predicate<VALUE> nullValueChecker, CheckedFunction<ENCODING, VALUE, EXCEPTION> specialDecoder) {
		this.nullValue = nullValue;
		this.nullValueChecker = nullValueChecker;
		this.nullEncoding = nullEncoding;
		this.nullEncodingChecker = nullEncodingChecker;
		this.specialEncoder = specialEncoder;
		this.specialDecoder = specialDecoder;
	}

	@Override
	public VALUE getNullValue() {
		return this.nullValue;
	}

	@Override
	public boolean isNullValue(VALUE v) {
		if (this.nullValueChecker != null) { return this.nullValueChecker.test(v); }
		return CheckedCodec.super.isNullValue(v);
	}

	@Override
	public boolean isNullEncoding(ENCODING e) {
		if (this.nullEncodingChecker != null) { return this.nullEncodingChecker.test(e); }
		return CheckedCodec.super.isNullEncoding(e);
	}

	@Override
	public ENCODING getNullEncoding() {
		return this.nullEncoding;
	}

	public CheckedFunction<VALUE, ENCODING, EXCEPTION> getSpecialEncoder() {
		return this.specialEncoder;
	}

	public CheckedFunction<ENCODING, VALUE, EXCEPTION> getSpecialDecoder() {
		return this.specialDecoder;
	}

	@Override
	public ENCODING encode(VALUE v) throws EXCEPTION {
		if (this.specialEncoder == null) { throw new UnsupportedOperationException("No encoding function given"); }
		return this.specialEncoder.applyChecked(v);
	}

	@Override
	public VALUE decode(ENCODING e) throws EXCEPTION {
		if (this.specialDecoder == null) { throw new UnsupportedOperationException("No decoding function given"); }
		return this.specialDecoder.applyChecked(e);
	}
}