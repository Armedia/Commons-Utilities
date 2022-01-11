/*-
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (C) 2013 - 2022 Armedia, LLC
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
 */
package com.armedia.commons.utilities.cli.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Objects;
import java.util.stream.Stream;

public class DynamicClassLoader extends URLClassLoader {
	private static final URL[] NO_URLS = {};

	private static URL[] sanitize(URL[] urls) {
		return (urls != null ? urls : DynamicClassLoader.NO_URLS);
	}

	public DynamicClassLoader(URL[] urls) {
		super(DynamicClassLoader.sanitize(urls));
	}

	public DynamicClassLoader(URL[] urls, ClassLoader parent) {
		super(DynamicClassLoader.sanitize(urls), parent);
	}

	public DynamicClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
		super(DynamicClassLoader.sanitize(urls), parent, factory);
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		return super.getResourceAsStream(name);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loadClass(name, false);
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		return super.loadClass(name, resolve);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		return super.findClass(name);
	}

	@Override
	public URL findResource(String name) {
		return super.findResource(name);
	}

	@Override
	public Enumeration<URL> findResources(String name) throws IOException {
		return super.findResources(name);
	}

	@Override
	public void addURL(URL url) {
		super.addURL(url);
	}

	public static DynamicClassLoader find() {
		ClassLoader prev = null;
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		while (cl != null) {
			if (DynamicClassLoader.class.isInstance(cl)) { return DynamicClassLoader.class.cast(cl); }
			prev = cl;
			cl = cl.getParent();
			if (prev == cl) {
				break;
			}
		}
		return null;
	}

	public static boolean update(Stream<URL> urls) {
		if (urls == null) { return false; }
		DynamicClassLoader loader = DynamicClassLoader.find();
		if (loader == null) { return false; }
		urls.filter(Objects::nonNull).forEach(loader::addURL);
		return true;
	}

	public static boolean update(Collection<URL> urls) {
		if ((urls == null) || urls.isEmpty()) { return false; }
		return DynamicClassLoader.update(urls.stream());
	}

	public static boolean update(URL... urls) {
		if ((urls == null) || (urls.length < 1)) { return false; }
		return DynamicClassLoader.update(Stream.of(urls));
	}
}
