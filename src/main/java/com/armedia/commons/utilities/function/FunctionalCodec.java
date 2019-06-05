package com.armedia.commons.utilities.function;

import java.util.function.Function;
import java.util.function.Predicate;

import com.armedia.commons.utilities.Codec;

public class FunctionalCodec<VALUE, ENCODING> extends FunctionalCheckedCodec<VALUE, ENCODING, RuntimeException>
	implements Codec<VALUE, ENCODING> {

	public FunctionalCodec(VALUE nullValue, Predicate<ENCODING> nullEncodingChecker,
		Function<VALUE, ENCODING> specialEncoder, ENCODING nullEncoding, Predicate<VALUE> nullValueChecker,
		Function<ENCODING, VALUE> specialDecoder) {
		super(nullValue, nullEncodingChecker, CheckedTools.check(specialEncoder), nullEncoding, nullValueChecker,
			CheckedTools.check(specialDecoder));
	}
}