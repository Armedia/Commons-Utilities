package com.armedia.commons.utilities;

public interface Codec<VALUE, ENCODING> extends CheckedCodec<VALUE, ENCODING> {

	@Override
	public ENCODING encode(VALUE v);

	@Override
	public VALUE decode(ENCODING e);

}