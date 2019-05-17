package com.armedia.commons.utilities.xml;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.armedia.commons.utilities.Tools;

public class AnyElementMapTest {
	@XmlRootElement(name = "test-any-map")
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(propOrder = {
		"map", "other"
	})
	public static class TestAnyMap {
		@XmlJavaTypeAdapter(StringToStringAnyElementMapAdapter.class)
		@XmlElement(name = "any-element-map-test")
		private Map<String, String> map;

		@XmlElement(name = "other-value")
		private String other;

		public TestAnyMap() {
		}

		public Map<String, String> getMap() {
			if (this.map == null) {
				this.map = new LinkedHashMap<>();
			}
			return this.map;
		}

		public String getOther() {
			return this.other;
		}

		public void setOther(String other) {
			this.other = other;
		}
	}

	private static Marshaller MARSHALLER = null;
	private static Unmarshaller UNMARSHALLER = null;

	@BeforeAll
	public static void beforeAll() throws Exception {
		AnyElementMapTest.MARSHALLER = XmlTools.getMarshaller(TestAnyMap.class);
		AnyElementMapTest.MARSHALLER.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		AnyElementMapTest.MARSHALLER.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		AnyElementMapTest.UNMARSHALLER = XmlTools.getUnmarshaller(TestAnyMap.class);
	}

	@Test
	public void testMarshalling() throws Exception {
		TestAnyMap outgoing = new TestAnyMap();
		Map<String, String> m = outgoing.getMap();
		for (int i = 0; i < 10; i++) {
			m.put(String.format("key-%02d", i), String.format("value-#-%02d", i));
		}
		outgoing.setOther(UUID.randomUUID().toString());

		StringWriter w = new StringWriter();
		AnyElementMapTest.MARSHALLER.marshal(outgoing, w);
		String xml = w.toString();
		Assertions.assertNotNull(xml);
		Object o = AnyElementMapTest.UNMARSHALLER.unmarshal(new StringReader(xml));
		Assertions.assertNotNull(o);
		TestAnyMap incoming = Tools.cast(TestAnyMap.class, o);
		Assertions.assertNotNull(incoming);
		Assertions.assertEquals(outgoing.getMap(), incoming.getMap());
		Assertions.assertEquals(outgoing.getOther(), incoming.getOther());
	}
}