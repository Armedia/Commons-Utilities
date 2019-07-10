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
package com.armedia.commons.utilities.line;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.line.LineIteratorConfig.Feature;
import com.armedia.commons.utilities.line.LineIteratorConfig.Trim;

public class LineIteratorTest {

	@Test
	public void patternTest() throws Exception {
		String[] data = {
			"abcde ", //
			"abcde \\", //
			"abcde \\\\", //
			"abcde \\\\\\", //
			"abcde \\\\\\\\", //
			"abcde \\\\\\\\\\", //
		};
		for (int i = 0; i < data.length; i++) {
			Assertions.assertTrue(((i % 2) != 0) == LineIterator.CONTINUATION.matcher(data[i]).find(), data[i]);
		}
	}

	@Test
	public void testConstructors() throws Exception {
		final LineIteratorConfig cfg = new LineIteratorConfig();
		final Collection<String> c = Collections.emptyList();
		final LineSource ls = new LineSource("abc") {
			@Override
			public Iterable<String> load() throws LineSourceException {
				return c;
			}
		};
		final Collection<LineSourceFactory> f = Collections.emptyList();

		try (LineIterator it = new LineIterator(null, null, (LineSource) null)) {
		}
		try (LineIterator it = new LineIterator(null, null, ls)) {
		}
		try (LineIterator it = new LineIterator(null, cfg, (LineSource) null)) {
		}
		try (LineIterator it = new LineIterator(null, cfg, ls)) {
		}
		try (LineIterator it = new LineIterator(f, null, (LineSource) null)) {
		}
		try (LineIterator it = new LineIterator(f, null, ls)) {
		}
		try (LineIterator it = new LineIterator(f, cfg, (LineSource) null)) {
		}
		try (LineIterator it = new LineIterator(f, cfg, ls)) {
		}

		try (LineIterator it = new LineIterator(null, null, (Iterable<String>) null)) {
		}
		try (LineIterator it = new LineIterator(null, null, c)) {
		}
		try (LineIterator it = new LineIterator(null, cfg, (Iterable<String>) null)) {
		}
		try (LineIterator it = new LineIterator(null, cfg, c)) {
		}
		try (LineIterator it = new LineIterator(f, null, (Iterable<String>) null)) {
		}
		try (LineIterator it = new LineIterator(f, null, c)) {
		}
		try (LineIterator it = new LineIterator(f, cfg, (Iterable<String>) null)) {
		}
		try (LineIterator it = new LineIterator(f, cfg, c)) {
		}
	}

	@Test
	public void testGetFactories() {
		Collection<LineSourceFactory> f = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			f.add(EasyMock.createStrictMock(LineSourceFactory.class));

			EasyMock.reset(f.toArray());
			EasyMock.replay(f.toArray());
			try (LineIterator it = new LineIterator(f, null, Collections.emptyList())) {
				Assertions.assertEquals(f.size(), it.getSourceFactories().size());
				Assertions.assertEquals(f, it.getSourceFactories());
			}
			EasyMock.verify(f.toArray());
		}
	}

	@Test
	public void testGetTransformer() {
		try (LineIterator it = new LineIterator(null, null, Collections.emptyList())) {
			for (int i = 0; i < 10; i++) {
				Assertions.assertNotNull(it.getTransformer());
				Function<String, String> transformer = EasyMock.createStrictMock(Function.class);
				Assertions.assertNotSame(it.getTransformer(), transformer);
				EasyMock.reset(transformer);
				EasyMock.replay(transformer);
				it.setTransformer(transformer);
				EasyMock.verify(transformer);
				Assertions.assertSame(it.getTransformer(), transformer);
			}
		}
	}

	@Test
	public void testGetConfig() {
		for (Collection<Feature> f : LineIteratorConfigTest.ALL_FEATURES) {
			for (Trim trim : LineIteratorConfig.Trim.values()) {
				for (int d = 0; d < 100; d++) {
					LineIteratorConfig cfg = new LineIteratorConfig();
					cfg.setTrim(trim);
					cfg.setFeatures(f);
					cfg.setMaxDepth(d);
					try (LineIterator it = new LineIterator(null, cfg, Collections.emptyList())) {
						Assertions.assertNotSame(cfg, it.getConfig());
						Assertions.assertEquals(cfg, it.getConfig());
					}
				}
			}
		}
	}

	private void run(long pos, LineIteratorConfig config) throws Exception {
		AtomicLong n = new AtomicLong(0);
		n.set(0);
		try (LineSource ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-1.test", null)) {
			try (LineIterator rli = new LineIterator(LineScanner.DEFAULT_FACTORIES.values(), config, ls)) {
				System.out.printf("%n%nCONFIG[ %-3d ]: %s%n", pos, config);
				System.out.printf("Processed lines:%n");
				rli.forEachRemaining((l) -> System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l));
			}
		}
		n.set(0);
		try (LineSource ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-1.test", null)) {
			try (LineSource ls2 = new LineSource(ls.getId(), false) {
				@Override
				public Iterable<String> load() throws LineSourceException {
					return ls.load();
				}
			}) {
				try (LineIterator rli = new LineIterator(LineScanner.DEFAULT_FACTORIES.values(), config, ls2)) {
					System.out.printf("%n%nCONFIG[ %-3d ]: %s%n", pos, config);
					System.out.printf("Processed lines:%n");
					rli.forEachRemaining((l) -> System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l));
				}
			}
		}
		n.set(0);
		try (LineSource ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-1.test", null)) {
			try (LineSource ls2 = new LineSource(ls.getId(), true) {
				@Override
				public Iterable<String> load() throws LineSourceException {
					return ls.load();
				}
			}) {
				try (LineIterator rli = new LineIterator(LineScanner.DEFAULT_FACTORIES.values(), config, ls2)) {
					System.out.printf("%n%nCONFIG[ %-3d ]: %s%n", pos, config);
					System.out.printf("Processed lines:%n");
					rli.forEachRemaining((l) -> System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l));
				}
			}
		}
		if (config.hasFeature(Feature.RECURSION) && (config.getMaxDepth() > 0)) {
			n.set(0);
			try (LineSource ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-3.test", null)) {
				try (LineIterator rli = new LineIterator(LineScanner.DEFAULT_FACTORIES.values(), config, ls)) {
					System.out.printf("%n%nCONFIG[ %-3d ]: %s%n", pos, config);
					System.out.printf("Processed lines:%n");
					try {
						rli.forEachRemaining((l) -> System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l));
						Assertions.fail("Did not fail with a reference loop");
					} catch (Exception e) {
						Assertions.assertTrue(LineSourceException.class.isInstance(e.getCause()));
					}
				}
			}
			n.set(0);
			try (LineSource ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-4.test", null)) {
				Collection<LineSourceFactory> f = new LinkedList<>(LineScanner.DEFAULT_FACTORIES.values());
				f.add((s, r) -> null);
				try (LineIterator rli = new LineIterator(f, config, ls)) {
					System.out.printf("%n%nCONFIG[ %-3d ]: %s%n", pos, config);
					System.out.printf("Processed lines:%n");
					try {
						rli.forEachRemaining((l) -> System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l));
						Assertions.fail("Did not fail with a bad source reference");
					} catch (Exception e) {
						Assertions.assertTrue(LineSourceException.class.isInstance(e.getCause()));
					}
				}
			}
			n.set(0);
			try (LineSource ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-4.test", null)) {
				Collection<LineSourceFactory> f = new LinkedList<>(LineScanner.DEFAULT_FACTORIES.values());
				f.add((s, r) -> {
					throw new Exception("intentional fault");
				});
				try (LineIterator rli = new LineIterator(f, config, ls)) {
					System.out.printf("%n%nCONFIG[ %-3d ]: %s%n", pos, config);
					System.out.printf("Processed lines:%n");
					try {
						rli.forEachRemaining((l) -> System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l));
						Assertions.fail("Did not fail with a chained exception");
					} catch (Exception e) {
						Assertions.assertTrue(LineSourceException.class.isInstance(e.getCause()));
					}
				}
			}
		}
	}

	@Test
	public void test() throws Exception {

		final LineIteratorConfig config = new LineIteratorConfig();
		long pos = 0;

		config //
			.reset()//
			.setTrim(Trim.NONE) //
		;
		run(pos++, config);

		config //
			.reset()//
			.setTrim(Trim.LEADING) //
		;
		run(pos++, config);

		config //
			.reset()//
			.setTrim(Trim.TRAILING) //
		;
		run(pos++, config);

		config //
			.reset()//
			.setTrim(Trim.BOTH) //
		;
		run(pos++, config);

		config //
			.reset()//
			.setMaxDepth(0);
		run(pos++, config);

		config //
			.reset()//
			.setMaxDepth(1);
		run(pos++, config);

		config //
			.reset()//
			.setFeatures() //
		;
		run(pos++, config);

		config //
			.reset()//
			.setFeatures(Feature.COMMENTS) //
		;
		run(pos++, config);

		config //
			.reset()//
			.setFeatures(Feature.CONTINUATION) //
		;
		run(pos++, config);

		config //
			.reset()//
			.setFeatures(Feature.COMMENTS, Feature.CONTINUATION) //
		;
		run(pos++, config);

		config //
			.reset()//
			.setFeatures(Feature.IGNORE_EMPTY_LINES) //
		;
		run(pos++, config);

		config //
			.reset()//
			.setFeatures(Feature.COMMENTS, Feature.IGNORE_EMPTY_LINES) //
		;
		run(pos++, config);

		config //
			.reset()//
			.setFeatures(Feature.CONTINUATION, Feature.IGNORE_EMPTY_LINES) //
		;
		run(pos++, config);

		config //
			.reset()//
			.setFeatures(Feature.COMMENTS, Feature.CONTINUATION, Feature.IGNORE_EMPTY_LINES) //
		;
		run(pos++, config);

		config //
			.reset()//
			.setFeatures(Feature.RECURSION) //
		;
		run(pos++, config);

		config //
			.reset()//
			.setFeatures(Feature.COMMENTS, Feature.RECURSION) //
		;
		run(pos++, config);

		config //
			.reset()//
			.setFeatures(Feature.CONTINUATION, Feature.RECURSION) //
		;
		run(pos++, config);

		config //
			.reset()//
			.setFeatures(Feature.COMMENTS, Feature.CONTINUATION, Feature.RECURSION) //
		;
		run(pos++, config);

		config //
			.reset()//
			.setFeatures(Feature.IGNORE_EMPTY_LINES, Feature.RECURSION) //
		;
		run(pos++, config);

		config //
			.reset()//
			.setFeatures(Feature.COMMENTS, Feature.IGNORE_EMPTY_LINES, Feature.RECURSION) //
		;
		run(pos++, config);

		config //
			.reset()//
			.setFeatures(Feature.CONTINUATION, Feature.IGNORE_EMPTY_LINES, Feature.RECURSION) //
		;
		run(pos++, config);

		config //
			.reset()//
			.setFeatures(Feature.COMMENTS, Feature.CONTINUATION, Feature.IGNORE_EMPTY_LINES, Feature.RECURSION) //
		;
		run(pos++, config);

	}

}
