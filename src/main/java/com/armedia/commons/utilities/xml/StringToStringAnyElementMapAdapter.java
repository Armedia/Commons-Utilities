package com.armedia.commons.utilities.xml;

import com.armedia.commons.utilities.SimpleTypeCodec;

public class StringToStringAnyElementMapAdapter extends AnyElementMapAdapter<String, String> {
	public StringToStringAnyElementMapAdapter() {
		super(SimpleTypeCodec.STRING, SimpleTypeCodec.STRING);
	}
}