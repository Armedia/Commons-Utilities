package com.armedia.commons.utilities.xml;

import java.nio.charset.Charset;

import com.armedia.commons.utilities.codec.CheckedCodec;
import com.armedia.commons.utilities.codec.FunctionalCodec;

public class CharsetAdapter extends AbstractCodecAdapter<String, Charset, RuntimeException> {

	public static CheckedCodec<Charset, String, RuntimeException> CODEC;
	static {
		FunctionalCodec.Builder<Charset, String> builder = new FunctionalCodec.Builder<>();
		builder.setEncoder(Charset::name);
		builder.setDecoder(Charset::forName);
		CharsetAdapter.CODEC = builder.build();
	}

	public CharsetAdapter() {
		super(CharsetAdapter.CODEC);
	}
}