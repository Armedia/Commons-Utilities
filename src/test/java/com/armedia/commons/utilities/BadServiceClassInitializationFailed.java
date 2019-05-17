package com.armedia.commons.utilities;

import java.util.UUID;

public class BadServiceClassInitializationFailed implements BadService {
	static final String ERROR_STR = UUID.randomUUID().toString();

	static {
		try {
			Class.forName(BadServiceClassInitializationFailed.ERROR_STR);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(BadServiceClassInitializationFailed.ERROR_STR, e);
		}
	}

	public BadServiceClassInitializationFailed() throws ClassNotFoundException {
		Class.forName(BadServiceClassInitializationFailed.ERROR_STR);
	}
}