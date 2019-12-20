package com.armedia.commons.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.codec.digest.DigestUtils;

import com.armedia.commons.utilities.function.CheckedConsumer;

public class BucketHasher {

	public static final long MIN_BUCKET = 0x00000000L;
	public static final long MAX_BUCKET = 0xFFFFFFFFL;
	public static final long DEF_BUCKET = BucketHasher.MAX_BUCKET;
	private static final long BUCKET_MASK = 0xFFFFFFFFL;

	public static final long MIN_SEED = 0x00000000L;
	public static final long MAX_SEED = 0xFFFFFFFFL;
	public static final long DEF_SEED = BucketHasher.MIN_SEED;
	private static final long SEED_MASK = 0xFFFFFFFFL;

	public static long hash(Byte value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	public static long hash(Byte value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	public static long hash(Byte value, long bucketCount, long seed) {
		Consumer<ByteBuffer> converter = null;
		if (value != null) {
			converter = (buf) -> buf.put(value.byteValue());
		}
		return BucketHasher.calculate(converter, 4, bucketCount, seed);
	}

	public static long hash(Short value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	public static long hash(Short value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	public static long hash(Short value, long bucketCount, long seed) {
		Consumer<ByteBuffer> converter = null;
		if (value != null) {
			converter = (buf) -> buf.putShort(value.shortValue());
		}
		return BucketHasher.calculate(converter, 2, bucketCount, seed);
	}

	public static long hash(Integer value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	public static long hash(Integer value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	public static long hash(Integer value, long bucketCount, long seed) {
		Consumer<ByteBuffer> converter = null;
		if (value != null) {
			converter = (buf) -> buf.putInt(value.intValue());
		}
		return BucketHasher.calculate(converter, 4, bucketCount, seed);
	}

	public static long hash(Long value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	public static long hash(Long value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	public static long hash(Long value, long bucketCount, long seed) {
		Consumer<ByteBuffer> converter = null;
		if (value != null) {
			converter = (buf) -> buf.putLong(value.longValue());
		}
		return BucketHasher.calculate(converter, 4, bucketCount, seed);
	}

	public static long hash(BigInteger value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	public static long hash(BigInteger value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	public static long hash(BigInteger value, long bucketCount, long seed) {
		if (value == null) { return -1; }
		return BucketHasher.hash(value::toByteArray, bucketCount, seed);
	}

	public static long hash(Float value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	public static long hash(Float value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	public static long hash(Float value, long bucketCount, long seed) {
		Consumer<ByteBuffer> converter = null;
		if (value != null) {
			converter = (buf) -> buf.putFloat(value.floatValue());
		}
		return BucketHasher.calculate(converter, 4, bucketCount, seed);
	}

	public static long hash(Double value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	public static long hash(Double value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	public static long hash(Double value, long bucketCount, long seed) {
		Consumer<ByteBuffer> converter = null;
		if (value != null) {
			converter = (buf) -> buf.putDouble(value.doubleValue());
		}
		return BucketHasher.calculate(converter, 4, bucketCount, seed);
	}

	public static long hash(BigDecimal value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	public static long hash(BigDecimal value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	public static long hash(BigDecimal value, long bucketCount, long seed) {
		if (value == null) { return -1; }
		return BucketHasher.hash(value.unscaledValue(), bucketCount, seed);
	}

	private static long calculate(Consumer<ByteBuffer> converter, int bytes, long bucketCount, long seed) {
		if (converter == null) { return -1; }
		ByteBuffer buf = ByteBuffer.allocate(bytes);
		converter.accept(buf);
		buf.flip();
		return BucketHasher.hash(buf, bucketCount, seed);
	}

	/**
	 * <p>
	 * Calculates a hash value that is guaranteed to be between {@code 0L} and {@code 4294967295L}
	 * (hex {@code 0xFFFFFFFFL}, inclusive), or {@code -1L} if the given value is {@code null}.
	 * </p>
	 * <p>
	 * The given value is serialized, and its hash is computed against the serialized bytes.
	 * </p>
	 *
	 * @param value
	 *            the value to calculate the hash for
	 * @return a hash value that is guaranteed to be between 0 and {@code bucketCount} (inclusive)
	 */
	public static long hash(Serializable value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * Calculates a hash value that is guaranteed to be between {@code 0L} and {@code bucketCount}
	 * (inclusive), or {@code -1L} if the given value is {@code null}. If {@code bucketCount} is 0,
	 * the constant value 0 will be returned without any calculation being performed. The maximum
	 * value for {@code bucketCount} is 4294967295 (hex 0xFFFFFFFFL).
	 * </p>
	 * <p>
	 * The given value is serialized, and its hash is computed against the serialized bytes.
	 * </p>
	 *
	 * @param value
	 *            the value to calculate the hash for
	 * @param bucketCount
	 *            the (inclusive) upper bound for the 0-based index that will be returned
	 * @return a hash value that is guaranteed to be between 0 and {@code bucketCount} (inclusive)
	 */
	public static long hash(Serializable value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * Calculates a hash value that is guaranteed to be between {@code 0L} and {@code bucketCount}
	 * (inclusive), or {@code -1L} if the given value is {@code null}. If {@code bucketCount} is 0,
	 * the constant value 0 will be returned without any calculation being performed. The maximum
	 * value for {@code bucketCount} is 4294967295 (hex 0xFFFFFFFFL). The {@code seed} value can be
	 * used to further alter the hash computation, and only the lower 32 bits of the seed will be
	 * used (i.e. values between 0 and 4294967295).
	 * </p>
	 * <p>
	 * The given value is serialized, and its hash is computed against the serialized bytes.
	 * </p>
	 *
	 * @param value
	 *            the value to calculate the hash for
	 * @param bucketCount
	 *            the (inclusive) upper bound for the 0-based index that will be returned
	 * @param seed
	 *            a seed value to further influence the computed hash
	 * @return a hash value that is guaranteed to be between 0 and {@code bucketCount} (inclusive)
	 */
	public static long hash(Serializable value, long bucketCount, long seed) {
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
		return BucketHasher.calculate(updater, bucketCount, seed);
	}

	/**
	 * <p>
	 * Calculates a hash value that is guaranteed to be between {@code 0L} and {@code 4294967295L}
	 * (hex {@code 0xFFFFFFFFL}, inclusive), or {@code -1L} if the given value is {@code null}.
	 * </p>
	 * <p>
	 * The given {@link String} converted to a byte array using {@link String#getBytes()}, and the
	 * hash is calculated against the resulting byte array.
	 * </p>
	 *
	 * @param value
	 *            the value to calculate the hash for
	 * @return a hash value that is guaranteed to be between 0 and {@code bucketCount} (inclusive)
	 */
	public static long hash(String value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * Calculates a hash value that is guaranteed to be between {@code 0L} and {@code bucketCount}
	 * (inclusive), or {@code -1L} if the given value is {@code null}. If {@code bucketCount} is 0,
	 * the constant value 0 will be returned without any calculation being performed. The maximum
	 * value for {@code bucketCount} is 4294967295 (hex 0xFFFFFFFFL).
	 * </p>
	 * <p>
	 * The given {@link String} converted to a byte array using {@link String#getBytes()}, and the
	 * hash is calculated against the resulting byte array.
	 * </p>
	 *
	 * @param value
	 *            the value to calculate the hash for
	 * @param bucketCount
	 *            the (inclusive) upper bound for the 0-based index that will be returned
	 * @return a hash value that is guaranteed to be between 0 and {@code bucketCount} (inclusive)
	 */
	public static long hash(String value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * Calculates a hash value that is guaranteed to be between {@code 0L} and {@code bucketCount}
	 * (inclusive), or {@code -1L} if the given value is {@code null}. If {@code bucketCount} is 0,
	 * the constant value 0 will be returned without any calculation being performed. The maximum
	 * value for {@code bucketCount} is 4294967295 (hex 0xFFFFFFFFL). The {@code seed} value can be
	 * used to further alter the hash computation, and only the lower 32 bits of the seed will be
	 * used (i.e. values between 0 and 4294967295).
	 * </p>
	 * <p>
	 * The given {@link String} converted to a byte array using {@link String#getBytes()}, and the
	 * hash is calculated against the resulting byte array.
	 * </p>
	 *
	 * @param value
	 *            the value to calculate the hash for
	 * @param bucketCount
	 *            the (inclusive) upper bound for the 0-based index that will be returned
	 * @param seed
	 *            a seed value to further influence the computed hash
	 * @return a hash value that is guaranteed to be between 0 and {@code bucketCount} (inclusive)
	 */
	public static long hash(String value, long bucketCount, long seed) {
		ByteBuffer buf = null;
		if (value != null) {
			buf = ByteBuffer.wrap(value.getBytes());
		}
		return BucketHasher.hash(buf, bucketCount, seed);
	}

	/**
	 * <p>
	 * Calculates a hash value that is guaranteed to be between {@code 0L} and {@code 4294967295L}
	 * (hex {@code 0xFFFFFFFFL}, inclusive), or {@code -1L} if the given value is {@code null}.
	 * </p>
	 *
	 * @param value
	 *            the value to calculate the hash for
	 * @return a hash value that is guaranteed to be between 0 and {@code bucketCount} (inclusive)
	 */
	public static long hash(byte[] value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * Calculates a hash value that is guaranteed to be between {@code 0L} and {@code bucketCount}
	 * (inclusive), or {@code -1L} if the given value is {@code null}. If {@code bucketCount} is 0,
	 * the constant value 0 will be returned without any calculation being performed. The maximum
	 * value for {@code bucketCount} is 4294967295 (hex 0xFFFFFFFFL).
	 * </p>
	 *
	 * @param value
	 *            the value to calculate the hash for
	 * @param bucketCount
	 *            the (inclusive) upper bound for the 0-based index that will be returned
	 * @return a hash value that is guaranteed to be between 0 and {@code bucketCount} (inclusive)
	 */
	public static long hash(byte[] value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * Calculates a hash value that is guaranteed to be between {@code 0L} and {@code bucketCount}
	 * (inclusive), or {@code -1L} if the given value is {@code null}. If {@code bucketCount} is 0,
	 * the constant value 0 will be returned without any calculation being performed. The maximum
	 * value for {@code bucketCount} is 4294967295 (hex 0xFFFFFFFFL). The {@code seed} value can be
	 * used to further alter the hash computation, and only the lower 32 bits of the seed will be
	 * used (i.e. values between 0 and 4294967295).
	 * </p>
	 *
	 * @param value
	 *            the value to calculate the hash for
	 * @param bucketCount
	 *            the (inclusive) upper bound for the 0-based index that will be returned
	 * @param seed
	 *            a seed value to further influence the computed hash
	 * @return a hash value that is guaranteed to be between 0 and {@code bucketCount} (inclusive)
	 */
	public static long hash(byte[] value, long bucketCount, long seed) {
		ByteBuffer buf = null;
		if (value != null) {
			buf = ByteBuffer.wrap(value);
		}
		return BucketHasher.hash(buf, bucketCount, seed);
	}

	/**
	 * <p>
	 * Calculates a hash value that is guaranteed to be between {@code 0L} and {@code 4294967295L}
	 * (hex {@code 0xFFFFFFFFL}, inclusive), or {@code -1L} if the given value is {@code null} or
	 * its supplied value (via {@link Supplier#get()}) is {@code null}.
	 * </p>
	 *
	 * @param value
	 *            the value to calculate the hash for
	 * @return a hash value that is guaranteed to be between 0 and {@code bucketCount} (inclusive)
	 */
	public static long hash(Supplier<byte[]> value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * Calculates a hash value that is guaranteed to be between {@code 0L} and {@code bucketCount}
	 * (inclusive), or {@code -1L} if the given value is {@code null} or its supplied value (via
	 * {@link Supplier#get()}) is {@code null}. If {@code bucketCount} is 0, the constant value 0
	 * will be returned without any calculation being performed. The maximum value for
	 * {@code bucketCount} is 4294967295 (hex 0xFFFFFFFFL).
	 * </p>
	 *
	 * @param value
	 *            the value to calculate the hash for
	 * @param bucketCount
	 *            the (inclusive) upper bound for the 0-based index that will be returned
	 * @return a hash value that is guaranteed to be between 0 and {@code bucketCount} (inclusive)
	 */
	public static long hash(Supplier<byte[]> value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * Calculates a hash value that is guaranteed to be between {@code 0L} and {@code bucketCount}
	 * (inclusive), or {@code -1L} if the given value is {@code null} or its supplied value (via
	 * {@link Supplier#get()}) is {@code null}. If {@code bucketCount} is 0, the constant value 0
	 * will be returned without any calculation being performed. The maximum value for
	 * {@code bucketCount} is 4294967295 (hex 0xFFFFFFFFL). The {@code seed} value can be used to
	 * further alter the hash computation, and only the lower 32 bits of the seed will be used (i.e.
	 * values between 0 and 4294967295).
	 * </p>
	 *
	 * @param value
	 *            the {@link Supplier} from where the hash data will be pulled
	 * @param bucketCount
	 *            the (inclusive) upper bound for the 0-based index that will be returned
	 * @param seed
	 *            a seed value to further influence the computed hash
	 * @return a hash value that is guaranteed to be between 0 and {@code bucketCount} (inclusive)
	 */
	public static long hash(Supplier<byte[]> value, long bucketCount, long seed) {
		byte[] data = null;
		if (value != null) {
			data = value.get();
		}
		return BucketHasher.hash(data, bucketCount, seed);
	}

	/**
	 * <p>
	 * Calculates a hash value that is guaranteed to be between {@code 0L} and {@code 4294967295L}
	 * (hex {@code 0xFFFFFFFFL}, inclusive), or {@code -1L} if the given value is {@code null}.
	 * </p>
	 *
	 * @param value
	 *            the value to calculate the hash for
	 * @return a hash value that is guaranteed to be between 0 and {@code bucketCount} (inclusive)
	 */
	public static long hash(ByteBuffer value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * Calculates a hash value that is guaranteed to be between {@code 0L} and {@code bucketCount}
	 * (inclusive), or {@code -1L} if the given value is {@code null}. If {@code bucketCount} is 0,
	 * the constant value 0 will be returned without any calculation being performed. The maximum
	 * value for {@code bucketCount} is 4294967295 (hex 0xFFFFFFFFL).
	 * </p>
	 *
	 * @param value
	 *            the value to calculate the hash for
	 * @param bucketCount
	 *            the (inclusive) upper bound for the 0-based index that will be returned
	 * @return a hash value that is guaranteed to be between 0 and {@code bucketCount} (inclusive)
	 */
	public static long hash(ByteBuffer value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * Calculates a hash value that is guaranteed to be between {@code 0L} and {@code bucketCount}
	 * (inclusive), or {@code -1L} if the given value is {@code null}. If {@code bucketCount} is 0,
	 * the constant value 0 will be returned without any calculation being performed. The maximum
	 * value for {@code bucketCount} is 4294967295 (hex 0xFFFFFFFFL). The {@code seed} value can be
	 * used to further alter the hash computation, and only the lower 32 bits of the seed will be
	 * used (i.e. values between 0 and 4294967295).
	 * </p>
	 *
	 * @param value
	 *            the value to calculate the hash for
	 * @param bucketCount
	 *            the (inclusive) upper bound for the 0-based index that will be returned
	 * @param seed
	 *            a seed value to further influence the computed hash
	 * @return a hash value that is guaranteed to be between 0 and {@code bucketCount} (inclusive)
	 */
	public static long hash(ByteBuffer value, long bucketCount, long seed) {
		CheckedConsumer<MessageDigest, RuntimeException> updater = null;
		if (value != null) {
			updater = (md) -> DigestUtils.updateDigest(md, value);
		}
		return BucketHasher.calculate(updater, bucketCount, seed);
	}

	/**
	 * <p>
	 * Calculates a hash value that is guaranteed to be between {@code 0L} and {@code 4294967295L}
	 * (hex {@code 0xFFFFFFFFL}, inclusive), or {@code -1L} if the given value is {@code null}.
	 * </p>
	 *
	 * @param value
	 *            the value to calculate the hash for
	 * @return a hash value that is guaranteed to be between 0 and {@code bucketCount} (inclusive)
	 * @throws IOException
	 *             if an I/O error occurrs reading from the given stream
	 */
	public static long hash(ReadableByteChannel value) throws IOException {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * Calculates a hash value that is guaranteed to be between {@code 0L} and {@code bucketCount}
	 * (inclusive), or {@code -1L} if the given value is {@code null}. If {@code bucketCount} is 0,
	 * the constant value 0 will be returned without any calculation being performed. The maximum
	 * value for {@code bucketCount} is 4294967295 (hex 0xFFFFFFFFL).
	 * </p>
	 *
	 * @param value
	 *            the value to calculate the hash for
	 * @param bucketCount
	 *            the (inclusive) upper bound for the 0-based index that will be returned
	 * @return a hash value that is guaranteed to be between 0 and {@code bucketCount} (inclusive)
	 * @throws IOException
	 *             if an I/O error occurrs reading from the given stream
	 */
	public static long hash(ReadableByteChannel value, long bucketCount) throws IOException {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * Calculates a hash value that is guaranteed to be between {@code 0L} and {@code bucketCount}
	 * (inclusive), or {@code -1L} if the given value is {@code null}. If {@code bucketCount} is 0,
	 * the constant value 0 will be returned without any calculation being performed. The maximum
	 * value for {@code bucketCount} is 4294967295 (hex 0xFFFFFFFFL). The {@code seed} value can be
	 * used to further alter the hash computation, and only the lower 32 bits of the seed will be
	 * used (i.e. values between 0 and 4294967295).
	 * </p>
	 *
	 * @param value
	 *            the value to calculate the hash for
	 * @param bucketCount
	 *            the (inclusive) upper bound for the 0-based index that will be returned
	 * @param seed
	 *            a seed value to further influence the computed hash
	 * @return a hash value that is guaranteed to be between 0 and {@code bucketCount} (inclusive)
	 * @throws IOException
	 *             if an I/O error occurrs reading from the given stream
	 */
	public static long hash(ReadableByteChannel value, long bucketCount, long seed) throws IOException {
		if (value == null) { return -1; }
		return BucketHasher.hash(Channels.newInputStream(value), bucketCount, seed);
	}

	/**
	 * <p>
	 * Calculates a hash value that is guaranteed to be between {@code 0L} and {@code 4294967295L}
	 * (hex {@code 0xFFFFFFFFL}, inclusive), or {@code -1L} if the given value is {@code null}.
	 * </p>
	 *
	 * @param value
	 *            the value to calculate the hash for
	 * @return a hash value that is guaranteed to be between 0 and {@code bucketCount} (inclusive)
	 * @throws IOException
	 *             if an I/O error occurrs reading from the given stream
	 */
	public static long hash(InputStream value) throws IOException {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * Calculates a hash value that is guaranteed to be between {@code 0L} and {@code bucketCount}
	 * (inclusive), or {@code -1L} if the given value is {@code null}. If {@code bucketCount} is 0,
	 * the constant value 0 will be returned without any calculation being performed. The maximum
	 * value for {@code bucketCount} is 4294967295 (hex 0xFFFFFFFFL).
	 * </p>
	 *
	 * @param value
	 *            the value to calculate the hash for
	 * @param bucketCount
	 *            the (inclusive) upper bound for the 0-based index that will be returned
	 * @return a hash value that is guaranteed to be between 0 and {@code bucketCount} (inclusive)
	 * @throws IOException
	 *             if an I/O error occurrs reading from the given stream
	 */
	public static long hash(InputStream value, long bucketCount) throws IOException {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * Calculates a hash value that is guaranteed to be between {@code 0L} and {@code bucketCount}
	 * (inclusive), or {@code -1L} if the given value is {@code null}. If {@code bucketCount} is 0,
	 * the constant value 0 will be returned without any calculation being performed. The maximum
	 * value for {@code bucketCount} is 4294967295 (hex 0xFFFFFFFFL). The {@code seed} value can be
	 * used to further alter the hash computation, and only the lower 32 bits of the seed will be
	 * used (i.e. values between 0 and 4294967295).
	 * </p>
	 *
	 * @param value
	 *            the value to calculate the hash for
	 * @param bucketCount
	 *            the (inclusive) upper bound for the 0-based index that will be returned
	 * @param seed
	 *            a seed value to further influence the computed hash
	 * @return a hash value that is guaranteed to be between 0 and {@code bucketCount} (inclusive)
	 * @throws IOException
	 *             if an I/O error occurrs reading from the given stream
	 */
	public static long hash(InputStream value, long bucketCount, long seed) throws IOException {
		CheckedConsumer<MessageDigest, IOException> updater = null;
		if (value != null) {
			updater = (md) -> DigestUtils.updateDigest(md, value);
		}
		return BucketHasher.calculate(updater, bucketCount, seed);
	}

	/**
	 * Implements the hash() calculation
	 *
	 * @param <E>
	 * @param updater
	 * @param bucketCount
	 * @param seed
	 * @return
	 * @throws E
	 */
	private static <E extends Throwable> long calculate(CheckedConsumer<MessageDigest, E> updater, long bucketCount,
		long seed) throws E {
		if (updater == null) { return -1; }

		// Ensure both bucketCount and seed values meet the required criteria
		bucketCount &= BucketHasher.BUCKET_MASK;
		seed &= BucketHasher.SEED_MASK;

		if (bucketCount == 0) { return 0; }

		// First things first: consume the data
		MessageDigest md = DigestUtils.getSha1Digest();
		updater.acceptChecked(md);

		// Next, Apply the seed
		if (seed != 0L) {
			int shift = 24;
			for (int i = 0; i < 4; i++) {
				md.update((byte) (seed >> shift));
				shift -= 8;
			}
		}

		// Finally, compute the actual bucket
		byte[] digest = md.digest();
		ByteBuffer buf = ByteBuffer.wrap(digest);

		long bucket = buf.getLong();
		// Strip the signum, and ensure the value is within our bucket range
		return ((bucket & Long.MAX_VALUE) % bucketCount);
	}
}