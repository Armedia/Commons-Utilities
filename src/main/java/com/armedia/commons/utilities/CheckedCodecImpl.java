package com.armedia.commons.utilities;

import com.armedia.commons.utilities.function.CheckedFunction;

public class CheckedCodecImpl<VALUE, ENCODING, EXCEPTION extends Exception>
	implements CheckedCodec<VALUE, ENCODING, EXCEPTION> {

	private final VALUE nullValue;
	private final ENCODING nullEncoding;
	private final CheckedFunction<VALUE, ENCODING, EXCEPTION> specialEncoder;
	private final CheckedFunction<ENCODING, VALUE, EXCEPTION> specialDecoder;

	public CheckedCodecImpl(VALUE nullValue, CheckedFunction<VALUE, ENCODING, EXCEPTION> specialEncoder,
		ENCODING nullEncoding, CheckedFunction<ENCODING, VALUE, EXCEPTION> specialDecoder) {
		this.nullValue = nullValue;
		this.nullEncoding = nullEncoding;
		this.specialEncoder = specialEncoder;
		this.specialDecoder = specialDecoder;
	}

	@Override
	public VALUE getNullValue() {
		return this.nullValue;
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
		if (this.specialEncoder == null) { return null; }
		return this.specialEncoder.applyChecked(v);
	}

	@Override
	public VALUE decode(ENCODING e) throws EXCEPTION {
		if (this.specialDecoder == null) { return null; }
		return this.specialDecoder.applyChecked(e);
	}
}