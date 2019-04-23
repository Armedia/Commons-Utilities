module com.armedia.commons.utilities {
	exports com.armedia.commons.utilities;
	exports com.armedia.commons.utilities.concurrent;
	exports com.armedia.commons.utilities.function;
	exports com.armedia.commons.utilities.line;

	requires static transitive java.xml;
	requires static transitive java.xml.bind;
	requires static transitive java.activation;

	requires org.apache.commons.codec;
	requires org.apache.commons.io;
	requires org.apache.commons.lang3;
	requires org.apache.commons.text;

	requires slf4j.api;
}