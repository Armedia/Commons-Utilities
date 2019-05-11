package com.armedia.commons.utilities;

import java.util.UUID;

public class BadServiceException implements BadService {

	static final String ERROR_STR = UUID.randomUUID().toString();

	public BadServiceException() throws Exception {
		throw new Exception(BadServiceException.ERROR_STR);
	}
}