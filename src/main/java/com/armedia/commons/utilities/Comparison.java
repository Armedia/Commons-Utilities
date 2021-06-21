/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2021 Armedia, LLC
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
package com.armedia.commons.utilities;

import java.util.HashMap;
import java.util.Map;

/**
 * This enum permits easy comparison between to same-classed instances of {@link Comparable}.
 * Importantly, it can be used for parameterized comparison of values.
 *
 *
 *
 */
public enum Comparison {
	//
	LT("<") {
		@Override
		public <T extends Comparable<T>> boolean matches(T a, T b) {
			return (CompareUtil.compare(a, b) < 0);
		}
	},
	LE("<=") {
		@Override
		public <T extends Comparable<T>> boolean matches(T a, T b) {
			return (CompareUtil.compare(a, b) <= 0);
		}
	},
	EQ("=", "==") {
		@Override
		public <T extends Comparable<T>> boolean matches(T a, T b) {
			return (CompareUtil.compare(a, b) == 0);
		}
	},
	GE(">=") {
		@Override
		public <T extends Comparable<T>> boolean matches(T a, T b) {
			return (CompareUtil.compare(a, b) >= 0);
		}
	},
	GT(">") {
		@Override
		public <T extends Comparable<T>> boolean matches(T a, T b) {
			return (CompareUtil.compare(a, b) > 0);
		}
	};

	private static final Map<String, Comparison> SYMBOL_MAP;
	static {
		Map<String, Comparison> m = new HashMap<>();
		for (Comparison c : Comparison.values()) {
			for (String s : c.symbols) {
				m.put(s.toUpperCase(), c);
			}
			m.put(c.name().toUpperCase(), c);
		}
		SYMBOL_MAP = Tools.freezeMap(m);
	}

	private final String[] symbols;

	private Comparison(String... symbols) {
		this.symbols = symbols;
	}

	/**
	 * Executes the comparison between the two objects, and returns {@code true} if the comparison
	 * relationship is matched, or {@code false} otherwise. For instance, for {@link Comparison#LE},
	 * this method will return {@code true} if and only if a is less than, or equal to b.
	 *
	 * @param a
	 * @param b
	 * @return {@code true} if this comparison relationship is matched between a and b,
	 *         {@code false} otherwise.
	 */
	public abstract <T extends Comparable<T>> boolean matches(T a, T b);

	/**
	 * Decode the given string into a valid comparison that can be used for comparing two
	 * similarly-typed values. If no comparison matches the given symbols, then {@code null} is
	 * returned. The valid symbols are (case-insensitive):
	 * <ul>
	 * <li>Greater Than: GT, &gt;</li>
	 * <li>Greater Than or Equal To: GE, &gt;=</li>
	 * <li>Equal To: EQ, =, ==</li>
	 * <li>Less Than or Equal To: LE, &lt=</li>
	 * <li>Less Than: LT, &lt</li>
	 * </ul>
	 *
	 * @param symbol
	 * @return a valid comparison that can be used for comparing two similarly-typed values, or
	 *         {@code null} if symbol is null or if no comparison matches.
	 */
	public static Comparison decode(String symbol) {
		if (symbol == null) { return null; }
		return Comparison.SYMBOL_MAP.get(symbol.toUpperCase());
	}
}
