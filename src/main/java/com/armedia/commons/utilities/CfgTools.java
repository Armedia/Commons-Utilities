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

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;

/**
 * @author drivera@armedia.com
 * 
 */
public class CfgTools implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();
	public static final CfgTools EMPTY = new CfgTools(CfgTools.EMPTY_MAP);

	private static void validateSetting(Object setting) {
		if (setting == null) { throw new IllegalArgumentException("Setting cannot be null"); }
	}

	/**
	 * Decode the named setting from the given map as a {@link Boolean} value, returning the value
	 * sent in {@code defaultValue} if it's not defined or is the empty string.
	 * 
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link Boolean} value
	 */
	public static Boolean decodeBoolean(String label, Map<String, String> settings, Boolean defaultValue) {
		CfgTools.validateSetting(label);
		String value = Tools.toTrimmedString(settings.get(label), true);
		return (value != null ? Boolean.valueOf(value) : defaultValue);
	}

	/**
	 * Decode the named setting from the given map as a {@link Boolean} value, returning
	 * {@code null} if it's not defined or is the empty string. This is equivalent to calling
	 * {@code decodeBoolean(label, settings, null)}
	 * 
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link Boolean} value
	 */
	public static Boolean decodeBoolean(String label, Map<String, String> settings) {
		return CfgTools.decodeBoolean(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link Boolean} value, returning
	 * {@code null} if it's not defined or is the empty string. This is equivalent to calling
	 * {@code decodeBoolean(setting.getLabel(), settings, setting.getDefaultValue())}. The default
	 * value obtained from {@code setting} is cast to a Boolean if it's a Boolean value, or
	 * calculated using {@link Boolean#valueOf(String)} otherwise. If the default value is
	 * {@code null}, then it's simply passed verbatim.
	 * 
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link Boolean} value
	 */
	public static Boolean decodeBoolean(ConfigurationSetting setting, Map<String, String> settings) {
		CfgTools.validateSetting(setting);
		Boolean defaultValue = null;
		Object dv = setting.getDefaultValue();
		if (dv != null) {
			if (dv instanceof Boolean) {
				defaultValue = Boolean.class.cast(dv);
			} else {
				defaultValue = Boolean.valueOf(dv.toString());
			}
		}
		return CfgTools.decodeBoolean(setting.getLabel(), settings, defaultValue);
	}

	/**
	 * Decode the named setting from the given map as a {@link Byte} value, returning the value
	 * sent in {@code defaultValue} if it's not defined or is the empty string.
	 * 
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link Byte} value
	 */
	public static Byte decodeByte(String label, Map<String, String> settings, Byte defaultValue) {
		CfgTools.validateSetting(label);
		String value = Tools.toTrimmedString(settings.get(label), true);
		if (value != null) { return Byte.valueOf(value); }
		return defaultValue;
	}

	/**
	 * Decode the named setting from the given map as a {@link Byte} value, returning {@code null}
	 * if it's not defined or is the empty string. This is equivalent to calling
	 * {@code decodeByte(label, settings, null)}
	 * 
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link Byte} value
	 */
	public static Byte decodeByte(String label, Map<String, String> settings) {
		return CfgTools.decodeByte(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link Byte} value, returning {@code null}
	 * if it's not defined or is the empty string. This is equivalent to calling
	 * {@code decodeByte(setting.getLabel(), settings, setting.getDefaultValue())}. The default
	 * value obtained from {@code setting} is cast to a {@link Byte} if it's a Byte-type, and
	 * passed on to the subsequent calls. Otherwise, the value is parsed using
	 * {@link Byte#valueOf(String)}. If the default value is {@code null}, then it's simply passed
	 * verbatim.
	 * 
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link Byte} value
	 */
	public static Byte decodeByte(ConfigurationSetting setting, Map<String, String> settings) {
		CfgTools.validateSetting(setting);
		Byte defaultValue = null;
		Object dv = setting.getDefaultValue();
		if (dv != null) {
			if (dv instanceof Byte) {
				defaultValue = Byte.class.cast(dv);
			} else if (dv instanceof Number) {
				defaultValue = Number.class.cast(dv).byteValue();
			} else {
				defaultValue = Byte.valueOf(dv.toString());
			}
		}
		return CfgTools.decodeByte(setting.getLabel(), settings, defaultValue);
	}

	/**
	 * Decode the named setting from the given map as a {@link Short} value, returning the value
	 * sent in {@code defaultValue} if it's not defined or is the empty string.
	 * 
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link Short} value
	 */
	public static Short decodeShort(String label, Map<String, String> settings, Short defaultValue) {
		CfgTools.validateSetting(label);
		String value = Tools.toTrimmedString(settings.get(label), true);
		if (value != null) { return Short.valueOf(value); }
		if (defaultValue != null) { return Short.valueOf(defaultValue.shortValue()); }
		return null;
	}

	/**
	 * Decode the named setting from the given map as a {@link Short} value, returning {@code null}
	 * if it's not defined or is the empty string. This is equivalent to calling
	 * {@code decodeShort(label, settings, null)}
	 * 
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link Short} value
	 */
	public static Short decodeShort(String label, Map<String, String> settings) {
		return CfgTools.decodeShort(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link Short} value, returning {@code null}
	 * if it's not defined or is the empty string. This is equivalent to calling
	 * {@code decodeShort(setting.getLabel(), settings, setting.getDefaultValue())}. The default
	 * value obtained from {@code setting} is cast to a {@link Short} if it's a Short-type, and
	 * passed on to the subsequent calls. Otherwise, the value is parsed using
	 * {@link Short#valueOf(String)}. If the default value is {@code null}, then it's simply passed
	 * verbatim.
	 * 
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link Short} value
	 */
	public static Short decodeShort(ConfigurationSetting setting, Map<String, String> settings) {
		CfgTools.validateSetting(setting);
		Short defaultValue = null;
		Object dv = setting.getDefaultValue();
		if (dv != null) {
			if (dv instanceof Short) {
				defaultValue = Short.class.cast(dv);
			} else if (dv instanceof Number) {
				defaultValue = Number.class.cast(dv).shortValue();
			} else {
				defaultValue = Short.valueOf(dv.toString());
			}
		}
		return CfgTools.decodeShort(setting.getLabel(), settings, defaultValue);
	}

	/**
	 * Decode the named setting from the given map as a {@link Integer} value, returning the value
	 * sent in {@code defaultValue} if it's not defined or is the empty string.
	 * 
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as an {@link Integer} value
	 */
	public static Integer decodeInteger(String label, Map<String, String> settings, Integer defaultValue) {
		CfgTools.validateSetting(label);
		String value = Tools.toTrimmedString(settings.get(label), true);
		if (value != null) { return Integer.valueOf(value); }
		if (defaultValue != null) { return Integer.valueOf(defaultValue.intValue()); }
		return null;
	}

	/**
	 * Decode the named setting from the given map as a {@link Integer} value, returning
	 * {@code null} if it's not defined or is the empty string. This is equivalent to calling
	 * {@code decodeInteger(label, settings, null)}
	 * 
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as an {@link Integer} value
	 */
	public static Integer decodeInteger(String label, Map<String, String> settings) {
		return CfgTools.decodeInteger(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link Integer} value, returning
	 * {@code null} if it's not defined or is the empty string. This is equivalent to calling
	 * {@code decodeInteger(setting.getLabel(), settings, setting.getDefaultValue())}. The default
	 * value obtained from {@code setting} is cast to a {@link Integer} if it's a Integer-type, and
	 * passed on to the subsequent calls. Otherwise, the value is parsed using
	 * {@link Integer#valueOf(String)}. If the default value is {@code null}, then it's simply
	 * passed
	 * verbatim.
	 * 
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as an {@link Integer} value
	 */
	public static Integer decodeInteger(ConfigurationSetting setting, Map<String, String> settings) {
		CfgTools.validateSetting(setting);
		Integer defaultValue = null;
		Object dv = setting.getDefaultValue();
		if (dv != null) {
			if (dv instanceof Integer) {
				defaultValue = Integer.class.cast(dv);
			} else if (dv instanceof Number) {
				defaultValue = Number.class.cast(dv).intValue();
			} else {
				defaultValue = Integer.valueOf(dv.toString());
			}
		}
		return CfgTools.decodeInteger(setting.getLabel(), settings, defaultValue);
	}

	/**
	 * Decode the named setting from the given map as a {@link Long} value, returning the value
	 * sent in {@code defaultValue} if it's not defined or is the empty string.
	 * 
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link Long} value
	 */
	public static Long decodeLong(String label, Map<String, String> settings, Long defaultValue) {
		CfgTools.validateSetting(label);
		String value = Tools.toTrimmedString(settings.get(label), true);
		if (value != null) { return Long.valueOf(value); }
		if (defaultValue != null) { return Long.valueOf(defaultValue.longValue()); }
		return null;
	}

	/**
	 * Decode the named setting from the given map as a {@link Long} value, returning {@code null}
	 * if it's not defined or is the empty string. This is equivalent to calling
	 * {@code decodeLong(label, settings, null)}
	 * 
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link Long} value
	 */
	public static Long decodeLong(String label, Map<String, String> settings) {
		return CfgTools.decodeLong(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link Long} value, returning {@code null}
	 * if it's not defined or is the empty string. This is equivalent to calling
	 * {@code decodeLong(setting.getLabel(), settings, setting.getDefaultValue())}. The default
	 * value obtained from {@code setting} is cast to a {@link Long} if it's a Long-type, and
	 * passed on to the subsequent calls. Otherwise, the value is parsed using
	 * {@link Long#valueOf(String)}. If the default value is {@code null}, then it's simply passed
	 * verbatim.
	 * 
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link Long} value
	 */
	public static Long decodeLong(ConfigurationSetting setting, Map<String, String> settings) {
		CfgTools.validateSetting(setting);
		Long defaultValue = null;
		Object dv = setting.getDefaultValue();
		if (dv != null) {
			if (dv instanceof Long) {
				defaultValue = Long.class.cast(dv);
			} else if (dv instanceof Number) {
				defaultValue = Number.class.cast(dv).longValue();
			} else {
				defaultValue = Long.valueOf(dv.toString());
			}
		}
		return CfgTools.decodeLong(setting.getLabel(), settings, defaultValue);
	}

	/**
	 * Decode the named setting from the given map as a {@link Float} value, returning the value
	 * sent in {@code defaultValue} if it's not defined or is the empty string.
	 * 
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link Float} value
	 */
	public static Float decodeFloat(String label, Map<String, String> settings, Number defaultValue) {
		CfgTools.validateSetting(label);
		String value = Tools.toTrimmedString(settings.get(label), true);
		if (value != null) { return Float.valueOf(value); }
		if (defaultValue != null) { return Float.valueOf(defaultValue.floatValue()); }
		return null;
	}

	/**
	 * Decode the named setting from the given map as a {@link Float} value, returning {@code null}
	 * if it's not defined or is the empty string. This is equivalent to calling
	 * {@code decodeFloat(label, settings, null)}
	 * 
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link Float} value
	 */
	public static Float decodeFloat(String label, Map<String, String> settings) {
		return CfgTools.decodeFloat(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link Float} value, returning {@code null}
	 * if it's not defined or is the empty string. This is equivalent to calling
	 * {@code decodeFloat(setting.getLabel(), settings, setting.getDefaultValue())}. The default
	 * value obtained from {@code setting} is cast to a {@link Float} if it's a Float-type, and
	 * passed on to the subsequent calls. Otherwise, the value is parsed using
	 * {@link Float#valueOf(String)}. If the default value is {@code null}, then it's simply passed
	 * verbatim.
	 * 
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link Float} value
	 */
	public static Float decodeFloat(ConfigurationSetting setting, Map<String, String> settings) {
		CfgTools.validateSetting(setting);
		Float defaultValue = null;
		Object dv = setting.getDefaultValue();
		if (dv != null) {
			if (dv instanceof Float) {
				defaultValue = Float.class.cast(dv);
			} else if (dv instanceof Number) {
				defaultValue = Number.class.cast(dv).floatValue();
			} else {
				defaultValue = Float.valueOf(dv.toString());
			}
		}
		return CfgTools.decodeFloat(setting.getLabel(), settings, defaultValue);
	}

	/**
	 * Decode the named setting from the given map as a {@link Double} value, returning the value
	 * sent in {@code defaultValue} if it's not defined or is the empty string.
	 * 
	 * @param label
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link Double} value
	 */
	public static Double decodeDouble(String label, Map<String, String> settings, Number defaultValue) {
		CfgTools.validateSetting(label);
		String value = Tools.toTrimmedString(settings.get(label), true);
		if (value != null) { return Double.valueOf(value); }
		if (defaultValue != null) { return Double.valueOf(defaultValue.doubleValue()); }
		return null;
	}

	/**
	 * Decode the named setting from the given map as a {@link Double} value, returning {@code null}
	 * if it's not defined or is the empty string. This is equivalent to calling
	 * {@code decodeDouble(label, settings, null)}
	 * 
	 * @param label
	 * @param settings
	 * @return the named setting from the given map as a {@link Double} value
	 */
	public static Double decodeDouble(String label, Map<String, String> settings) {
		return CfgTools.decodeDouble(label, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link Double} value, returning {@code null}
	 * if it's not defined or is the empty string. This is equivalent to calling
	 * {@code decodeDouble(setting.getLabel(), settings, setting.getDefaultValue())}. The default
	 * value obtained from {@code setting} is cast to a {@link Double} if it's a Double-type, and
	 * passed on to the subsequent calls. Otherwise, the value is parsed using
	 * {@link Double#valueOf(String)}. If the default value is {@code null}, then it's simply passed
	 * verbatim.
	 * 
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link Double} value
	 */
	public static Double decodeDouble(ConfigurationSetting setting, Map<String, String> settings) {
		CfgTools.validateSetting(setting);
		Double defaultValue = null;
		Object dv = setting.getDefaultValue();
		if (dv != null) {
			if (dv instanceof Double) {
				defaultValue = Double.class.cast(dv);
			} else if (dv instanceof Number) {
				defaultValue = Number.class.cast(dv).doubleValue();
			} else {
				defaultValue = Double.valueOf(dv.toString());
			}
		}
		return CfgTools.decodeDouble(setting.getLabel(), settings, defaultValue);
	}

	/**
	 * Returns the named setting from the given map, returning the value
	 * sent in {@code defaultValue} if it's not defined or is the empty string.
	 * 
	 * @param setting
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link String} value
	 */
	public static String decodeString(String setting, Map<String, String> settings, String defaultValue) {
		CfgTools.validateSetting(setting);
		String value = Tools.toString(settings.get(setting), true);
		if (value == null) { return String.class.cast(defaultValue); }
		return value;
	}

	/**
	 * Decode the named setting from the given map, returning {@code null} if it's not defined or is
	 * the empty string. This is equivalent to calling {@code decodeString(label, settings, null)}
	 * 
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link String} value
	 */
	public static String decodeString(String setting, Map<String, String> settings) {
		return CfgTools.decodeString(setting, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@link String} value, returning {@code null}
	 * if it's not defined or is the empty string. This is equivalent to calling
	 * {@code decodeString(setting.getLabel(), settings, setting.getDefaultValue())}. The default
	 * value obtained from {@code setting} is cast to a {@link String} if it's a String-type, and
	 * passed on to the subsequent calls. Otherwise, the value is parsed using
	 * {@link Tools#toString(Object, boolean)} with the {@code emptyAsNull} parameter set to
	 * {@code true}. This means that empty strings will not be returned - and will only be
	 * returned as {@code null}. If the default value is {@code null}, then it's simply
	 * passed verbatim.
	 * 
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link String} value
	 */
	public static String decodeString(ConfigurationSetting setting, Map<String, String> settings) {
		CfgTools.validateSetting(setting);
		return CfgTools.decodeString(setting.getLabel(), settings, Tools.toString(setting.getDefaultValue(), true));
	}

	/**
	 * Returns the named setting from the given map, returning the value sent in
	 * {@code defaultValue} if it's not defined or is the empty string. The value is presumed to be
	 * Base64 encoded.
	 * 
	 * @param setting
	 * @param settings
	 * @param defaultValue
	 * @return the named setting from the given map as a {@link String} value
	 * @throws DecoderException
	 *             if the string value is not a valid Base64 string
	 */
	public static byte[] decodeBinary(String setting, Map<String, String> settings, byte[] defaultValue)
		throws DecoderException {
		String str = CfgTools.decodeString(setting, settings, null);
		if (str == null) { return defaultValue; }
		if (!Base64.isBase64(str)) { throw new DecoderException("The given string is not a valid Base64 string"); }
		return Base64.decodeBase64(str);
	}

	/**
	 * Decode the named setting from the given map, returning {@code null} if it's not defined or is
	 * the empty string. This is equivalent to calling {@code decodeBinary(label, settings, null)}
	 * 
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link String} value
	 * @throws DecoderException
	 *             if the string value is not a valid Base64 string
	 */
	public static byte[] decodeBinary(String setting, Map<String, String> settings) throws DecoderException {
		return CfgTools.decodeBinary(setting, settings, null);
	}

	/**
	 * Decode the given setting from the given map as a {@code byte[]} value, returning {@code null}
	 * if it's not defined or is the empty string. This is equivalent to calling
	 * {@code decodeBinary(setting.getLabel(), settings, setting.getDefaultValue())}. The default
	 * value obtained from {@code setting} is cast to a {@code byte[]} if it's a byte[]-type, and
	 * passed on to the subsequent calls. Otherwise, the value is converted using
	 * {@link Object#toString()} and then decoded as a Base64 value. If the conversion result isn't
	 * a valid Base64 string, then a {@link DecoderException} is raised. If the default value is
	 * {@code null}, then it's simply
	 * passed verbatim.
	 * 
	 * @param setting
	 * @param settings
	 * @return the named setting from the given map as a {@link String} value
	 * @throws DecoderException
	 *             if the string value is not a valid Base64 string, or if the default value can't
	 *             be properly decoded into a byte[] (not a byte[], or its toString() isn't a
	 *             Base-64 encoded binary
	 */
	public static byte[] decodeBinary(ConfigurationSetting setting, Map<String, String> settings)
		throws DecoderException {
		CfgTools.validateSetting(setting);
		Object dv = setting.getDefaultValue();
		if ((dv != null) && (!(dv instanceof byte[]))) {
			final String str = dv.toString();
			if (!Base64.isBase64(str)) { throw new DecoderException(
				"The given default value is not a valid Base64 string"); }
			dv = Base64.decodeBase64(str);
		}
		return CfgTools.decodeBinary(setting.getLabel(), settings, (byte[]) dv);
	}

	private Map<String, String> settings;

	public CfgTools(Map<String, String> settings) {
		this.settings = settings;
	}

	public Boolean getBoolean(String label, Boolean defaultValue) {
		return CfgTools.decodeBoolean(label, this.settings, defaultValue);
	}

	public Boolean getBoolean(String label) {
		return CfgTools.decodeBoolean(label, this.settings);
	}

	public Boolean getBoolean(ConfigurationSetting setting) {
		return CfgTools.decodeBoolean(setting, this.settings);
	}

	public Byte getByte(String label, Byte defaultValue) {
		return CfgTools.decodeByte(label, this.settings, defaultValue);
	}

	public Byte getByte(String label) {
		return CfgTools.decodeByte(label, this.settings);
	}

	public Byte getByte(ConfigurationSetting setting) {
		return CfgTools.decodeByte(setting, this.settings);
	}

	public Short getShort(String label, Short defaultValue) {
		return CfgTools.decodeShort(label, this.settings, defaultValue);
	}

	public Short getShort(String label) {
		return CfgTools.decodeShort(label, this.settings);
	}

	public Short getShort(ConfigurationSetting setting) {
		return CfgTools.decodeShort(setting, this.settings);
	}

	public Integer getInteger(String label, Integer defaultValue) {
		return CfgTools.decodeInteger(label, this.settings, defaultValue);
	}

	public Integer getInteger(String label) {
		return CfgTools.decodeInteger(label, this.settings);
	}

	public Integer getInteger(ConfigurationSetting setting) {
		return CfgTools.decodeInteger(setting, this.settings);
	}

	public Long getLong(String label, Long defaultValue) {
		return CfgTools.decodeLong(label, this.settings, defaultValue);
	}

	public Long getLong(String label) {
		return CfgTools.decodeLong(label, this.settings);
	}

	public Long getLong(ConfigurationSetting setting) {
		return CfgTools.decodeLong(setting, this.settings);
	}

	public Float getFloat(String label, Number defaultValue) {
		return CfgTools.decodeFloat(label, this.settings, defaultValue);
	}

	public Float getFloat(String label) {
		return CfgTools.decodeFloat(label, this.settings);
	}

	public Float getFloat(ConfigurationSetting setting) {
		return CfgTools.decodeFloat(setting, this.settings);
	}

	public Double getDouble(String label, Number defaultValue) {
		return CfgTools.decodeDouble(label, this.settings, defaultValue);
	}

	public Double getDouble(String label) {
		return CfgTools.decodeDouble(label, this.settings);
	}

	public Double getDouble(ConfigurationSetting setting) {
		return CfgTools.decodeDouble(setting, this.settings);
	}

	public String getString(String setting, String defaultValue) {
		return CfgTools.decodeString(setting, this.settings, defaultValue);
	}

	public String getString(String setting) {
		return CfgTools.decodeString(setting, this.settings);
	}

	public String getString(ConfigurationSetting setting) {
		return CfgTools.decodeString(setting, this.settings);
	}

	public byte[] getBinary(String setting, byte[] defaultValue) throws DecoderException {
		return CfgTools.decodeBinary(setting, this.settings, defaultValue);
	}

	public byte[] getBinary(String setting) throws DecoderException {
		return CfgTools.decodeBinary(setting, this.settings);
	}

	public byte[] getBinary(ConfigurationSetting setting) throws DecoderException {
		return CfgTools.decodeBinary(setting, this.settings);
	}

	public int getCount() {
		return this.settings.size();
	}

	public Set<String> getSettings() {
		return this.settings.keySet();
	}
}