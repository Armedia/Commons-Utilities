package com.armedia.commons.utilities.line;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.Tools;
import com.armedia.commons.utilities.line.LineIteratorConfig.Feature;
import com.armedia.commons.utilities.line.LineIteratorConfig.Trim;

public class LineIteratorConfigTest {

	static final Collection<Set<Feature>> ALL_FEATURES;
	static final int FEATURE_MASK;
	static final Map<Integer, Set<Feature>> BIT_FEATURES;

	static Set<Feature> getFeatureCombination(int bits) {
		return LineIteratorConfigTest.BIT_FEATURES.get(bits & LineIteratorConfigTest.FEATURE_MASK);
	}

	static {
		Collection<Set<Feature>> allFeatures = new ArrayList<>();
		Map<Integer, Set<Feature>> bitFeatures = new LinkedHashMap<>();
		Feature[] values = Feature.values();
		for (int i = 0; i < (1 << values.length); i++) {
			Set<Feature> features = EnumSet.noneOf(Feature.class);
			for (Feature f : values) {
				int bit = (1 << f.ordinal());
				if ((i & bit) != 0) {
					features.add(f);
				}
			}
			features = Tools.freezeSet(features);
			allFeatures.add(features);
			bitFeatures.put(i, features);
		}
		ALL_FEATURES = Tools.freezeCollection(allFeatures);
		BIT_FEATURES = Tools.freezeMap(bitFeatures);
		FEATURE_MASK = ((1 << values.length) - 1);
	}

	@Test
	public void testConstructors() {
		LineIteratorConfig cfg = null;

		cfg = new LineIteratorConfig();
		Assertions.assertEquals(LineIteratorConfig.DEFAULT_FEATURES, cfg.getFeatures());

		cfg = new LineIteratorConfig(null);
		Assertions.assertEquals(LineIteratorConfig.DEFAULT_FEATURES, cfg.getFeatures());

		for (Collection<Feature> f : LineIteratorConfigTest.ALL_FEATURES) {
			for (Trim trim : LineIteratorConfig.Trim.values()) {
				for (int d = 0; d < 100; d++) {
					LineIteratorConfig other = new LineIteratorConfig();
					other.setTrim(trim);
					other.setFeatures(f);
					other.setMaxDepth(d);

					cfg = new LineIteratorConfig(other);
					Assertions.assertSame(other.getTrim(), cfg.getTrim());
					Assertions.assertEquals(other.getFeatures(), cfg.getFeatures());
					Assertions.assertEquals(other.getMaxDepth(), cfg.getMaxDepth());
				}
			}
		}
	}

	@Test
	public void testAddFeature() {
		LineIteratorConfig cfg = new LineIteratorConfig();
		cfg.setFeatures(Collections.emptyList());
		for (Feature f : LineIteratorConfigTest.getFeatureCombination(0b111111)) {
			Assertions.assertFalse(cfg.hasFeature(f));
			cfg.addFeature(f);
			Assertions.assertTrue(cfg.hasFeature(f));
		}
	}

	@Test
	public void testAddFeatures() {
		LineIteratorConfig cfg = new LineIteratorConfig();
		for (Feature base : Feature.values()) {
			for (Set<Feature> baseFeatures : LineIteratorConfigTest.ALL_FEATURES) {
				Set<Feature> features = (baseFeatures.isEmpty() ? EnumSet.noneOf(Feature.class)
					: EnumSet.copyOf(baseFeatures));
				cfg.setFeatures(Collections.emptyList());
				cfg.addFeature(base);
				for (Feature f : Feature.values()) {
					Assertions.assertEquals(base == f, cfg.hasFeature(f),
						String.format("%s from %s (on %s)", f.name(), features, base.name()));
				}
				cfg.addFeatures(features);
				features.add(base);
				for (Feature f : Feature.values()) {
					Assertions.assertEquals(features.contains(f), cfg.hasFeature(f),
						String.format("%s from %s (on %s)", f.name(), features, base.name()));
				}
			}
		}
		Feature[] features = {
			null
		};
		cfg.addFeatures(features);
		cfg.addFeatures((Feature[]) null);
		cfg.addFeatures((Collection<Feature>) null);
	}

	@Test
	public void testCopyFrom() {
		LineIteratorConfig cfg = new LineIteratorConfig();

		for (Collection<Feature> f : LineIteratorConfigTest.ALL_FEATURES) {
			for (Trim trim : LineIteratorConfig.Trim.values()) {
				for (int d = 0; d < 100; d++) {
					LineIteratorConfig other = new LineIteratorConfig();
					other.setTrim(trim);
					other.setFeatures(f);
					other.setMaxDepth(d);

					cfg.reset();
					Assertions.assertSame(LineIteratorConfig.DEFAULT_TRIM, cfg.getTrim());
					Assertions.assertEquals(LineIteratorConfig.DEFAULT_FEATURES, cfg.getFeatures());
					Assertions.assertEquals(LineIteratorConfig.DEFAULT_MAX_DEPTH, cfg.getMaxDepth());
					cfg.copyFrom(other);
					Assertions.assertSame(other.getTrim(), cfg.getTrim());
					Assertions.assertEquals(other.getFeatures(), cfg.getFeatures());
					Assertions.assertEquals(other.getMaxDepth(), cfg.getMaxDepth());
					cfg.copyFrom(null);
					Assertions.assertSame(LineIteratorConfig.DEFAULT_TRIM, cfg.getTrim());
					Assertions.assertEquals(LineIteratorConfig.DEFAULT_FEATURES, cfg.getFeatures());
					Assertions.assertEquals(LineIteratorConfig.DEFAULT_MAX_DEPTH, cfg.getMaxDepth());
				}
			}
		}
	}

	@Test
	public void testEquals() {
		List<LineIteratorConfig> A = new LinkedList<>();
		List<LineIteratorConfig> B = new LinkedList<>();
		for (Collection<Feature> f : LineIteratorConfigTest.ALL_FEATURES) {
			for (Trim trim : LineIteratorConfig.Trim.values()) {
				for (int d = 0; d < 5; d++) {
					LineIteratorConfig cfg = new LineIteratorConfig();
					cfg.setTrim(trim);
					cfg.setFeatures(f);
					cfg.setMaxDepth(d);
					A.add(cfg);
					B.add(new LineIteratorConfig(cfg));
				}
			}
		}

		for (int a = 0; a < A.size(); a++) {
			LineIteratorConfig cfgA = A.get(a);
			Assertions.assertFalse(cfgA.equals(null));
			Assertions.assertTrue(cfgA.equals(cfgA));
			Assertions.assertFalse(cfgA.equals(new Object()));
			for (int b = 0; b < B.size(); b++) {
				LineIteratorConfig cfgB = B.get(b);
				if (a == b) {
					Assertions.assertEquals(cfgA, cfgB);
					Assertions.assertEquals(cfgA.hashCode(), cfgB.hashCode());
				} else {
					Assertions.assertNotEquals(cfgA, cfgB);
					Assertions.assertNotEquals(cfgA.hashCode(), cfgB.hashCode());
				}
			}
		}
	}

	@Test
	public void testHasFeature() {
		LineIteratorConfig cfg = new LineIteratorConfig();
		Assertions.assertThrows(NullPointerException.class, () -> cfg.hasFeature(null));
		for (Collection<Feature> features : LineIteratorConfigTest.ALL_FEATURES) {
			cfg.setFeatures(features);
			for (Feature f : Feature.values()) {
				Assertions.assertEquals(features.contains(f), cfg.hasFeature(f));
			}
		}
	}

	@Test
	public void testRemoveFeature() {
		LineIteratorConfig cfg = new LineIteratorConfig();
		cfg.setFeatures(LineIteratorConfigTest.getFeatureCombination(0b11111));
		for (Feature f : Feature.values()) {
			Assertions.assertTrue(cfg.hasFeature(f));
			cfg.removeFeature(f);
			Assertions.assertFalse(cfg.hasFeature(f));
		}
	}

	@Test
	public void testRemoveFeatures() {
		LineIteratorConfig cfg = new LineIteratorConfig();
		Set<Feature> allFeatures = EnumSet.allOf(Feature.class);
		for (Feature base : Feature.values()) {
			for (Set<Feature> baseFeatures : LineIteratorConfigTest.ALL_FEATURES) {
				Set<Feature> features = (baseFeatures.isEmpty() ? EnumSet.noneOf(Feature.class)
					: EnumSet.copyOf(baseFeatures));
				cfg.setFeatures(allFeatures);
				cfg.removeFeature(base);
				for (Feature f : Feature.values()) {
					Assertions.assertEquals(base != f, cfg.hasFeature(f),
						String.format("%s from %s (on %s)", f.name(), features, base.name()));
				}
				cfg.removeFeatures(features);
				features.add(base);
				for (Feature f : Feature.values()) {
					Assertions.assertNotEquals(features.contains(f), cfg.hasFeature(f),
						String.format("%s from %s (on %s)", f.name(), features, base.name()));
				}
			}
		}
		Feature[] features = {
			null
		};
		cfg.removeFeatures(features);
		cfg.removeFeatures((Feature[]) null);
		cfg.removeFeatures((Collection<Feature>) null);
	}

	@Test
	public void testReset() {
		LineIteratorConfig cfg = new LineIteratorConfig(null);
		for (Collection<Feature> f : LineIteratorConfigTest.ALL_FEATURES) {
			for (Trim trim : LineIteratorConfig.Trim.values()) {
				for (int d = -1; d < 10; d++) {
					cfg.setTrim(trim);
					cfg.setFeatures(f);
					cfg.setMaxDepth(d);
					Assertions.assertSame(trim, cfg.getTrim());
					Assertions.assertEquals(f, cfg.getFeatures());
					Assertions.assertEquals(d, cfg.getMaxDepth());

					cfg.reset();
					Assertions.assertSame(LineIteratorConfig.DEFAULT_TRIM, cfg.getTrim());
					Assertions.assertEquals(LineIteratorConfig.DEFAULT_FEATURES, cfg.getFeatures());
					Assertions.assertEquals(LineIteratorConfig.DEFAULT_MAX_DEPTH, cfg.getMaxDepth());
				}
			}
		}
	}

	@Test
	public void testSetFeatures() {
		Feature[] F = {
			null
		};
		LineIteratorConfig cfg = new LineIteratorConfig();
		for (Collection<Feature> f : LineIteratorConfigTest.ALL_FEATURES) {
			cfg.setFeatures(f);
			Assertions.assertEquals(f, cfg.getFeatures());

			cfg.setFeatures((Collection<Feature>) null);
			Assertions.assertEquals(LineIteratorConfig.DEFAULT_FEATURES, cfg.getFeatures());

			cfg.setFeatures(f.toArray(F));
			Assertions.assertEquals(f, cfg.getFeatures());

			cfg.setFeatures((Feature[]) null);
			Assertions.assertEquals(LineIteratorConfig.DEFAULT_FEATURES, cfg.getFeatures());
		}
	}

	@Test
	public void testSetMaxDepth() {
		LineIteratorConfig cfg = new LineIteratorConfig();
		cfg.setMaxDepth(null);
		Assertions.assertEquals(LineIteratorConfig.INFINITE_RECURSION, cfg.getMaxDepth());
		for (int i = -100; i < 100; i++) {
			cfg.setMaxDepth(i);
			if (i < 0) {
				Assertions.assertEquals(LineIteratorConfig.INFINITE_RECURSION, cfg.getMaxDepth());
			} else {
				Assertions.assertEquals(i, cfg.getMaxDepth());
			}
		}
	}

	@Test
	public void testSetTrim() {
		LineIteratorConfig cfg = new LineIteratorConfig();
		cfg.setTrim(null);
		Assertions.assertSame(LineIteratorConfig.DEFAULT_TRIM, cfg.getTrim());
		for (Trim t : Trim.values()) {
			cfg.setTrim(t);
			Assertions.assertSame(t, cfg.getTrim());
		}
	}

	@Test
	public void testToString() {
		new LineIteratorConfig().toString();
	}

	@Test
	public void testTrim() {
		String none = "abc";
		String left = "    abc";
		String right = "abc    ";
		String both = "    abc    ";

		Assertions.assertEquals(left, Trim.TRAILING.apply(both));
		Assertions.assertEquals(right, Trim.LEADING.apply(both));
		Assertions.assertEquals(none, Trim.BOTH.apply(both));
		Assertions.assertEquals(both, Trim.NONE.apply(both));
	}
}