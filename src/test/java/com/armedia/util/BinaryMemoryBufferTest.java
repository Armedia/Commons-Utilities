/**
 * *******************************************************************
 * 
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS.
 * REPRODUCTION OF ANY PORTION OF THE SOURCE CODE, CONTAINED HEREIN,
 * OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE,
 * IS STRICTLY PROHIBITED.
 * 
 * Confidential Property of Armedia LLC.
 * (c) Copyright Armedia LLC 2011.
 * All Rights reserved.
 * 
 * *******************************************************************
 */
package com.armedia.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;

import com.armedia.commons.utilities.BinaryMemoryBuffer;

/**
 * @author drivera@armedia.com
 * 
 */
public class BinaryMemoryBufferTest {

	private static final byte[] FWD = new byte[256];
	private static final byte[] REV = new byte[256];

	static {
		for (int i = 0; i < BinaryMemoryBufferTest.FWD.length; i++) {
			BinaryMemoryBufferTest.FWD[i] = (byte) (i & 0xFF);
			BinaryMemoryBufferTest.REV[i] = (byte) ((~BinaryMemoryBufferTest.FWD[i]) & 0xFF);
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.BinaryMemoryBuffer#BinaryMemoryBuffer()}.
	 */
	@Test
	public void testByteBuffer() {
		BinaryMemoryBuffer b = new BinaryMemoryBuffer();
		Assert.assertEquals(BinaryMemoryBuffer.DEFAULT_CHUNK_SIZE, b.getChunkSize());
		Assert.assertEquals(0, b.getAllocatedSize());
		b.close();
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.BinaryMemoryBuffer#BinaryMemoryBuffer(int)}.
	 */
	@Test
	public void testByteBufferInt() {
		int[] chunkSizes = {
			Integer.MIN_VALUE, -10, -5, -1, 0, 1, 5, 10, 129, 256, 1024, 2048, Integer.MAX_VALUE
		};
		for (int chunkSize : chunkSizes) {
			BinaryMemoryBuffer b = new BinaryMemoryBuffer(chunkSize);
			if (chunkSize < BinaryMemoryBuffer.MINIMUM_CHUNK_SIZE) {
				Assert.assertEquals(BinaryMemoryBuffer.MINIMUM_CHUNK_SIZE, b.getChunkSize());
			} else {
				Assert.assertEquals(chunkSize, b.getChunkSize());
			}
			Assert.assertEquals(0, b.getAllocatedSize());
			b.close();
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.BinaryMemoryBuffer#write(int)}.
	 */
	@Test
	public void testWriteInt() throws IOException {
		BinaryMemoryBuffer b = new BinaryMemoryBuffer();
		for (int i = 0; i < BinaryMemoryBufferTest.FWD.length; i++) {
			Assert.assertEquals(i, b.getCurrentSize());
			b.write(BinaryMemoryBufferTest.FWD[i]);
			Assert.assertEquals(i + 1, b.getCurrentSize());
		}
		b.close();
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.BinaryMemoryBuffer#write(byte[])}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testWriteByteArray() throws IOException {
		BinaryMemoryBuffer b = new BinaryMemoryBuffer();
		for (int i = 0; i < BinaryMemoryBufferTest.FWD.length; i++) {
			Assert.assertEquals(i * BinaryMemoryBufferTest.FWD.length, b.getCurrentSize());
			b.write(BinaryMemoryBufferTest.FWD);
			Assert.assertEquals((i + 1) * BinaryMemoryBufferTest.FWD.length, b.getCurrentSize());
		}
		try {
			b.write(null);
			Assert.fail("Failed to raise a NullPointerException");
		} catch (NullPointerException e) {
			// This is ok
		}
		b.close();
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.BinaryMemoryBuffer#write(byte[], int, int)}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testWriteByteArrayIntInt() throws IOException {
		BinaryMemoryBuffer b = new BinaryMemoryBuffer();
		try {
			b.write(null, 0, 0);
			Assert.fail("Failed to raise a NullPointerException");
		} catch (NullPointerException e) {
			// This is ok
		}
		try {
			b.write(null, 0, -1);
			Assert.fail("Failed to raise a NullPointerException");
		} catch (NullPointerException e) {
			// This is ok
		}
		try {
			b.write(null, -1, 0);
			Assert.fail("Failed to raise a NullPointerException");
		} catch (NullPointerException e) {
			// This is ok
		}
		try {
			b.write(null, -1, -1);
			Assert.fail("Failed to raise a NullPointerException");
		} catch (NullPointerException e) {
			// This is ok
		}
		try {
			b.write(null, 0, BinaryMemoryBufferTest.FWD.length);
			Assert.fail("Failed to raise a NullPointerException");
		} catch (NullPointerException e) {
			// This is ok
		}
		try {
			b.write(null, 1, BinaryMemoryBufferTest.FWD.length);
			Assert.fail("Failed to raise a NullPointerException");
		} catch (NullPointerException e) {
			// This is ok
		}

		b.write(BinaryMemoryBufferTest.FWD, 0, 0);
		try {
			b.write(BinaryMemoryBufferTest.FWD, 0, -1);
			Assert.fail("Failed to raise a IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// This is ok
		}
		try {
			b.write(BinaryMemoryBufferTest.FWD, -1, 0);
			Assert.fail("Failed to raise a IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// This is ok
		}
		try {
			b.write(BinaryMemoryBufferTest.FWD, -1, -1);
			Assert.fail("Failed to raise a IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// This is ok
		}
		b.write(BinaryMemoryBufferTest.FWD, 0, BinaryMemoryBufferTest.FWD.length);
		try {
			b.write(BinaryMemoryBufferTest.FWD, 1, BinaryMemoryBufferTest.FWD.length);
			Assert.fail("Failed to raise a IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// This is ok
		}

		b.close();
		b = new BinaryMemoryBuffer();

		long writeCount = 0;
		for (int o = 0; o < BinaryMemoryBufferTest.FWD.length; o++) {
			int maxLen = Math.min(BinaryMemoryBufferTest.FWD.length / 2, BinaryMemoryBufferTest.FWD.length - o);
			for (int l = 0; l < maxLen; l++) {
				b.write(BinaryMemoryBufferTest.FWD, o, l);

				writeCount += l;
				Assert.assertEquals(writeCount, b.getCurrentSize());

				long x = (writeCount / b.getChunkSize());
				long y = (writeCount % b.getChunkSize());
				if (y > 0) {
					x++;
				}
				Assert.assertEquals(String.format("Failure with offset %d, length %d", o, l), (x * b.getChunkSize()),
					b.getAllocatedSize());
			}
		}
		b.close();
	}

	@Test
	public void testGetAllocatedSize() throws IOException {
		int[] writeCounts = {
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
		};
		for (int chunkSize = BinaryMemoryBuffer.MINIMUM_CHUNK_SIZE; chunkSize < 512; chunkSize++) {
			for (int writeCount : writeCounts) {
				BinaryMemoryBuffer b = new BinaryMemoryBuffer(chunkSize);
				Assert.assertEquals(chunkSize, b.getChunkSize());
				Assert.assertEquals(0, b.getAllocatedSize());

				for (int c = 0; c < writeCount; c++) {
					b.write(BinaryMemoryBufferTest.FWD);
				}

				int totalSize = BinaryMemoryBufferTest.FWD.length * writeCount;
				int x = (totalSize / chunkSize);
				int y = (totalSize % chunkSize);
				if (y > 0) {
					x++;
				}
				Assert.assertEquals(String.format("Failure with chunk size %d, write count %d", chunkSize, writeCount),
					x * chunkSize, b.getAllocatedSize());
				b.close();
			}
		}
	}

	@Test
	public void testGetCurrentSize() throws IOException {
		int[] writeCounts = {
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
		};
		for (int chunkSize = BinaryMemoryBuffer.MINIMUM_CHUNK_SIZE; chunkSize < 512; chunkSize++) {
			for (int writeCount : writeCounts) {
				BinaryMemoryBuffer b = new BinaryMemoryBuffer(chunkSize);
				Assert.assertEquals(chunkSize, b.getChunkSize());
				Assert.assertEquals(0, b.getCurrentSize());

				for (int c = 0; c < writeCount; c++) {
					b.write(BinaryMemoryBufferTest.FWD);
				}

				Assert.assertEquals(BinaryMemoryBufferTest.FWD.length * writeCount, b.getCurrentSize());
				b.close();
			}
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.BinaryMemoryBuffer#close()}.
	 */
	@Test
	public void testClose() throws IOException {
		BinaryMemoryBuffer buf = new BinaryMemoryBuffer();
		buf.write(BinaryMemoryBufferTest.FWD);
		buf.close();
		try {
			buf.write(BinaryMemoryBufferTest.REV);
			Assert.fail("Should have failed with IOException");
		} catch (IOException e) {
			// All is well
		}
		try {
			buf.write(BinaryMemoryBufferTest.REV, 30, 40);
			Assert.fail("Should have failed with IOException");
		} catch (IOException e) {
			// All is well
		}
		try {
			buf.write(BinaryMemoryBufferTest.FWD[30]);
			Assert.fail("Should have failed with IOException");
		} catch (IOException e) {
			// All is well
		}
	}

	@Test
	public void testInput() throws IOException {
		BinaryMemoryBuffer b = new BinaryMemoryBuffer();
		b.write(BinaryMemoryBufferTest.FWD);
		b.write(BinaryMemoryBufferTest.REV);
		InputStream in = b.getInputStream();
		for (int i = 0; i < BinaryMemoryBufferTest.FWD.length; i++) {
			int r = in.read();
			Assert.assertTrue(r >= 0);
			Assert.assertEquals(BinaryMemoryBufferTest.FWD[i], (byte) r);
		}
		for (int i = 0; i < BinaryMemoryBufferTest.REV.length; i++) {
			int r = in.read();
			Assert.assertTrue(r >= 0);
			Assert.assertEquals(BinaryMemoryBufferTest.REV[i], (byte) r);
		}
		byte[] buf = new byte[BinaryMemoryBufferTest.FWD.length];

		in = b.getInputStream();
		in.read(buf);
		Assert.assertArrayEquals(BinaryMemoryBufferTest.FWD, buf);
		in.read(buf, 0, buf.length);
		Assert.assertArrayEquals(BinaryMemoryBufferTest.REV, buf);

		try {
			in.read(null);
			Assert.fail("Failed to raise a NullPointerException");
		} catch (NullPointerException e) {
			// All is well
		}

		try {
			in.read(null, 0, 0);
			Assert.fail("Failed to raise a NullPointerException");
		} catch (NullPointerException e) {
			// All is well
		}

		try {
			in.read(null, 0, -1);
			Assert.fail("Failed to raise a NullPointerException");
		} catch (NullPointerException e) {
			// All is well
		}

		try {
			in.read(null, -1, 0);
			Assert.fail("Failed to raise a NullPointerException");
		} catch (NullPointerException e) {
			// All is well
		}

		try {
			in.read(null, -1, -1);
			Assert.fail("Failed to raise a NullPointerException");
		} catch (NullPointerException e) {
			// All is well
		}
		try {
			in.read(null);
			Assert.fail("Failed to raise a NullPointerException");
		} catch (NullPointerException e) {
			// All is well
		}

		Assert.assertEquals(0, in.read(buf, 0, 0));

		try {
			in.read(buf, 0, -1);
			Assert.fail("Failed to raise a IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}

		try {
			in.read(buf, -1, 0);
			Assert.fail("Failed to raise a IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}

		try {
			in.read(buf, -1, -1);
			Assert.fail("Failed to raise a IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// All is well
		}

		try {
			in.read(buf, 1, (int) b.getCurrentSize());
			Assert.fail("Failed to raise a IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// This is ok
		}

		b.close();
		Assert.assertEquals(-1, in.read());
		Assert.assertEquals(0, in.read(buf, 0, 0));
		Assert.assertEquals(-1, in.read(buf));
		Assert.assertEquals(-1, in.read(buf, 0, 10));
	}

	@Test
	public void testBlockingInput() throws IOException {

		BinaryMemoryBuffer b = new BinaryMemoryBuffer();
		final InputStream in = b.getInputStream();
		final AtomicInteger counter = new AtomicInteger(0);
		final AtomicReference<Throwable> thrown = new AtomicReference<Throwable>();
		final AtomicBoolean started = new AtomicBoolean(false);
		final AtomicBoolean finished = new AtomicBoolean(false);
		final AtomicBoolean readyToRead = new AtomicBoolean(false);
		final Object lock = new Object();
		final int blockTimeMs = 10;

		Runnable reader = new Runnable() {
			@Override
			public void run() {
				// We try to block for every byte to be read... ensure we've blocked for at
				// least 5 ms, since the writing thread will write one byte every 10 ms... after
				// 100ms
				try {
					started.set(true);
					for (byte b : BinaryMemoryBufferTest.FWD) {
						if (Thread.currentThread().isInterrupted()) { return; }
						long now = System.currentTimeMillis();
						synchronized (lock) {
							readyToRead.set(true);
							lock.notify();
						}
						int r = in.read();
						synchronized (lock) {
							readyToRead.set(false);
							lock.notify();
						}
						long end = System.currentTimeMillis();
						if (r == -1) { return; }
						// Tolerate 10ms difference...
						// This check tells us if we really did block
						Assert.assertTrue((end - now) >= ((blockTimeMs * 4) / 5));
						// This ensures we read the expected value
						Assert.assertEquals(b, (byte) r);
						// This ensures we account for a successful read
						counter.incrementAndGet();
					}
					finished.set(true);
				} catch (Throwable t) {
					thrown.set(t);
				}
			}
		};
		// Ensure the thread is a daemon thread, just in case
		Thread t = new Thread(reader);
		t.setDaemon(true);
		t.start();
		InterruptedException ex = null;

		// Wait for the reader thread to start
		for (int i = 0; i < 5; i++) {
			if (started.get()) {
				break;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// We wait some more
				ex = e;
			}
		}
		if (ex != null) {
			Thread.currentThread().interrupt();
		}
		Assert.assertTrue(started.get());

		// Ensure we don't write until the reader thread is ready to read
		ex = null;
		for (byte w : BinaryMemoryBufferTest.FWD) {
			synchronized (lock) {
				while (!readyToRead.get() && t.isAlive()) {
					try {
						lock.wait(1000);
					} catch (InterruptedException e) {
						// ignore this
					}
				}
			}
			if (!t.isAlive()) {
				break;
			}
			try {
				Thread.sleep(blockTimeMs);
			} catch (InterruptedException e) {
				ex = e;
			}
			b.write(w);
		}
		if (ex != null) {
			Thread.currentThread().interrupt();
		}

		// Give the reader a short time to catch up, then kill it where it lies
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			if (ex == null) {
				Thread.currentThread().interrupt();
			}
		}
		t.interrupt();
		Assert.assertNull(String.format("Failed to complete the read, caught an exception: %s", thrown.get()),
			thrown.get());
		Assert.assertTrue("Failed to complete the read - failed early", finished.get());
		Assert.assertEquals("Failed to complete the read - fell short", BinaryMemoryBufferTest.FWD.length,
			counter.get());
		b.close();
	}

	@Test
	public void testBlockingInputClosure() {
		BinaryMemoryBuffer b = null;
		final AtomicReference<Throwable> thrown = new AtomicReference<Throwable>();
		final AtomicBoolean started = new AtomicBoolean(false);
		final AtomicBoolean finished = new AtomicBoolean(false);
		final AtomicReference<InputStream> in = new AtomicReference<InputStream>();

		{
			Runnable[] readers = {
				new Runnable() {
					@Override
					public void run() {
						try {
							started.set(true);
							int r = in.get().read();
							Assert.assertEquals(r, -1);
							finished.set(true);
						} catch (Throwable t) {
							thrown.set(t);
						}
					}
				}, new Runnable() {
					@Override
					public void run() {
						try {
							byte[] buf = new byte[128];
							started.set(true);
							int r = in.get().read(buf);
							Assert.assertEquals(r, -1);
							finished.set(true);
						} catch (Throwable t) {
							thrown.set(t);
						}
					}
				}, new Runnable() {
					@Override
					public void run() {
						try {
							byte[] buf = new byte[128];
							started.set(true);
							int r = in.get().read(buf, 10, 20);
							Assert.assertEquals(r, -1);
							finished.set(true);
						} catch (Throwable t) {
							thrown.set(t);
						}
					}
				},
			};

			for (Runnable reader : readers) {
				b = new BinaryMemoryBuffer();
				in.set(b.getInputStream());
				started.set(false);
				finished.set(false);
				thrown.set(null);
				Thread t = new Thread(reader);
				t.setDaemon(true);
				t.start();
				InterruptedException ex = null;

				// Wait for the reader thread to start
				for (int i = 0; i < 5; i++) {
					if (started.get()) {
						break;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// We wait some more
						ex = e;
					}
				}
				if (ex != null) {
					Thread.currentThread().interrupt();
				}
				Assert.assertTrue(started.get());

				b.close();
				try {
					t.join(1000);
				} catch (InterruptedException e) {
					// Do nothing
				}
				Assert.assertTrue(finished.get());
			}
		}
	}

	@Test
	public void testBlockingInputPartialRead() throws IOException {
		BinaryMemoryBuffer b = null;
		final AtomicReference<Throwable> thrown = new AtomicReference<Throwable>();
		final AtomicBoolean started = new AtomicBoolean(false);
		final AtomicBoolean finished = new AtomicBoolean(false);
		final AtomicReference<InputStream> in = new AtomicReference<InputStream>();

		{
			Runnable[] readers = {
				new Runnable() {
					@Override
					public void run() {
						try {
							started.set(true);
							int r = in.get().read();
							Assert.assertFalse(r == -1);
							finished.set(true);
						} catch (Throwable t) {
							thrown.set(t);
						}
					}
				}, new Runnable() {
					@Override
					public void run() {
						try {
							byte[] buf = new byte[128];
							started.set(true);
							int r = in.get().read(buf);
							Assert.assertEquals(r, 1);
							finished.set(true);
						} catch (Throwable t) {
							thrown.set(t);
						}
					}
				}, new Runnable() {
					@Override
					public void run() {
						try {
							byte[] buf = new byte[128];
							started.set(true);
							int r = in.get().read(buf, 0, 128);
							Assert.assertEquals(r, 1);
							finished.set(true);
						} catch (Throwable t) {
							thrown.set(t);
						}
					}
				},
			};

			for (Runnable reader : readers) {
				b = new BinaryMemoryBuffer();
				in.set(b.getInputStream());
				started.set(false);
				finished.set(false);
				thrown.set(null);
				Thread t = new Thread(reader);
				t.setDaemon(true);
				t.start();
				InterruptedException ex = null;

				// Wait for the reader thread to start
				for (int i = 0; i < 5; i++) {
					if (started.get()) {
						break;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// We wait some more
						ex = e;
					}
				}
				if (ex != null) {
					Thread.currentThread().interrupt();
				}
				Assert.assertTrue(started.get());

				b.write(0);
				try {
					t.join(1000);
				} catch (InterruptedException e) {
					// Do nothing
				}
				Assert.assertTrue(finished.get());
				b.close();
			}
		}
	}

	@Test
	public void testBlockingInputInterruption() {
		BinaryMemoryBuffer b = null;
		final AtomicReference<Throwable> thrown = new AtomicReference<Throwable>();
		final AtomicBoolean started = new AtomicBoolean(false);
		final AtomicBoolean finished = new AtomicBoolean(false);
		final AtomicReference<InputStream> in = new AtomicReference<InputStream>();

		{
			Runnable[] readers = {
				new Runnable() {
					@Override
					public void run() {
						try {
							started.set(true);
							int r = in.get().read();
							Assert.assertEquals(r, -1);
							finished.set(true);
						} catch (Throwable t) {
							thrown.set(t);
						}
					}
				}, new Runnable() {
					@Override
					public void run() {
						try {
							byte[] buf = new byte[128];
							started.set(true);
							int r = in.get().read(buf);
							Assert.assertEquals(r, -1);
							finished.set(true);
						} catch (Throwable t) {
							thrown.set(t);
						}
					}
				}, new Runnable() {
					@Override
					public void run() {
						try {
							byte[] buf = new byte[128];
							started.set(true);
							int r = in.get().read(buf, 10, 20);
							Assert.assertEquals(r, -1);
							finished.set(true);
						} catch (Throwable t) {
							thrown.set(t);
						}
					}
				},
			};

			for (Runnable reader : readers) {
				b = new BinaryMemoryBuffer();
				started.set(false);
				finished.set(false);
				thrown.set(null);
				in.set(b.getInputStream());
				Thread t = new Thread(reader);
				t.setDaemon(true);
				t.start();
				InterruptedException ex = null;

				// Wait for the reader thread to start
				for (int i = 0; i < 5; i++) {
					if (started.get()) {
						break;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// We wait some more
						ex = e;
					}
				}
				if (ex != null) {
					Thread.currentThread().interrupt();
				}
				Assert.assertTrue(started.get());
				try {
					// Wait for the thread to be in the read() call
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// ignore this
				}

				t.interrupt();
				try {
					t.join(1000);
				} catch (InterruptedException e) {
					// Do nothing
				}
				Assert.assertFalse(finished.get());
				Assert.assertNotNull(thrown.get());
				Assert.assertEquals(IOException.class, thrown.get().getClass());
			}
		}
	}

	@Test
	public void testInputMarkReset() throws IOException {
		BinaryMemoryBuffer b = new BinaryMemoryBuffer();
		Random r = new Random(System.currentTimeMillis());
		byte[] random = new byte[1024];
		r.nextBytes(random);
		b.write(random);

		InputStream in = b.getInputStream();
		Assert.assertTrue(in.markSupported());
		int start = r.nextInt(512);
		int length = r.nextInt(random.length - start);
		if (length < 512) {
			length = 512;
		}

		long available = in.available();
		Assert.assertEquals(random.length, in.available());
		in.skip(start);
		Assert.assertEquals(available - start, in.available());
		in.mark(-1);
		byte[] segment = new byte[length];
		System.arraycopy(random, start, segment, 0, length);
		for (int i = 0; i < length; i++) {
			final int v = in.read();
			Assert.assertFalse(v == -1);
			Assert.assertEquals(String.format("Failed to read byte %d", i), segment[i], (byte) v);
		}
		in.reset();
		Assert.assertEquals(available - start, in.available());
		for (int i = 0; i < length; i++) {
			final int v = in.read();
			Assert.assertFalse(v == -1);
			Assert.assertEquals(String.format("Failed to read byte %d", i), segment[i], (byte) v);
		}

		b.close();
	}
}