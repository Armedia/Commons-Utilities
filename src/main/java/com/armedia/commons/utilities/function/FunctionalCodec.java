package com.armedia.commons.utilities.function;

import java.util.function.Function;
import java.util.function.Predicate;

import com.armedia.commons.utilities.Codec;
import com.armedia.commons.utilities.concurrent.BaseShareableLockable;
import com.armedia.commons.utilities.concurrent.SharedAutoLock;

public class FunctionalCodec<VALUE, ENCODING> extends FunctionalCheckedCodec<VALUE, ENCODING, RuntimeException>
	implements Codec<VALUE, ENCODING> {

	public static class Builder<VALUE, ENCODING> extends BaseShareableLockable {
		private VALUE nullValue = null;
		private Predicate<VALUE> nullValueChecker = null;
		private Function<ENCODING, VALUE> specialDecoder = null;
		private ENCODING nullEncoding = null;
		private Predicate<ENCODING> nullEncodingChecker = null;
		private Function<VALUE, ENCODING> specialEncoder = null;

		public VALUE getNullValue() {
			return shareLocked(() -> this.nullValue);
		}

		public Builder<VALUE, ENCODING> setNullValue(VALUE nullValue) {
			mutexLocked(() -> this.nullValue = nullValue);
			return this;
		}

		public Predicate<VALUE> getNullValueChecker() {
			return shareLocked(() -> this.nullValueChecker);
		}

		public Builder<VALUE, ENCODING> setNullValueChecker(Predicate<VALUE> nullValueChecker) {
			mutexLocked(() -> this.nullValueChecker = nullValueChecker);
			return this;
		}

		public Function<ENCODING, VALUE> getSpecialDecoder() {
			return shareLocked(() -> this.specialDecoder);
		}

		public Builder<VALUE, ENCODING> setSpecialDecoder(Function<ENCODING, VALUE> specialDecoder) {
			mutexLocked(() -> this.specialDecoder = specialDecoder);
			return this;
		}

		public ENCODING getNullEncoding() {
			return shareLocked(() -> this.nullEncoding);
		}

		public Builder<VALUE, ENCODING> setNullEncoding(ENCODING nullEncoding) {
			mutexLocked(() -> this.nullEncoding = nullEncoding);
			return this;
		}

		public Predicate<ENCODING> getNullEncodingChecker() {
			return shareLocked(() -> this.nullEncodingChecker);
		}

		public Builder<VALUE, ENCODING> setNullEncodingChecker(Predicate<ENCODING> nullEncodingChecker) {
			mutexLocked(() -> this.nullEncodingChecker = nullEncodingChecker);
			return this;
		}

		public Function<VALUE, ENCODING> getSpecialEncoder() {
			return shareLocked(() -> this.specialEncoder);
		}

		public Builder<VALUE, ENCODING> setSpecialEncoder(Function<VALUE, ENCODING> specialEncoder) {
			mutexLocked(() -> this.specialEncoder = specialEncoder);
			return this;
		}

		public FunctionalCodec<VALUE, ENCODING> build() {
			try (SharedAutoLock lock = autoSharedLock()) {
				return new FunctionalCodec<>(this);
			}
		}
	}

	private FunctionalCodec(Builder<VALUE, ENCODING> builder) {
		this(builder.getNullValue(), builder.getNullValueChecker(), builder.getSpecialEncoder(),
			builder.getNullEncoding(), builder.getNullEncodingChecker(), builder.getSpecialDecoder());
	}

	public FunctionalCodec(VALUE nullValue, Predicate<VALUE> nullValueChecker, Function<VALUE, ENCODING> specialEncoder,
		ENCODING nullEncoding, Predicate<ENCODING> nullEncodingChecker, Function<ENCODING, VALUE> specialDecoder) {
		super(nullValue, nullValueChecker, CheckedTools.check(specialEncoder), nullEncoding, nullEncodingChecker,
			CheckedTools.check(specialDecoder));
	}
}