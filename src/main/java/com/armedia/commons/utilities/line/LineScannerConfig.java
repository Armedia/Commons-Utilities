package com.armedia.commons.utilities.line;

import java.io.Serializable;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.armedia.commons.utilities.Tools;

public final class LineScannerConfig implements Serializable, Cloneable {
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
	public static final int DEFAULT_MAX_DEPTH = LineScannerConfig.INFINITE_RECURSION;
	public static final Set<LineScannerConfig.Feature> DEFAULT_FEATURES = Tools
		.freezeSet(EnumSet.allOf(LineScannerConfig.Feature.class));
	public static final LineScannerConfig.Trim DEFAULT_TRIM = LineScannerConfig.Trim.NONE;

	private LineScannerConfig.Trim trim = LineScannerConfig.DEFAULT_TRIM;
	private int maxDepth = LineScannerConfig.INFINITE_RECURSION;
	private final Set<LineScannerConfig.Feature> features = EnumSet.noneOf(LineScannerConfig.Feature.class);

	public LineScannerConfig() {
		this.features.addAll(LineScannerConfig.DEFAULT_FEATURES);
	}

	public LineScannerConfig(LineScannerConfig other) {
		this();
		if (other != null) {
			this.trim = other.getTrim();
			this.maxDepth = other.getMaxDepth();
			this.features.clear();
			this.features.addAll(other.getFeatures());
		}
	}

	public LineScannerConfig reset() {
		this.trim = LineScannerConfig.DEFAULT_TRIM;
		this.maxDepth = LineScannerConfig.DEFAULT_MAX_DEPTH;
		this.features.clear();
		this.features.addAll(LineScannerConfig.DEFAULT_FEATURES);
		return this;
	}

	public Set<LineScannerConfig.Feature> getFeatures() {
		return this.features;
	}

	public LineScannerConfig addFeature(LineScannerConfig.Feature feature) {
		return addFeatures(Objects.requireNonNull(feature, "Must provide a non-null Feature to add"));
	}

	public LineScannerConfig removeFeature(LineScannerConfig.Feature feature) {
		return removeFeatures(Objects.requireNonNull(feature, "Must provide a non-null Feature to remove"));
	}

	public LineScannerConfig addFeatures(LineScannerConfig.Feature... features) {
		if (features != null) {
			for (LineScannerConfig.Feature f : features) {
				if (f != null) {
					this.features.add(f);
				}
			}
		}
		return this;
	}

	public LineScannerConfig removeFeatures(LineScannerConfig.Feature... features) {
		if (features != null) {
			for (LineScannerConfig.Feature f : features) {
				if (f != null) {
					this.features.remove(f);
				}
			}
		}
		return this;
	}

	public LineScannerConfig addFeatures(Collection<LineScannerConfig.Feature> features) {
		if (features != null) {
			features.stream().filter(Objects::nonNull).sequential().forEachOrdered(this.features::remove);
		}
		return this;
	}

	public LineScannerConfig removeFeatures(Collection<LineScannerConfig.Feature> features) {
		if (features != null) {
			features.stream().filter(Objects::nonNull).sequential().forEachOrdered(this.features::remove);
		}
		return this;
	}

	public LineScannerConfig setFeatures(LineScannerConfig.Feature... features) {
		this.features.clear();
		if (features == null) {
			this.features.addAll(LineScannerConfig.DEFAULT_FEATURES);
		} else {
			for (LineScannerConfig.Feature f : features) {
				if (f != null) {
					this.features.add(f);
				}
			}
		}
		return this;
	}

	public LineScannerConfig setFeatures(Collection<LineScannerConfig.Feature> features) {
		this.features.clear();
		if (features == null) {
			this.features.addAll(LineScannerConfig.DEFAULT_FEATURES);
		} else {
			features.stream().filter(Objects::nonNull).sequential().forEachOrdered(this.features::add);
		}
		return this;
	}

	public boolean hasFeature(LineScannerConfig.Feature f) {
		if (f == null) { throw new IllegalArgumentException("Must provide a non-null feature"); }
		return this.features.contains(f);
	}

	public LineScannerConfig.Trim getTrim() {
		return this.trim;
	}

	public LineScannerConfig setTrim(LineScannerConfig.Trim trim) {
		this.trim = Tools.coalesce(trim, LineScannerConfig.DEFAULT_TRIM);
		return this;
	}

	public int getMaxDepth() {
		return this.maxDepth;
	}

	public LineScannerConfig setMaxDepth(Integer maxDepth) {
		if (maxDepth == null) {
			this.maxDepth = LineScannerConfig.INFINITE_RECURSION;
		} else {
			this.maxDepth = Math.max(LineScannerConfig.INFINITE_RECURSION, maxDepth);
		}
		return this;
	}

	public LineScannerConfig copyFrom(LineScannerConfig other) {
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
		LineScannerConfig other = LineScannerConfig.class.cast(obj);
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