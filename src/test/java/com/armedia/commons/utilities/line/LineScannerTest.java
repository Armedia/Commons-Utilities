/*******************************************************************************
 * #%L
 * Armedia Commons Utilities
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
package com.armedia.commons.utilities.line;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.line.LineIteratorConfig.Feature;
import com.armedia.commons.utilities.line.LineIteratorConfig.Trim;

public class LineScannerTest {

	@Test
	public void testLineScanner() {
		new LineScanner();
	}

	@Test
	public void testGetSourceFactories() {
		LineScanner ls = new LineScanner();
		Assertions.assertEquals(new ArrayList<>(LineScanner.DEFAULT_FACTORIES.values()), ls.getSourceFactories());
	}

	@Test
	public void testAddSourceFactory() {
		LineScanner ls = new LineScanner();
		List<LineSourceFactory> f = new ArrayList<>(LineScanner.DEFAULT_FACTORIES.values());
		for (int i = 0; i < 10; i++) {
			LineSourceFactory lsf = EasyMock.createStrictMock(LineSourceFactory.class);
			f.add(i, lsf);
			ls.addSourceFactory(null);
			ls.addSourceFactory(lsf);
			EasyMock.reset(lsf);
			EasyMock.replay(lsf);
			Assertions.assertEquals(f, ls.getSourceFactories());
			EasyMock.verify(lsf);
		}
	}

	@Test
	public void testAddSourceFactories() {
		LineSourceFactory[] arr = {};
		List<LineSourceFactory> accumulated = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			LineSourceFactory lsf = EasyMock.createStrictMock(LineSourceFactory.class);
			accumulated.add(lsf);

			List<LineSourceFactory> f = new ArrayList<>(accumulated);
			LineScanner ls = new LineScanner();
			ls.addSourceFactories((Collection<LineSourceFactory>) null);
			ls.addSourceFactories(Collections.emptyList());

			ls.addSourceFactories(f);
			f.addAll(LineScanner.DEFAULT_FACTORIES.values());
			Assertions.assertEquals(f, ls.getSourceFactories());

			ls = new LineScanner();
			ls.addSourceFactories((LineSourceFactory[]) null);
			ls.addSourceFactories(arr);

			ls.addSourceFactories(f.toArray(arr));
			f.addAll(LineScanner.DEFAULT_FACTORIES.values());
			Assertions.assertEquals(f, ls.getSourceFactories());
		}
	}

	@Test
	public void testRemoveSourceFactory() {
		LineScanner ls = new LineScanner();
		ls.removeSourceFactory(null);
		List<LineSourceFactory> f = new ArrayList<>();
		Collection<LineSourceFactory> orig = ls.getSourceFactories();
		for (int i = 0; i < 10; i++) {
			LineSourceFactory lsf = EasyMock.createStrictMock(LineSourceFactory.class);
			f.add(lsf);
			ls.addSourceFactory(lsf);
		}
		for (LineSourceFactory lsf : f) {
			Assertions.assertTrue(ls.hasSourceFactory(lsf));
			ls.removeSourceFactory(lsf);
			Assertions.assertFalse(ls.hasSourceFactory(lsf));
		}
		Assertions.assertEquals(orig, ls.getSourceFactories());
	}

	@Test
	public void testRemoveSourceFactories() {
		LineSourceFactory[] arr = {};
		List<LineSourceFactory> accumulated = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			LineSourceFactory lsf = EasyMock.createStrictMock(LineSourceFactory.class);
			accumulated.add(lsf);
		}

		LineScanner ls = new LineScanner();
		Collection<LineSourceFactory> orig = ls.getSourceFactories();
		ls.addSourceFactories(accumulated);
		ls.removeSourceFactories((Collection<LineSourceFactory>) null);
		ls.removeSourceFactories(Collections.emptyList());
		Assertions.assertNotEquals(orig, ls.getSourceFactories());

		ls.removeSourceFactories(accumulated);
		Assertions.assertEquals(orig, ls.getSourceFactories());

		ls.addSourceFactories(accumulated);
		ls.removeSourceFactories((LineSourceFactory[]) null);
		ls.removeSourceFactories(arr);
		Assertions.assertNotEquals(orig, ls.getSourceFactories());

		ls.removeSourceFactories(accumulated.toArray(arr));
		Assertions.assertEquals(orig, ls.getSourceFactories());
	}

	@Test
	public void testHasSourceFactory() {
		LineScanner ls = new LineScanner();
		Assertions.assertFalse(ls.hasSourceFactory(null));
		List<LineSourceFactory> f = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			LineSourceFactory lsf = EasyMock.createStrictMock(LineSourceFactory.class);
			f.add(lsf);
			Assertions.assertFalse(ls.hasSourceFactory(lsf));
			ls.addSourceFactory(lsf);
			Assertions.assertTrue(ls.hasSourceFactory(lsf));
		}
		for (LineSourceFactory lsf : f) {
			Assertions.assertTrue(ls.hasSourceFactory(lsf));
			ls.removeSourceFactory(lsf);
			Assertions.assertFalse(ls.hasSourceFactory(lsf));
		}
	}

	@Test
	public void testIterator() {
		LineScanner ls = new LineScanner();
		LineIteratorConfig config = new LineIteratorConfig();

		LineIterator it = null;

		it = ls.iterator((Collection<String>) null);
		Assertions.assertSame(LineIterator.NULL_ITERATOR, it);

		it = ls.iterator(Collections.emptyList());
		Assertions.assertSame(LineIterator.NULL_ITERATOR, it);

		it = ls.iterator(Arrays.asList("a", "b", "c"));
		Assertions.assertNotNull(it);
		Assertions.assertEquals(ls.getSourceFactories(), it.getSourceFactories());
		Assertions.assertEquals(config, it.getConfig());

		it = ls.iterator((String[]) null);
		Assertions.assertSame(LineIterator.NULL_ITERATOR, it);

		it = ls.iterator(new String[0]);
		Assertions.assertSame(LineIterator.NULL_ITERATOR, it);

		it = ls.iterator(new String[] {
			"a", "b", "c"
		});
		Assertions.assertNotNull(it);
		Assertions.assertEquals(ls.getSourceFactories(), it.getSourceFactories());
		Assertions.assertEquals(config, it.getConfig());
	}

	@Test
	public void testIteratorWithConfig() {
		LineScanner ls = new LineScanner();

		for (Collection<Feature> f : LineIteratorConfigTest.ALL_FEATURES) {
			for (Trim trim : LineIteratorConfig.Trim.values()) {
				for (int d = 0; d < 100; d++) {
					LineIteratorConfig config = new LineIteratorConfig();
					config.setTrim(trim);
					config.setFeatures(f);
					config.setMaxDepth(d);

					LineIterator it = null;

					it = ls.iterator(config, (Collection<String>) null);
					Assertions.assertSame(LineIterator.NULL_ITERATOR, it);

					it = ls.iterator(config, Collections.emptyList());
					Assertions.assertSame(LineIterator.NULL_ITERATOR, it);

					it = ls.iterator(config, Arrays.asList("a", "b", "c"));
					Assertions.assertNotNull(it);
					Assertions.assertEquals(ls.getSourceFactories(), it.getSourceFactories());
					Assertions.assertEquals(config, it.getConfig());

					it = ls.iterator(config, (String[]) null);
					Assertions.assertSame(LineIterator.NULL_ITERATOR, it);

					it = ls.iterator(config, new String[0]);
					Assertions.assertSame(LineIterator.NULL_ITERATOR, it);

					it = ls.iterator(config, new String[] {
						"a", "b", "c"
					});
					Assertions.assertNotNull(it);
					Assertions.assertEquals(ls.getSourceFactories(), it.getSourceFactories());
					Assertions.assertEquals(config, it.getConfig());
				}
			}
		}
	}
}