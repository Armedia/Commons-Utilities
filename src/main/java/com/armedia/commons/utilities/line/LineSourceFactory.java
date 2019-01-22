package com.armedia.commons.utilities.line;

@FunctionalInterface
public interface LineSourceFactory {

	public LineSource newInstance(String recursionSpec, LineSource relativeTo) throws LineSourceException;

}