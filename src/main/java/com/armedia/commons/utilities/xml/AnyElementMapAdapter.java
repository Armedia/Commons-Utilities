package com.armedia.commons.utilities.xml;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public abstract class AnyElementMapAdapter<KEY, VALUE> extends XmlAdapter<AnyElementMap, Map<KEY, VALUE>> {

	private final AnyElementCodec<VALUE> valueCodec;
	private final AnyElementCodec<KEY> keyCodec;

	protected AnyElementMapAdapter(AnyElementCodec<KEY> keyCodec, AnyElementCodec<VALUE> valueCodec) {
		this.keyCodec = Objects.requireNonNull(keyCodec, "Must provide a codec for the keys");
		this.valueCodec = Objects.requireNonNull(valueCodec, "Must provide a codec for the values");
	}

	@Override
	public final AnyElementMap marshal(Map<KEY, VALUE> map) throws Exception {
		// First, convert to a Map<String, String>, then convert to AnyElementMap
		final Function<KEY, String> keyEncoder = this.keyCodec.getEncoder();
		final Function<VALUE, String> valueEncoder = this.valueCodec.getEncoder();
		Map<String, String> newMap = new LinkedHashMap<>();
		map.forEach((k, v) -> newMap.put(keyEncoder.apply(k), valueEncoder.apply(v)));
		return new AnyElementMap(newMap);
	}

	@Override
	public Map<KEY, VALUE> unmarshal(AnyElementMap adaptedMap) throws Exception {
		// Convert the Map<String, String> to the other type
		final Function<String, KEY> keyDecoder = this.keyCodec.getDecoder();
		final Function<String, VALUE> valueDecoder = this.valueCodec.getDecoder();
		Map<KEY, VALUE> map = new LinkedHashMap<>();
		adaptedMap.getMap().forEach((k, v) -> map.put(keyDecoder.apply(k), valueDecoder.apply(v)));
		return map;
	}
}