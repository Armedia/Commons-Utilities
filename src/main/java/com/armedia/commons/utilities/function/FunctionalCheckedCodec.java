package com.armedia.commons.utilities.function;

import java.util.function.Predicate;

import com.armedia.commons.utilities.CheckedCodec;
import com.armedia.commons.utilities.concurrent.BaseShareableLockable;
import com.armedia.commons.utilities.concurrent.SharedAutoLock;

public class FunctionalCheckedCodec<VALUE, ENCODING, EXCEPTION extends Exception>
	implements CheckedCodec<VALUE, ENCODING, EXCEPTION> {

	public static class Builder<VALUE, ENCODING, EXCEPTION extends Exception> extends BaseShareableLockable {
		private VALUE nullValue = null;
		private Predicate<VALUE> nullValueChecker = null;
		private CheckedFunction<ENCODING, VALUE, EXCEPTION> specialDecoder = null;
		private ENCODING nullEncoding = null;
		private Predicate<ENCODING> nullEncodingChecker = null;
		private CheckedFunction<VALUE, ENCODING, EXCEPTION> specialEncoder = null;

		public VALUE getNullValue() {
			return shareLocked(() -> this.nullValue);
		}

		public Builder<VALUE, ENCODING, EXCEPTION> setNullValue(VALUE nullValue) {
			mutexLocked(() -> this.nullValue = nullValue);
			return this;
		}

		public Predicate<VALUE> getNullValueChecker() {
			return shareLocked(() -> this.nullValueChecker);
		}

		public Builder<VALUE, ENCODING, EXCEPTION> setNullValueChecker(Predicate<VALUE> nullValueChecker) {
			mutexLocked(() -> this.nullValueChecker = nullValueChecker);
			return this;
		}

		public CheckedFunction<ENCODING, VALUE, EXCEPTION> getSpecialDecoder() {
			return shareLocked(() -> this.specialDecoder);
		}

		public Builder<VALUE, ENCODING, EXCEPTION> setSpecialDecoder(
			CheckedFunction<ENCODING, VALUE, EXCEPTION> specialDecoder) {
			mutexLocked(() -> this.specialDecoder = specialDecoder);
			return this;
		}

		public ENCODING getNullEncoding() {
			return shareLocked(() -> this.nullEncoding);
		}

		public Builder<VALUE, ENCODING, EXCEPTION> setNullEncoding(ENCODING nullEncoding) {
			mutexLocked(() -> this.nullEncoding = nullEncoding);
			return this;
		}

		public Predicate<ENCODING> getNullEncodingChecker() {
			return shareLocked(() -> this.nullEncodingChecker);
		}

		public Builder<VALUE, ENCODING, EXCEPTION> setNullEncodingChecker(Predicate<ENCODING> nullEncodingChecker) {
			mutexLocked(() -> this.nullEncodingChecker = nullEncodingChecker);
			return this;
		}

		public CheckedFunction<VALUE, ENCODING, EXCEPTION> getSpecialEncoder() {
			return shareLocked(() -> this.specialEncoder);
		}

		public Builder<VALUE, ENCODING, EXCEPTION> setSpecialEncoder(
			CheckedFunction<VALUE, ENCODING, EXCEPTION> specialEncoder) {
			mutexLocked(() -> this.specialEncoder = specialEncoder);
			return this;
		}

		public FunctionalCheckedCodec<VALUE, ENCODING, EXCEPTION> build() {
			try (SharedAutoLock lock = autoSharedLock()) {
				return new FunctionalCheckedCodec<>(this);
			}
		}
	}

	private final VALUE nullValue;
	private final Predicate<VALUE> nullValueChecker;
	private final CheckedFunction<ENCODING, VALUE, EXCEPTION> specialDecoder;
	private final ENCODING nullEncoding;
	private final Predicate<ENCODING> nullEncodingChecker;
	private final CheckedFunction<VALUE, ENCODING, EXCEPTION> specialEncoder;

	private FunctionalCheckedCodec(Builder<VALUE, ENCODING, EXCEPTION> builder) {
		this(builder.getNullValue(), builder.getNullValueChecker(), builder.getSpecialEncoder(),
			builder.getNullEncoding(), builder.getNullEncodingChecker(), builder.getSpecialDecoder());
	}

	public FunctionalCheckedCodec(VALUE nullValue, Predicate<VALUE> nullValueChecker,
		CheckedFunction<VALUE, ENCODING, EXCEPTION> specialEncoder, ENCODING nullEncoding,
		Predicate<ENCODING> nullEncodingChecker, CheckedFunction<ENCODING, VALUE, EXCEPTION> specialDecoder) {
		this.nullValue = nullValue;
		this.nullValueChecker = nullValueChecker;
		this.nullEncoding = nullEncoding;
		this.nullEncodingChecker = nullEncodingChecker;
		this.specialEncoder = specialEncoder;
		this.specialDecoder = specialDecoder;
	}

	@Override
	public VALUE getNullValue() {
		return this.nullValue;
	}

	@Override
	public boolean isNullValue(VALUE v) {
		if (this.nullValueChecker != null) { return this.nullValueChecker.test(v); }
		return CheckedCodec.super.isNullValue(v);
	}

	@Override
	public boolean isNullEncoding(ENCODING e) {
		if (this.nullEncodingChecker != null) { return this.nullEncodingChecker.test(e); }
		return CheckedCodec.super.isNullEncoding(e);
	}

	@Override
	public ENCODING getNullEncoding() {
		return this.nullEncoding;
	}

	public CheckedFunction<VALUE, ENCODING, EXCEPTION> getSpecialEncoder() {
		return this.specialEncoder;
	}

	public CheckedFunction<ENCODING, VALUE, EXCEPTION> getSpecialDecoder() {
		return this.specialDecoder;
	}

	@Override
	public ENCODING encode(VALUE v) throws EXCEPTION {
		if (this.specialEncoder == null) { throw new UnsupportedOperationException("No encoding function given"); }
		return this.specialEncoder.applyChecked(v);
	}

	@Override
	public VALUE decode(ENCODING e) throws EXCEPTION {
		if (this.specialDecoder == null) { throw new UnsupportedOperationException("No decoding function given"); }
		return this.specialDecoder.applyChecked(e);
	}
}