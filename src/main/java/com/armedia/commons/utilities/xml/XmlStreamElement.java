package com.armedia.commons.utilities.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.StringUtils;

public class XmlStreamElement implements AutoCloseable {

	private final String prefix;
	private final String localName;
	private final String namespaceURI;
	private XMLStreamWriter xml;

	public XmlStreamElement(XMLStreamWriter xml, String localName) throws XMLStreamException {
		this.prefix = StringUtils.EMPTY;
		this.localName = localName;
		this.namespaceURI = StringUtils.EMPTY;
		this.xml = xml;

		this.xml.writeStartElement(localName);
	}

	public XmlStreamElement(XMLStreamWriter xml, String namespaceURI, String localName) throws XMLStreamException {
		this.prefix = StringUtils.EMPTY;
		this.localName = localName;
		this.namespaceURI = this.prefix;
		this.xml = xml;

		this.xml.writeStartElement(namespaceURI, localName);
	}

	public XmlStreamElement(XMLStreamWriter xml, String prefix, String localName, String namespaceURI)
		throws XMLStreamException {
		this.prefix = prefix;
		this.localName = localName;
		this.namespaceURI = prefix;
		this.xml = xml;

		this.xml.writeStartElement(prefix, localName, namespaceURI);
	}

	public String getPrefix() {
		return this.prefix;
	}

	public String getLocalName() {
		return this.localName;
	}

	public String getNamespaceURI() {
		return this.namespaceURI;
	}

	public XMLStreamWriter getXml() {
		return this.xml;
	}

	@Override
	public void close() throws XMLStreamException {
		this.xml.writeEndElement();
		this.xml.flush();
	}
}