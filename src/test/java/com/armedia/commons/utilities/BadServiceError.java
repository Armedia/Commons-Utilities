package com.armedia.commons.utilities;

import java.util.UUID;

public class BadServiceError implements BadService {

	static final String ERROR_STR = UUID.randomUUID().toString();

	public BadServiceError() {
		throw new OutOfMemoryError(BadServiceError.ERROR_STR);
	}
}