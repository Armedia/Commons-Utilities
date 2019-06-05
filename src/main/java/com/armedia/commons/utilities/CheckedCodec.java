package com.armedia.commons.utilities;

import java.util.Objects;

public interface CheckedCodec<VALUE, ENCODING> {

	public ENCODING encode(VALUE v) throws Exception;

	public default boolean isNullValue(VALUE v) {
		return Objects.isNull(v);
	}

	public default VALUE getNullValue() {
		return null;
	}

	public VALUE decode(ENCODING e) throws Exception;

	public default boolean isNullEncoding(ENCODING e) {
		return Objects.isNull(e);
	}

	public default ENCODING getNullEncoding() {
		return null;
	}
}