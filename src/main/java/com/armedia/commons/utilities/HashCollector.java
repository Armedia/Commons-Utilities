package com.armedia.commons.utilities;

import java.security.MessageDigest;

import org.apache.commons.lang3.tuple.Pair;

public interface HashCollector {

	public MessageDigest getDigest();

	public Pair<Long, byte[]> collectHash();

	public void resetHash();

}