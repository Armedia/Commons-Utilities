package com.armedia.commons.utilities;

import java.util.UUID;

public class BadServiceUncheckedException implements BadService {

	static final String ERROR_STR = UUID.randomUUID().toString();

	public BadServiceUncheckedException() {
		throw new RuntimeException(BadServiceUncheckedException.ERROR_STR);
	}
}