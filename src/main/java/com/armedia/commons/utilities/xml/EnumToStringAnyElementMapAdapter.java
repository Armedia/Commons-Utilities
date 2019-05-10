package com.armedia.commons.utilities.xml;

import com.armedia.commons.utilities.SimpleTypeCodec;

public abstract class EnumToStringAnyElementMapAdapter<E extends Enum<E>> extends AnyElementMapAdapter<E, String> {
	public EnumToStringAnyElementMapAdapter(SimpleTypeCodec<E> enumCodec) {
		super(enumCodec, SimpleTypeCodec.STRING);
	}
}