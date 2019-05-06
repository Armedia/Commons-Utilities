package com.armedia.commons.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.concurrent.ShareableSet;

public class StreamToolsTest {

	@Test
	public void testConstructor() {
		new StreamTools();
	}

	@Test
	public void testOfIteratorOfT() {
		Stream<UUID> s = StreamTools.of(null);
		Assertions.assertEquals(0L, s.count());
		List<UUID> c = Arrays.asList(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
			UUID.randomUUID());
		s = StreamTools.of(c.iterator());
		Assertions.assertFalse(s.isParallel());
		List<UUID> l = new ArrayList<>(c.size());
		s.forEach(l::add);
		Assertions.assertEquals(c, l);
	}

	@Test
	public void testOfIteratorOfTInt() {
		for (Integer characteristics : CloseableIteratorTest.ALL_CHARACTERISTICS) {
			Stream<UUID> s = StreamTools.of(null, characteristics);
			Assertions.assertEquals(0L, s.count());
			List<UUID> c = Arrays.asList(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
				UUID.randomUUID());
			s = StreamTools.of(c.iterator(), characteristics);
			Assertions.assertFalse(s.isParallel());
			List<UUID> l = new ArrayList<>(c.size());
			s.forEach(l::add);
			Assertions.assertEquals(c, l);
		}
	}

	@Test
	public void testOfIteratorOfTBoolean() {
		Stream<UUID> s = StreamTools.of(null, false);
		Assertions.assertEquals(0L, s.count());

		Set<UUID> c = new HashSet<>(Arrays.asList(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
			UUID.randomUUID(), UUID.randomUUID()));
		s = StreamTools.of(c.iterator(), false);
		Assertions.assertFalse(s.isParallel());
		Set<UUID> l = new HashSet<>();
		s.forEach(l::add);
		Assertions.assertEquals(c, l);

		s = StreamTools.of(null, true);
		Assertions.assertEquals(0L, s.count());

		s = StreamTools.of(c.iterator(), true);
		Assertions.assertTrue(s.isParallel());
		l = new ShareableSet<>(new HashSet<>());
		s.forEach(l::add);
		Assertions.assertEquals(c, l);
	}

	@Test
	public void testOfIteratorOfTIntBoolean() {
		Set<UUID> c = new HashSet<>(Arrays.asList(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
			UUID.randomUUID(), UUID.randomUUID()));
		for (Integer characteristics : CloseableIteratorTest.ALL_CHARACTERISTICS) {
			Stream<UUID> s = StreamTools.of(null, characteristics, false);
			Assertions.assertEquals(0L, s.count());
			s = StreamTools.of(c.iterator(), characteristics, false);
			Assertions.assertFalse(s.isParallel());
			Set<UUID> l = new ShareableSet<>(new HashSet<>());
			s.forEach(l::add);
			Assertions.assertEquals(c, l);
		}
		for (Integer characteristics : CloseableIteratorTest.ALL_CHARACTERISTICS) {
			Stream<UUID> s = StreamTools.of(null, characteristics, true);
			Assertions.assertEquals(0L, s.count());
			s = StreamTools.of(c.iterator(), characteristics);
			Set<UUID> l = new ShareableSet<>(new HashSet<>());
			s.forEach(l::add);
			Assertions.assertEquals(c, l);
		}
	}
}