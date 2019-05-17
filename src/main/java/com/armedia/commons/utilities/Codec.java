package com.armedia.commons.utilities;

import java.util.Objects;

public interface Codec<VALUE, ENCODING> {

	public ENCODING encode(VALUE v);

	public default boolean isNullValue(VALUE v) {
		return Objects.isNull(v);
	}

	public default VALUE getNullValue() {
		return null;
	}

	public VALUE decode(ENCODING e);

	public default boolean isNullEncoding(ENCODING e) {
		return Objects.isNull(e);
	}

	public default ENCODING getNullEncoding() {
		return null;
	}
}