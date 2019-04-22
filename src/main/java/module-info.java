open module com.armedia.commons.utilities {
	exports com.armedia.commons.utilities;
	exports com.armedia.commons.utilities.concurrent;
	exports com.armedia.commons.utilities.function;
	exports com.armedia.commons.utilities.line;

	requires static transitive java.xml;
	requires static transitive java.xml.bind;
	requires static transitive org.apache.commons.codec;
	requires static transitive org.apache.commons.io;
	requires static transitive org.apache.commons.lang3;
	requires static transitive org.apache.commons.text;
	requires static transitive slf4j.api;
}