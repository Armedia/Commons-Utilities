package com.armedia.commons.utilities.xml;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.armedia.commons.utilities.codec.StringCodec;

public abstract class AnyElementMapAdapter<KEY, VALUE> extends XmlAdapter<AnyElementMap, Map<KEY, VALUE>> {

	private final StringCodec<VALUE> valueCodec;
	private final StringCodec<KEY> keyCodec;

	protected AnyElementMapAdapter(StringCodec<KEY> keyCodec, StringCodec<VALUE> valueCodec) {
		this.keyCodec = Objects.requireNonNull(keyCodec, "Must provide a codec for the keys");
		this.valueCodec = Objects.requireNonNull(valueCodec, "Must provide a codec for the values");
	}

	@Override
	public final AnyElementMap marshal(Map<KEY, VALUE> map) throws Exception {
		Map<String, String> newMap = new LinkedHashMap<>();
		map.forEach((k, v) -> newMap.put(this.keyCodec.encode(k), this.valueCodec.encode(v)));
		return new AnyElementMap(newMap);
	}

	@Override
	public final Map<KEY, VALUE> unmarshal(AnyElementMap adaptedMap) throws Exception {
		// Convert the Map<String, String> to the other type
		Map<KEY, VALUE> map = new LinkedHashMap<>();
		adaptedMap.getMap().forEach((k, v) -> map.put(this.keyCodec.decode(k), this.valueCodec.decode(v)));
		return map;
	}
}