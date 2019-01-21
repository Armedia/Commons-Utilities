package com.armedia.commons.utilities.line;

public class LineSourceException extends Exception {

	private static final long serialVersionUID = 1L;

	public LineSourceException() {
	}

	public LineSourceException(String message) {
		super(message);
	}

	public LineSourceException(Throwable cause) {
		super(cause);
	}

	public LineSourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public LineSourceException(String message, Throwable cause, boolean enableSuppression,
		boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}