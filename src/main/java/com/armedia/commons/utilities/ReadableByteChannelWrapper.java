package com.armedia.commons.utilities;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class ReadableByteChannelWrapper extends ChannelWrapper<ReadableByteChannel> implements ReadableByteChannel {

	public ReadableByteChannelWrapper(ReadableByteChannel wrapped) {
		super(wrapped);
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		return this.wrapped.read(dst);
	}

}