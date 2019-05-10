package com.armedia.commons.utilities.xml;

public class StringToEnumAnyElementMapAdapter<E extends Enum<E>> extends AnyElementMapAdapter<String, E> {
	public StringToEnumAnyElementMapAdapter(AnyElementCodec<E> enumCodec) {
		super(AnyElementCodec.STRING, enumCodec);
	}
}