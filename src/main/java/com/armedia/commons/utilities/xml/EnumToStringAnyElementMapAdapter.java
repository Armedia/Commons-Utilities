package com.armedia.commons.utilities.xml;

public abstract class EnumToStringAnyElementMapAdapter<E extends Enum<E>> extends AnyElementMapAdapter<E, String> {
	public EnumToStringAnyElementMapAdapter(AnyElementCodec<E> enumCodec) {
		super(enumCodec, AnyElementCodec.STRING);
	}
}