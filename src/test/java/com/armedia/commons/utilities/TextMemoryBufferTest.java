/**
 * *******************************************************************
 *
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS. REPRODUCTION OF ANY PORTION
 * OF THE SOURCE CODE, CONTAINED HEREIN, OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE, IS
 * STRICTLY PROHIBITED.
 *
 * Confidential Property of Armedia LLC. (c) Copyright Armedia LLC 2011. All Rights reserved.
 *
 * *******************************************************************
 */
package com.armedia.commons.utilities;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.TextMemoryBuffer.TextMemoryBufferReader;

/**
 * @author drivera@armedia.com
 *
 */
public class TextMemoryBufferTest {

	private static final char[] ALPHABET;
	private static final char[] FWD = new char[256];
	private static final char[] REV = new char[256];

	static {
		List<Character> a = new ArrayList<>();
		for (int i = 0; i < 65536; i++) {
			try {
				if (!Character.isDefined(i)) {
					continue;
				}
				switch (Character.getType(i)) {
					case Character.CONTROL:
					case Character.UNASSIGNED:
					case Character.PRIVATE_USE:
					case Character.NON_SPACING_MARK:
						continue;
				}
				a.add(Character.valueOf((char) i));
			} catch (Throwable t) {
				// Not a valid character
			}
		}
		Collections.shuffle(a);
		ALPHABET = new char[a.size()];
		int j = 0;
		for (Character c : a) {
			TextMemoryBufferTest.ALPHABET[j] = c;
			j++;
		}

		for (int i = 0; i < TextMemoryBufferTest.FWD.length; i++) {
			TextMemoryBufferTest.FWD[i] = TextMemoryBufferTest.ALPHABET[i];
			TextMemoryBufferTest.REV[i] = TextMemoryBufferTest.ALPHABET[TextMemoryBufferTest.ALPHABET.length - i - 1];
		}
	}

	private static void randomChars(Random r, char[] c) {
		for (int i = 0; i < c.length; i++) {
			c[i] = TextMemoryBufferTest.ALPHABET[r.nextInt(TextMemoryBufferTest.ALPHABET.length)];
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.TextMemoryBuffer#TextMemoryBuffer()}.
	 */
	@Test
	public void testCharBuffer() {
		TextMemoryBuffer b = new TextMemoryBuffer();
		Assertions.assertEquals(TextMemoryBuffer.DEFAULT_CHUNK_SIZE, b.getChunkSize());
		Assertions.assertEquals(0, b.getAllocatedSize());
		b.close();
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.TextMemoryBuffer#TextMemoryBuffer(int)}.
	 */
	@Test
	public void testCharBufferInt() {
		int[] chunkSizes = {
			Integer.MIN_VALUE, -10, -5, -1, 0, 1, 5, 10, 129, 256, 1024, 2048, Integer.MAX_VALUE
		};
		for (int chunkSize : chunkSizes) {
			TextMemoryBuffer b = new TextMemoryBuffer(chunkSize);
			if (chunkSize < TextMemoryBuffer.MINIMUM_CHUNK_SIZE) {
				Assertions.assertEquals(TextMemoryBuffer.MINIMUM_CHUNK_SIZE, b.getChunkSize());
			} else {
				Assertions.assertEquals(chunkSize, b.getChunkSize());
			}
			Assertions.assertEquals(0, b.getAllocatedSize());
			b.close();
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.TextMemoryBuffer#write(int)}.
	 */
	@Test
	public void testWrite() throws IOException {
		TextMemoryBuffer b = new TextMemoryBuffer();
		for (int i = 0; i < TextMemoryBufferTest.FWD.length; i++) {
			Assertions.assertEquals(i, b.getCurrentSize());
			b.write(TextMemoryBufferTest.FWD[i]);
			Assertions.assertEquals(i + 1, b.getCurrentSize());
		}
		b.close();
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.TextMemoryBuffer#write(char[])}.
	 *
	 * @throws IOException
	 */
	@Test
	public void testWriteByteArray() throws IOException {
		TextMemoryBuffer b = new TextMemoryBuffer();
		for (int i = 0; i < TextMemoryBufferTest.FWD.length; i++) {
			Assertions.assertEquals(i * TextMemoryBufferTest.FWD.length, b.getCurrentSize());
			b.write(TextMemoryBufferTest.FWD);
			Assertions.assertEquals((i + 1) * TextMemoryBufferTest.FWD.length, b.getCurrentSize());
		}
		Assertions.assertThrows(NullPointerException.class, () -> b.write((char[]) null));
		b.close();
	}

	/**
	 * Test method for
	 * {@link com.armedia.commons.utilities.TextMemoryBuffer#write(char[], int, int)}.
	 *
	 * @throws IOException
	 */
	@Test
	public void testWriteByteArrayIntInt() throws IOException {
		try (TextMemoryBuffer b = new TextMemoryBuffer()) {
			Assertions.assertThrows(NullPointerException.class, () -> b.write((char[]) null, 0, 0));
			Assertions.assertThrows(NullPointerException.class, () -> b.write((char[]) null, 0, -1));
			Assertions.assertThrows(NullPointerException.class, () -> b.write((char[]) null, -1, 0));
			Assertions.assertThrows(NullPointerException.class, () -> b.write((char[]) null, -1, -1));
			Assertions.assertThrows(NullPointerException.class,
				() -> b.write((char[]) null, 0, TextMemoryBufferTest.FWD.length));
			Assertions.assertThrows(NullPointerException.class,
				() -> b.write((char[]) null, 1, TextMemoryBufferTest.FWD.length));

			b.write(TextMemoryBufferTest.FWD, 0, 0);
			Assertions.assertThrows(IllegalArgumentException.class, () -> b.write(TextMemoryBufferTest.FWD, 0, -1));
			Assertions.assertThrows(IllegalArgumentException.class, () -> b.write(TextMemoryBufferTest.FWD, -1, 0));
			Assertions.assertThrows(IllegalArgumentException.class, () -> b.write(TextMemoryBufferTest.FWD, -1, -1));

			b.write(TextMemoryBufferTest.FWD, 0, TextMemoryBufferTest.FWD.length);
			Assertions.assertThrows(IllegalArgumentException.class,
				() -> b.write(TextMemoryBufferTest.FWD, 1, TextMemoryBufferTest.FWD.length));
		}

		try (TextMemoryBuffer b = new TextMemoryBuffer()) {
			long writeCount = 0;
			for (int o = 0; o < TextMemoryBufferTest.FWD.length; o++) {
				int maxLen = Math.min(TextMemoryBufferTest.FWD.length / 2, TextMemoryBufferTest.FWD.length - o);
				for (int l = 0; l < maxLen; l++) {
					b.write(TextMemoryBufferTest.FWD, o, l);

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
		for (int chunkSize = TextMemoryBuffer.MINIMUM_CHUNK_SIZE; chunkSize < 512; chunkSize++) {
			for (int writeCount : writeCounts) {
				TextMemoryBuffer b = new TextMemoryBuffer(chunkSize);
				Assertions.assertEquals(chunkSize, b.getChunkSize());
				Assertions.assertEquals(0, b.getAllocatedSize());

				for (int c = 0; c < writeCount; c++) {
					b.write(TextMemoryBufferTest.FWD);
				}

				int totalSize = TextMemoryBufferTest.FWD.length * writeCount;
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
		for (int chunkSize = TextMemoryBuffer.MINIMUM_CHUNK_SIZE; chunkSize < 512; chunkSize++) {
			for (int writeCount : writeCounts) {
				TextMemoryBuffer b = new TextMemoryBuffer(chunkSize);
				Assertions.assertEquals(chunkSize, b.getChunkSize());
				Assertions.assertEquals(0, b.getCurrentSize());

				for (int c = 0; c < writeCount; c++) {
					b.write(TextMemoryBufferTest.FWD);
				}

				Assertions.assertEquals(TextMemoryBufferTest.FWD.length * writeCount, b.getCurrentSize());
				b.close();
			}
		}
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.TextMemoryBuffer#close()}.
	 */
	@Test
	public void testClose() throws IOException {
		TextMemoryBuffer buf = new TextMemoryBuffer();
		buf.write(TextMemoryBufferTest.FWD);
		buf.close();
		Assertions.assertThrows(IOException.class, () -> buf.write(TextMemoryBufferTest.REV));
		Assertions.assertThrows(IOException.class, () -> buf.write(TextMemoryBufferTest.REV, 30, 40));
		Assertions.assertThrows(IOException.class, () -> buf.write(TextMemoryBufferTest.FWD[30]));
	}

	@Test
	public void testInput() throws IOException {
		char[] buf = new char[TextMemoryBufferTest.FWD.length];
		TextMemoryBuffer b = new TextMemoryBuffer();
		b.write(TextMemoryBufferTest.FWD);
		b.write(TextMemoryBufferTest.REV);
		try (Reader in = b.getReader()) {
			for (int i = 0; i < TextMemoryBufferTest.FWD.length; i++) {
				int r = in.read();
				Assertions.assertTrue(r >= 0);
				Assertions.assertEquals(TextMemoryBufferTest.FWD[i], r);
			}
			for (int i = 0; i < TextMemoryBufferTest.REV.length; i++) {
				int r = in.read();
				Assertions.assertTrue(r >= 0);
				Assertions.assertEquals(TextMemoryBufferTest.REV[i], r);
			}
		}

		try (Reader in = b.getReader()) {
			in.read(buf);
			Assertions.assertArrayEquals(TextMemoryBufferTest.FWD, buf);
			in.read(buf, 0, buf.length);
			Assertions.assertArrayEquals(TextMemoryBufferTest.REV, buf);

			Assertions.assertThrows(NullPointerException.class, () -> in.read((char[]) null));
			Assertions.assertThrows(NullPointerException.class, () -> in.read(null, 0, 0));
			Assertions.assertThrows(NullPointerException.class, () -> in.read(null, 0, -1));
			Assertions.assertThrows(NullPointerException.class, () -> in.read(null, -1, 0));
			Assertions.assertThrows(NullPointerException.class, () -> in.read(null, -1, -1));
			Assertions.assertThrows(NullPointerException.class, () -> in.read((char[]) null));

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
	public void testBlockingInput() throws IOException {

		TextMemoryBuffer b = new TextMemoryBuffer();
		final Reader in = b.getReader();
		final AtomicInteger counter = new AtomicInteger(0);
		final AtomicReference<Throwable> thrown = new AtomicReference<>();
		final AtomicBoolean finished = new AtomicBoolean(false);
		final int blockTimeMs = 100;
		final int itemCount = 64;
		final int itemStep = (TextMemoryBufferTest.FWD.length / itemCount);
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
					for (int i = 0; i < TextMemoryBufferTest.FWD.length; i += itemStep) {
						final char b = TextMemoryBufferTest.FWD[i];
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
						Assertions.assertEquals(b, r);
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
		for (int i = 0; i < TextMemoryBufferTest.FWD.length; i += itemStep) {
			if (!t.isAlive()) {
				break;
			}
			final char w = TextMemoryBufferTest.FWD[i];
			try {
				ioBarrier.await();
				Assertions.assertFalse(ioBarrier.isBroken(), "IO barrier broken in assertion");
			} catch (InterruptedException e) {
				t.interrupt();
				Assertions.fail(String.format("IO barrier broken due to thread interruption (%s)", w));
			} catch (BrokenBarrierException e) {
				Assertions.fail(String.format("IO barrier broken due to broken barrier exception (%s)", w));
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
				throw new RuntimeException(String.format("Failed to complete the read, caught an exception: %s", ex),
					ex);
			}
			Assertions.assertTrue(finished.get(), "Failed to complete the read - failed early");
			Assertions.assertEquals(itemCount, counter.get(), "Failed to complete the read - fell short");
		} finally {
			b.close();
		}
	}

	@Test
	public void testBlockingInputClosure() {
		TextMemoryBuffer b = null;
		final AtomicReference<Throwable> thrown = new AtomicReference<>();
		final AtomicBoolean started = new AtomicBoolean(false);
		final AtomicBoolean finished = new AtomicBoolean(false);
		final AtomicReference<Reader> in = new AtomicReference<>();

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
							char[] buf = new char[128];
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
							char[] buf = new char[128];
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
				b = new TextMemoryBuffer();
				in.set(b.getReader());
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
		TextMemoryBuffer b = null;
		final AtomicReference<Throwable> thrown = new AtomicReference<>();
		final AtomicBoolean started = new AtomicBoolean(false);
		final AtomicBoolean finished = new AtomicBoolean(false);
		final AtomicReference<Reader> in = new AtomicReference<>();

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
							char[] buf = new char[128];
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
							char[] buf = new char[128];
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
				b = new TextMemoryBuffer();
				in.set(b.getReader());
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
		TextMemoryBuffer b = null;
		final AtomicReference<Throwable> thrown = new AtomicReference<>();
		final AtomicBoolean started = new AtomicBoolean(false);
		final AtomicBoolean finished = new AtomicBoolean(false);
		final AtomicReference<Reader> in = new AtomicReference<>();

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
							char[] buf = new char[128];
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
							char[] buf = new char[128];
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
				b = new TextMemoryBuffer();
				started.set(false);
				finished.set(false);
				thrown.set(null);
				in.set(b.getReader());
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
		TextMemoryBuffer b = new TextMemoryBuffer();
		Random r = new Random(System.currentTimeMillis());
		char[] random = new char[1024];
		TextMemoryBufferTest.randomChars(r, random);
		b.write(random);

		TextMemoryBufferReader in = b.getReader();
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
		char[] segment = new char[length];
		System.arraycopy(random, start, segment, 0, length);
		for (int i = 0; i < length; i++) {
			final int v = in.read();
			Assertions.assertFalse(v == -1);
			Assertions.assertEquals(segment[i], v, String.format("Failed to read byte %d", i));
		}
		in.reset();
		Assertions.assertEquals(available - start, in.available());
		for (int i = 0; i < length; i++) {
			final int v = in.read();
			Assertions.assertFalse(v == -1);
			Assertions.assertEquals(segment[i], v, String.format("Failed to read byte %d", i));
		}

		b.close();
	}

	@Test
	public void testCharSequence() throws IOException {
		TextMemoryBuffer buf = new TextMemoryBuffer();
		Random r = new Random(System.currentTimeMillis());
		char[] random = new char[128];
		TextMemoryBufferTest.randomChars(r, random);
		buf.write(random);
		buf.close();

		String randomString = new String(random);

		final CharSequence a = randomString;
		final CharSequence b = buf.getCharSequence();

		Assertions.assertEquals(a.length(), b.length());

		for (int i = 0; i < a.length(); i++) {
			Assertions.assertEquals(a.charAt(i), b.charAt(i), String.format("Difference in position %d", i));
		}

		for (int o = 0; o < a.length(); o++) {
			for (int l = 0; l < (a.length() - o); l++) {
				CharSequence subA = a.subSequence(o, o + l);
				CharSequence subB = b.subSequence(o, o + l);
				Assertions.assertEquals(subA.length(), subB.length(),
					String.format("Length failed at parameters (%d,%d)", o, l));
				for (int i = 0; i < subA.length(); i++) {
					try {
						final char ca = subA.charAt(i);
						final char cb = subB.charAt(i);
						Assertions.assertEquals(ca, cb,
							String.format("Length failed at parameters (%d,%d,%d)", o, l, i));
					} catch (ArrayIndexOutOfBoundsException e) {
						Assertions.fail(String.format("Array Index failed at parameters (%d,%d,%d)", o, l, i));
					}
				}
			}
		}
	}
}