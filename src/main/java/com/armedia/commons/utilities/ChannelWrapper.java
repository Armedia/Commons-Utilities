package com.armedia.commons.utilities;

import java.io.IOException;
import java.nio.channels.Channel;
import java.util.Objects;

public class ChannelWrapper<C extends Channel> implements Channel {

	protected final C wrapped;

	public ChannelWrapper(C wrapped) {
		this.wrapped = Objects.requireNonNull(wrapped);
	}

	@Override
	public boolean isOpen() {
		return this.wrapped.isOpen();
	}

	@Override
	public void close() throws IOException {
		this.wrapped.close();
	}
}