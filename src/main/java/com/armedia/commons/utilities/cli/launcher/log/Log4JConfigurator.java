/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2021 Armedia, LLC
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
package com.armedia.commons.utilities.cli.launcher.log;

import java.net.URISyntaxException;
import java.net.URL;

import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Log4JConfigurator extends LogConfigurator {

	@Override
	public Logger initialize() {
		// First, find log4j-boot.xml
		URL config = getClass().getResource("log4j.xml");
		if (config == null) {
			throw new RuntimeException("Failed to configure the boot log - no Log4J boot configuration was found");
		}
		try {
			Configurator.reconfigure(config.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException("Failed to configure the boot log - the configuration URL [" + config
				+ "] could not be converted to a valid URI", e);
		}
		return LoggerFactory.getLogger(LogConfigurator.DEFAULT_LOG_NAME);
	}

}