package com.armedia.commons.utilities.line;

import java.util.concurrent.atomic.AtomicLong;

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

	private void run(long pos, LineIteratorConfig config) throws Exception {
		AtomicLong n = new AtomicLong(0);
		LineSource ls = null;

		n.set(0);
		ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-1.test", null);
		try (LineIterator rli = new LineIterator(LineScanner.DEFAULT_FACTORIES.values(), config, ls)) {
			System.out.printf("%n%nCONFIG[ %-3d ]: %s%n", pos, config);
			System.out.printf("Processed lines:%n");
			rli.forEachRemaining((l) -> System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l));
		}
		ls.close();
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