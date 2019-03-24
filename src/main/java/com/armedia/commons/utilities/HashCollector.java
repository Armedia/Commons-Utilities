package com.armedia.commons.utilities;

import java.security.MessageDigest;

public interface HashCollector {

	public MessageDigest getDigest();

	public byte[] collectHash();

	public void resetHash();

}