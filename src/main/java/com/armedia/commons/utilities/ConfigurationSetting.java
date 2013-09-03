/**
 * *******************************************************************
 * 
 * THIS SOFTWARE IS PROTECTED BY U.S. AND INTERNATIONAL COPYRIGHT LAWS.
 * REPRODUCTION OF ANY PORTION OF THE SOURCE CODE, CONTAINED HEREIN,
 * OR ANY PORTION OF THE PRODUCT, EITHER IN PART OR WHOLE,
 * IS STRICTLY PROHIBITED.
 * 
 * Confidential Property of Armedia LLC.
 * (c) Copyright Armedia LLC 2011.
 * All Rights reserved.
 * 
 * *******************************************************************
 */
package com.armedia.commons.utilities;

/**
 * This interface provides a means for easy handling of configuration settings.
 * 
 * @author drivera@armedia.com
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