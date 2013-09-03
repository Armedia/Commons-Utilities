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
package com.armedia.util;

import org.apache.commons.codec.binary.Base64;

import com.armedia.commons.utilities.ConfigurationSetting;

/**
 * @author drivera@armedia.com
 * 
 */
public enum TestSetting implements ConfigurationSetting {
	BOOLEAN_TRUE_UNDEF(true),
	BOOLEAN_TRUE(true),
	BOOLEAN_EMPTY(),
	BOOLEAN_FALSE(false),
	BOOLEAN_FALSE_UNDEF(false),

	BYTE_OVER(128),
	BYTE_MAX_UNDEF(127),
	BYTE_MAX(127),
	BYTE_EMPTY(),
	BYTE_MIN(-128),
	BYTE_MIN_UNDEF(-128),
	BYTE_UNDER(-129),
	BYTE_BYTE((byte) 64),
	BYTE_SHORT((short) 64),
	BYTE_INTEGER(64),
	BYTE_LONG(64L),
	BYTE_FLOAT(64.35f),
	BYTE_DOUBLE(64.35),

	SHORT_OVER(32768),
	SHORT_MAX_UNDEF(32767),
	SHORT_MAX(32767),
	SHORT_EMPTY(),
	SHORT_MIN(-32768),
	SHORT_MIN_UNDEF(-32768),
	SHORT_UNDER(-32769),
	SHORT_BYTE((byte) 64),
	SHORT_SHORT((short) 64),
	SHORT_INTEGER(64),
	SHORT_LONG(64L),
	SHORT_FLOAT(64.35f),
	SHORT_DOUBLE(64.35),

	INTEGER_OVER(2147483648L),
	INTEGER_MAX_UNDEF(2147483647),
	INTEGER_MAX(2147483647),
	INTEGER_EMPTY(),
	INTEGER_MIN(-2147483648),
	INTEGER_MIN_UNDEF(-2147483648),
	INTEGER_UNDER(-2147483649L),
	INTEGER_BYTE((byte) 64),
	INTEGER_SHORT((short) 64),
	INTEGER_INTEGER(64),
	INTEGER_LONG(64L),
	INTEGER_FLOAT(64.35f),
	INTEGER_DOUBLE(64.35),

	LONG_OVER("9223372036854775808"),
	LONG_MAX_UNDEF(9223372036854775807L),
	LONG_MAX(9223372036854775807L),
	LONG_EMPTY(),
	LONG_MIN(-9223372036854775808L),
	LONG_MIN_UNDEF(-9223372036854775808L),
	LONG_UNDER("-9223372036854775809"),
	LONG_BYTE((byte) 64),
	LONG_SHORT((short) 64),
	LONG_INTEGER(64),
	LONG_LONG(64L),
	LONG_FLOAT(64.35f),
	LONG_DOUBLE(64.35),

	FLOAT_OVER(3402823466385288600000000000000000000000.000000),
	FLOAT_MAX_UNDEF(340282346638528860000000000000000000000.000000f),
	FLOAT_MAX(340282346638528860000000000000000000000.000000f),
	FLOAT_EMPTY(),
	FLOAT_MIN(-340282346638528860000000000000000000000.000000f),
	FLOAT_MIN_UNDEF(-340282346638528860000000000000000000000.000000f),
	FLOAT_UNDER(-3402823466385288600000000000000000000000.000000),
	FLOAT_BYTE((byte) 64),
	FLOAT_SHORT((short) 64),
	FLOAT_INTEGER(64),
	FLOAT_LONG(64L),
	FLOAT_FLOAT(64.35f),
	FLOAT_DOUBLE(64.35),

	DOUBLE_OVER(
		"1797693134862315700000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.000000"),
	DOUBLE_MAX_UNDEF(
		179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.000000),
	DOUBLE_MAX(
		179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.000000),
	DOUBLE_EMPTY(),
	DOUBLE_MIN(
		-179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.000000),
	DOUBLE_MIN_UNDEF(
		-179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.000000),
	DOUBLE_UNDER(
		"-1797693134862315700000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.000000"),
	DOUBLE_BYTE((byte) 64),
	DOUBLE_SHORT((short) 64),
	DOUBLE_INTEGER(64),
	DOUBLE_LONG(64L),
	DOUBLE_FLOAT(64.35f),
	DOUBLE_DOUBLE(64.35),

	STRING_EMPTY(),
	STRING_UNDEF("'R-jXvzq4H#wF/6 s|?XN&*c7n;zf'!N~};PM/NL8$#<8fn}N7fkKS!n|c 4GN?8;B&V;_qDL&?) 5+_"),
	STRING_SAMPLE("`d^sL^LlL D&`6N|&!>f(hb.jq)!v)FdT$2zp7CWn{Q?7%KB#tLvsZp}fXfpjW p_2L?(${bh}k^kX4$"),

	BINARY_EMPTY(),
	BINARY_UNDEF(
		// Checksum = 3a8facc69441078c0214cab3477997debf35926b79a422f30f51817f2d3f7321
		Base64
			.decodeBase64("xy0dZm9C5zT/6ZCdq7ttZ2MHbHLpnqGIlWosnTmHD1v/k30tVc++fhtDdyY31k5tgPpfnDVzNgE4Au8eMvmjlr16e3hrFSuWhc7Bjb05vwodNHoIK0j9LHlO0vcp/WeptHpJlop3ThSYRnr3d/Hx1LkOE3gDpcyUmgeVTr97xkjH3tJgQhCEVzyPd593DHnBu8v0yC0QzJmI+H51Px2sLPlVsapDkztv1ORrwva2XFLhWHyI7C+294/DLHimdZD6qrpsktVXpolGkF09SZsnJvziuRgm/Zdq0gfVSVbjJy4ra8FN3iSegEWMGdg/GweyLyvOVqHFBjMTfg1IxltCzqF5xmQIDvox9CyuzAvYcsQk2/LCn6sciz93Wk5/xheiZIFZrtjx+A9aLfPqmMIDDsPJ4yXD0kgRtBBMzyEMkrTTswLOABmKI3JvMwtxH33vMeiqPTtvhts4gbo1xOUJMb6q0KYq6cs5f/vT5V2r8jHidJ+vc5ROYxi8Wb16js/lLW2AQzOO8Qi7KJZYGpFHB0oCXIuyUO8qvAPYDjX4RbT3Z5wX8bssVUglu/u4hr5i1eV6uR6AfnhueoXl8+duCpegQyNgIvuhUM2sfQYo/l7fIHu18kxKVPj2Tit1ruu1gUNKSNQUUliG0gdWi8xMcjaEIEwOPAihRgU5qDLS9kOd/UTNfdlS+TjtcS8zeT1QwedPD5M40hqmPtdnwv6ZUglcZSF2qlcu7WyimwmGfoyJXmXpK0c64yTL/2lKGsXQo5F12UIeZV82bRWY8FtWBZehM+5w50JLsbjb6kUh6T+Ly3xf8ge0OQdbw/Dp9EBxnhtuYUXeOpOwE0+QSFH15V1847gAGZTtRu2UpXG3vwF4Nn9PlY9HhUHg+redI3YMTs5Tgq/nauMrGmbkm33vDOVuGQ+7Amv0x5hX+jjNvR6oNYe5xEwr3mtRA5Tv9o0jcpkySDIygGbrjJRSIyH78EmgtE30WWIx2YT7qB61pD5nk1/UUHcyO6Z+3IB1Oc+n4ArdvXuGvDFpTT7eg1eKKZnPYg3NbOpAZRJYskZJaGAzCKf6DXDbkopASPZAm7yF/oNdYb3yw02dg4bGWrt6uwlnIYbEBcFVjfEqhEERRO92rhRwaw8fOjnWO3Ho33wv3dwkWdJ3gvAvK+xWHe0TSF2WkhlZZx//zzkDG+ZcXf1y7iPP+GeqZLUypLIrxB73sx6S7Evgj9KKHORDPSYr+S3zj/mgpDkCuhdldPr6rdz4F21DpwTcHOrA8he8r6gKYtUVqCBB2qaSZfUZGB+eo0p3ORyItURgLvYs8GnC9izVXv0tlDDLki0fkpFx7wfOHX4WbCcdIN+31RU6sRaj/Q==")),
	BINARY_SAMPLE(
		// Checksum = 9a49cb66ffa9a793f807ba8b69104d319e28a7bfc4fce501d6f074c63d556f99
		Base64
			.decodeBase64("Z3ay+B2Js47GLO3lBuSOhyButcqmy5Pv9bk1E/BpcZ9eRLS/rBN0qChaKX+MbZAYgilNtB2G41EZkSqUay1fier4EEmQ/3MDv+/1bQ2I4h1eAiURadhvoWeMiGx2xbRsdAzTJS07wa6xTc95VDtHSMMuBR0EzSPNezx2n3qm256G7Pb3wJAYwJX0TzFtlVkf+UGATrGGTyMSXaZFgOQGPDN0njnVfkGqGXyICtJazMBf2BRRWf2DwfhCDPh33yAXSSg+UHZ2a4eESWhhTMT+cdO5881B+zH7mvVDFweQNxgftNxCOSX+Y1hd4weewy3pqp8M/TuAlSAq/4SxOHIbjBXk9cdbbwsdDVa/OmIujXSPp4TXqaPOLWhrjgqhDUsJGKMVXni+a0dlmDcvzdNSew1pLc7zA3Z1Y4xVgZfJs89qE2YDJG3hEsV7djKlkpifEfu5hyfSGi+S0twsaVPdlPfGNOgUshufyccXr2Ld92NaiukScKToZt5yp0oozCM1Qc1ykGu1ZQS/NyDBzFAMiHK5UeayiE31MjeNt7Wo02EpU/3T/c5XJuihbYZ5rVbGdJ0Lq0YfiyrRX9bFcYBENoKrx6HnvcCMfsgEExkatK3OhDI6aHWgGqUyK7OMsimDBRY5IiK0LwjvI/4HF3QQGVB8XsHM1uBPk6vrtwUKZJUaFRJsk/J2ef+p9rvtDhpbhWmsG4q2h1B7f+RFNxvGXS+t1GXLScZrheeB1wYU3dAWTOPrKBWD9wwp/rN6V2Sw6coqBXHC2HK2xmVFEJAJVfEyneAZfDmfgusR464vLwO2vs3dwBuIY22EgTaZe3hfryvsH5NTHO/J0Xvf0jplx+C8D0TJVL4IHzz7C37kfRvE1XTpfN+725ToVRETkRHH+TQruMb1omGsaBA5AOuHv89jrXoC9pCsye8CLcCEOt2LDvs7Z1TzyGG55GYMPcfQQxg2sV0ARahsw2k+GaicIyW2QmOWoHmJQllcVyREABI9+rR0CItYzefh1EP67Ll/HzuIfFk7GudNOfjyKsdYdMXCc3DnRnzVyOkMzWcb4sF3oSABk3QO7OIKUtFDQ8L1PQmj0auu/MtVcetFlSgh6EZ917PZ/AlGDA8j+Y5B5ByzPj889/MVSISG2RdZw+Zi3y2Ycew3IY0BcnUEUlig0QIAOfYkKHhmgsIwCZzSlJqzffWSxA4NmnS4GTldskckPfopeU5cpudHECcVxS72SNnHSA/WhNJi6VLl+T/zqaLKofNsBigKf6xNwzQzZ17CsmKXpYLRijkaibe85QW5Mb0uIwbB9mFUDFmh4T7DE8QQ2TovkfRaByYGUpL9UlF2ViaJJxvO6zNrs99zUDUsDA=="));

	private final String label;
	private final Object defaultValue;

	private TestSetting() {
		this(null);
	}

	private TestSetting(Object defaultValue) {
		this.label = name().trim().toLowerCase().replace('_', '.');
		this.defaultValue = defaultValue;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	@Override
	public Object getDefaultValue() {
		return this.defaultValue;
	}
}