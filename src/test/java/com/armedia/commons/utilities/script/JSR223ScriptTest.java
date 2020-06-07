/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2020 Armedia, LLC
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
package com.armedia.commons.utilities.script;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.armedia.commons.utilities.function.CheckedConsumer;
import com.armedia.commons.utilities.script.JSR223Script.CacheKey;
import com.armedia.commons.utilities.script.JSR223Script.ScriptBindings;

public class JSR223ScriptTest {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void testGetLanguages() {
		Map<String, String> languages = JSR223Script.getLanguages();
		this.log.info("Languages");
		languages.entrySet().forEach((e) -> this.log.info("[{}] -> [{}]", e.getKey(), e.getValue()));
	}

	@Test
	public void testGetCacheKeyStringString() throws Exception {
		Map<String, String> languages = JSR223Script.getLanguages();

		List<String> data = new ArrayList<>(10);
		for (int i = 0; i < 100; i++) {
			data.add(String.format("Test-Script-%02d", i));
		}
		List<CacheKey> keys = new ArrayList<>(languages.size());
		for (String k : languages.keySet()) {
			for (String a : data) {
				CacheKey keyA = JSR223Script.getCacheKey(k, a);
				keys.add(keyA);
				Assertions.assertFalse(keyA.equals(null));
				Assertions.assertFalse(keyA.equals(a));
				Assertions.assertTrue(keyA.equals(keyA));

				for (String b : data) {
					CacheKey keyB = JSR223Script.getCacheKey(k, b);
					keys.add(keyB);

					if (a == b) {
						Assertions.assertEquals(keyA, keyB);
						Assertions.assertEquals(keyB, keyA);
						Assertions.assertEquals(keyA.hashCode(), keyB.hashCode());
						Assertions.assertEquals(keyA.getHash(), keyB.getHash());
					} else {
						Assertions.assertNotEquals(keyA, keyB);
						Assertions.assertNotEquals(keyB, keyA);
						Assertions.assertNotEquals(keyA.hashCode(), keyB.hashCode());
						Assertions.assertNotEquals(keyA.getHash(), keyB.getHash());
					}
				}
			}
		}
		keys.removeIf(JSR223Script::purge);
	}

	@Test
	public void testBuild() throws ScriptException, IOException {
		JSR223Script.Builder builder = new JSR223Script.Builder() //
			.allowCompilation(true) //
		;
		Map<String, String> languages = JSR223Script.getLanguages();

		String script = "1 + 1";

		List<CacheKey> keys = new ArrayList<>(languages.size());
		for (String language : languages.keySet()) {
			JSR223Script s1 = builder //
				.language(language) //
				.source(script).build() //
			;

			JSR223Script s2 = builder.build();

			Assertions.assertSame(s1, s2);
			keys.add(s1.getCacheKey());
		}
		keys.removeIf(JSR223Script::purge);
	}

	@Test
	public void testPurge() throws ScriptException, IOException {
		JSR223Script.Builder builder = new JSR223Script.Builder() //
			.allowCompilation(true) //
		;
		Map<String, String> languages = JSR223Script.getLanguages();

		String script = "1 + 1";

		Assertions.assertFalse(JSR223Script.purge(null));

		for (String language : languages.keySet()) {
			JSR223Script s1 = builder //
				.language(language) //
				.source(script).build() //
			;

			Assertions.assertTrue(JSR223Script.purge(s1.getCacheKey()));
			Assertions.assertFalse(JSR223Script.purge(s1.getCacheKey()));

			JSR223Script s2 = builder //
				.source(script).build() //
			;
			Assertions.assertNotSame(s1, s2);
			Assertions.assertTrue(JSR223Script.purge(s2.getCacheKey()));
			Assertions.assertFalse(JSR223Script.purge(s2.getCacheKey()));
		}
	}

	@Test
	public void testDispose() throws ScriptException, IOException {
		JSR223Script.Builder builder = new JSR223Script.Builder() //
			.allowCompilation(true) //
		;
		Map<String, String> languages = JSR223Script.getLanguages();

		String script = "1 + 1";

		for (String language : languages.keySet()) {
			JSR223Script s1 = builder //
				.language(language) //
				.source(script).build() //
			;

			Assertions.assertTrue(s1.dispose());
			Assertions.assertFalse(s1.dispose());

			JSR223Script s2 = builder //
				.source(script).build() //
			;
			Assertions.assertNotSame(s1, s2);
			Assertions.assertTrue(s2.dispose());
			Assertions.assertFalse(s2.dispose());
		}
	}

	@Test
	public void testGetInstanceString() throws Exception {
		Assertions.assertNull(JSR223Script.getInstance(null));

		JSR223Script.Builder builder = new JSR223Script.Builder() //
			.allowCompilation(true) //
		;
		Map<String, String> languages = JSR223Script.getLanguages();

		String scriptA = "1 + 1";
		String scriptB = "2 + 2";

		List<CacheKey> keys = new ArrayList<>(languages.size());
		for (String language : languages.keySet()) {
			JSR223Script a = builder //
				.language(language) //
				.source(scriptA).build() //
			;
			keys.add(a.getCacheKey());

			JSR223Script b = builder //
				.source(scriptB).build() //
			;
			keys.add(b.getCacheKey());

			Assertions.assertSame(a, JSR223Script.getInstance(a.getCacheKey()));
			Assertions.assertSame(b, JSR223Script.getInstance(b.getCacheKey()));
			Assertions.assertNotSame(a, JSR223Script.getInstance(b.getCacheKey()));
			Assertions.assertNotSame(b, JSR223Script.getInstance(a.getCacheKey()));
		}
		keys.removeIf(JSR223Script::purge);
	}

	@Test
	public void testGetInstancePath() throws Exception {
		Assertions.assertNull(JSR223Script.getInstance(null));

		JSR223Script.Builder builder = new JSR223Script.Builder() //
			.allowCompilation(true) //
		;
		Map<String, String> languages = JSR223Script.getLanguages();

		Charset charset = StandardCharsets.UTF_8;
		builder.charset(charset);
		final Path scriptA = Files.createTempFile("jsr223test.", ".tmp");
		final Path scriptB = Files.createTempFile("jsr223test.", ".tmp");
		Files.write(scriptA, "1 + 1".getBytes(charset));
		Files.write(scriptB, "2 + 2".getBytes(charset));

		Runnable onExit = () -> {
			try {
				Files.delete(scriptA);
			} catch (IOException e) {
				// DO nothing...
			}
			try {
				Files.delete(scriptB);
			} catch (IOException e) {
				// DO nothing...
			}
		};
		Runtime.getRuntime().addShutdownHook(new Thread(onExit));

		List<CacheKey> keys = new ArrayList<>(languages.size());
		for (String language : languages.keySet()) {
			JSR223Script a = builder //
				.language(language) //
				.source(scriptA).build() //
			;
			keys.add(a.getCacheKey());

			JSR223Script b = builder //
				.source(scriptB).build() //
			;
			keys.add(b.getCacheKey());

			Assertions.assertSame(a, JSR223Script.getInstance(a.getCacheKey()));
			Assertions.assertSame(b, JSR223Script.getInstance(b.getCacheKey()));
			Assertions.assertNotSame(a, JSR223Script.getInstance(b.getCacheKey()));
			Assertions.assertNotSame(b, JSR223Script.getInstance(a.getCacheKey()));
		}
		keys.removeIf(JSR223Script::purge);

		// Try to trigger exceptions
		for (String language : languages.keySet()) {
			// Write the files
			Files.write(scriptA, "1 + 1".getBytes(charset));
			Files.write(scriptB, "2 + 2".getBytes(charset));

			JSR223Script a = builder //
				.language(language) //
				.source(scriptA).build() //
			;
			keys.add(a.getCacheKey());

			JSR223Script b = builder //
				.source(scriptB).build() //
			;
			keys.add(b.getCacheKey());

			// Delete the files
			Files.delete(scriptA);
			Files.delete(scriptB);

			Assertions.assertThrows(IOException.class, () -> a.eval());
			Assertions.assertThrows(IOException.class, () -> b.eval());
		}
	}

	@Test
	public void testGetLanguage() throws ScriptException, IOException {
		JSR223Script.Builder builder = new JSR223Script.Builder() //
			.allowCompilation(true) //
		;
		Map<String, String> languages = JSR223Script.getLanguages();

		String script = "1 + 1";

		List<CacheKey> keys = new ArrayList<>(languages.size());
		for (String language : languages.keySet()) {
			JSR223Script s = builder //
				.language(language) //
				.source(script).build() //
			;
			keys.add(s.getCacheKey());

			Assertions.assertEquals(language, s.getLanguage());
		}
		keys.removeIf(JSR223Script::purge);
	}

	@Test
	public void testCompile() throws ScriptException, IOException {
		JSR223Script.Builder builder = new JSR223Script.Builder() //
			.language("jexl") //
		;
		String script = "1 + 1";
		boolean[] compilation = {
			false, true
		};

		for (boolean compile : compilation) {
			JSR223Script s = builder //
				.allowCompilation(compile) //
				.source(script) //
				.build() //
			;
			try {
				Assertions.assertEquals(compile, s.compile());
			} finally {
				JSR223Script.purge(s.getCacheKey());
			}
		}
	}

	@Test
	public void testNewBindings() throws ScriptException, IOException {
		JSR223Script.Builder builder = new JSR223Script.Builder() //
			.allowCompilation(true) //
		;
		Map<String, String> languages = JSR223Script.getLanguages();

		String script = "1 + 1";

		List<CacheKey> keys = new ArrayList<>(languages.size());
		for (String l1 : languages.keySet()) {
			JSR223Script s1 = builder //
				.language(l1) //
				.source(script) //
				.build() //
			;
			keys.add(s1.getCacheKey());

			ScriptBindings b1 = s1.newBindings();
			Assertions.assertNotSame(b1, s1.newBindings());

			Assertions.assertFalse(b1.isSupportedBy(null));
			Assertions.assertTrue(b1.isSupportedBy(s1));
			Assertions.assertFalse(s1.supports(null));
			Assertions.assertTrue(s1.supports(b1));

			for (String l2 : languages.keySet()) {
				JSR223Script s2 = builder //
					.language(l2) //
					.source(script) //
					.build() //
				;
				keys.add(s2.getCacheKey());

				ScriptBindings b2 = s2.newBindings();

				if (StringUtils.equals(l1, l2)) {
					Assertions.assertTrue(b1.isSupportedBy(s2));
					Assertions.assertTrue(s1.supports(b2));
					Assertions.assertTrue(b2.isSupportedBy(s1));
					Assertions.assertTrue(s2.supports(b1));
				} else {
					Assertions.assertFalse(b1.isSupportedBy(s2));
					Assertions.assertFalse(s1.supports(b2));
					Assertions.assertFalse(b2.isSupportedBy(s1));
					Assertions.assertFalse(s2.supports(b1));
				}

			}
		}
		keys.removeIf(JSR223Script::purge);
	}

	@Test
	public void testEval() throws Exception {
		JSR223Script.Builder builder = new JSR223Script.Builder() //
			.allowCompilation(true) //
		;
		Map<String, String> languages = JSR223Script.getLanguages();

		Charset charset = StandardCharsets.UTF_8;
		builder.charset(charset);
		final String sourceA = "1 + 1";
		final Path scriptA = Files.createTempFile("jsr223test.", ".tmp");
		Files.write(scriptA, sourceA.getBytes(charset));
		final String fail = "{ {][]],:] >> this is crap meant ? < to fail " + UUID.randomUUID().toString();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				Files.delete(scriptA);
			} catch (IOException e) {
				// DO nothing...
			}
		}));

		List<CacheKey> keys = new ArrayList<>(languages.size());
		for (String language : languages.keySet()) {
			JSR223Script s = builder //
				.language(language) //
				.source(sourceA) //
				.build() //
			;
			keys.add(s.getCacheKey());

			Assertions.assertEquals(Integer.valueOf(2), s.eval());

			s = builder //
				.source(scriptA) //
				.build() //
			;
			keys.add(s.getCacheKey());

			Assertions.assertEquals(Integer.valueOf(2), s.eval());

			JSR223Script c = builder //
				.source(fail) //
				.build() //
			;
			keys.add(c.getCacheKey());

			Assertions.assertThrows(ScriptException.class, () -> c.eval());

			final IOException ioe = new IOException();
			try {
				builder //
					.source(new Reader() {
						@Override
						public int read(char[] cbuf, int off, int len) throws IOException {
							throw ioe;
						}

						@Override
						public void close() throws IOException {
						}
					}) //
					.build() //
				;
			} catch (IOException e) {
				Assertions.assertSame(ioe, e);
			}
		}
		keys.removeIf(JSR223Script::purge);
	}

	@Test
	public void testEvalScriptBindings() throws Exception {
		final ScriptBindings nullBindings = null;

		JSR223Script.Builder builder = new JSR223Script.Builder() //
			.allowCompilation(true) //
		;
		Map<String, String> languages = JSR223Script.getLanguages();

		Charset charset = StandardCharsets.UTF_8;
		builder.charset(charset);
		final String sourceA = "1 + 1";
		final Path scriptA = Files.createTempFile("jsr223test.", ".tmp");
		Files.write(scriptA, sourceA.getBytes(charset));

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				Files.delete(scriptA);
			} catch (IOException e) {
				// DO nothing...
			}
		}));

		List<CacheKey> keys = new ArrayList<>(languages.size());
		for (String lang1 : languages.keySet()) {
			JSR223Script s1 = builder //
				.language(lang1) //
				.source(sourceA) //
				.build() //
			;
			keys.add(s1.getCacheKey());

			ScriptBindings bindings1 = s1.newBindings();
			Assertions.assertEquals(Integer.valueOf(2), s1.eval(bindings1));
			Assertions.assertEquals(Integer.valueOf(2), s1.eval(nullBindings));

			for (String lang2 : languages.keySet()) {
				JSR223Script s2 = builder //
					.language(lang2) //
					.source(sourceA) //
					.build() //
				;
				keys.add(s2.getCacheKey());
				ScriptBindings bindings2 = s2.newBindings();
				if (StringUtils.equals(lang1, lang2)) {
					Assertions.assertEquals(s1.eval(bindings2), s2.eval(bindings1));
				} else {
					Assertions.assertThrows(ScriptException.class, () -> s2.eval(bindings1));
					Assertions.assertThrows(ScriptException.class, () -> s1.eval(bindings2));
				}
			}
		}
		keys.removeIf(JSR223Script::purge);
		keys.clear();
		builder.allowCompilation(false);
		for (String lang1 : languages.keySet()) {
			JSR223Script s1 = builder //
				.language(lang1) //
				.source(sourceA) //
				.build() //
			;
			keys.add(s1.getCacheKey());

			ScriptBindings bindings1 = s1.newBindings();
			Assertions.assertEquals(Integer.valueOf(2), s1.eval(bindings1));
			Assertions.assertEquals(Integer.valueOf(2), s1.eval(nullBindings));

			for (String lang2 : languages.keySet()) {
				JSR223Script s2 = builder //
					.language(lang2) //
					.source(sourceA) //
					.build() //
				;
				keys.add(s2.getCacheKey());
				ScriptBindings bindings2 = s2.newBindings();
				if (StringUtils.equals(lang1, lang2)) {
					Assertions.assertEquals(s1.eval(bindings2), s2.eval(bindings1));
				} else {
					Assertions.assertThrows(ScriptException.class, () -> s2.eval(bindings1));
					Assertions.assertThrows(ScriptException.class, () -> s1.eval(bindings2));
				}
			}
		}
		keys.removeIf(JSR223Script::purge);
	}

	@Test
	public void testEvalConsumerOfBindings() throws Exception {
		final Consumer<Bindings> consumer = EasyMock.createStrictMock(Consumer.class);
		final Consumer<Bindings> nullConsumer = null;

		JSR223Script.Builder builder = new JSR223Script.Builder() //
			.allowCompilation(true) //
		;
		Map<String, String> languages = JSR223Script.getLanguages();

		Charset charset = StandardCharsets.UTF_8;
		builder.charset(charset);
		final String sourceA = "1 + 1";
		final Path scriptA = Files.createTempFile("jsr223test.", ".tmp");
		Files.write(scriptA, sourceA.getBytes(charset));

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				Files.delete(scriptA);
			} catch (IOException e) {
				// DO nothing...
			}
		}));

		List<CacheKey> keys = new ArrayList<>(languages.size());
		for (String language : languages.keySet()) {
			JSR223Script s = builder //
				.language(language) //
				.source(sourceA) //
				.build() //
			;
			keys.add(s.getCacheKey());

			EasyMock.reset(consumer);
			consumer.accept(EasyMock.anyObject(Bindings.class));
			EasyMock.expectLastCall().once();
			EasyMock.replay(consumer);
			Assertions.assertEquals(Integer.valueOf(2), s.eval(consumer));
			EasyMock.verify(consumer);

			EasyMock.reset(consumer);
			consumer.accept(EasyMock.anyObject(Bindings.class));
			EasyMock.expectLastCall().once();
			EasyMock.replay(consumer);
			Assertions.assertEquals(Integer.valueOf(2), s.eval(consumer));
			EasyMock.verify(consumer);

			EasyMock.reset(consumer);
			EasyMock.replay(consumer);
			Assertions.assertEquals(Integer.valueOf(2), s.eval(nullConsumer));
			EasyMock.verify(consumer);

			s = builder //
				.source(scriptA) //
				.build() //
			;
			keys.add(s.getCacheKey());

			EasyMock.reset(consumer);
			consumer.accept(EasyMock.anyObject(Bindings.class));
			EasyMock.expectLastCall().once();
			EasyMock.replay(consumer);
			Assertions.assertEquals(Integer.valueOf(2), s.eval(consumer));
			EasyMock.verify(consumer);

			EasyMock.reset(consumer);
			consumer.accept(EasyMock.anyObject(Bindings.class));
			EasyMock.expectLastCall().once();
			EasyMock.replay(consumer);
			Assertions.assertEquals(Integer.valueOf(2), s.eval(consumer));
			EasyMock.verify(consumer);

			EasyMock.reset(consumer);
			EasyMock.replay(consumer);
			Assertions.assertEquals(Integer.valueOf(2), s.eval(nullConsumer));
			EasyMock.verify(consumer);
		}
		keys.removeIf(JSR223Script::purge);
	}

	@Test
	public void testEvalCheckedConsumerOfBindingsEX() throws Exception {
		final CheckedConsumer<Bindings, SQLException> consumer = EasyMock.createStrictMock(CheckedConsumer.class);
		final CheckedConsumer<Bindings, SQLException> nullConsumer = null;

		JSR223Script.Builder builder = new JSR223Script.Builder() //
			.allowCompilation(true) //
		;
		Map<String, String> languages = JSR223Script.getLanguages();

		Charset charset = StandardCharsets.UTF_8;
		builder.charset(charset);
		final String sourceA = "1 + 1";
		final Path scriptA = Files.createTempFile("jsr223test.", ".tmp");
		Files.write(scriptA, sourceA.getBytes(charset));

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				Files.delete(scriptA);
			} catch (IOException e) {
				// DO nothing...
			}
		}));

		List<CacheKey> keys = new ArrayList<>(languages.size());
		for (String language : languages.keySet()) {
			JSR223Script s = builder //
				.language(language) //
				.source(sourceA) //
				.build() //
			;
			keys.add(s.getCacheKey());

			SQLException e = new SQLException();

			EasyMock.reset(consumer);
			consumer.acceptChecked(EasyMock.anyObject(Bindings.class));
			EasyMock.expectLastCall().once();
			EasyMock.replay(consumer);
			Assertions.assertEquals(Integer.valueOf(2), s.eval(consumer));
			EasyMock.verify(consumer);

			EasyMock.reset(consumer);
			consumer.acceptChecked(EasyMock.anyObject(Bindings.class));
			EasyMock.expectLastCall().andThrow(e).once();
			EasyMock.replay(consumer);
			try {
				Assertions.assertEquals(Integer.valueOf(2), s.eval(consumer));
				Assertions.fail("Should have failed with an exception");
			} catch (SQLException ex) {
				Assertions.assertSame(ex, e);
			}
			EasyMock.verify(consumer);

			EasyMock.reset(consumer);
			EasyMock.replay(consumer);
			Assertions.assertEquals(Integer.valueOf(2), s.eval(nullConsumer));
			EasyMock.verify(consumer);
			s.dispose();

			s = builder //
				.source(scriptA) //
				.build() //
			;
			keys.add(s.getCacheKey());

			EasyMock.reset(consumer);
			consumer.acceptChecked(EasyMock.anyObject(Bindings.class));
			EasyMock.expectLastCall().once();
			EasyMock.replay(consumer);
			Assertions.assertEquals(Integer.valueOf(2), s.eval(consumer));
			EasyMock.verify(consumer);

			EasyMock.reset(consumer);
			consumer.acceptChecked(EasyMock.anyObject(Bindings.class));
			EasyMock.expectLastCall().andThrow(e).once();
			EasyMock.replay(consumer);
			try {
				Assertions.assertEquals(Integer.valueOf(2), s.eval(consumer));
				Assertions.fail("Should have failed with an exception");
			} catch (SQLException ex) {
				Assertions.assertSame(ex, e);
			}
			EasyMock.verify(consumer);

			EasyMock.reset(consumer);
			EasyMock.replay(consumer);
			Assertions.assertEquals(Integer.valueOf(2), s.eval(nullConsumer));
			EasyMock.verify(consumer);
		}
		keys.removeIf(JSR223Script::purge);
	}

	@Test
	public void testEvalScriptContext() throws Exception {
		final ScriptContext nullContext = null;

		JSR223Script.Builder builder = new JSR223Script.Builder() //
			.allowCompilation(true) //
		;
		Map<String, String> languages = JSR223Script.getLanguages();

		Charset charset = StandardCharsets.UTF_8;
		builder.charset(charset);
		final String sourceA = "1 + 1";
		final Path scriptA = Files.createTempFile("jsr223test.", ".tmp");
		Files.write(scriptA, sourceA.getBytes(charset));

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				Files.delete(scriptA);
			} catch (IOException e) {
				// DO nothing...
			}
		}));

		List<CacheKey> keys = new ArrayList<>(languages.size());
		for (String lang1 : languages.keySet()) {
			JSR223Script s1 = builder //
				.language(lang1) //
				.source(sourceA) //
				.build() //
			;
			keys.add(s1.getCacheKey());

			ScriptContext ctx1 = new SimpleScriptContext();
			Assertions.assertEquals(Integer.valueOf(2), s1.eval(ctx1));
			Assertions.assertEquals(Integer.valueOf(2), s1.eval(nullContext));
		}
		keys.removeIf(JSR223Script::purge);
		keys.clear();
		builder.allowCompilation(false);
		for (String lang1 : languages.keySet()) {
			JSR223Script s1 = builder //
				.language(lang1) //
				.source(sourceA) //
				.build() //
			;
			keys.add(s1.getCacheKey());

			ScriptContext ctx1 = new SimpleScriptContext();
			Assertions.assertEquals(Integer.valueOf(2), s1.eval(ctx1));
			Assertions.assertEquals(Integer.valueOf(2), s1.eval(nullContext));
		}
		keys.removeIf(JSR223Script::purge);
	}

	@Test
	public void testBuilder() throws Exception {
		JSR223Script s = null;

		final JSR223Script.Builder builder = new JSR223Script.Builder("jexl");

		Map<String, String> languages = JSR223Script.getLanguages();
		for (String l : languages.keySet()) {
			Assertions.assertSame(builder, builder.language(l));
			Assertions.assertEquals(l, builder.language());

			// Make sure the constructor works as well
			new JSR223Script.Builder(l);
		}

		builder.language("jexl");

		// Ensure it explodes if there's no source set
		Assertions.assertThrows(NullPointerException.class, () -> builder.build());

		builder.charset(null);
		Assertions.assertNull(builder.charset());
		for (Charset C : Charset.availableCharsets().values()) {
			Assertions.assertSame(builder, builder.charset(C));
			Assertions.assertSame(C, builder.charset());
		}

		Charset cs = StandardCharsets.UTF_16LE;
		builder.charset(cs);
		final Integer result = Integer.valueOf(2);
		final String sourceA = "1 + 1";
		final Path scriptA = Files.createTempFile("jsr223test.", ".tmp");
		FileUtils.write(scriptA.toFile(), sourceA, cs);
		for (int i = 0; i < 2; i++) {
			final boolean allowCompilation = ((i % 2) == 0);

			Assertions.assertSame(builder, builder.allowCompilation(allowCompilation));
			Assertions.assertEquals(allowCompilation, builder.allowCompilation());

			s = builder.source(sourceA).build();
			Assertions.assertEquals(result, s.eval());
			s.dispose();

			s = builder.source(scriptA).build();
			Assertions.assertEquals(result, s.eval());
			s.dispose();

			Assertions.assertSame(builder, builder.source((File) null));
			s = builder.source(scriptA.toFile()).build();
			Assertions.assertEquals(result, s.eval());
			s.dispose();

			try (InputStream in = Files.newInputStream(scriptA)) {
				s = builder.source(in).build();
			}
			Assertions.assertEquals(result, s.eval());
			s.dispose();

			try (Reader r = Files.newBufferedReader(scriptA, cs)) {
				s = builder.source(r).build();
			}
			Assertions.assertEquals(result, s.eval());
			s.dispose();
		}

		// Test the unhappiest path: bad language
	}
}
