package com.armedia.commons.utilities;

import com.armedia.commons.utilities.function.CheckedFunction;

public class CodecImpl<VALUE, ENCODING> extends CheckedCodecImpl<VALUE, ENCODING, RuntimeException>
	implements Codec<VALUE, ENCODING> {

	public CodecImpl(VALUE nullValue, CheckedFunction<VALUE, ENCODING, RuntimeException> specialEncoder,
		ENCODING nullEncoding, CheckedFunction<ENCODING, VALUE, RuntimeException> specialDecoder) {
		super(nullValue, specialEncoder, nullEncoding, specialDecoder);
	}

	@Override
	public ENCODING encode(VALUE v) {
		return super.encode(v);
	}

	@Override
	public VALUE decode(ENCODING e) {
		return super.decode(e);
	}
}