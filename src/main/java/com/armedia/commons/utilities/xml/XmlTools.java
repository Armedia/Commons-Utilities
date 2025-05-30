/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2025 Armedia, LLC
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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import com.armedia.commons.utilities.Tools;
import com.armedia.commons.utilities.concurrent.ConcurrentTools;

/**
 * This class provides some utility methods for JAXB bindings to facilitate the loading and storing
 * of XML-bound classes.
 *
 *
 *
 */
public class XmlTools {

	private static final boolean DEFAULT_FORMAT = false;

	private static final Class<?>[] NO_CLASSES = {};

	private static class JAXBContextConfig {
		protected final Class<?>[] classes;

		public JAXBContextConfig(Class<?>... classes) {
			// Sort the classes by name, alphabetically
			TreeMap<String, Class<?>> m = new TreeMap<>();
			if (classes != null) {
				for (Class<?> c : classes) {
					if (c != null) {
						m.put(c.getCanonicalName(), c);
					}
				}
			}
			if (m.isEmpty()) {
				this.classes = ArrayUtils.EMPTY_CLASS_ARRAY;
			} else {
				this.classes = m.values().toArray(ArrayUtils.EMPTY_CLASS_ARRAY);
			}
		}

		@Override
		public int hashCode() {
			return Tools.hashTool(this, null, Arrays.hashCode(this.classes));
		}

		@Override
		public boolean equals(Object obj) {
			if (!Tools.baseEquals(this, obj)) { return false; }
			JAXBContextConfig other = JAXBContextConfig.class.cast(obj);
			if (!Arrays.equals(this.classes, other.classes)) { return false; }
			return true;
		}
	}

	private static final ConcurrentMap<URL, Schema> SCHEMATA = new ConcurrentHashMap<>();
	private static final ConcurrentMap<JAXBContextConfig, JAXBContext> JAXB_CONTEXTS = new ConcurrentHashMap<>();

	// private static final Logger LOG = Logger.getLogger(XmlTools.class);

	/**
	 * Gets an instance of {@link JAXBContext} that supports reading and writing the classes listed
	 * in (and referenced by) the {@code targetClasses} parameter.
	 *
	 * @param targetClasses
	 *            the classes to build the context for
	 * @return a new {@link JAXBContext} instance
	 * @throws JAXBException
	 */
	public static JAXBContext getContext(Class<?>... targetClasses) throws JAXBException {
		return ConcurrentTools.createIfAbsent(XmlTools.JAXB_CONTEXTS, new JAXBContextConfig(targetClasses),
			(c) -> JAXBContext.newInstance(c.classes));
	}

	/**
	 * Loads a {@link Schema} instance from the classpath resource name {@code schemaName}.
	 * Classpath resource name rules apply (i.e. package names, etc).
	 *
	 * @param schemaName
	 * @return the loaded {@link Schema} instance
	 * @throws JAXBException
	 */
	public static Schema loadSchema(String schemaName) throws JAXBException {
		return XmlTools.loadSchema(null, schemaName);
	}

	/**
	 * Loads a {@link Schema} instance from the classpath resource name {@code schemaName}.
	 * Classpath resource name rules apply (i.e. package names, etc).
	 *
	 * @param schemaName
	 * @return the loaded {@link Schema} instance
	 * @throws JAXBException
	 */
	public static Schema loadSchema(ClassLoader cl, String schemaName) throws JAXBException {
		// Now, load up the schema that will be used for validation
		if (schemaName == null) { return null; }

		/*
		XmlTools.LOG.debug("Loading schema [{}]", schemaName);
		*/
		if (cl == null) {
			cl = Thread.currentThread().getContextClassLoader();
		}
		final URL schemaUrl = cl.getResource(schemaName);
		if (schemaUrl == null) {
			throw new JAXBException(String.format("Failed to load the schema from '%s'", schemaName));
		}
		return XmlTools.loadSchema(schemaUrl);
	}

	/**
	 * Loads a {@link Schema} instance from the given URL Classpath resource name rules apply (i.e.
	 * package names, etc).
	 *
	 * @param schemaUrl
	 * @return the loaded {@link Schema} instance
	 * @throws JAXBException
	 */
	public static Schema loadSchema(URL schemaUrl) throws JAXBException {
		Objects.requireNonNull(schemaUrl, "Must provide a non-null Schema URL");
		// Now, load up the schema that will be used for validation
		Schema schema = null;
		if (schemaUrl != null) {
			try {
				schema = ConcurrentTools.createIfAbsent(XmlTools.SCHEMATA, schemaUrl,
					SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)::newSchema);
			} catch (SAXException e) {
				throw new JAXBException(String.format("Failed to load the schema from [%s]", schemaUrl), e.getCause());
			}
		}
		return schema;
	}

	/**
	 * Perform the actual XML loading from the given stream reader, and assuming the root element is
	 * an element of the given targetClass. This is equivalent to invoking
	 * {@code unmarshal(targetClass, null, in)}.
	 *
	 * @param targetClass
	 * @param in
	 * @return The unmarshalled object
	 * @throws JAXBException
	 */
	public static <T> T unmarshal(final Class<T> targetClass, final XMLStreamReader in) throws JAXBException {
		return XmlTools.unmarshal(targetClass, null, in);
	}

	/**
	 * Perform the actual XML loading from the given stream reader using the named schema, and
	 * assuming the root element is an element of the given targetClass.
	 *
	 * @param targetClass
	 * @param schemaName
	 * @param in
	 * @return The unmarshalled object
	 * @throws JAXBException
	 */
	public static <T> T unmarshal(final Class<T> targetClass, final String schemaName, final XMLStreamReader in)
		throws JAXBException {
		return XmlTools.doUnmarshal(targetClass, schemaName, in);
	}

	/**
	 * Perform the actual XML loading from the given input stream, and assuming the root element is
	 * an element of the given targetClass. This is equivalent to invoking
	 * {@code unmarshal(targetClass, null, in)}.
	 *
	 * @param targetClass
	 * @param in
	 * @return The unmarshalled object
	 * @throws JAXBException
	 */
	public static <T> T unmarshal(final Class<T> targetClass, final InputStream in) throws JAXBException {
		return XmlTools.unmarshal(targetClass, null, in);
	}

	/**
	 * Perform the actual XML loading from the given input stream using the named schema, and
	 * assuming the root element is an element of the given targetClass.
	 *
	 * @param targetClass
	 * @param schemaName
	 * @param in
	 * @return The unmarshalled object
	 * @throws JAXBException
	 */
	public static <T> T unmarshal(final Class<T> targetClass, final String schemaName, final InputStream in)
		throws JAXBException {
		return XmlTools.doUnmarshal(targetClass, schemaName, in);
	}

	/**
	 * Perform the actual XML loading from the given reader, and assuming the root element is an
	 * element of the given targetClass. This is equivalent to invoking
	 * {@code unmarshal(targetClass, null, r)}.
	 *
	 * @param targetClass
	 * @param r
	 * @return The unmarshalled object
	 * @throws JAXBException
	 */
	public static <T> T unmarshal(final Class<T> targetClass, final Reader r) throws JAXBException {
		return XmlTools.unmarshal(targetClass, null, r);
	}

	/**
	 * Perform the actual XML loading from the given reader using the named schema, and assuming the
	 * root element is an element of the given targetClass.
	 *
	 * @param targetClass
	 * @param schemaName
	 * @param r
	 * @return The unmarshalled object
	 * @throws JAXBException
	 */
	public static <T> T unmarshal(final Class<T> targetClass, final String schemaName, final Reader r)
		throws JAXBException {
		return XmlTools.doUnmarshal(targetClass, schemaName, r);
	}

	public static Unmarshaller getUnmarshaller(Class<?>... targetClasses) throws JAXBException {
		return XmlTools.getUnmarshaller(null, targetClasses);
	}

	/**
	 * Returns a valid JAXB {@link Unmarshaller} instance supporting the given classes, as per the
	 * named schema (may be {@code null}).
	 *
	 * @param schemaName
	 * @param targetClasses
	 * @return a valid JAXB {@link Unmarshaller}
	 * @throws JAXBException
	 */
	public static Unmarshaller getUnmarshaller(final String schemaName, Class<?>... targetClasses)
		throws JAXBException {
		if (targetClasses == null) {
			targetClasses = XmlTools.NO_CLASSES;
		}

		Unmarshaller u = XmlTools.getContext(targetClasses).createUnmarshaller();
		if (!StringUtils.isEmpty(schemaName)) {
			final Schema schema = XmlTools.loadSchema(schemaName);
			if (schema != null) {
				u.setSchema(schema);
			}
		}
		return u;
	}

	private static <T> T doUnmarshal(final Class<T> targetClass, final String schemaName, final Object src)
		throws JAXBException {
		if (targetClass == null) { throw new IllegalArgumentException("Must supply a class to unmarshal"); }
		if (src == null) {
			throw new IllegalArgumentException(String.format("No reader to read %s from", targetClass.getName()));
		}
		Unmarshaller u = XmlTools.getUnmarshaller(schemaName, targetClass);
		if (src instanceof XMLStreamReader) { return targetClass.cast(u.unmarshal(XMLStreamReader.class.cast(src))); }
		if (src instanceof Reader) { return targetClass.cast(u.unmarshal(Reader.class.cast(src))); }
		return targetClass.cast(u.unmarshal(InputStream.class.cast(src)));
	}

	public static String marshal(Object target) throws JAXBException {
		return XmlTools.marshal(target, (String) null, XmlTools.DEFAULT_FORMAT);
	}

	/**
	 * Marshal out the given object using JAXB bindings, and return the resulting XML string.
	 *
	 * @param target
	 * @return The marshalled object, in {@link String} form
	 * @throws JAXBException
	 */
	public static String marshal(Object target, boolean format) throws JAXBException {
		return XmlTools.marshal(target, (String) null, format);
	}

	/**
	 * Marshal out the given object using JAXB bindings, and return the resulting XML string.
	 *
	 * @param target
	 * @param schemaName
	 * @return The marshalled object, in {@link String} form
	 * @throws JAXBException
	 */
	public static String marshal(Object target, String schemaName) throws JAXBException {
		return XmlTools.marshal(target, schemaName, XmlTools.DEFAULT_FORMAT);
	}

	/**
	 * Marshal out the given object using JAXB bindings, and return the resulting XML string.
	 *
	 * @param target
	 * @param schemaName
	 * @return The marshalled object, in {@link String} form
	 * @throws JAXBException
	 */
	public static String marshal(Object target, String schemaName, boolean format) throws JAXBException {
		StringWriter w = new StringWriter();
		XmlTools.marshal(target, schemaName, w, format);
		return w.toString();
	}

	public static void marshal(final Object target, final OutputStream out) throws JAXBException {
		XmlTools.marshal(target, null, out, XmlTools.DEFAULT_FORMAT);
	}

	public static void marshal(final Object target, final OutputStream out, boolean format) throws JAXBException {
		XmlTools.marshal(target, null, out, format);
	}

	public static void marshal(final Object target, final String schemaName, final OutputStream out)
		throws JAXBException {
		XmlTools.marshal(target, schemaName, out, XmlTools.DEFAULT_FORMAT);
	}

	public static void marshal(final Object target, final String schemaName, final OutputStream out, boolean format)
		throws JAXBException {
		XmlTools.doMarshal(target, schemaName, out, format);
	}

	public static void marshal(final Object target, final Writer w) throws JAXBException {
		XmlTools.marshal(target, null, w, XmlTools.DEFAULT_FORMAT);
	}

	public static void marshal(final Object target, final Writer w, boolean format) throws JAXBException {
		XmlTools.marshal(target, null, w, format);
	}

	public static void marshal(Object target, final String schemaName, final Writer w) throws JAXBException {
		XmlTools.marshal(target, schemaName, w, XmlTools.DEFAULT_FORMAT);
	}

	public static void marshal(Object target, final String schemaName, final Writer w, boolean format)
		throws JAXBException {
		XmlTools.doMarshal(target, schemaName, w, format);
	}

	public static void marshal(final Object target, final XMLStreamWriter out) throws JAXBException {
		XmlTools.marshal(target, null, out, XmlTools.DEFAULT_FORMAT);
	}

	public static void marshal(final Object target, final XMLStreamWriter out, boolean format) throws JAXBException {
		XmlTools.marshal(target, null, out, format);
	}

	public static void marshal(final Object target, final String schemaName, final XMLStreamWriter out)
		throws JAXBException {
		XmlTools.marshal(target, schemaName, out, XmlTools.DEFAULT_FORMAT);
	}

	public static void marshal(final Object target, final String schemaName, final XMLStreamWriter out, boolean format)
		throws JAXBException {
		XmlTools.doMarshal(target, schemaName, out, format);
	}

	public static Marshaller getMarshaller(Class<?>... targetClasses) throws JAXBException {
		return XmlTools.getMarshaller(null, targetClasses);
	}

	/**
	 * Returns a valid JAXB {@link Marshaller} instance supporting the given classes, as per the
	 * named schema (may be {@code null}).
	 *
	 * @param schemaName
	 * @param targetClasses
	 * @return a valid JAXB {@link Marshaller}
	 * @throws JAXBException
	 */
	public static Marshaller getMarshaller(final String schemaName, Class<?>... targetClasses) throws JAXBException {
		if (targetClasses == null) {
			targetClasses = XmlTools.NO_CLASSES;
		}

		Marshaller m = XmlTools.getContext(targetClasses).createMarshaller();
		if (!StringUtils.isEmpty(schemaName)) {
			final Schema schema = XmlTools.loadSchema(schemaName);
			if (schema != null) {
				m.setSchema(schema);
			}
		}
		return m;
	}

	private static void doMarshal(Object target, final String schemaName, Object out, boolean format)
		throws JAXBException {
		if (target == null) { throw new IllegalArgumentException("Must supply an object to marshal"); }
		if (out == null) {
			throw new IllegalArgumentException(String.format("Nowhere to write %s to", target.getClass().getName()));
		}

		Marshaller m = XmlTools.getMarshaller(schemaName, target.getClass());
		m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		if (format) {
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		}
		if (out instanceof XMLStreamWriter) {
			m.marshal(target, XMLStreamWriter.class.cast(out));
			return;
		}
		if (out instanceof Writer) {
			m.marshal(target, Writer.class.cast(out));
			return;
		}
		m.marshal(target, OutputStream.class.cast(out));
	}

	public static void skipBranch(XMLStreamReader xml) throws XMLStreamException {
		long depth = 1;
		while ((depth > 0) && xml.hasNext()) {
			switch (xml.nextTag()) {
				case XMLStreamConstants.START_DOCUMENT:
				case XMLStreamConstants.START_ELEMENT:
					depth++;
					break;
				case XMLStreamConstants.END_DOCUMENT:
				case XMLStreamConstants.END_ELEMENT:
					depth--;
					break;
			}
		}
	}
}
