/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2019 Armedia, LLC
 * %%
 * This file is part of the Caliente software.
 * 
 * If the software was purchased under a paid Caliente license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * 
 * Caliente is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Caliente is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Caliente. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 *******************************************************************************/
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
