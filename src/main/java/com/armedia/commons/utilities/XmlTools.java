/**********************************************************************
 *
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS. REPRODUCTION OF ANY PORTION
 * OF THE SOURCE CODE, CONTAINED HEREIN, OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE, IS
 * STRICTLY PROHIBITED.
 *
 * Confidential Property of Armedia LLC. (c) Copyright Armedia LLC 2007. All Rights reserved.
 *
 *********************************************************************/
package com.armedia.commons.utilities;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;

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

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

/**
 * This class provides some utility methods for JAXB bindings to facilitate the loading and storing
 * of XML-bound classes.
 *
 * @author drivera@armedia.com
 *
 */
public class XmlTools {

	private static final boolean DEFAULT_FORMAT = false;

	private static final Class<?>[] NO_CLASSES = {};

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
		return JAXBContext.newInstance(targetClasses);
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
		// Now, load up the schema that will be used for validation
		final Schema schema;
		if (schemaName != null) {
			/*
			XmlTools.LOG.debug("Loading schema [{}]", schemaName);
			*/
			final URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource(schemaName);
			if (schemaUrl == null) {
				throw new JAXBException(String.format("Failed to load the schema from '%s'", schemaName));
			}
			try {
				SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				schema = sf.newSchema(schemaUrl);
			} catch (SAXException e) {
				throw new JAXBException(String.format("Failed to load the schema '%s'", schemaName), e);
			}
		} else {
			schema = null;
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