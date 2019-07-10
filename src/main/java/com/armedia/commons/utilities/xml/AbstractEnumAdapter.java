/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (c) 2010 - 2019 Armedia LLC
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

import java.util.Objects;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.armedia.commons.utilities.codec.EnumCodec;

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
