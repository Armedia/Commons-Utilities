package com.armedia.commons.utilities.line;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;

import com.armedia.commons.utilities.line.LineScannerConfig.Feature;
import com.armedia.commons.utilities.line.LineScannerConfig.Trim;

public class RecursiveLineScannerTest {

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
			Assert.assertTrue(data[i], ((i % 2) != 0) == RecursiveLineScanner.CONTINUATION.matcher(data[i]).find());
		}
	}

	private void run(LineScannerConfig config) throws Exception {
		AtomicLong n = new AtomicLong(0);
		RecursiveLineScanner rls = null;
		LineSource ls = null;

		n.set(0);
		rls = new RecursiveLineScanner(LineScanner.DEFAULT_FACTORIES.values(), config);
		ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-1.test", null);
		System.out.printf("CONFIG: %s%n", config);
		System.out.printf("Processed lines:%n");
		rls.process((l) -> {
			System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l);
			return true;
		}, ls);
		ls.close();
	}

	@Test
	public void test() throws Exception {

		final LineScannerConfig config = new LineScannerConfig();

		config //
			.reset()//
			.setTrim(Trim.NONE) //
			.setMaxDepth(-1) //
			.setFeatures(Feature.COMMENTS, Feature.CONTINUATION, Feature.IGNORE_EMPTY_LINES, Feature.RECURSION) //
		;
		run(config);

		config //
			.reset()//
			.setTrim(Trim.LEADING) //
			.setMaxDepth(-1) //
			.setFeatures(Feature.COMMENTS, Feature.CONTINUATION, Feature.IGNORE_EMPTY_LINES, Feature.RECURSION) //
		;
		run(config);

		config //
			.reset()//
			.setTrim(Trim.TRAILING) //
			.setMaxDepth(-1) //
			.setFeatures(Feature.COMMENTS, Feature.CONTINUATION, Feature.IGNORE_EMPTY_LINES, Feature.RECURSION) //
		;
		run(config);

		config //
			.reset()//
			.setTrim(Trim.BOTH) //
			.setMaxDepth(-1) //
			.setFeatures(Feature.COMMENTS, Feature.CONTINUATION, Feature.IGNORE_EMPTY_LINES, Feature.RECURSION) //
		;
		run(config);

		config //
			.reset()//
			.setTrim(Trim.NONE) //
			.setMaxDepth(-1) //
			.setFeatures() //
		;
		run(config);

		config //
			.reset()//
			.setTrim(Trim.NONE) //
			.setMaxDepth(-1) //
			.setFeatures(Feature.COMMENTS) //
		;
		run(config);

		config //
			.reset()//
			.setTrim(Trim.NONE) //
			.setMaxDepth(-1) //
			.setFeatures(Feature.CONTINUATION) //
		;
		run(config);

		config //
			.reset()//
			.setTrim(Trim.NONE) //
			.setMaxDepth(-1) //
			.setFeatures(Feature.COMMENTS, Feature.CONTINUATION) //
		;
		run(config);

		config //
			.reset()//
			.setTrim(Trim.NONE) //
			.setMaxDepth(-1) //
			.setFeatures(Feature.IGNORE_EMPTY_LINES) //
		;
		run(config);

		config //
			.reset()//
			.setTrim(Trim.NONE) //
			.setMaxDepth(-1) //
			.setFeatures(Feature.COMMENTS, Feature.IGNORE_EMPTY_LINES) //
		;
		run(config);

		config //
			.reset()//
			.setTrim(Trim.NONE) //
			.setMaxDepth(-1) //
			.setFeatures(Feature.CONTINUATION, Feature.IGNORE_EMPTY_LINES) //
		;
		run(config);

		config //
			.reset()//
			.setTrim(Trim.NONE) //
			.setMaxDepth(-1) //
			.setFeatures(Feature.COMMENTS, Feature.CONTINUATION, Feature.IGNORE_EMPTY_LINES) //
		;
		run(config);

		config //
			.reset()//
			.setTrim(Trim.NONE) //
			.setMaxDepth(-1) //
			.setFeatures(Feature.RECURSION) //
		;
		run(config);

		config //
			.reset()//
			.setTrim(Trim.NONE) //
			.setMaxDepth(-1) //
			.setFeatures(Feature.COMMENTS, Feature.RECURSION) //
		;
		run(config);

		config //
			.reset()//
			.setTrim(Trim.NONE) //
			.setMaxDepth(-1) //
			.setFeatures(Feature.CONTINUATION, Feature.RECURSION) //
		;
		run(config);

		config //
			.reset()//
			.setTrim(Trim.NONE) //
			.setMaxDepth(-1) //
			.setFeatures(Feature.COMMENTS, Feature.CONTINUATION, Feature.RECURSION) //
		;
		run(config);

		config //
			.reset()//
			.setTrim(Trim.NONE) //
			.setMaxDepth(-1) //
			.setFeatures(Feature.IGNORE_EMPTY_LINES, Feature.RECURSION) //
		;
		run(config);

		config //
			.reset()//
			.setTrim(Trim.NONE) //
			.setMaxDepth(-1) //
			.setFeatures(Feature.COMMENTS, Feature.IGNORE_EMPTY_LINES, Feature.RECURSION) //
		;
		run(config);

		config //
			.reset()//
			.setTrim(Trim.NONE) //
			.setMaxDepth(-1) //
			.setFeatures(Feature.CONTINUATION, Feature.IGNORE_EMPTY_LINES, Feature.RECURSION) //
		;
		run(config);

		config //
			.reset()//
			.setTrim(Trim.NONE) //
			.setMaxDepth(-1) //
			.setFeatures(Feature.COMMENTS, Feature.CONTINUATION, Feature.IGNORE_EMPTY_LINES, Feature.RECURSION) //
		;
		run(config);

	}

}