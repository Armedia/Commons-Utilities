package com.armedia.commons.utilities.xml;

import com.armedia.commons.utilities.StringCodec;

public abstract class StringToEnumAnyElementMapAdapter<E extends Enum<E>> extends AnyElementMapAdapter<String, E> {
	public StringToEnumAnyElementMapAdapter(StringCodec<E> enumCodec) {
		super(StringCodec.STRING, enumCodec);
	}
}