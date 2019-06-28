/*******************************************************************************
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (C) 2013 - 2019 Armedia
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
package com.armedia.commons.utilities.line;

import java.io.Serializable;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.armedia.commons.utilities.Tools;

public final class LineIteratorConfig implements Serializable, Cloneable {
	public static enum Trim {
		//
		NONE, //
		LEADING {
			@Override
			public String apply(String s) {
				return StringUtils.stripStart(s, null);
			}
		}, //
		TRAILING {
			@Override
			public String apply(String s) {
				return StringUtils.stripEnd(s, null);
			}
		}, //
		BOTH {
			@Override
			public String apply(String s) {
				return StringUtils.strip(s);
			}
		}, //
			//
		;

		public String apply(String s) {
			return s;
		}
	}

	public static enum Feature {
		//
		COMMENTS, //
		CONTINUATION, //
		RECURSION, //
		IGNORE_EMPTY_LINES, //
		CONTINUED_NEWLINES, //
		//
		;
	}

	private static final long serialVersionUID = 1L;

	public static final int INFINITE_RECURSION = -1;
	public static final int DEFAULT_MAX_DEPTH = LineIteratorConfig.INFINITE_RECURSION;
	public static final Set<LineIteratorConfig.Feature> DEFAULT_FEATURES = Tools
		.freezeSet(EnumSet.allOf(LineIteratorConfig.Feature.class));
	public static final LineIteratorConfig.Trim DEFAULT_TRIM = LineIteratorConfig.Trim.NONE;

	private LineIteratorConfig.Trim trim = LineIteratorConfig.DEFAULT_TRIM;
	private int maxDepth = LineIteratorConfig.INFINITE_RECURSION;
	private final Set<LineIteratorConfig.Feature> features = EnumSet.noneOf(LineIteratorConfig.Feature.class);

	public LineIteratorConfig() {
		this.features.addAll(LineIteratorConfig.DEFAULT_FEATURES);
	}

	public LineIteratorConfig(LineIteratorConfig other) {
		this();
		if (other != null) {
			this.trim = other.getTrim();
			this.maxDepth = other.getMaxDepth();
			this.features.clear();
			this.features.addAll(other.getFeatures());
		}
	}

	public LineIteratorConfig reset() {
		this.trim = LineIteratorConfig.DEFAULT_TRIM;
		this.maxDepth = LineIteratorConfig.DEFAULT_MAX_DEPTH;
		this.features.clear();
		this.features.addAll(LineIteratorConfig.DEFAULT_FEATURES);
		return this;
	}

	public Set<LineIteratorConfig.Feature> getFeatures() {
		return this.features;
	}

	public LineIteratorConfig addFeature(LineIteratorConfig.Feature feature) {
		return addFeatures(Objects.requireNonNull(feature, "Must provide a non-null Feature to add"));
	}

	public LineIteratorConfig removeFeature(LineIteratorConfig.Feature feature) {
		return removeFeatures(Objects.requireNonNull(feature, "Must provide a non-null Feature to remove"));
	}

	public LineIteratorConfig addFeatures(LineIteratorConfig.Feature... features) {
		if (features != null) {
			for (LineIteratorConfig.Feature f : features) {
				if (f != null) {
					this.features.add(f);
				}
			}
		}
		return this;
	}

	public LineIteratorConfig removeFeatures(LineIteratorConfig.Feature... features) {
		if (features != null) {
			for (LineIteratorConfig.Feature f : features) {
				if (f != null) {
					this.features.remove(f);
				}
			}
		}
		return this;
	}

	public LineIteratorConfig addFeatures(Collection<LineIteratorConfig.Feature> features) {
		if (features != null) {
			features.stream().filter(Objects::nonNull).forEachOrdered(this.features::add);
		}
		return this;
	}

	public LineIteratorConfig removeFeatures(Collection<LineIteratorConfig.Feature> features) {
		if (features != null) {
			features.stream().filter(Objects::nonNull).forEachOrdered(this.features::remove);
		}
		return this;
	}

	public LineIteratorConfig setFeatures(LineIteratorConfig.Feature... features) {
		this.features.clear();
		if (features == null) {
			this.features.addAll(LineIteratorConfig.DEFAULT_FEATURES);
		} else {
			for (LineIteratorConfig.Feature f : features) {
				if (f != null) {
					this.features.add(f);
				}
			}
		}
		return this;
	}

	public LineIteratorConfig setFeatures(Collection<LineIteratorConfig.Feature> features) {
		this.features.clear();
		if (features == null) {
			this.features.addAll(LineIteratorConfig.DEFAULT_FEATURES);
		} else {
			features.stream().filter(Objects::nonNull).forEachOrdered(this.features::add);
		}
		return this;
	}

	public boolean hasFeature(LineIteratorConfig.Feature f) {
		return this.features.contains(Objects.requireNonNull(f, "Must provide a non-null feature"));
	}

	public LineIteratorConfig.Trim getTrim() {
		return this.trim;
	}

	public LineIteratorConfig setTrim(LineIteratorConfig.Trim trim) {
		this.trim = Tools.coalesce(trim, LineIteratorConfig.DEFAULT_TRIM);
		return this;
	}

	public int getMaxDepth() {
		return this.maxDepth;
	}

	public LineIteratorConfig setMaxDepth(Integer maxDepth) {
		if (maxDepth == null) {
			this.maxDepth = LineIteratorConfig.INFINITE_RECURSION;
		} else {
			this.maxDepth = Math.max(LineIteratorConfig.INFINITE_RECURSION, maxDepth);
		}
		return this;
	}

	public LineIteratorConfig copyFrom(LineIteratorConfig other) {
		if (other != null) {
			this.maxDepth = other.getMaxDepth();
			this.trim = other.getTrim();
			this.features.clear();
			this.features.addAll(other.getFeatures());
		} else {
			reset();
		}
		return this;
	}

	@Override
	public int hashCode() {
		return Tools.hashTool(this, null, this.maxDepth, this.trim, this.features);
	}

	@Override
	public boolean equals(Object obj) {
		if (!Tools.baseEquals(this, obj)) { return false; }
		LineIteratorConfig other = LineIteratorConfig.class.cast(obj);
		if (this.trim != other.trim) { return false; }
		if (this.maxDepth != other.maxDepth) { return false; }
		if (!Tools.equals(this.features, other.features)) { return false; }
		return true;
	}

	@Override
	public String toString() {
		return String.format("LineScannerConfig [trim=%s, maxDepth=%s, features=%s]", this.trim, this.maxDepth,
			this.features);
	}
}
