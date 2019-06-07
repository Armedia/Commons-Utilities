package com.armedia.commons.utilities.codec;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.armedia.commons.utilities.Tools;

public class StringCodec<T> extends FunctionalCodec<T, String> {

	public static class Builder<VALUE> extends FunctionalCodec.Builder<VALUE, String> {
		public Builder() {
			super();
			setEncoder(Tools::toString);
		}

		@Override
		protected StringCodec<VALUE> newCodec() {
			return new StringCodec<>(this);
		}
	}

	protected StringCodec(Builder<T> builder) {
		super(builder);
	}

	public StringCodec(Function<String, T> decoder) {
		this(Tools::toString, null, null, decoder, null, null);
	}

	public StringCodec(Function<T, String> encoder, Function<String, T> decoder) {
		this(encoder, null, null, decoder, null, null);
	}

	public StringCodec(Function<T, String> encoder, String nullString, Predicate<String> nullStringCheck,
		Function<String, T> decoder, T nullValue, Predicate<T> nullValueCheck) {
		super(encoder, nullValue, nullValueCheck, decoder, nullString, nullStringCheck);
	}

	public static final StringCodec<Boolean> BOOLEAN = new StringCodec<>(Tools::decodeBoolean);

	public static final StringCodec<Byte> BYTE = new StringCodec<>(Byte::valueOf);

	public static final StringCodec<Short> SHORT = new StringCodec<>(Short::valueOf);

	public static final StringCodec<Integer> INTEGER = new StringCodec<>(Integer::valueOf);

	public static final StringCodec<Long> LONG = new StringCodec<>(Long::valueOf);

	public static final StringCodec<BigInteger> BIG_INTEGER = new StringCodec<>((s) -> new BigInteger(s));

	public static final StringCodec<Float> FLOAT = new StringCodec<>(Float::valueOf);

	public static final StringCodec<Double> DOUBLE = new StringCodec<>(Double::valueOf);

	public static final StringCodec<BigDecimal> BIG_DECIMAL = new StringCodec<>((s) -> new BigDecimal(s));

	public static final StringCodec<Character> CHARACTER;
	static {
		Builder<Character> builder = new Builder<>();
		CHARACTER = builder //
			.setNullEncodingChecker(StringUtils::isEmpty) //
			.setDecoder((s) -> s.charAt(0)) //
			.build();
	}

	public static final StringCodec<String> STRING = new StringCodec<>(Function.identity(), Function.identity());
}