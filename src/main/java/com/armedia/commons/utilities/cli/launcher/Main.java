/*******************************************************************************
 * #%L
 * Armedia Caliente
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
 *******************************************************************************/
package com.armedia.commons.utilities.cli.launcher;

import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.armedia.commons.utilities.cli.launcher.log.LogConfigurator;

public final class Main {

	private static final String AEP = "com.armedia.commons.utilities.cli.launcher.AbstractEntrypoint";

	private Main() {
		// So we can't instantiate
	}

	public static final Logger BOOT_LOG = LogConfigurator.getBootLogger();

	private static void run(AtomicInteger result, String... args) {
		result.set(1);
		try {
			Class<?> k = Class.forName(Main.AEP, true, Thread.currentThread().getContextClassLoader());
			Method m = k.getDeclaredMethod("run", String[].class);
			Object r = m.invoke(null, new Object[] {
				args
			});
			if ((r != null) && Integer.class.isInstance(r)) {
				result.set(Integer.class.cast(r).intValue());
			}
		} catch (Exception e) {
			// Something went wrong ...
			Main.BOOT_LOG.error("Failed to execute the EntryPoint", e);
		}
	}

	public static final void main(String... args) {
		final AtomicInteger result = new AtomicInteger(0);
		final Thread main = Thread.currentThread();

		URL[] urls = null;
		try {
			final Class<?> c = Class.forName(Main.AEP, false, main.getContextClassLoader());
			CodeSource src = c.getProtectionDomain().getCodeSource();
			urls = new URL[] {
				src.getLocation()
			};
		} catch (ClassNotFoundException e) {
			Main.BOOT_LOG.error("Failed to locate a required class: {}", Main.AEP, e);
			System.exit(1);
		}

		final ClassLoader childCl = new DynamicClassLoader(urls, ClassLoader.getSystemClassLoader().getParent());
		main.setContextClassLoader(childCl);

		final Thread entry = new Thread(main.getThreadGroup(), () -> Main.run(result, args), "entrypoint");
		entry.setContextClassLoader(childCl);
		entry.start();
		try {
			entry.join();
		} catch (InterruptedException e) {
			Main.BOOT_LOG.warn("Interrupted while waiting for the {} thread", entry.getName());
			result.set(1);
		}

		System.exit(result.get());
	}
}