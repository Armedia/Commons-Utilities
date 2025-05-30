/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2025 Armedia, LLC
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
package com.armedia.commons.utilities.cli.filter;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import com.armedia.commons.utilities.Tools;
import com.armedia.commons.utilities.cli.OptionValueFilter;

public abstract class NumericValueFilter<N extends Number> extends OptionValueFilter implements Comparator<N> {

	protected static final boolean DEFAULT_INCLUSIVE = true;

	private final N min;
	private final boolean minInc;
	private final N max;
	private final boolean maxInc;
	private final String description;

	protected NumericValueFilter(String label, N min, N max) {
		this(label, min, NumericValueFilter.DEFAULT_INCLUSIVE, max, NumericValueFilter.DEFAULT_INCLUSIVE);
	}

	protected NumericValueFilter(String label, N min, boolean minInclusive, N max, boolean maxInclusive) {
		this.min = min;
		this.minInc = minInclusive;
		this.max = max;
		this.maxInc = maxInclusive;
		String prefix = "a";
		if (StringUtils.startsWithAny(label.toLowerCase(), "a", "e", "i", "o", "u")) {
			prefix = "an";
		}
		String lorange = null;
		if (min != null) {
			lorange = String.format(" greater than%s %s", (minInclusive ? " or equal to" : ""), min);
		}
		String hirange = null;
		if (max != null) {
			hirange = String.format(" less than%s %s", (maxInclusive ? " or equal to" : ""), max);
		}

		String range = null;
		if ((lorange != null) && (hirange != null)) {
			// We have to render a chained range...
			range = String.format("%s, and %s", lorange, hirange);
		} else {
			// Pick the first non-null, since we don't have to chain any of them...include the empty
			// string to cover for the case when both are null (i.e. no range)
			range = Tools.coalesce(lorange, hirange, "");
		}

		this.description = String.format("%s %s number%s", prefix, label, range);
	}

	protected abstract N convert(String str) throws NumberFormatException;

	protected boolean isProper(N value) {
		return (value != null);
	}

	private final boolean isInRange(N n) {
		// If no min, always be higher than the minimum
		final int lo = (this.min != null ? compare(this.min, n) : -1);
		// If no max, always be lower than the maximum
		final int hi = (this.max != null ? compare(n, this.max) : -1);

		boolean ok = true;
		if (ok && (this.min != null)) {
			if (this.minInc) {
				ok &= (lo <= 0);
			} else {
				ok &= (lo < 0);
			}
		}

		if (ok && (this.max != null)) {
			if (this.maxInc) {
				ok &= (hi <= 0);
			} else {
				ok &= (hi < 0);
			}
		}

		return ok;
	}

	@Override
	protected final boolean checkValue(String value) {
		try {
			// 1) Is it a number?
			final N n = convert(value);
			// 2) Is it a proper value in the required range?
			return isProper(n) && isInRange(n);
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public final String getDefinition() {
		return this.description;
	}
}
