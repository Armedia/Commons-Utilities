package com.armedia.commons.utilities.xml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Element;

import com.armedia.commons.utilities.Tools;
import com.armedia.commons.utilities.concurrent.ShareableMap;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
	"properties"
})
public final class AnyElementMap {

	private static final Map<Class<?>, Pair<Function<Object, String>, Function<Object, String>>> EXTRACTORS;
	static {
		Map<Class<?>, Pair<Function<Object, String>, Function<Object, String>>> m = new LinkedHashMap<>();
		m.put(JAXBElement.class, Pair.of( //
			(o) -> Tools.cast(JAXBElement.class, o).getName().getLocalPart(), //
			(o) -> Tools.toString(Tools.cast(JAXBElement.class, o).getValue()) //
		));
		m.put(Element.class, Pair.of( //
			(o) -> Tools.cast(Element.class, o).getLocalName(), //
			(o) -> Tools.cast(Element.class, o).getTextContent() //
		));
		EXTRACTORS = Tools.freezeMap(m);
	}

	@XmlAnyElement
	private List<?> properties;

	@XmlTransient
	private Map<String, String> map;

	public AnyElementMap() {
		this(null);
	}

	public AnyElementMap(Map<String, String> map) {
		this.map = (map != null ? map : new LinkedHashMap<>());
	}

	protected void beforeMarshal(Marshaller m) {
		// Convert the map into the properties
		final List<JAXBElement<String>> properties = new ArrayList<>(this.map.size());
		this.map.forEach((k, v) -> {
			properties.add(new JAXBElement<>(new QName(k), String.class, v));
		});
		this.properties = properties;
	}

	protected void afterUnmarshal(Unmarshaller u, Object parent) {
		// Convert the properties into the map
		final Map<String, String> map = new LinkedHashMap<>();
		if (this.properties != null) {
			this.properties.forEach((p) -> {
				for (Class<?> c : AnyElementMap.EXTRACTORS.keySet()) {
					Object o = Tools.cast(c, p);
					if (o != null) {
						Pair<Function<Object, String>, Function<Object, String>> e = AnyElementMap.EXTRACTORS.get(c);
						map.put(e.getLeft().apply(o), e.getRight().apply(o));
						return;
					}
				}
				// Ignore it...we don't know what to do with it
			});
		}
		this.map = new ShareableMap<>(map);
	}

	public List<?> getProperties() {
		if (this.properties == null) {
			this.properties = new ArrayList<>();
		}
		return this.properties;
	}

	Map<String, String> getMap() {
		return this.map;
	}
}