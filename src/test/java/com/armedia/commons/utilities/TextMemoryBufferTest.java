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
package com.armedia.commons.utilities;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;

import com.armedia.commons.utilities.TextMemoryBuffer;
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
		List<Character> a = new ArrayList<Character>();
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
		Assert.assertEquals(TextMemoryBuffer.DEFAULT_CHUNK_SIZE, b.getChunkSize());
		Assert.assertEquals(0, b.getAllocatedSize());
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
				Assert.assertEquals(TextMemoryBuffer.MINIMUM_CHUNK_SIZE, b.getChunkSize());
			} else {
				Assert.assertEquals(chunkSize, b.getChunkSize());
			}
			Assert.assertEquals(0, b.getAllocatedSize());
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
			Assert.assertEquals(i, b.getCurrentSize());
			b.write(TextMemoryBufferTest.FWD[i]);
			Assert.assertEquals(i + 1, b.getCurrentSize());
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
			Assert.assertEquals(i * TextMemoryBufferTest.FWD.length, b.getCurrentSize());
			b.write(TextMemoryBufferTest.FWD);
			Assert.assertEquals((i + 1) * TextMemoryBufferTest.FWD.length, b.getCurrentSize());
		}
		try {
			b.write((char[]) null);
			Assert.fail("Failed to raise a NullPointerException");
		} catch (NullPointerException e) {
			// This is ok
		}
		b.close();
	}

	/**
	 * Test method for {@link com.armedia.commons.utilities.TextMemoryBuffer#write(char[], int, int)}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testWriteByteArrayIntInt() throws IOException {
		TextMemoryBuffer b = new TextMemoryBuffer();
		try {
			b.write((char[]) null, 0, 0);
			Assert.fail("Failed to raise a NullPointerException");
		} catch (NullPointerException e) {
			// This is ok
		}
		try {
			b.write((char[]) null, 0, -1);
			Assert.fail("Failed to raise a NullPointerException");
		} catch (NullPointerException e) {
			// This is ok
		}
		try {
			b.write((char[]) null, -1, 0);
			Assert.fail("Failed to raise a NullPointerException");
		} catch (NullPointerException e) {
			// This is ok
		}
		try {
			b.write((char[]) null, -1, -1);
			Assert.fail("Failed to raise a NullPointerException");
		} catch (NullPointerException e) {
			// This is ok
		}
		try {
			b.write((char[]) null, 0, TextMemoryBufferTest.FWD.length);
			Assert.fail("Failed to raise a NullPointerException");
		} catch (NullPointerException e) {
			// This is ok
		}
		try {
			b.write((char[]) null, 1, TextMemoryBufferTest.FWD.length);
			Assert.fail("Failed to raise a NullPointerException");
		} catch (NullPointerException e) {
			// This is ok
		}

		b.write(TextMemoryBufferTest.FWD, 0, 0);
		try {
			b.write(TextMemoryBufferTest.FWD, 0, -1);
			Assert.fail("Failed to raise a IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// This is ok
		}
		try {
			b.write(TextMemoryBufferTest.FWD, -1, 0);
			Assert.fail("Failed to raise a IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// This is ok
		}
		try {
			b.write(TextMemoryBufferTest.FWD, -1, -1);
			Assert.fail("Failed to raise a IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// This is ok
		}
		b.write(TextMemoryBufferTest.FWD, 0, TextMemoryBufferTest.FWD.length);
		try {
			b.write(TextMemoryBufferTest.FWD, 1, TextMemoryBufferTest.FWD.length);
			Assert.fail("Failed to raise a IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// This is ok
		}

		b.close();
		b = new TextMemoryBuffer();

		long writeCount = 0;
		for (int o = 0; o < TextMemoryBufferTest.FWD.length; o++) {
			int maxLen = Math.min(TextMemoryBufferTest.FWD.length / 2, TextMemoryBufferTest.FWD.length - o);
			for (int l = 0; l < maxLen; l++) {
				b.write(TextMemoryBufferTest.FWD, o, l);

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
		for (int chunkSize = TextMemoryBuffer.MINIMUM_CHUNK_SIZE; chunkSize < 512; chunkSize++) {
			for (int writeCount : writeCounts) {
				TextMemoryBuffer b = new TextMemoryBuffer(chunkSize);
				Assert.assertEquals(chunkSize, b.getChunkSize());
				Assert.assertEquals(0, b.getAllocatedSize());

				for (int c = 0; c < writeCount; c++) {
					b.write(TextMemoryBufferTest.FWD);
				}

				int totalSize = TextMemoryBufferTest.FWD.length * writeCount;
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
		for (int chunkSize = TextMemoryBuffer.MINIMUM_CHUNK_SIZE; chunkSize < 512; chunkSize++) {
			for (int writeCount : writeCounts) {
				TextMemoryBuffer b = new TextMemoryBuffer(chunkSize);
				Assert.assertEquals(chunkSize, b.getChunkSize());
				Assert.assertEquals(0, b.getCurrentSize());

				for (int c = 0; c < writeCount; c++) {
					b.write(TextMemoryBufferTest.FWD);
				}

				Assert.assertEquals(TextMemoryBufferTest.FWD.length * writeCount, b.getCurrentSize());
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
		try {
			buf.write(TextMemoryBufferTest.REV);
			Assert.fail("Should have failed with IOException");
		} catch (IOException e) {
			// All is well
		}
		try {
			buf.write(TextMemoryBufferTest.REV, 30, 40);
			Assert.fail("Should have failed with IOException");
		} catch (IOException e) {
			// All is well
		}
		try {
			buf.write(TextMemoryBufferTest.FWD[30]);
			Assert.fail("Should have failed with IOException");
		} catch (IOException e) {
			// All is well
		}
	}

	@Test
	public void testInput() throws IOException {
		TextMemoryBuffer b = new TextMemoryBuffer();
		b.write(TextMemoryBufferTest.FWD);
		b.write(TextMemoryBufferTest.REV);
		Reader in = b.getReader();
		for (int i = 0; i < TextMemoryBufferTest.FWD.length; i++) {
			int r = in.read();
			Assert.assertTrue(r >= 0);
			Assert.assertEquals(TextMemoryBufferTest.FWD[i], r);
		}
		for (int i = 0; i < TextMemoryBufferTest.REV.length; i++) {
			int r = in.read();
			Assert.assertTrue(r >= 0);
			Assert.assertEquals(TextMemoryBufferTest.REV[i], r);
		}
		char[] buf = new char[TextMemoryBufferTest.FWD.length];

		in = b.getReader();
		in.read(buf);
		Assert.assertArrayEquals(TextMemoryBufferTest.FWD, buf);
		in.read(buf, 0, buf.length);
		Assert.assertArrayEquals(TextMemoryBufferTest.REV, buf);

		try {
			in.read((char[]) null);
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
			in.read((char[]) null);
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

		TextMemoryBuffer b = new TextMemoryBuffer();
		final Reader in = b.getReader();
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
					for (char b : TextMemoryBufferTest.FWD) {
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
						Assert.assertEquals(b, r);
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
		for (char w : TextMemoryBufferTest.FWD) {
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
		Assert.assertEquals("Failed to complete the read - fell short", TextMemoryBufferTest.FWD.length, counter.get());
		b.close();
	}

	@Test
	public void testBlockingInputClosure() {
		TextMemoryBuffer b = null;
		final AtomicReference<Throwable> thrown = new AtomicReference<Throwable>();
		final AtomicBoolean started = new AtomicBoolean(false);
		final AtomicBoolean finished = new AtomicBoolean(false);
		final AtomicReference<Reader> in = new AtomicReference<Reader>();

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
							char[] buf = new char[128];
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
							char[] buf = new char[128];
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
		TextMemoryBuffer b = null;
		final AtomicReference<Throwable> thrown = new AtomicReference<Throwable>();
		final AtomicBoolean started = new AtomicBoolean(false);
		final AtomicBoolean finished = new AtomicBoolean(false);
		final AtomicReference<Reader> in = new AtomicReference<Reader>();

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
							char[] buf = new char[128];
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
							char[] buf = new char[128];
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
		TextMemoryBuffer b = null;
		final AtomicReference<Throwable> thrown = new AtomicReference<Throwable>();
		final AtomicBoolean started = new AtomicBoolean(false);
		final AtomicBoolean finished = new AtomicBoolean(false);
		final AtomicReference<Reader> in = new AtomicReference<Reader>();

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
							char[] buf = new char[128];
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
							char[] buf = new char[128];
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
		TextMemoryBuffer b = new TextMemoryBuffer();
		Random r = new Random(System.currentTimeMillis());
		char[] random = new char[1024];
		TextMemoryBufferTest.randomChars(r, random);
		b.write(random);

		TextMemoryBufferReader in = b.getReader();
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
		char[] segment = new char[length];
		System.arraycopy(random, start, segment, 0, length);
		for (int i = 0; i < length; i++) {
			final int v = in.read();
			Assert.assertFalse(v == -1);
			Assert.assertEquals(String.format("Failed to read byte %d", i), segment[i], v);
		}
		in.reset();
		Assert.assertEquals(available - start, in.available());
		for (int i = 0; i < length; i++) {
			final int v = in.read();
			Assert.assertFalse(v == -1);
			Assert.assertEquals(String.format("Failed to read byte %d", i), segment[i], v);
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

		Assert.assertEquals(a.length(), b.length());

		for (int i = 0; i < a.length(); i++) {
			Assert.assertEquals(String.format("Difference in position %d", i), a.charAt(i), b.charAt(i));
		}

		for (int o = 0; o < a.length(); o++) {
			for (int l = 0; l < (a.length() - o); l++) {
				CharSequence subA = a.subSequence(o, o + l);
				CharSequence subB = b.subSequence(o, o + l);
				Assert.assertEquals(String.format("Length failed at parameters (%d,%d)", o, l), subA.length(),
					subB.length());
				for (int i = 0; i < subA.length(); i++) {
					try {
						final char ca = subA.charAt(i);
						final char cb = subB.charAt(i);
						Assert.assertEquals(String.format("Length failed at parameters (%d,%d,%d)", o, l, i), ca, cb);
					} catch (ArrayIndexOutOfBoundsException e) {
						Assert.fail(String.format("Array Index failed at parameters (%d,%d,%d)", o, l, i));
					}
				}
			}
		}
	}
}