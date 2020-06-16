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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.ConcurrentInitializer;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.armedia.commons.utilities.Tools;
import com.armedia.commons.utilities.function.CheckedConsumer;
import com.armedia.commons.utilities.function.CheckedLazySupplier;
import com.armedia.commons.utilities.function.CheckedSupplier;
import com.armedia.commons.utilities.function.CheckedTools;
import com.armedia.commons.utilities.function.LazySupplier;

public final class JSR223Script {

	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
	public static final Map<String, String> LANGUAGES;
	private static final Map<String, ScriptEngineFactory> SCRIPT_ENGINE_FACTORIES;
	static {
		Map<String, String> languages = new LinkedHashMap<>();
		Map<String, ScriptEngineFactory> factories = new TreeMap<>();
		for (ScriptEngineFactory factory : new ScriptEngineManager().getEngineFactories()) {
			for (String name : factory.getNames()) {
				factories.putIfAbsent(StringUtils.lowerCase(name), factory);
			}
		}
		factories.forEach((n, f) -> languages.put(n, JSR223Script.describeLanguage(f)));
		SCRIPT_ENGINE_FACTORIES = Collections.unmodifiableMap(new LinkedHashMap<>(factories));
		LANGUAGES = Collections.unmodifiableMap(languages);
	}

	private static String describeLanguage(ScriptEngineFactory factory) {
		return factory.getLanguageName() + " " + factory.getLanguageVersion() //
			+ " / " //
			+ factory.getEngineName() + " " + factory.getEngineVersion() //
		;
	}

	private static final ConcurrentMap<CacheKey, JSR223Script> CACHE = new ConcurrentHashMap<>();

	public static class Builder {

		private String language = null;
		private boolean allowCompilation = true;
		private boolean precompile = true;
		private Charset charset = null;
		private Object source = null;

		public Builder() {
		}

		public Builder(String language) {
			language(language);
		}

		public Builder language(String language) {
			this.language = language;
			return this;
		}

		public String language() {
			return this.language;
		}

		public Builder allowCompilation(boolean allowCompilation) {
			this.allowCompilation = allowCompilation;
			return this;
		}

		public boolean allowCompilation() {
			return this.allowCompilation;
		}

		public Builder precompile(boolean precompile) {
			this.precompile = precompile;
			return this;
		}

		public boolean precompile() {
			return this.precompile;
		}

		public Builder charset(Charset charset) {
			this.charset = charset;
			return this;
		}

		public Charset charset() {
			return this.charset;
		}

		public Builder source(CharSequence script) {
			this.source = script;
			return this;
		}

		public Builder source(File file) {
			this.source = (file != null ? file.toPath() : null);
			return this;
		}

		public Builder source(Path path) {
			this.source = path;
			return this;
		}

		public Builder source(InputStream in) {
			this.source = in;
			return this;
		}

		public Builder source(Reader r) {
			this.source = r;
			return this;
		}

		public JSR223Script build() throws ScriptException, IOException {
			Objects.requireNonNull(this.source, "Must provide a non-null source for the script");
			JSR223Script script = JSR223Script.getInstance(this.allowCompilation, JSR223Script.sanitize(this.language),
				this.source, JSR223Script.sanitize(this.charset));
			if (this.allowCompilation && this.precompile) {
				try {
					script.compile();
				} catch (ScriptException | IOException e) {
					script.dispose();
					throw e;
				}
			}
			return script;
		}
	}

	public static class CacheKey implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String language;
		private final String hash;

		private CacheKey(String language, String hash) {
			this.language = language;
			this.hash = hash;
		}

		public String getLanguage() {
			return this.language;
		}

		public String getHash() {
			return this.hash;
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.language, this.hash);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) { return true; }
			if (obj == null) { return false; }
			if (getClass() != obj.getClass()) { return false; }
			CacheKey other = CacheKey.class.cast(obj);
			if (!StringUtils.equalsIgnoreCase(this.language, other.language)) { return false; }
			if (!StringUtils.equalsIgnoreCase(this.hash, other.hash)) { return false; }
			return true;
		}

		@Override
		public String toString() {
			return "CacheKey[" + this.language + this.hash + "]";
		}
	}

	private static Charset sanitize(Charset charset) {
		return (charset != null ? charset : JSR223Script.DEFAULT_CHARSET);
	}

	private static String sanitize(String language) throws ScriptException {
		language = StringUtils.lowerCase(language);
		if ((language == null) || !JSR223Script.SCRIPT_ENGINE_FACTORIES.containsKey(language)) {
			throw new ScriptException("Unsupported script language [" + language + "]");
		}
		return language;
	}

	private static Pair<CacheKey, String> computeKey(String language, Object source, Charset charset)
		throws ScriptException, IOException {
		if (CharSequence.class.isInstance(source)) {
			return Pair.of(new CacheKey(language, "#" + DigestUtils.sha256Hex(source.toString())), source.toString());
		}

		if (Path.class.isInstance(source)) {
			return Pair.of(new CacheKey(language, "@" + Tools.canonicalize(Path.class.cast(source)).toUri()), null);
		}

		if (InputStream.class.isInstance(source)) {
			source = new InputStreamReader(InputStream.class.cast(source), charset);
		}

		return JSR223Script.computeKey(language, IOUtils.toString(Reader.class.cast(source)), null);
	}

	public static Map<String, String> getLanguages() {
		return JSR223Script.LANGUAGES;
	}

	public static CacheKey getCacheKey(String language, String script) throws ScriptException {
		try {
			return JSR223Script.computeKey(language, script, null).getKey();
		} catch (IOException e) {
			throw new UncheckedIOException("IOException caught while working in memory", e);
		}
	}

	public static boolean purge(CacheKey cacheKey) {
		if (cacheKey == null) { return false; }
		return (JSR223Script.CACHE.remove(cacheKey) != null);
	}

	public static JSR223Script getInstance(CacheKey cacheKey) {
		if (cacheKey == null) { return null; }
		return JSR223Script.CACHE.get(cacheKey);
	}

	private static JSR223Script getInstance(boolean allowCompilation, String language, Object source, Charset charset)
		throws ScriptException, IOException {
		final Pair<CacheKey, String> key = JSR223Script.computeKey(language, source, charset);

		final ConcurrentInitializer<JSR223Script> initializer;
		if (key.getValue() != null) {
			initializer = new ConcurrentInitializer<JSR223Script>() {
				@Override
				public JSR223Script get() {
					return new JSR223Script(allowCompilation, key.getKey(), language, key::getValue);
				}
			};
		} else {
			initializer = new ConcurrentInitializer<JSR223Script>() {

				@Override
				public JSR223Script get() {
					return new JSR223Script(allowCompilation, key.getKey(), language, () -> {
						try (Reader r = Files.newBufferedReader(Path.class.cast(source), charset)) {
							return IOUtils.toString(r);
						}
					});
				}
			};
		}

		try {
			return ConcurrentUtils.createIfAbsent(JSR223Script.CACHE, key.getKey(), initializer);
		} catch (ConcurrentException e) {
			throw new RuntimeException("Unexpected exception working in memory", e);
		}
	}

	private class CompilationResult {
		private final CompiledScript compiled;
		private final ScriptException scriptException;
		private final IOException ioException;

		private CompilationResult(CompiledScript compiled) {
			this.compiled = compiled;
			this.scriptException = null;
			this.ioException = null;
		}

		private CompilationResult(Throwable thrown) {
			this.compiled = null;
			if (IOException.class.isInstance(thrown)) {
				this.ioException = IOException.class.cast(thrown);
				this.scriptException = null;
			} else {
				this.ioException = null;
				this.scriptException = ScriptException.class.cast(thrown);
			}
		}

		private CompiledScript get() throws IOException, ScriptException {
			if (this.ioException != null) { throw this.ioException; }
			if (this.scriptException != null) { throw this.scriptException; }
			return this.compiled;
		}
	}

	public static class ScriptBindings implements Supplier<Bindings> {
		public final Bindings bindings;
		private final ScriptEngine engine;

		private ScriptBindings(ScriptEngine engine) {
			this.engine = Objects.requireNonNull(engine, "Must provide a ScriptEngine instance");
			this.bindings = this.engine.createBindings();
		}

		public Bindings getBindings() {
			return get();
		}

		public boolean isSupportedBy(JSR223Script script) {
			if (script == null) { return false; }
			return (this.engine == script.engine.get());
		}

		@Override
		public Bindings get() {
			return this.bindings;
		}
	}

	private final boolean allowCompilation;
	private final CacheKey cacheKey;
	private final String language;
	private final ScriptEngineFactory factory;
	private final CheckedLazySupplier<String, IOException> sourceCode;
	private final LazySupplier<ScriptEngine> engine;

	private volatile CompilationResult compilationResult = null;

	private JSR223Script(boolean allowCompilation, CacheKey cacheKey, String language,
		final CheckedSupplier<String, IOException> script) {
		this.allowCompilation = allowCompilation;
		this.cacheKey = cacheKey;
		this.language = language;
		this.factory = JSR223Script.SCRIPT_ENGINE_FACTORIES.get(StringUtils.lowerCase(this.language));
		if (this.factory == null) { throw new IllegalArgumentException("Unsupported language [" + language + "]"); }
		this.engine = new LazySupplier<>(this.factory::getScriptEngine);
		this.sourceCode = new CheckedLazySupplier<>(script);
	}

	public String getLanguage() {
		return this.language;
	}

	public boolean compile() throws ScriptException, IOException {
		return (getCompiledScript() != null);
	}

	private CompiledScript getCompiledScript() throws ScriptException, IOException {
		ScriptEngine engine = this.engine.get();
		if (!this.allowCompilation || !Compilable.class.isInstance(engine)) { return null; }

		CompilationResult result = this.compilationResult;
		if (result == null) {
			synchronized (this) {
				result = this.compilationResult;
				if (result == null) {
					try {
						String source = this.sourceCode.getChecked();
						CompiledScript compiled = Compilable.class.cast(engine).compile(source);
						result = new CompilationResult(compiled);
						this.compilationResult = result;
					} catch (IOException | ScriptException e) {
						result = new CompilationResult(e);
						this.compilationResult = result;
					}
				}
			}
		}
		// This will return the result, or rethrow any caught exceptions
		return result.get();
	}

	public ScriptBindings newBindings() {
		return new ScriptBindings(this.engine.get());
	}

	public Object eval() throws ScriptException, IOException {
		return eval(newBindings());
	}

	public Object eval(ScriptBindings bindings) throws ScriptException, IOException {
		if (bindings == null) {
			bindings = newBindings();
		} else if (!supports(bindings)) {
			throw new ScriptException(
				"The given bindings did not come from this script's engine, and therefore can't be used with it");
		}
		return eval(bindings.getBindings());
	}

	public Object eval(Consumer<Bindings> initializer) throws ScriptException, IOException {
		return eval(initializer != null ? CheckedTools.check(initializer) : null);
	}

	public <EX extends Throwable> Object eval(CheckedConsumer<Bindings, EX> initializer)
		throws ScriptException, IOException, EX {
		final Bindings bindings = this.engine.get().createBindings();
		if (initializer != null) {
			initializer.acceptChecked(bindings);
		}
		return eval(bindings);
	}

	private Object eval(Bindings bindings) throws ScriptException, IOException {
		ScriptEngine engine = this.engine.get();
		final CompiledScript compiledScript = getCompiledScript();
		if (compiledScript != null) { return compiledScript.eval(bindings); }

		return engine.eval(this.sourceCode.getChecked(), bindings);
	}

	public Object eval(ScriptContext ctx) throws ScriptException, IOException {
		ScriptEngine engine = this.engine.get();
		if (ctx == null) {
			ctx = new SimpleScriptContext();
		}
		final CompiledScript compiledScript = getCompiledScript();
		if (compiledScript != null) { return compiledScript.eval(ctx); }

		return engine.eval(this.sourceCode.getChecked(), ctx);
	}

	public boolean supports(ScriptBindings bindings) {
		if (bindings == null) { return false; }
		return (this.engine.get() == bindings.engine);
	}

	public CacheKey getCacheKey() {
		return this.cacheKey;
	}

	public boolean dispose() {
		return JSR223Script.purge(this.cacheKey);
	}
}