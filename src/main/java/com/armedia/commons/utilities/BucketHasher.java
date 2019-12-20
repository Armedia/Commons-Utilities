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

/**
 * <p>
 * This class provides methods to calculate a hash value that is guaranteed to be between {@code 0}
 * and the given {@code bucketCount} (inclusive), or {@code -1} if the value to calculate the hash
 * for results in a {@code null}-value. All methods work in basically the same way: they conver the
 * given {@code value} parameter into a "byte stream" (an {@link InputStream}, a {@link ByteBuffer},
 * or a {@code byte[]}), as this is necessary to feed the underlying {@link MessageDigest} instance.
 * The hash is calculated using SHA1 as the hashing algorithm.
 * </p>
 * <p>
 * The optional {@code bucketCount} value determines the maximum value returned. If this is
 * {@code 0}, the constant value 0 will be returned without performing any computation because
 * (obviously) there can only be one bucket. The maximum value for {@code bucketCount} is
 * {@code 4,294,967,295} (hex {@code 0xFFFFFFFFL}), which is also used as a default when no bucket
 * count value is available as a parameter. If the bucket count given is less than {@code 0}, then
 * an {@link IllegalArgumentException} will be raised. If the bucket count given is greater than the
 * maximum bucket count of {@code 4,294,967,295}, this maximum value will be used instead.
 * </p>
 * <p>
 * The optional {@code seed} value can be used to further alter the hash computation, but only the
 * lower 32 bits of the seed will be used (i.e. values between {@code 0} and {@code 4,294,967,295}).
 * If the seed value is {@code 0}, then no seed computation will be performed.
 * </p>
 */
public class BucketHasher {

	public static final long MIN_BUCKET = 0x00000000L;
	public static final long MAX_BUCKET = 0xFFFFFFFFL;
	public static final long DEF_BUCKET = BucketHasher.MAX_BUCKET;

	public static final long MIN_SEED = 0x00000000L;
	public static final long MAX_SEED = 0xFFFFFFFFL;
	public static final long DEF_SEED = BucketHasher.MIN_SEED;
	private static final long SEED_MASK = 0xFFFFFFFFL;

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
	public static long hash(Byte value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Byte value, long bucketCount, long seed) {
		Consumer<ByteBuffer> converter = null;
		if (value != null) {
			converter = (buf) -> buf.put(value.byteValue());
		}
		return BucketHasher.calculate(converter, 4, bucketCount, seed);
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
	public static long hash(Short value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Short value, long bucketCount, long seed) {
		Consumer<ByteBuffer> converter = null;
		if (value != null) {
			converter = (buf) -> buf.putShort(value.shortValue());
		}
		return BucketHasher.calculate(converter, 2, bucketCount, seed);
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
	public static long hash(Integer value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Integer value, long bucketCount, long seed) {
		Consumer<ByteBuffer> converter = null;
		if (value != null) {
			converter = (buf) -> buf.putInt(value.intValue());
		}
		return BucketHasher.calculate(converter, 4, bucketCount, seed);
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
	public static long hash(Long value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Long value, long bucketCount, long seed) {
		Consumer<ByteBuffer> converter = null;
		if (value != null) {
			converter = (buf) -> buf.putLong(value.longValue());
		}
		return BucketHasher.calculate(converter, 4, bucketCount, seed);
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
	public static long hash(BigInteger value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(BigInteger value, long bucketCount, long seed) {
		if (value == null) { return -1; }
		return BucketHasher.hash(value::toByteArray, bucketCount, seed);
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
	public static long hash(Float value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Float value, long bucketCount, long seed) {
		Consumer<ByteBuffer> converter = null;
		if (value != null) {
			converter = (buf) -> buf.putFloat(value.floatValue());
		}
		return BucketHasher.calculate(converter, 4, bucketCount, seed);
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
	public static long hash(Double value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(Double value, long bucketCount, long seed) {
		Consumer<ByteBuffer> converter = null;
		if (value != null) {
			converter = (buf) -> buf.putDouble(value.doubleValue());
		}
		return BucketHasher.calculate(converter, 4, bucketCount, seed);
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
	public static long hash(BigDecimal value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(BigDecimal value, long bucketCount, long seed) {
		if (value == null) { return -1; }
		return BucketHasher.hash(value.unscaledValue(), bucketCount, seed);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	private static long calculate(Consumer<ByteBuffer> converter, int bytes, long bucketCount, long seed) {
		if (converter == null) { return -1; }
		ByteBuffer buf = ByteBuffer.allocate(bytes);
		converter.accept(buf);
		buf.flip();
		return BucketHasher.hash(buf, bucketCount, seed);
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
	public static long hash(Serializable value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
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
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(String value) {
		return BucketHasher.hash(value, BucketHasher.DEF_BUCKET, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(String value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
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
	public static long hash(byte[] value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
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
	public static long hash(Supplier<byte[]> value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
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
	public static long hash(ByteBuffer value, long bucketCount) {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
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
	public static long hash(ReadableByteChannel value, long bucketCount) throws IOException {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(ReadableByteChannel value, long bucketCount, long seed) throws IOException {
		if (value == null) { return -1; }
		return BucketHasher.hash(Channels.newInputStream(value), bucketCount, seed);
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
	public static long hash(InputStream value, long bucketCount) throws IOException {
		return BucketHasher.hash(value, bucketCount, BucketHasher.DEF_SEED);
	}

	/**
	 * <p>
	 * See the {@link BucketHasher class documentation} for details.
	 * </p>
	 *
	 * @see BucketHasher
	 */
	public static long hash(InputStream value, long bucketCount, long seed) throws IOException {
		CheckedConsumer<MessageDigest, IOException> updater = null;
		if (value != null) {
			updater = (md) -> DigestUtils.updateDigest(md, value);
		}
		return BucketHasher.calculate(updater, bucketCount, seed);
	}

	private static <E extends Throwable> long calculate(CheckedConsumer<MessageDigest, E> updater, long bucketCount,
		long seed) throws E {
		if (updater == null) { return -1; }

		// Sanitize the bucketCount value
		if (bucketCount < 0) { throw new IllegalArgumentException("The bucket count may not be a negative number"); }
		bucketCount = Tools.ensureBetween(BucketHasher.MIN_BUCKET, bucketCount, BucketHasher.MAX_BUCKET);
		if (bucketCount == 0) { return 0; }

		// Sanitize the seed value
		seed &= BucketHasher.SEED_MASK;

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
		ByteBuffer buf = ByteBuffer.wrap(digest);

		long bucket = buf.getLong();
		// Strip the signum, and ensure the value is within our bucket range
		return ((bucket & Long.MAX_VALUE) % bucketCount);
	}
}