/**
 * *******************************************************************
 * 
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS.
 * REPRODUCTION OF ANY PORTION OF THE SOURCE CODE, CONTAINED HEREIN,
 * OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE,
 * IS STRICTLY PROHIBITED.
 * 
 * Confidential Property of Armedia LLC.
 * (c) Copyright Armedia LLC 2011-2012.
 * All Rights reserved.
 * 
 * *******************************************************************
 */
package com.armedia.commons.utilities;

import java.io.File;
import java.io.IOException;

/**
 * @author drivera@armedia.com
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
		final String user = Tools.coalesce(System.getProperty("user.name"), "<unknown>");
		File f = path.getAbsoluteFile();
		if (!f.exists()) {
			if (!f.mkdirs()) { throw new IOException(String.format("User %s failed to create the cache directory '%s'",
				user, f.getAbsolutePath())); }
		}
		f = f.getCanonicalFile();
		if (!f.isDirectory()) { throw new IOException(
			String.format("Path '%s' is not a directory", f.getAbsolutePath())); }
		if (!f.canRead()) { throw new IOException(String.format("Directory '%s' is not readable by %s",
			f.getAbsolutePath(), user)); }
		if (!f.canWrite()) { throw new IOException(String.format("Directory '%s' is not writable by %s",
			f.getAbsolutePath(), user)); }
		return f.getAbsoluteFile();
	}

	public static File getSystemTemp() {
		String t = System.getProperty("java.io.tmpdir");
		if (t == null) { return null; }
		final File f = new File(t);
		try {
			return f.getCanonicalFile();
		} catch (IOException e) {
			return f.getAbsoluteFile();
		}
	}
}