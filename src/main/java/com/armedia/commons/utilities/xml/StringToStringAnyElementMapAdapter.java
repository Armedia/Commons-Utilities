package com.armedia.commons.utilities.xml;

import com.armedia.commons.utilities.codec.StringCodec;

public class StringToStringAnyElementMapAdapter extends AnyElementMapAdapter<String, String> {
	public StringToStringAnyElementMapAdapter() {
		super(StringCodec.STRING, StringCodec.STRING);
	}
}