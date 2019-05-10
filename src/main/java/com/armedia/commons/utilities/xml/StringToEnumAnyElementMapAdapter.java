package com.armedia.commons.utilities.xml;

import com.armedia.commons.utilities.SimpleTypeCodec;

public abstract class StringToEnumAnyElementMapAdapter<E extends Enum<E>> extends AnyElementMapAdapter<String, E> {
	public StringToEnumAnyElementMapAdapter(SimpleTypeCodec<E> enumCodec) {
		super(SimpleTypeCodec.STRING, enumCodec);
	}
}