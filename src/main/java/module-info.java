/*******************************************************************************
 * #%L Armedia Caliente %% Copyright (C) 2013 - 2020 Armedia, LLC %% This file is part of the
 * Caliente software.
 *
 * If the software was purchased under a paid Caliente license, the terms of the paid license
 * agreement will prevail. Otherwise, the software is provided under the following open source
 * license terms:
 *
 * Caliente is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Caliente is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with Caliente. If
 * not, see <http://www.gnu.org/licenses/>. #L%
 *******************************************************************************/
module com.armedia.commons.utilities {
	exports com.armedia.commons.utilities;
	exports com.armedia.commons.utilities.script;
	exports com.armedia.commons.utilities.xml;
	exports com.armedia.commons.utilities.io;
	exports com.armedia.commons.utilities.line;
	exports com.armedia.commons.utilities.function;
	exports com.armedia.commons.utilities.concurrent;
	exports com.armedia.commons.utilities.codec;
	exports com.armedia.commons.utilities.cli;
	exports com.armedia.commons.utilities.cli.classpath;
	exports com.armedia.commons.utilities.cli.exception;
	exports com.armedia.commons.utilities.cli.filter;
	exports com.armedia.commons.utilities.cli.help;
	exports com.armedia.commons.utilities.cli.launcher;
	exports com.armedia.commons.utilities.cli.launcher.log;
	exports com.armedia.commons.utilities.cli.token;
	exports com.armedia.commons.utilities.cli.utils;

	requires static transitive java.xml;
	requires static transitive java.xml.bind;
	requires static transitive java.activation;
	requires static transitive java.scripting;

	requires org.apache.commons.codec;
	requires org.apache.commons.io;
	requires org.apache.commons.lang3;
	requires org.apache.commons.text;

	requires org.slf4j;
	requires ch.qos.logback.classic;
	requires ch.qos.logback.core;
	requires org.apache.logging.log4j;
	requires org.apache.logging.log4j.core;
}
