/*******************************************************************************
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (C) 2013 - 2025 Armedia, LLC
 * %%
 * This file is part of the Caliente software.
 * 
 * If the software was purchased under a paid Caliente license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * 
 * Caliente is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Caliente is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Caliente. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 *******************************************************************************/
package com.armedia.commons.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.codec.digest.DigestUtils;

import com.armedia.commons.utilities.function.CheckedConsumer;
import com.armedia.commons.utilities.io.BinaryMemoryBuffer;

/**
 * <p>
 * This class provides methods to calculate a hash value that is guaranteed to be between {@code 0}
 * and the given {@code maxBucketNumber} (inclusive), or {@code -1} if the value to calculate the
 * hash from results in a {@code null}-value. All methods work in basically the same way: they
 * convert the given {@code value} parameter into a "byte stream" (an {@link InputStream}, a
 * {@link ByteBuffer}, or a {@code byte[]}), as this is necessary to feed the underlying
 * {@link MessageDigest} instance. The hash is calculated using SHA1 as the hashing algorithm
 * applied to the generated {@code byte[]}.
 * </p>
 * <p>
 * The optional {@code maxBucketNumber} value determines the maximum value returned. If this is
 * {@code 0}, the constant value 0 will be returned without performing any computation because
 * (obviously) there can only be one bucket. The maximum value for {@code maxBucketNumber} is
 * {@code 4,294,967,295} (hex {@code 0xFFFFFFFFL}), which is also used as a default when no bucket
 * count value is available as a parameter. If the bucket count given is less than {@code 0}, or
 * greater than the maximum value, then an {@link IllegalArgumentException} will be raised. For the
 * methods taht accept a {@link Boolean} parameter, the {@code maxBucketNumber} parameter will be
 * folded to either {@code 0} or {@code 1}.
 * </p>
 * <p>
 * The optional {@code seed} value can be used to further alter the hash computation, and can be any
 * number between {@code 0} and {@code 4,294,967,295} (inclusive). Any other value will result in an
 * {@link IllegalArgumentException} being raised. If the seed value is {@code 0}, then no additional
 * seed computations will be performed. For the methods taht accept a {@link Boolean} parameter, the
 * seed will always be {@code 0}.
 * </p>
 */
public class BucketHasher {

	/**
	 * <p>
	 * The minimuim acceptable maximum bucket value
	 * </p>
	 */
	public static final long MIN_BUCKET = 0x00000000L;

	/**
	 * <p>
	 * The maximum acceptable maximum bucket value
	 * </p>
	 */
	public static final long MAX_BUCKET = 0xFFFFFFFFL;

	/**
	 * <p>
	 * The default maximum bucket value if none is available via parameters
	 * </p>
	 */
	public static final long DEF_BUCKET = BucketHasher.MAX_BUCKET;

	/**
	 * <p>
	 * The minimum acceptable seed value
	 * </p>
	 */
	public static final long MIN_SEED = 0x00000000L;

	/**
	 * <p>
	 * The maximum acceptable seed value
	 * </p>
	 */
	public static final long MAX_SEED = 0xFFFFFFFFL;

	/**
	 * <p>
	 * The default seed value if none is available via parameters
	 * </p>
	 */
	public static final long DEF_SEED = BucketHasher.MIN_SEED;

	/**
	 * <p>
	 * Return a sanitized, guaranteed-valid value for the {@code seed} parameter, where any value
	 * less than {@link #MIN_SEED} ({@code 0L}) is folded to {@link #MIN_SEED}, and any value above
	 * {@link #MAX_SEED} is folded to {@link #MAX_SEED}. Any other value within the normal limits
	 * (as per the {@link BucketHasher class documentation}) is returned as-is.
	 * </p>
	 *
	 * @param seed
	 *            the prospective value
	 * @return the sanitized value
	 */
	public static long sanitizeSeed(long seed) {
		return Tools.ensureBetween(BucketHasher.MIN_SEED, seed, BucketHasher.MAX_SEED);
	}

	/**
	 * <p>
	 * Return a sanitized, guaranteed-valid value for the {@code maxBucket} parameter, where any
	 * value less than {@link #MIN_BUCKET} ({@code 0L}) is folded to {@link #MIN_BUCKET}, and any
	 * value above {@link #MAX_BUCKET} is folded to {@link #MAX_BUCKET}. Any other value within the
	 * normal limits (as per the {@link BucketHasher class documentation}) is returned as-is.
	 * </p>
	 *
	 * @param bucket
	 *            the prospective value
	 * @return the sanitized value
	 */
	public static long sanitizeBucket(long bucket) {
		return Tools.ensureBetween(BucketHasher.MIN_BUCKET, bucket, BucketHasher.MAX_BUCKET);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Boolean value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Boolean value, long maxBucketNumber) {
		return BucketHasher.hash(value, maxBucketNumber, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Boolean value, long maxBucketNumber, long seed) {
		if (value == null) { return -1; }
		return ((maxBucketNumber == 0) || !value ? 0 : 1);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Byte value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Byte value, long maxBucketNumber) {
		return BucketHasher.hash(value, maxBucketNumber, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Byte value, long maxBucketNumber, long seed) {
		return BucketHasher.calculate(ByteBuffer::put, Number::byteValue, value, maxBucketNumber, seed);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Short value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Short value, long maxBucketNumber) {
		return BucketHasher.hash(value, maxBucketNumber, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Short value, long maxBucketNumber, long seed) {
		return BucketHasher.calculate(ByteBuffer::putShort, Number::shortValue, value, maxBucketNumber, seed);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Integer value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Integer value, long maxBucketNumber) {
		return BucketHasher.hash(value, maxBucketNumber, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Integer value, long maxBucketNumber, long seed) {
		return BucketHasher.calculate(ByteBuffer::putInt, Number::intValue, value, maxBucketNumber, seed);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Long value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Long value, long maxBucketNumber) {
		return BucketHasher.hash(value, maxBucketNumber, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Long value, long maxBucketNumber, long seed) {
		return BucketHasher.calculate(ByteBuffer::putLong, Number::longValue, value, maxBucketNumber, seed);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(BigInteger value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(BigInteger value, long maxBucketNumber) {
		return BucketHasher.hash(value, maxBucketNumber, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(BigInteger value, long maxBucketNumber, long seed) {
		if (value == null) { return -1; }
		return BucketHasher.hash(value::toByteArray, maxBucketNumber, seed);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Float value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Float value, long maxBucketNumber) {
		return BucketHasher.hash(value, maxBucketNumber, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Float value, long maxBucketNumber, long seed) {
		return BucketHasher.calculate(ByteBuffer::putFloat, Number::floatValue, value, maxBucketNumber, seed);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Double value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Double value, long maxBucketNumber) {
		return BucketHasher.hash(value, maxBucketNumber, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Double value, long maxBucketNumber, long seed) {
		return BucketHasher.calculate(ByteBuffer::putDouble, Number::doubleValue, value, maxBucketNumber, seed);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(BigDecimal value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(BigDecimal value, long maxBucketNumber) {
		return BucketHasher.hash(value, maxBucketNumber, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(BigDecimal value, long maxBucketNumber, long seed) {
		if (value == null) { return -1; }
		return BucketHasher.hash(value.unscaledValue(), maxBucketNumber, seed);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	private static <N extends Number> long calculate(BiConsumer<ByteBuffer, N> consumer, Function<Number, N> producer,
		Number value, long maxBucketNumber, long seed) {
		if ((consumer == null) || (producer == null) || (value == null)) { return -1; }
		// The largest primitive is 8 bytes long, so let's use that
		ByteBuffer buf = ByteBuffer.allocate(8);
		consumer.accept(buf, producer.apply(value));
		buf.flip();
		return BucketHasher.hash(buf, maxBucketNumber, seed);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Serializable value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Serializable value, long maxBucketNumber) {
		return BucketHasher.hash(value, maxBucketNumber, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Serializable value, long maxBucketNumber, long seed) {
		CheckedConsumer<MessageDigest, RuntimeException> updater = null;
		if (value != null) {
			updater = (md) -> {
				try (BinaryMemoryBuffer buf = new BinaryMemoryBuffer()) {
					try (ObjectOutputStream oos = new ObjectOutputStream(buf)) {
						oos.writeObject(value);
					}
					buf.flush();
					DigestUtils.updateDigest(md, buf.getInputStream());
				} catch (IOException e) {
					throw new UncheckedIOException("Unexpected IOException caught while serializing in memory", e);
				}
			};
		}
		return BucketHasher.hash(updater, maxBucketNumber, seed);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details. The bytes from the string are
	 * obtained by invoking {@link String#getBytes(java.nio.charset.Charset)} with
	 * {@link StandardCharsets#UTF_8}.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(CharSequence value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details. The bytes from the string are
	 * obtained by invoking {@link String#getBytes(java.nio.charset.Charset)} with
	 * {@link StandardCharsets#UTF_8}.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(CharSequence value, long maxBucketNumber) {
		return BucketHasher.hash(value, maxBucketNumber, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details. The bytes from the string are
	 * obtained by invoking {@link String#getBytes(java.nio.charset.Charset)} with
	 * {@link StandardCharsets#UTF_8}.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(CharSequence value, long maxBucketNumber, long seed) {
		ByteBuffer buf = null;
		if (value != null) {
			buf = StandardCharsets.UTF_8.encode(CharBuffer.wrap(value));
		}
		return BucketHasher.hash(buf, maxBucketNumber, seed);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(byte[] value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(byte[] value, long maxBucketNumber) {
		return BucketHasher.hash(value, maxBucketNumber, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(byte[] value, long maxBucketNumber, long seed) {
		ByteBuffer buf = null;
		if (value != null) {
			buf = ByteBuffer.wrap(value);
		}
		return BucketHasher.hash(buf, maxBucketNumber, seed);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Supplier<byte[]> value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Supplier<byte[]> value, long maxBucketNumber) {
		return BucketHasher.hash(value, maxBucketNumber, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Supplier<byte[]> value, long maxBucketNumber, long seed) {
		byte[] data = null;
		if (value != null) {
			data = value.get();
		}
		return BucketHasher.hash(data, maxBucketNumber, seed);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(ByteBuffer value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(ByteBuffer value, long maxBucketNumber) {
		return BucketHasher.hash(value, maxBucketNumber, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(ByteBuffer value, long maxBucketNumber, long seed) {
		CheckedConsumer<MessageDigest, RuntimeException> updater = null;
		if (value != null) {
			updater = (md) -> DigestUtils.updateDigest(md, value);
		}
		return BucketHasher.hash(updater, maxBucketNumber, seed);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(ReadableByteChannel value) throws IOException {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(ReadableByteChannel value, long maxBucketNumber) throws IOException {
		return BucketHasher.hash(value, maxBucketNumber, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(ReadableByteChannel value, long maxBucketNumber, long seed) throws IOException {
		if (value == null) { return -1; }
		return BucketHasher.hash(Channels.newInputStream(value), maxBucketNumber, seed);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(InputStream value) throws IOException {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(InputStream value, long maxBucketNumber) throws IOException {
		return BucketHasher.hash(value, maxBucketNumber, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(InputStream value, long maxBucketNumber, long seed) throws IOException {
		CheckedConsumer<MessageDigest, IOException> updater = null;
		if (value != null) {
			updater = (md) -> DigestUtils.updateDigest(md, value);
		}
		return BucketHasher.hash(updater, maxBucketNumber, seed);
	}

	/**
	 * <p>
	 * Verifies that the given value is within the given limits, or explodes loudly otherwise.
	 * </p>
	 *
	 * @param name
	 *            the name of the value (for the exception message)
	 * @param value
	 *            the value to validate
	 * @param min
	 *            the lower limit
	 * @param max
	 *            the upper limit
	 */
	private static void validateValue(String name, long value, long min, long max) {
		if ((value < min) || (value > max)) {
			throw new IllegalArgumentException(
				String.format("The %s must be between %d and %d (%d was given)", name, min, max, value));
		}
	}

	/**
	 * <p>
	 * Implements the actual hash calculation.
	 * </p>
	 *
	 * @param <E>
	 * @param updater
	 * @param maxBucketNumber
	 * @param seed
	 * @return
	 * @throws E
	 */
	private static <E extends Throwable> long hash(CheckedConsumer<MessageDigest, E> updater, long maxBucketNumber,
		long seed) throws E {

		// Parameter sanity
		BucketHasher.validateValue("maximum bucket", maxBucketNumber, BucketHasher.MIN_BUCKET, BucketHasher.MAX_BUCKET);
		BucketHasher.validateValue("seed", seed, BucketHasher.MIN_SEED, BucketHasher.MAX_SEED);

		// Now proceed to the calculation, applying any shortcuts as appropriate
		if (updater == null) { return -1; }
		if (maxBucketNumber == 0) { return 0; }

		// First things first: consume the primary data
		MessageDigest md = DigestUtils.getSha1Digest();
		updater.acceptChecked(md);

		// Next, apply the seed if any was given
		if (seed != 0L) {
			int shift = 24;
			for (int i = 0; i < 4; i++) {
				md.update((byte) (seed >> shift));
				shift -= 8;
			}
		}

		// Finally, compute the actual bucket
		byte[] digest = md.digest();
		// long bucket = BucketHasher.readLong(digest, 0);
		ByteBuffer buf = ByteBuffer.wrap(digest);
		long bucket = buf.getLong();
		// Strip the signum, and ensure the value is within our bucket range
		return ((bucket & Long.MAX_VALUE) % (maxBucketNumber + 1));
	}
}
