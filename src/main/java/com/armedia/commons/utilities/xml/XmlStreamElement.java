/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2022 Armedia, LLC
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

	public void newAttribute(String localName, String value) throws XMLStreamException {
		this.xml.writeAttribute(localName, value);
	}

	public void newAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
		this.xml.writeAttribute(namespaceURI, localName, value);
	}

	public void newAttribute(String prefix, String namespaceURI, String localName, String value)
		throws XMLStreamException {
		this.xml.writeAttribute(prefix, namespaceURI, localName, value);
	}

	public void newCDATA(String content) throws XMLStreamException {
		this.xml.writeCData(content);
	}

	public XmlStreamElement newElement(String localName) throws XMLStreamException {
		return new XmlStreamElement(this.xml, localName);
	}

	public XmlStreamElement newElement(String namespaceURI, String localName) throws XMLStreamException {
		return new XmlStreamElement(this.xml, namespaceURI, localName);
	}

	public XmlStreamElement newElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		return new XmlStreamElement(this.xml, prefix, localName, namespaceURI);
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
