/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2019 Armedia, LLC
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
package com.armedia.commons.utilities;

/**
 * This interface provides a means for easy handling of configuration settings.
 *
 *
 *
 */
public interface ConfigurationSetting {

	/**
	 * Return the string literal which is used in configuration files to set the configuration in
	 * question (i.e. the actual text label from the configuration file, NOT the user-friendly
	 * label)
	 *
	 * @return the string literal which is used in configuration files to set the configuration in
	 *         question
	 */
	public String getLabel();

	/**
	 * Return the default value for the configuration setting, in case it's not set at all.
	 *
	 * @return the default value for the configuration setting, in case it's not set at all.
	 */
	public Object getDefaultValue();
}
