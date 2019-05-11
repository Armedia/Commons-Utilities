package com.armedia.commons.utilities.xml;

import com.armedia.commons.utilities.StringCodec;

public abstract class EnumToStringAnyElementMapAdapter<E extends Enum<E>> extends AnyElementMapAdapter<E, String> {
	public EnumToStringAnyElementMapAdapter(StringCodec<E> enumCodec) {
		super(enumCodec, StringCodec.STRING);
	}
}