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
package com.armedia.commons.utilities;

import java.io.File;
import java.io.IOException;

/**
 *
 *
 */
public class FileSystemTools {

	public static File ensureDirectory(final String path) throws IOException {
		if (path == null) { throw new IllegalArgumentException("No path given"); }
		return FileSystemTools.ensureDirectory(new File(path));
	}

	public static File ensureDirectory(final String parent, String name) throws IOException {
		if (parent == null) { throw new IllegalArgumentException("Parent file may not be null"); }
		if (name == null) { throw new IllegalArgumentException("Directory name may not be null"); }
		return FileSystemTools.ensureDirectory(new File(parent, name));
	}

	public static File ensureDirectory(final File parent, String name) throws IOException {
		if (parent == null) { throw new IllegalArgumentException("Parent file may not be null"); }
		if (name == null) { throw new IllegalArgumentException("Directory name may not be null"); }
		return FileSystemTools.ensureDirectory(new File(parent, name));
	}

	public static File ensureDirectory(final File path) throws IOException {
		if (path == null) { throw new IllegalArgumentException("No path given"); }
		final String user = Tools.coalesce(System.getProperty("user.name"), "the current user");
		File f = path.getAbsoluteFile();
		if (!f.exists()) {
			if (!f.mkdirs()) {
				throw new IOException(
					String.format("Failed to create the directory [%s] as %s", f.getAbsolutePath(), user));
			}
		}
		if (!f.isDirectory()) {
			throw new IOException(String.format("Path [%s] is not a directory", f.getAbsolutePath()));
		}
		if (!f.canRead()) {
			throw new IOException(String.format("Directory [%s] is not readable by %s", f.getAbsolutePath(), user));
		}
		if (!f.canWrite()) {
			throw new IOException(String.format("Directory [%s] is not writable by %s", f.getAbsolutePath(), user));
		}
		return f;
	}

	public static File getSystemTemp() {
		String t = System.getProperty("java.io.tmpdir");
		return (t == null ? null : Tools.canonicalize(new File(t)));
	}
}
