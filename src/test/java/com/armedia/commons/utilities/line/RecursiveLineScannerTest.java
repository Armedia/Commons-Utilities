package com.armedia.commons.utilities.line;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;

import com.armedia.commons.utilities.line.LineScanner.Trim;

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

	@Test
	public void test() throws Exception {
		AtomicLong n = new AtomicLong(0);
		RecursiveLineScanner rls = null;

		LineSource ls = null;

		n.set(0);
		ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-1.test", null);
		System.out.printf("Raw lines:%n");
		for (String l : ls.load()) {
			System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l);
		}
		ls.close();

		n.set(0);
		rls = new RecursiveLineScanner(LineScanner.DEFAULT_FACTORIES.values(), Trim.NONE, -1, true);
		ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-1.test", null);
		System.out.printf("Processed lines:%n");
		rls.process((l) -> {
			System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l);
			return true;
		}, ls);
		ls.close();

		n.set(0);
		rls = new RecursiveLineScanner(LineScanner.DEFAULT_FACTORIES.values(), Trim.LEADING, -1, true);
		ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-1.test", null);
		System.out.printf("Processed lines:%n");
		rls.process((l) -> {
			System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l);
			return true;
		}, ls);
		ls.close();

		n.set(0);
		rls = new RecursiveLineScanner(LineScanner.DEFAULT_FACTORIES.values(), Trim.TRAILING, -1, true);
		ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-1.test", null);
		System.out.printf("Processed lines:%n");
		rls.process((l) -> {
			System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l);
			return true;
		}, ls);
		ls.close();
		n.set(0);

		rls = new RecursiveLineScanner(LineScanner.DEFAULT_FACTORIES.values(), Trim.BOTH, -1, true);
		ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-1.test", null);
		System.out.printf("Processed lines:%n");
		rls.process((l) -> {
			System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l);
			return true;
		}, ls);
		ls.close();

		n.set(0);
		rls = new RecursiveLineScanner(LineScanner.DEFAULT_FACTORIES.values(), Trim.NONE, -1, false);
		ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-1.test", null);
		System.out.printf("Processed lines:%n");
		rls.process((l) -> {
			System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l);
			return true;
		}, ls);
		ls.close();

		n.set(0);
		rls = new RecursiveLineScanner(LineScanner.DEFAULT_FACTORIES.values(), Trim.LEADING, -1, false);
		ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-1.test", null);
		System.out.printf("Processed lines:%n");
		rls.process((l) -> {
			System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l);
			return true;
		}, ls);
		ls.close();

		n.set(0);
		rls = new RecursiveLineScanner(LineScanner.DEFAULT_FACTORIES.values(), Trim.TRAILING, -1, false);
		ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-1.test", null);
		System.out.printf("Processed lines:%n");
		rls.process((l) -> {
			System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l);
			return true;
		}, ls);
		ls.close();
		n.set(0);

		rls = new RecursiveLineScanner(LineScanner.DEFAULT_FACTORIES.values(), Trim.BOTH, -1, false);
		ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-1.test", null);
		System.out.printf("Processed lines:%n");
		rls.process((l) -> {
			System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l);
			return true;
		}, ls);
		ls.close();

		n.set(0);
		rls = new RecursiveLineScanner(LineScanner.DEFAULT_FACTORIES.values(), Trim.NONE, 0, true);
		ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-1.test", null);
		System.out.printf("Processed lines:%n");
		rls.process((l) -> {
			System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l);
			return true;
		}, ls);
		ls.close();

		n.set(0);
		rls = new RecursiveLineScanner(LineScanner.DEFAULT_FACTORIES.values(), Trim.LEADING, 0, true);
		ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-1.test", null);
		System.out.printf("Processed lines:%n");
		rls.process((l) -> {
			System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l);
			return true;
		}, ls);
		ls.close();

		n.set(0);
		rls = new RecursiveLineScanner(LineScanner.DEFAULT_FACTORIES.values(), Trim.TRAILING, 0, true);
		ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-1.test", null);
		System.out.printf("Processed lines:%n");
		rls.process((l) -> {
			System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l);
			return true;
		}, ls);
		ls.close();
		n.set(0);

		rls = new RecursiveLineScanner(LineScanner.DEFAULT_FACTORIES.values(), Trim.BOTH, 0, true);
		ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-1.test", null);
		System.out.printf("Processed lines:%n");
		rls.process((l) -> {
			System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l);
			return true;
		}, ls);
		ls.close();

		n.set(0);
		rls = new RecursiveLineScanner(LineScanner.DEFAULT_FACTORIES.values(), Trim.NONE, 0, false);
		ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-1.test", null);
		System.out.printf("Processed lines:%n");
		rls.process((l) -> {
			System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l);
			return true;
		}, ls);
		ls.close();

		n.set(0);
		rls = new RecursiveLineScanner(LineScanner.DEFAULT_FACTORIES.values(), Trim.LEADING, 0, false);
		ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-1.test", null);
		System.out.printf("Processed lines:%n");
		rls.process((l) -> {
			System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l);
			return true;
		}, ls);
		ls.close();

		n.set(0);
		rls = new RecursiveLineScanner(LineScanner.DEFAULT_FACTORIES.values(), Trim.TRAILING, 0, false);
		ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-1.test", null);
		System.out.printf("Processed lines:%n");
		rls.process((l) -> {
			System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l);
			return true;
		}, ls);
		ls.close();
		n.set(0);

		rls = new RecursiveLineScanner(LineScanner.DEFAULT_FACTORIES.values(), Trim.BOTH, 0, false);
		ls = new ResourceLineSourceFactory().newInstance("classpath:/lines-1.test", null);
		System.out.printf("Processed lines:%n");
		rls.process((l) -> {
			System.out.printf("\t[%-4d]: [%s]%n", n.incrementAndGet(), l);
			return true;
		}, ls);
		ls.close();
	}

}