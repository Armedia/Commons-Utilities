package com.armedia.commons.utilities.codec;

import com.armedia.commons.utilities.Tools;

public interface CheckedCodec<VALUE, ENCODING, EXCEPTION extends Exception> {

	public ENCODING encode(VALUE v) throws EXCEPTION;

	public default boolean isNullValue(VALUE v) {
		VALUE n = getNullValue();
		return ((v == null) || (n == v) || Tools.equals(n, v));
	}

	public default VALUE getNullValue() {
		return null;
	}

	public VALUE decode(ENCODING e) throws EXCEPTION;

	public default boolean isNullEncoding(ENCODING e) {
		ENCODING n = getNullEncoding();
		return ((e == null) || (n == e) || Tools.equals(n, e));
	}

	public default ENCODING getNullEncoding() {
		return null;
	}
}