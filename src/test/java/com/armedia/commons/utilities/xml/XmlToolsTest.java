package com.armedia.commons.utilities.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.xml.bind.JAXBContext;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.LazyFormatter;

public class XmlToolsTest {

	@Test
	public void testGetContext() throws Exception {
		Class<?>[] c = {
			Object.class, String.class, Number.class, Long.class, Short.class, Integer.class, Double.class, Float.class,
			Byte.class, Character.class, null, Object.class, String.class, Number.class, Long.class, Short.class,
			Integer.class, Double.class, Float.class, Byte.class, Character.class, null
		};

		List<Class<?>> l = new ArrayList<>();
		for (Class<?> C : c) {
			l.add(C);
		}

		Class<?>[] origClasses = null;
		JAXBContext origCtx = null;
		boolean shuffled = false;
		for (int i = 0; i < 10; i++) {
			final Class<?>[] oldClasses = origClasses;
			final Class<?>[] newClasses = l.toArray(ArrayUtils.EMPTY_CLASS_ARRAY);
			final JAXBContext newCtx = XmlTools.getContext(newClasses);
			Assertions.assertNotNull(newCtx);
			if (origCtx == null) {
				origCtx = newCtx;
				origClasses = newClasses;
				continue;
			}
			Supplier<String> oldMsg = () -> Arrays.toString(oldClasses);
			Supplier<String> newMsg = () -> Arrays.toString(newClasses);
			Assertions.assertNotSame(oldClasses, newClasses);
			if (shuffled) {
				Assertions.assertFalse(Arrays.deepEquals(oldClasses, newClasses));
			}
			Assertions.assertSame(origCtx, newCtx,
				LazyFormatter.of("Failed to get the same context for %s and %s", oldMsg, newMsg));
			Collections.shuffle(l);
			shuffled = true;
		}
		origClasses = null;
		Assertions.assertSame(XmlTools.getContext(), XmlTools.getContext());
		Assertions.assertSame(XmlTools.getContext(), XmlTools.getContext(origClasses));
		Assertions.assertSame(XmlTools.getContext(), XmlTools.getContext((Class<?>) null));

	}

	@Test
	public void testLoadSchemaURL() {
	}

}
