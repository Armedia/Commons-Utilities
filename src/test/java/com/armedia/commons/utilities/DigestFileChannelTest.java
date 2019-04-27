package com.armedia.commons.utilities;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.DigestFileChannel.DigestFileLock;

public class DigestFileChannelTest {

	private static final String NULL_STRING = null;
	private static final MessageDigest NULL_DIGEST = null;
	private static final MessageDigest SHA256;
	static {
		try {
			SHA256 = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testDigestFileChannel() throws Exception {
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestFileChannel(null, DigestFileChannelTest.NULL_STRING));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestFileChannel(null, DigestFileChannelTest.NULL_DIGEST));
		Assertions.assertThrows(NullPointerException.class, () -> new DigestFileChannel(null, ""));
		Assertions.assertThrows(NullPointerException.class, () -> new DigestFileChannel(null, ""));

		final File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
		tempFile.deleteOnExit();

		FileChannel fc = EasyMock.createStrictMock(FileChannel.class);
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestFileChannel(fc, DigestFileChannelTest.NULL_STRING));
		Assertions.assertThrows(NullPointerException.class,
			() -> new DigestFileChannel(fc, DigestFileChannelTest.NULL_DIGEST));
		Assertions.assertThrows(NoSuchAlgorithmException.class, () -> new DigestFileChannel(fc, ""));

		try (RandomAccessFile raf = new RandomAccessFile(tempFile, "r")) {
			try (DigestFileChannel c = new DigestFileChannel(raf.getChannel(),
				DigestFileChannelTest.SHA256.getAlgorithm())) {
			}
		}
		try (RandomAccessFile raf = new RandomAccessFile(tempFile, "r")) {
			try (DigestFileChannel c = new DigestFileChannel(raf.getChannel(), DigestFileChannelTest.SHA256)) {
			}
		}
	}

	@Test
	public void testGetDigest() throws Exception {
		final File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
		tempFile.deleteOnExit();
		for (Provider p : Security.getProviders()) {
			for (Service s : p.getServices()) {
				if (StringUtils.equals(MessageDigest.class.getSimpleName(), s.getType())) {
					MessageDigest digest = MessageDigest.getInstance(s.getAlgorithm());
					try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rws")) {
						digest.reset();
						try (DigestFileChannel c = new DigestFileChannel(raf.getChannel(), digest)) {
							Assertions.assertSame(digest, c.getDigest());
						}
					}
					try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rws")) {
						digest.reset();
						try (DigestFileChannel c = new DigestFileChannel(raf.getChannel(), digest.getAlgorithm())) {
							Assertions.assertNotSame(digest, c.getDigest());
							Assertions.assertEquals(digest.getAlgorithm(), c.getDigest().getAlgorithm());
						}
					}
				}
			}
		}
	}

	@Test
	public void testCollectHash() throws Exception {
		final File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
		tempFile.deleteOnExit();
		final Random r = new Random(System.nanoTime());
		for (Provider p : Security.getProviders()) {
			for (Service s : p.getServices()) {
				if (StringUtils.equals(MessageDigest.class.getSimpleName(), s.getType())) {
					MessageDigest digest = MessageDigest.getInstance(s.getAlgorithm());
					byte[] data = new byte[16384];
					byte[] hash = null;
					try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rws")) {
						r.nextBytes(data);
						raf.write(data);
						digest.update(data);
						hash = digest.digest();
					}
					try (RandomAccessFile raf = new RandomAccessFile(tempFile, "r")) {
						digest.reset();
						DigestFileChannel dfc = new DigestFileChannel(raf.getChannel(), digest);
						try (DigestFileChannel c = dfc) {
						}
						Pair<Long, byte[]> actual = dfc.collectHash();
						Assertions.assertEquals(Hex.encodeHexString(hash), Hex.encodeHexString(actual.getRight()),
							String.format("Mismatch evaluating %s", s.getAlgorithm()));
						Assertions.assertArrayEquals(hash, actual.getRight());
						Assertions.assertEquals(data.length, actual.getLeft().longValue());
					}
				}
			}
		}
	}

	@Test
	public void testResetHash() throws Exception {
		final File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
		tempFile.deleteOnExit();
		final Random r = new Random(System.nanoTime());
		for (Provider p : Security.getProviders()) {
			for (Service s : p.getServices()) {
				if (StringUtils.equals(MessageDigest.class.getSimpleName(), s.getType())) {
					MessageDigest digest = MessageDigest.getInstance(s.getAlgorithm());
					byte[] data = new byte[16384];
					byte[] hash = null;
					try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rws")) {
						r.nextBytes(data);
						raf.write(data);
						digest.update(data);
						digest.reset();
						hash = digest.digest();
					}
					try (RandomAccessFile raf = new RandomAccessFile(tempFile, "r")) {
						digest.reset();
						DigestFileChannel dfc = new DigestFileChannel(raf.getChannel(), digest);
						try (DigestFileChannel c = dfc) {
						}
						dfc.resetHash();
						Pair<Long, byte[]> actual = dfc.collectHash();
						Assertions.assertEquals(Hex.encodeHexString(hash), Hex.encodeHexString(actual.getRight()),
							String.format("Mismatch evaluating %s", s.getAlgorithm()));
						Assertions.assertArrayEquals(hash, actual.getRight());
						Assertions.assertEquals(0, actual.getLeft().longValue());
					}
				}
			}
		}
	}

	@Test
	@SuppressWarnings("resource")
	public void testDelegateRead() throws Exception {
		final Random r = new Random(System.nanoTime());
		final FileChannel fc = EasyMock.createStrictMock(FileChannel.class);
		final ByteBuffer nullBuf = null;
		final ByteBuffer[] buf = {
			EasyMock.createStrictMock(ByteBuffer.class), EasyMock.createStrictMock(ByteBuffer.class),
			EasyMock.createStrictMock(ByteBuffer.class), EasyMock.createStrictMock(ByteBuffer.class),
			EasyMock.createStrictMock(ByteBuffer.class), EasyMock.createStrictMock(ByteBuffer.class),
			EasyMock.createStrictMock(ByteBuffer.class), EasyMock.createStrictMock(ByteBuffer.class)
		};

		// int read(ByteBuffer dst) throws IOException
		{
			for (int ret = 0; ret < 1000; ret++) {
				EasyMock.reset((Object[]) buf);
				EasyMock.replay((Object[]) buf);
				EasyMock.reset(fc);
				EasyMock.expect(fc.read(EasyMock.same(buf[0]))).andReturn(ret).once();
				EasyMock.replay(fc);
				DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
				Assertions.assertEquals(ret, dfc.read(buf[0]));
				EasyMock.verify(fc);
				EasyMock.verify((Object[]) buf);
			}
		}

		{
			EasyMock.reset((Object[]) buf);
			EasyMock.replay((Object[]) buf);
			EasyMock.reset(fc);
			EasyMock.expect(fc.read(EasyMock.same(buf[0]))).andThrow(new IOException()).once();
			EasyMock.replay(fc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(IOException.class, () -> dfc.read(buf[0]));
			EasyMock.verify(fc);
			EasyMock.verify((Object[]) buf);
		}

		{
			EasyMock.reset((Object[]) buf);
			EasyMock.replay((Object[]) buf);
			EasyMock.reset(fc);
			EasyMock.expect(fc.read(EasyMock.isNull(ByteBuffer.class))).andThrow(new RuntimeException()).once();
			EasyMock.replay(fc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(RuntimeException.class, () -> dfc.read(nullBuf));
			EasyMock.verify(fc);
			EasyMock.verify((Object[]) buf);
		}

		// public int read(ByteBuffer dst, long position) throws IOException
		{
			for (long pos = 0; pos < 1000; pos++) {
				final int ret = r.nextInt();
				EasyMock.reset((Object[]) buf);
				EasyMock.replay((Object[]) buf);
				EasyMock.reset(fc);
				EasyMock.expect(fc.read(EasyMock.same(buf[0]), EasyMock.eq(pos))).andReturn(ret).once();
				EasyMock.replay(fc);
				DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
				Assertions.assertEquals(ret, dfc.read(buf[0], pos));
				EasyMock.verify(fc);
				EasyMock.verify((Object[]) buf);
			}
		}

		{
			for (long pos = 0; pos < 1000; pos++) {
				EasyMock.reset((Object[]) buf);
				EasyMock.replay((Object[]) buf);
				EasyMock.reset(fc);
				final long P = pos;
				EasyMock.expect(fc.read(EasyMock.same(buf[0]), EasyMock.eq(pos))).andThrow(new IOException()).once();
				EasyMock.replay(fc);
				DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
				Assertions.assertThrows(IOException.class, () -> dfc.read(buf[0], P));
				EasyMock.verify(fc);
				EasyMock.verify((Object[]) buf);
			}
		}

		{
			for (long pos = 0; pos < 1000; pos++) {
				EasyMock.reset((Object[]) buf);
				EasyMock.replay((Object[]) buf);
				EasyMock.reset(fc);
				final long P = pos;
				EasyMock.expect(fc.read(EasyMock.isNull(ByteBuffer.class), EasyMock.eq(pos)))
					.andThrow(new RuntimeException()).once();
				EasyMock.replay(fc);
				DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
				Assertions.assertThrows(RuntimeException.class, () -> dfc.read(null, P));
				EasyMock.verify(fc);
				EasyMock.verify((Object[]) buf);
			}
		}

		// long read(ByteBuffer[] dsts, int offset, int length) throws IOException
		{
			for (int off = 0; off < 10; off++) {
				for (int len = 0; len < 10; len++) {
					final long ret = r.nextLong();
					EasyMock.reset((Object[]) buf);
					EasyMock.replay((Object[]) buf);
					EasyMock.reset(fc);
					EasyMock.expect(fc.read(EasyMock.same(buf), EasyMock.eq(off), EasyMock.eq(len))).andReturn(ret)
						.once();
					EasyMock.replay(fc);
					DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
					Assertions.assertEquals(ret, dfc.read(buf, off, len));
					EasyMock.verify(fc);
					EasyMock.verify((Object[]) buf);
				}
			}
		}

		{
			for (int off = 0; off < 10; off++) {
				for (int len = 0; len < 10; len++) {
					EasyMock.reset((Object[]) buf);
					EasyMock.replay((Object[]) buf);
					EasyMock.reset(fc);
					final int O = off;
					final int L = len;
					EasyMock.expect(fc.read(EasyMock.same(buf), EasyMock.eq(O), EasyMock.eq(L)))
						.andThrow(new IOException()).once();
					EasyMock.replay(fc);
					DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
					Assertions.assertThrows(IOException.class, () -> dfc.read(buf, O, L));
					EasyMock.verify(fc);
					EasyMock.verify((Object[]) buf);
				}
			}
		}

		{
			for (int off = 0; off < 10; off++) {
				for (int len = 0; len < 10; len++) {
					EasyMock.reset((Object[]) buf);
					EasyMock.replay((Object[]) buf);
					EasyMock.reset(fc);
					final int O = off;
					final int L = len;
					EasyMock.expect(fc.read(EasyMock.isNull(buf.getClass()), EasyMock.eq(off), EasyMock.eq(len)))
						.andThrow(new RuntimeException()).once();
					EasyMock.replay(fc);
					DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
					Assertions.assertThrows(RuntimeException.class, () -> dfc.read(null, O, L));
					EasyMock.verify(fc);
					EasyMock.verify((Object[]) buf);
				}
			}
		}
	}

	@Test
	@SuppressWarnings("resource")
	public void testDelegateWrite() throws Exception {
		final Random r = new Random(System.nanoTime());
		final FileChannel fc = EasyMock.createStrictMock(FileChannel.class);
		final ByteBuffer nullBuf = null;
		final ByteBuffer[] buf = {
			EasyMock.createStrictMock(ByteBuffer.class), EasyMock.createStrictMock(ByteBuffer.class),
			EasyMock.createStrictMock(ByteBuffer.class), EasyMock.createStrictMock(ByteBuffer.class),
			EasyMock.createStrictMock(ByteBuffer.class), EasyMock.createStrictMock(ByteBuffer.class),
			EasyMock.createStrictMock(ByteBuffer.class), EasyMock.createStrictMock(ByteBuffer.class)
		};

		// int write(ByteBuffer dst) throws IOException
		{
			for (int ret = 0; ret < 1000; ret++) {
				EasyMock.reset((Object[]) buf);
				EasyMock.replay((Object[]) buf);
				EasyMock.reset(fc);
				EasyMock.expect(fc.write(EasyMock.same(buf[0]))).andReturn(ret).once();
				EasyMock.replay(fc);
				DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
				Assertions.assertEquals(ret, dfc.write(buf[0]));
				EasyMock.verify(fc);
				EasyMock.verify((Object[]) buf);
			}
		}

		{
			EasyMock.reset((Object[]) buf);
			EasyMock.replay((Object[]) buf);
			EasyMock.reset(fc);
			EasyMock.expect(fc.write(EasyMock.same(buf[0]))).andThrow(new IOException()).once();
			EasyMock.replay(fc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(IOException.class, () -> dfc.write(buf[0]));
			EasyMock.verify(fc);
			EasyMock.verify((Object[]) buf);
		}

		{
			EasyMock.reset((Object[]) buf);
			EasyMock.replay((Object[]) buf);
			EasyMock.reset(fc);
			EasyMock.expect(fc.write(EasyMock.isNull(ByteBuffer.class))).andThrow(new RuntimeException()).once();
			EasyMock.replay(fc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(RuntimeException.class, () -> dfc.write(nullBuf));
			EasyMock.verify(fc);
			EasyMock.verify((Object[]) buf);
		}

		// public int write(ByteBuffer dst, long position) throws IOException
		{
			for (long pos = 0; pos < 1000; pos++) {
				final int ret = r.nextInt();
				EasyMock.reset((Object[]) buf);
				EasyMock.replay((Object[]) buf);
				EasyMock.reset(fc);
				EasyMock.expect(fc.write(EasyMock.same(buf[0]), EasyMock.eq(pos))).andReturn(ret).once();
				EasyMock.replay(fc);
				DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
				Assertions.assertEquals(ret, dfc.write(buf[0], pos));
				EasyMock.verify(fc);
				EasyMock.verify((Object[]) buf);
			}
		}

		{
			for (long pos = 0; pos < 1000; pos++) {
				EasyMock.reset((Object[]) buf);
				EasyMock.replay((Object[]) buf);
				EasyMock.reset(fc);
				final long P = pos;
				EasyMock.expect(fc.write(EasyMock.same(buf[0]), EasyMock.eq(pos))).andThrow(new IOException()).once();
				EasyMock.replay(fc);
				DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
				Assertions.assertThrows(IOException.class, () -> dfc.write(buf[0], P));
				EasyMock.verify(fc);
				EasyMock.verify((Object[]) buf);
			}
		}

		{
			for (long pos = 0; pos < 1000; pos++) {
				EasyMock.reset((Object[]) buf);
				EasyMock.replay((Object[]) buf);
				EasyMock.reset(fc);
				final long P = pos;
				EasyMock.expect(fc.write(EasyMock.isNull(ByteBuffer.class), EasyMock.eq(pos)))
					.andThrow(new RuntimeException()).once();
				EasyMock.replay(fc);
				DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
				Assertions.assertThrows(RuntimeException.class, () -> dfc.write(null, P));
				EasyMock.verify(fc);
				EasyMock.verify((Object[]) buf);
			}
		}

		// long write(ByteBuffer[] dsts, int offset, int length) throws IOException
		{
			for (int off = 0; off < 10; off++) {
				for (int len = 0; len < 10; len++) {
					final long ret = r.nextLong();
					EasyMock.reset((Object[]) buf);
					EasyMock.replay((Object[]) buf);
					EasyMock.reset(fc);
					EasyMock.expect(fc.write(EasyMock.same(buf), EasyMock.eq(off), EasyMock.eq(len))).andReturn(ret)
						.once();
					EasyMock.replay(fc);
					DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
					Assertions.assertEquals(ret, dfc.write(buf, off, len));
					EasyMock.verify(fc);
					EasyMock.verify((Object[]) buf);
				}
			}
		}

		{
			for (int off = 0; off < 10; off++) {
				for (int len = 0; len < 10; len++) {
					EasyMock.reset((Object[]) buf);
					EasyMock.replay((Object[]) buf);
					EasyMock.reset(fc);
					final int O = off;
					final int L = len;
					EasyMock.expect(fc.write(EasyMock.same(buf), EasyMock.eq(O), EasyMock.eq(L)))
						.andThrow(new IOException()).once();
					EasyMock.replay(fc);
					DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
					Assertions.assertThrows(IOException.class, () -> dfc.write(buf, O, L));
					EasyMock.verify(fc);
					EasyMock.verify((Object[]) buf);
				}
			}
		}

		{
			for (int off = 0; off < 10; off++) {
				for (int len = 0; len < 10; len++) {
					EasyMock.reset((Object[]) buf);
					EasyMock.replay((Object[]) buf);
					EasyMock.reset(fc);
					final int O = off;
					final int L = len;
					EasyMock.expect(fc.write(EasyMock.isNull(buf.getClass()), EasyMock.eq(off), EasyMock.eq(len)))
						.andThrow(new RuntimeException()).once();
					EasyMock.replay(fc);
					DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
					Assertions.assertThrows(RuntimeException.class, () -> dfc.write(null, O, L));
					EasyMock.verify(fc);
					EasyMock.verify((Object[]) buf);
				}
			}
		}
	}

	@Test
	@SuppressWarnings("resource")
	public void testDelegatePosition() throws Exception {
		final Random r = new Random(System.nanoTime());
		final FileChannel fc = EasyMock.createStrictMock(FileChannel.class);

		// long position() throws IOException
		{
			for (int i = 0; i < 1000; i++) {
				final long ret = r.nextLong();
				EasyMock.reset(fc);
				EasyMock.expect(fc.position()).andReturn(ret).once();
				EasyMock.replay(fc);
				DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
				Assertions.assertEquals(ret, dfc.position());
				EasyMock.verify(fc);
			}
		}

		{
			EasyMock.reset(fc);
			EasyMock.expect(fc.position()).andThrow(new IOException()).once();
			EasyMock.replay(fc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(IOException.class, () -> dfc.position());
			EasyMock.verify(fc);
		}

		{
			EasyMock.reset(fc);
			EasyMock.expect(fc.position()).andThrow(new RuntimeException()).once();
			EasyMock.replay(fc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(RuntimeException.class, () -> dfc.position());
			EasyMock.verify(fc);
		}

		// public FileChannel position(long newPosition) throws IOException
		{
			for (int i = 0; i < 1000; i++) {
				final long pos = r.nextLong();
				EasyMock.reset(fc);
				EasyMock.expect(fc.position(EasyMock.eq(pos))).andReturn(fc).once();
				EasyMock.replay(fc);
				DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
				Assertions.assertSame(dfc, dfc.position(pos));
				EasyMock.verify(fc);
			}
		}

		{
			EasyMock.reset(fc);
			EasyMock.expect(fc.position(EasyMock.eq(0L))).andThrow(new IOException()).once();
			EasyMock.replay(fc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(IOException.class, () -> dfc.position(0));
			EasyMock.verify(fc);
		}

		{
			EasyMock.reset(fc);
			EasyMock.expect(fc.position(EasyMock.eq(0L))).andThrow(new RuntimeException()).once();
			EasyMock.replay(fc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(RuntimeException.class, () -> dfc.position(0));
			EasyMock.verify(fc);
		}
	}

	@Test
	@SuppressWarnings("resource")
	public void testDelegateSize() throws Exception {
		final Random r = new Random(System.nanoTime());
		final FileChannel fc = EasyMock.createStrictMock(FileChannel.class);

		// long size() throws IOException
		{
			for (int i = 0; i < 1000; i++) {
				final long size = r.nextLong();
				EasyMock.reset(fc);
				EasyMock.expect(fc.size()).andReturn(size).once();
				EasyMock.replay(fc);
				DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
				Assertions.assertEquals(size, dfc.size());
				EasyMock.verify(fc);
			}
		}

		{
			EasyMock.reset(fc);
			EasyMock.expect(fc.size()).andThrow(new IOException()).once();
			EasyMock.replay(fc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(IOException.class, () -> dfc.size());
			EasyMock.verify(fc);
		}

		{
			EasyMock.reset(fc);
			EasyMock.expect(fc.size()).andThrow(new RuntimeException()).once();
			EasyMock.replay(fc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(RuntimeException.class, () -> dfc.size());
			EasyMock.verify(fc);
		}
	}

	@Test
	@SuppressWarnings("resource")
	public void testDelegateTruncate() throws Exception {
		final Random r = new Random(System.nanoTime());
		final FileChannel fc = EasyMock.createStrictMock(FileChannel.class);

		// public FileChannel truncate(long size) throws IOException
		{
			for (int i = 0; i < 1000; i++) {
				final long size = r.nextLong();
				EasyMock.reset(fc);
				EasyMock.expect(fc.truncate(EasyMock.eq(size))).andReturn(fc).once();
				EasyMock.replay(fc);
				DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
				Assertions.assertSame(dfc, dfc.truncate(size));
				EasyMock.verify(fc);
			}
		}

		{
			EasyMock.reset(fc);
			EasyMock.expect(fc.truncate(EasyMock.eq(0L))).andThrow(new IOException()).once();
			EasyMock.replay(fc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(IOException.class, () -> dfc.truncate(0));
			EasyMock.verify(fc);
		}

		{
			EasyMock.reset(fc);
			EasyMock.expect(fc.truncate(EasyMock.eq(0L))).andThrow(new RuntimeException()).once();
			EasyMock.replay(fc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(RuntimeException.class, () -> dfc.truncate(0));
			EasyMock.verify(fc);
		}
	}

	@Test
	@SuppressWarnings("resource")
	public void testDelegateForce() throws Exception {
		final FileChannel fc = EasyMock.createStrictMock(FileChannel.class);

		// public void force(boolean metaData) throws IOException
		{
			for (int i = 0; i < 2; i++) {
				final boolean force = ((i % 2) == 0);
				EasyMock.reset(fc);
				fc.force(EasyMock.eq(force));
				EasyMock.expectLastCall().once();
				EasyMock.replay(fc);
				DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
				dfc.force(force);
				EasyMock.verify(fc);
			}
		}

		{
			EasyMock.reset(fc);
			fc.force(EasyMock.eq(false));
			EasyMock.expectLastCall().andThrow(new IOException()).once();
			EasyMock.replay(fc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(IOException.class, () -> dfc.force(false));
			EasyMock.verify(fc);
		}

		{
			EasyMock.reset(fc);
			fc.force(EasyMock.eq(false));
			EasyMock.expectLastCall().andThrow(new RuntimeException()).once();
			EasyMock.replay(fc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(RuntimeException.class, () -> dfc.force(false));
			EasyMock.verify(fc);
		}
	}

	@Test
	@SuppressWarnings("resource")
	public void testDelegateMap() throws Exception {
		final FileChannel fc = EasyMock.createStrictMock(FileChannel.class);
		final MappedByteBuffer buf = EasyMock.createMock(MappedByteBuffer.class);
		final MapMode[] mapModes = {
			null, MapMode.PRIVATE, MapMode.READ_ONLY, MapMode.READ_WRITE
		};

		// public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException
		{
			for (MapMode mapMode : mapModes) {
				for (long pos = 0; pos < 10; pos++) {
					for (long len = 0; len < 10; len++) {
						EasyMock.reset(buf);
						EasyMock.replay(buf);
						EasyMock.reset(fc);
						EasyMock.expect(fc.map(EasyMock.same(mapMode), EasyMock.eq(pos), EasyMock.eq(len)))
							.andReturn(buf).once();
						EasyMock.replay(fc);
						DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
						Assertions.assertSame(buf, dfc.map(mapMode, pos, len));
						EasyMock.verify(fc);
						EasyMock.verify(buf);
					}
				}
			}
		}

		{
			EasyMock.reset(buf);
			EasyMock.replay(buf);
			EasyMock.reset(fc);
			EasyMock.expect(fc.map(EasyMock.same(mapModes[0]), EasyMock.eq(0L), EasyMock.eq(0L)))
				.andThrow(new IOException()).once();
			EasyMock.replay(fc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(IOException.class, () -> dfc.map(mapModes[0], 0, 0));
			EasyMock.verify(fc);
			EasyMock.verify(buf);
		}

		{
			EasyMock.reset(buf);
			EasyMock.replay(buf);
			EasyMock.reset(fc);
			EasyMock.expect(fc.map(EasyMock.same(mapModes[0]), EasyMock.eq(0L), EasyMock.eq(0L)))
				.andThrow(new RuntimeException()).once();
			EasyMock.replay(fc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(RuntimeException.class, () -> dfc.map(mapModes[0], 0, 0));
			EasyMock.verify(fc);
			EasyMock.verify(buf);
		}
	}

	@Test
	@SuppressWarnings("resource")
	public void testDelegateLocks() throws Exception {
		final FileChannel fc = EasyMock.createStrictMock(FileChannel.class);
		final boolean[] allShared = {
			false, true
		};

		// public FileLock lock(long position, long size, boolean shared) throws IOException
		for (long pos = 0; pos < 10; pos++) {
			for (long size = 0; size < 10; size++) {
				for (boolean shared : allShared) {
					for (final boolean valid : allShared) {
						EasyMock.reset(fc);
						final AtomicLong releaseCalled = new AtomicLong(0);
						final FileLock fl = new FileLock(fc, pos, size, shared) {

							@Override
							public void release() throws IOException {
								releaseCalled.incrementAndGet();
							}

							@Override
							public boolean isValid() {
								return valid;
							}
						};

						EasyMock.expect(fc.lock(EasyMock.eq(pos), EasyMock.eq(size), EasyMock.eq(shared))).andReturn(fl)
							.once();
						EasyMock.replay(fc);
						DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
						DigestFileLock dfl = dfc.lock(pos, size, shared);
						EasyMock.verify(fc);
						Assertions.assertNotNull(dfl);
						Assertions.assertSame(dfc, dfl.acquiredBy());
						Assertions.assertSame(dfc, dfl.channel());
						Assertions.assertEquals(pos, dfl.position());
						Assertions.assertEquals(size, dfl.size());
						Assertions.assertEquals(shared, dfl.isShared());
						Assertions.assertEquals(valid, dfl.isValid());
						Assertions.assertEquals(0, releaseCalled.get());
						dfl.release();
						Assertions.assertEquals(1, releaseCalled.get());
					}
				}
			}
		}

		{
			EasyMock.reset(fc);
			EasyMock.expect(fc.lock(EasyMock.eq(0L), EasyMock.eq(0L), EasyMock.eq(false))).andThrow(new IOException())
				.once();
			EasyMock.replay(fc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(IOException.class, () -> dfc.lock(0, 0, false));
			EasyMock.verify(fc);
		}

		{
			EasyMock.reset(fc);
			EasyMock.expect(fc.lock(EasyMock.eq(0L), EasyMock.eq(0L), EasyMock.eq(false)))
				.andThrow(new NullPointerException()).once();
			EasyMock.replay(fc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(NullPointerException.class, () -> dfc.lock(0, 0, false));
			EasyMock.verify(fc);
		}

		// public FileLock tryLock(long position, long size, boolean shared) throws IOException
		for (long pos = 0; pos < 10; pos++) {
			for (long size = 0; size < 10; size++) {
				for (boolean shared : allShared) {
					for (final boolean valid : allShared) {
						EasyMock.reset(fc);
						final AtomicLong releaseCalled = new AtomicLong(0);
						final FileLock fl = new FileLock(fc, pos, size, shared) {

							@Override
							public void release() throws IOException {
								releaseCalled.incrementAndGet();
							}

							@Override
							public boolean isValid() {
								return valid;
							}
						};

						EasyMock.expect(fc.tryLock(EasyMock.eq(pos), EasyMock.eq(size), EasyMock.eq(shared)))
							.andReturn(fl).once();
						EasyMock.replay(fc);
						DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
						DigestFileLock dfl = dfc.tryLock(pos, size, shared);
						EasyMock.verify(fc);
						Assertions.assertNotNull(dfl);
						Assertions.assertSame(dfc, dfl.acquiredBy());
						Assertions.assertSame(dfc, dfl.channel());
						Assertions.assertEquals(pos, dfl.position());
						Assertions.assertEquals(size, dfl.size());
						Assertions.assertEquals(shared, dfl.isShared());
						Assertions.assertEquals(valid, dfl.isValid());
						Assertions.assertEquals(0, releaseCalled.get());
						dfl.release();
						Assertions.assertEquals(1, releaseCalled.get());
					}
				}
			}
		}

		{
			EasyMock.reset(fc);
			EasyMock.expect(fc.tryLock(EasyMock.eq(0L), EasyMock.eq(0L), EasyMock.eq(false)))
				.andThrow(new IOException()).once();
			EasyMock.replay(fc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(IOException.class, () -> dfc.tryLock(0, 0, false));
			EasyMock.verify(fc);
		}

		{
			EasyMock.reset(fc);
			EasyMock.expect(fc.tryLock(EasyMock.eq(0L), EasyMock.eq(0L), EasyMock.eq(false)))
				.andThrow(new NullPointerException()).once();
			EasyMock.replay(fc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(NullPointerException.class, () -> dfc.tryLock(0, 0, false));
			EasyMock.verify(fc);
		}
	}

	@Test
	@SuppressWarnings("resource")
	public void testDelegateTransfers() throws Exception {
		final Random r = new Random(System.nanoTime());
		final FileChannel fc = EasyMock.createStrictMock(FileChannel.class);
		final WritableByteChannel wbc = EasyMock.createStrictMock(WritableByteChannel.class);
		final ReadableByteChannel rbc = EasyMock.createStrictMock(ReadableByteChannel.class);

		// public long transferTo(long position, long count, WritableByteChannel target) throws
		// IOException
		{
			for (long pos = 0; pos < 10; pos++) {
				for (long count = 0; count < 10; count++) {
					final long ret = r.nextLong();
					EasyMock.reset(fc, rbc, wbc);
					EasyMock.expect(fc.transferTo(EasyMock.eq(pos), EasyMock.eq(count), EasyMock.same(wbc)))
						.andReturn(ret).once();
					EasyMock.replay(fc, rbc, wbc);
					DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
					Assertions.assertEquals(ret, dfc.transferTo(pos, count, wbc));
					EasyMock.verify(fc, rbc, wbc);
				}
			}
		}

		{
			EasyMock.reset(fc, rbc, wbc);
			EasyMock.expect(fc.transferTo(EasyMock.eq(0L), EasyMock.eq(0L), EasyMock.same(wbc)))
				.andThrow(new IOException()).once();
			EasyMock.replay(fc, rbc, wbc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(IOException.class, () -> dfc.transferTo(0, 0, wbc));
			EasyMock.verify(fc, rbc, wbc);
		}

		{
			EasyMock.reset(fc, rbc, wbc);
			EasyMock.expect(fc.transferTo(EasyMock.eq(0L), EasyMock.eq(0L), EasyMock.same(wbc)))
				.andThrow(new RuntimeException()).once();
			EasyMock.replay(fc, rbc, wbc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(RuntimeException.class, () -> dfc.transferTo(0, 0, wbc));
			EasyMock.verify(fc, rbc, wbc);
		}

		// public long transferFrom(ReadableByteChannel src, long position, long count) throws
		// IOException
		{
			for (long pos = 0; pos < 10; pos++) {
				for (long count = 0; count < 10; count++) {
					final long ret = r.nextLong();
					EasyMock.reset(fc, rbc, wbc);
					EasyMock.expect(fc.transferFrom(EasyMock.same(rbc), EasyMock.eq(pos), EasyMock.eq(count)))
						.andReturn(ret).once();
					EasyMock.replay(fc, rbc, wbc);
					DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
					Assertions.assertEquals(ret, dfc.transferFrom(rbc, pos, count));
					EasyMock.verify(fc, rbc, wbc);
				}
			}
		}

		{
			EasyMock.reset(fc, rbc, wbc);
			EasyMock.expect(fc.transferFrom(EasyMock.same(rbc), EasyMock.eq(0L), EasyMock.eq(0L)))
				.andThrow(new IOException()).once();
			EasyMock.replay(fc, rbc, wbc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(IOException.class, () -> dfc.transferFrom(rbc, 0, 0));
			EasyMock.verify(fc, rbc, wbc);
		}

		{
			EasyMock.reset(fc, rbc, wbc);
			EasyMock.expect(fc.transferFrom(EasyMock.same(rbc), EasyMock.eq(0L), EasyMock.eq(0L)))
				.andThrow(new RuntimeException()).once();
			EasyMock.replay(fc, rbc, wbc);
			DigestFileChannel dfc = new DigestFileChannel(fc, DigestFileChannelTest.SHA256);
			Assertions.assertThrows(RuntimeException.class, () -> dfc.transferFrom(rbc, 0, 0));
			EasyMock.verify(fc, rbc, wbc);
		}
	}
}