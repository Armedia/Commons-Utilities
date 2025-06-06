/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2025 Armedia, LLC
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
package com.armedia.commons.utilities.cli.token;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.armedia.commons.utilities.Tools;

public class LocalPathTokenSource extends ReaderTokenSource {

	private final Path sourcePath;

	public LocalPathTokenSource(String sourcePath) throws IOException {
		if (sourcePath == null) { throw new IllegalArgumentException("Must provide a non-null path"); }
		this.sourcePath = Tools.canonicalize(Paths.get(sourcePath));
	}

	public LocalPathTokenSource(Path sourcePath) throws IOException {
		if (sourcePath == null) { throw new IllegalArgumentException("Must provide a non-null path"); }
		this.sourcePath = Tools.canonicalize(sourcePath);
	}

	public Path getSourcePath() {
		return this.sourcePath;
	}

	@Override
	public String getKey() {
		return this.sourcePath.toString();
	}

	@Override
	protected Reader openReader() throws IOException {
		return new InputStreamReader(new FileInputStream(this.sourcePath.toFile()), getCharset());
	}

	@Override
	public String toString() {
		return String.format("LocalPathTokenSource [path=%s]", this.sourcePath);
	}
}
