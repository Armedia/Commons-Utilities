package com.armedia.commons.utilities.line;

public class LineProcessorException extends Exception {

	private static final long serialVersionUID = 1L;

	public LineProcessorException() {
	}

	public LineProcessorException(String message) {
		super(message);
	}

	public LineProcessorException(Throwable cause) {
		super(cause);
	}

	public LineProcessorException(String message, Throwable cause) {
		super(message, cause);
	}

	public LineProcessorException(String message, Throwable cause, boolean enableSuppression,
		boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}