package com.armedia.commons.utilities.xml;

public class StringToStringAnyElementMapAdapter extends AnyElementMapAdapter<String, String> {
	public StringToStringAnyElementMapAdapter() {
		super(AnyElementCodec.STRING, AnyElementCodec.STRING);
	}
}