package com.armedia.commons.utilities.xml;

import com.armedia.commons.utilities.StringCodec;

public class StringToStringAnyElementMapAdapter extends AnyElementMapAdapter<String, String> {
	public StringToStringAnyElementMapAdapter() {
		super(StringCodec.STRING, StringCodec.STRING);
	}
}