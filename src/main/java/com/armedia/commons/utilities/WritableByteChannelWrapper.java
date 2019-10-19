package com.armedia.commons.utilities;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class WritableByteChannelWrapper extends ChannelWrapper<WritableByteChannel> implements WritableByteChannel {

	public WritableByteChannelWrapper(WritableByteChannel wrapped) {
		super(wrapped);
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		return this.wrapped.write(src);
	}

}