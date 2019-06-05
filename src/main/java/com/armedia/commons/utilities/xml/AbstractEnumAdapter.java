package com.armedia.commons.utilities.xml;

import java.util.Objects;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.armedia.commons.utilities.EnumCodec;

public abstract class AbstractEnumAdapter<E extends Enum<E>> extends XmlAdapter<String, E> {

	private final EnumCodec<E> codec;

	public AbstractEnumAdapter(Class<E> enumClass) {
		this(new EnumCodec<>(enumClass));
	}

	public AbstractEnumAdapter(EnumCodec<E> codec) {
		this.codec = Objects.requireNonNull(codec, "Must provide a valid Enum codec");
	}

	@Override
	public final E unmarshal(String v) throws Exception {
		return this.codec.decodeChecked(v);
	}

	@Override
	public final String marshal(E v) throws Exception {
		return this.codec.encodeChecked(v);
	}
}