/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (c) 2010 - 2019 Armedia LLC
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
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 *
 */
public class BinaryMemoryBufferTest implements GoodService {

	private static final byte[] FWD = new byte[256];
	private static final byte[] REV = new byte[256];

	static {
		for (int i = 0; i < BinaryMemoryBufferTest.FWD.length; i++) {
			BinaryMemoryBufferTest.FWD[i] = (byte) (i & 0xFF);
			BinaryMemoryBufferTest.REV[i] = (byte) ((~BinaryMemoryBufferTest.FWD[i]) & 0xFF);
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.BinaryMemoryBuffer#BinaryMemoryBuffer()}
	 * .
	 */
	@Test
	public void testByteBuffer() {
		BinaryMemoryBuffer b = new BinaryMemoryBuffer();
		Assertions.assertEquals(BinaryMemoryBuffer.DEFAULT_CHUNK_SIZE, b.getChunkSize());
		Assertions.assertEquals(0, b.getAllocatedSize());
		b.close();
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.BinaryMemoryBuffer#BinaryMemoryBuffer(int)}.
	 */
	@Test
	public void testByteBufferInt() {
		int[] chunkSizes = {
			Integer.MIN_VALUE, -10, -5, -1, 0, 1, 5, 10, 129, 256, 1024, 2048, Integer.MAX_VALUE
		};
		for (int chunkSize : chunkSizes) {
			BinaryMemoryBuffer b = new BinaryMemoryBuffer(chunkSize);
			if (chunkSize < BinaryMemoryBuffer.MINIMUM_CHUNK_SIZE) {
				Assertions.assertEquals(BinaryMemoryBuffer.MINIMUM_CHUNK_SIZE, b.getChunkSize());
			} else {
				Assertions.assertEquals(chunkSize, b.getChunkSize());
			}
			Assertions.assertEquals(0, b.getAllocatedSize());
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
			Assertions.assertEquals(i, b.getCurrentSize());
			b.write(BinaryMemoryBufferTest.FWD[i]);
			Assertions.assertEquals(i + 1, b.getCurrentSize());
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
			Assertions.assertEquals(i * BinaryMemoryBufferTest.FWD.length, b.getCurrentSize());
			b.write(BinaryMemoryBufferTest.FWD);
			Assertions.assertEquals((i + 1) * BinaryMemoryBufferTest.FWD.length, b.getCurrentSize());
		}
		Assertions.assertThrows(NullPointerException.class, () -> b.write(null));
		b.close();
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.BinaryMemoryBuffer#write(byte[], int, int)}.
	 *
	 * @throws IOException
	 */
	@Test
	public void testWriteByteArrayIntInt() throws IOException {
		try (BinaryMemoryBuffer b = new BinaryMemoryBuffer()) {
			Assertions.assertThrows(NullPointerException.class, () -> b.write(null, 0, 0));
			Assertions.assertThrows(NullPointerException.class, () -> b.write(null, 0, -1));
			Assertions.assertThrows(NullPointerException.class, () -> b.write(null, -1, 0));
			Assertions.assertThrows(NullPointerException.class, () -> b.write(null, -1, -1));
			Assertions.assertThrows(NullPointerException.class,
				() -> b.write(null, 0, BinaryMemoryBufferTest.FWD.length));
			Assertions.assertThrows(NullPointerException.class,
				() -> b.write(null, 1, BinaryMemoryBufferTest.FWD.length));

			b.write(BinaryMemoryBufferTest.FWD, 0, 0);
			Assertions.assertThrows(IllegalArgumentException.class, () -> b.write(BinaryMemoryBufferTest.FWD, 0, -1));
			Assertions.assertThrows(IllegalArgumentException.class, () -> b.write(BinaryMemoryBufferTest.FWD, -1, 0));
			Assertions.assertThrows(IllegalArgumentException.class, () -> b.write(BinaryMemoryBufferTest.FWD, -1, -1));

			b.write(BinaryMemoryBufferTest.FWD, 0, BinaryMemoryBufferTest.FWD.length);
			Assertions.assertThrows(IllegalArgumentException.class,
				() -> b.write(BinaryMemoryBufferTest.FWD, 1, BinaryMemoryBufferTest.FWD.length));
		}

		try (BinaryMemoryBuffer b = new BinaryMemoryBuffer()) {
			long writeCount = 0;
			for (int o = 0; o < BinaryMemoryBufferTest.FWD.length; o++) {
				int maxLen = Math.min(BinaryMemoryBufferTest.FWD.length / 2, BinaryMemoryBufferTest.FWD.length - o);
				for (int l = 0; l < maxLen; l++) {
					b.write(BinaryMemoryBufferTest.FWD, o, l);

					writeCount += l;
					Assertions.assertEquals(writeCount, b.getCurrentSize());

					long x = (writeCount / b.getChunkSize());
					long y = (writeCount % b.getChunkSize());
					if (y > 0) {
						x++;
					}
					Assertions.assertEquals((x * b.getChunkSize()), b.getAllocatedSize(),
						String.format("Failure with offset %d, length %d", o, l));
				}
			}
		}
	}

	@Test
	public void testGetAllocatedSize() throws IOException {
		int[] writeCounts = {
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
		};
		for (int chunkSize = BinaryMemoryBuffer.MINIMUM_CHUNK_SIZE; chunkSize < 512; chunkSize++) {
			for (int writeCount : writeCounts) {
				BinaryMemoryBuffer b = new BinaryMemoryBuffer(chunkSize);
				Assertions.assertEquals(chunkSize, b.getChunkSize());
				Assertions.assertEquals(0, b.getAllocatedSize());

				for (int c = 0; c < writeCount; c++) {
					b.write(BinaryMemoryBufferTest.FWD);
				}

				int totalSize = BinaryMemoryBufferTest.FWD.length * writeCount;
				int x = (totalSize / chunkSize);
				int y = (totalSize % chunkSize);
				if (y > 0) {
					x++;
				}
				Assertions.assertEquals(x * chunkSize, b.getAllocatedSize(),
					String.format("Failure with chunk size %d, write count %d", chunkSize, writeCount));
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
				Assertions.assertEquals(chunkSize, b.getChunkSize());
				Assertions.assertEquals(0, b.getCurrentSize());

				for (int c = 0; c < writeCount; c++) {
					b.write(BinaryMemoryBufferTest.FWD);
				}

				Assertions.assertEquals(BinaryMemoryBufferTest.FWD.length * writeCount, b.getCurrentSize());
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
		Assertions.assertThrows(IOException.class, () -> buf.write(BinaryMemoryBufferTest.REV));
		Assertions.assertThrows(IOException.class, () -> buf.write(BinaryMemoryBufferTest.REV, 30, 40));
		Assertions.assertThrows(IOException.class, () -> buf.write(BinaryMemoryBufferTest.FWD[30]));
	}

	@Test
	public void testInput() throws IOException {
		byte[] buf = new byte[BinaryMemoryBufferTest.FWD.length];
		BinaryMemoryBuffer b = new BinaryMemoryBuffer();
		b.write(BinaryMemoryBufferTest.FWD);
		b.write(BinaryMemoryBufferTest.REV);
		try (InputStream in = b.getInputStream()) {
			for (int i = 0; i < BinaryMemoryBufferTest.FWD.length; i++) {
				int r = in.read();
				Assertions.assertTrue(r >= 0);
				Assertions.assertEquals(BinaryMemoryBufferTest.FWD[i], (byte) r);
			}
			for (int i = 0; i < BinaryMemoryBufferTest.REV.length; i++) {
				int r = in.read();
				Assertions.assertTrue(r >= 0);
				Assertions.assertEquals(BinaryMemoryBufferTest.REV[i], (byte) r);
			}
		}

		try (InputStream in = b.getInputStream()) {
			in.read(buf);
			Assertions.assertArrayEquals(BinaryMemoryBufferTest.FWD, buf);
			in.read(buf, 0, buf.length);
			Assertions.assertArrayEquals(BinaryMemoryBufferTest.REV, buf);

			Assertions.assertThrows(NullPointerException.class, () -> in.read(null));
			Assertions.assertThrows(NullPointerException.class, () -> in.read(null, 0, 0));
			Assertions.assertThrows(NullPointerException.class, () -> in.read(null, 0, -1));
			Assertions.assertThrows(NullPointerException.class, () -> in.read(null, -1, 0));
			Assertions.assertThrows(NullPointerException.class, () -> in.read(null, -1, -1));
			Assertions.assertThrows(NullPointerException.class, () -> in.read(null));
			Assertions.assertEquals(0, in.read(buf, 0, 0));

			Assertions.assertThrows(IllegalArgumentException.class, () -> in.read(buf, 0, -1));
			Assertions.assertThrows(IllegalArgumentException.class, () -> in.read(buf, -1, 0));
			Assertions.assertThrows(IllegalArgumentException.class, () -> in.read(buf, -1, -1));
			Assertions.assertThrows(IllegalArgumentException.class, () -> in.read(buf, 1, (int) b.getCurrentSize()));

			b.close();
			Assertions.assertEquals(-1, in.read());
			Assertions.assertEquals(0, in.read(buf, 0, 0));
			Assertions.assertEquals(-1, in.read(buf));
			Assertions.assertEquals(-1, in.read(buf, 0, 10));
		}

	}

	@Test
	public void testBlockingInput() throws Throwable {

		BinaryMemoryBuffer b = new BinaryMemoryBuffer();
		final InputStream in = b.getInputStream();
		final AtomicInteger counter = new AtomicInteger(0);
		final AtomicReference<Throwable> thrown = new AtomicReference<>();
		final AtomicBoolean finished = new AtomicBoolean(false);
		final int blockTimeMs = 100;
		final int itemCount = 64;
		final int itemStep = (BinaryMemoryBufferTest.FWD.length / itemCount);
		final CyclicBarrier startBarrier = new CyclicBarrier(2);
		final CyclicBarrier ioBarrier = new CyclicBarrier(2);

		Runnable reader = new Runnable() {
			@Override
			public void run() {
				// We try to block for every byte to be read... ensure we've blocked for at
				// least 5 ms, since the writing thread will write one byte every 10 ms... after
				// 100ms
				try {
					startBarrier.await();
					for (int i = 0; i < BinaryMemoryBufferTest.FWD.length; i += itemStep) {
						final byte b = BinaryMemoryBufferTest.FWD[i];
						if (Thread.currentThread().isInterrupted()) { return; }
						final int r;
						ioBarrier.await();
						long end = 0;
						long now = System.currentTimeMillis();
						try {
							r = in.read();
						} finally {
							end = System.currentTimeMillis();
						}
						if (r == -1) { return; }
						// Tolerate 10ms difference...
						// This check tells us if we really did block
						Assertions.assertTrue((end - now) >= ((blockTimeMs * 4) / 5));
						// This ensures we read the expected value
						Assertions.assertEquals(b, (byte) r);
						// This ensures we account for a successful read
						counter.incrementAndGet();
					}
					finished.set(true);
				} catch (Throwable t) {
					thrown.set(t);
				} finally {
					ioBarrier.reset();
				}
			}
		};

		// Ensure the thread is a daemon thread, just in case
		Thread t = new Thread(reader);
		t.setDaemon(true);
		t.start();

		// Wait for the reader thread to start
		try {
			startBarrier.await();
		} catch (InterruptedException e) {
			t.interrupt();
		} catch (BrokenBarrierException e) {
		}
		Assertions.assertFalse(startBarrier.isBroken(), "Thread start barrier broken");

		// Ensure we don't write until the reader thread is ready to read
		for (int i = 0; i < BinaryMemoryBufferTest.FWD.length; i += itemStep) {
			if (!t.isAlive()) {
				break;
			}
			final byte w = BinaryMemoryBufferTest.FWD[i];
			try {
				ioBarrier.await();
				Assertions.assertFalse(ioBarrier.isBroken(), "IO barrier broken in assertion");
			} catch (InterruptedException e) {
				t.interrupt();
				Assertions.fail(String.format("IO barrier broken due to thread interruption (%d)", w));
			} catch (BrokenBarrierException e) {
				Assertions.fail(String.format("IO barrier broken due to broken barrier exception (%d)", w));
			} finally {
				ioBarrier.reset();
			}

			try {
				Thread.sleep(blockTimeMs);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			b.write(w);
		}

		// Give the reader a short time to catch up, then kill it where it lies
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		t.interrupt();
		Throwable ex = thrown.get();
		try {
			if (ex != null) {
				Assertions.fail(String.format("Failed to complete the read, caught an exception: %s", ex), ex);
			}
			Assertions.assertTrue(finished.get(), "Failed to complete the read - failed early");
			Assertions.assertEquals(itemCount, counter.get(), "Failed to complete the read - fell short");
		} finally {
			b.close();
		}
	}

	@Test
	public void testBlockingInputClosure() {
		BinaryMemoryBuffer b = null;
		final AtomicReference<Throwable> thrown = new AtomicReference<>();
		final AtomicBoolean started = new AtomicBoolean(false);
		final AtomicBoolean finished = new AtomicBoolean(false);
		final AtomicReference<InputStream> in = new AtomicReference<>();

		{
			Runnable[] readers = {
				new Runnable() {
					@Override
					public void run() {
						try {
							started.set(true);
							int r = in.get().read();
							Assertions.assertEquals(r, -1);
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
							Assertions.assertEquals(r, -1);
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
							Assertions.assertEquals(r, -1);
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
				Assertions.assertTrue(started.get());

				b.close();
				try {
					t.join(1000);
				} catch (InterruptedException e) {
					// Do nothing
				}
				Assertions.assertTrue(finished.get());
			}
		}
	}

	@Test
	public void testBlockingInputPartialRead() throws IOException {
		BinaryMemoryBuffer b = null;
		final AtomicReference<Throwable> thrown = new AtomicReference<>();
		final AtomicBoolean started = new AtomicBoolean(false);
		final AtomicBoolean finished = new AtomicBoolean(false);
		final AtomicReference<InputStream> in = new AtomicReference<>();

		{
			Runnable[] readers = {
				new Runnable() {
					@Override
					public void run() {
						try {
							started.set(true);
							int r = in.get().read();
							Assertions.assertFalse(r == -1);
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
							Assertions.assertEquals(r, 1);
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
							Assertions.assertEquals(r, 1);
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
				Assertions.assertTrue(started.get());

				b.write(0);
				try {
					t.join(1000);
				} catch (InterruptedException e) {
					// Do nothing
				}
				Assertions.assertTrue(finished.get());
				b.close();
			}
		}
	}

	@Test
	public void testBlockingInputInterruption() {
		BinaryMemoryBuffer b = null;
		final AtomicReference<Throwable> thrown = new AtomicReference<>();
		final AtomicBoolean started = new AtomicBoolean(false);
		final AtomicBoolean finished = new AtomicBoolean(false);
		final AtomicReference<InputStream> in = new AtomicReference<>();

		{
			Runnable[] readers = {
				new Runnable() {
					@Override
					public void run() {
						try {
							started.set(true);
							int r = in.get().read();
							Assertions.assertEquals(r, -1);
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
							Assertions.assertEquals(r, -1);
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
							Assertions.assertEquals(r, -1);
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
				Assertions.assertTrue(started.get());
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
				Assertions.assertFalse(finished.get());
				Assertions.assertNotNull(thrown.get());
				Assertions.assertEquals(IOException.class, thrown.get().getClass());
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
		Assertions.assertTrue(in.markSupported());
		int start = r.nextInt(512);
		int length = r.nextInt(random.length - start);
		if (length < 512) {
			length = 512;
		}

		long available = in.available();
		Assertions.assertEquals(random.length, in.available());
		in.skip(start);
		Assertions.assertEquals(available - start, in.available());
		in.mark(-1);
		byte[] segment = new byte[length];
		System.arraycopy(random, start, segment, 0, length);
		for (int i = 0; i < length; i++) {
			final int v = in.read();
			Assertions.assertFalse(v == -1);
			Assertions.assertEquals(segment[i], (byte) v, String.format("Failed to read byte %d", i));
		}
		in.reset();
		Assertions.assertEquals(available - start, in.available());
		for (int i = 0; i < length; i++) {
			final int v = in.read();
			Assertions.assertFalse(v == -1);
			Assertions.assertEquals(segment[i], (byte) v, String.format("Failed to read byte %d", i));
		}

		b.close();
	}
}
