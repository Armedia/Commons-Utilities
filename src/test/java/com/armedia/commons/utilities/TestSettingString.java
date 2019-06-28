/*******************************************************************************
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (c) 2010 - 2019 Armedia LLC
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
 * @author drivera@armedia.com
 *
 */
public enum TestSettingString implements ConfigurationSetting {
	BOOLEAN_TRUE_UNDEF("true"),
	BOOLEAN_TRUE("true"),
	BOOLEAN_EMPTY(),
	BOOLEAN_FALSE("false"),
	BOOLEAN_FALSE_UNDEF("false"),

	BYTE_OVER("128"),
	BYTE_MAX_UNDEF("127"),
	BYTE_MAX("127"),
	BYTE_EMPTY(),
	BYTE_MIN("-128"),
	BYTE_MIN_UNDEF("-128"),
	BYTE_UNDER("-129"),

	SHORT_OVER("32768"),
	SHORT_MAX_UNDEF("32767"),
	SHORT_MAX("32767"),
	SHORT_EMPTY(),
	SHORT_MIN("-32768"),
	SHORT_MIN_UNDEF("-32768"),
	SHORT_UNDER("-32769"),

	INTEGER_OVER("2147483648"),
	INTEGER_MAX_UNDEF("2147483647"),
	INTEGER_MAX("2147483647"),
	INTEGER_EMPTY(),
	INTEGER_MIN("-2147483648"),
	INTEGER_MIN_UNDEF("-2147483648"),
	INTEGER_UNDER("-2147483649"),

	LONG_OVER("9223372036854775808"),
	LONG_MAX_UNDEF("9223372036854775807"),
	LONG_MAX("9223372036854775807"),
	LONG_EMPTY(),
	LONG_MIN("-9223372036854775808"),
	LONG_MIN_UNDEF("-9223372036854775808"),
	LONG_UNDER("-9223372036854775809"),

	FLOAT_OVER("3402823466385288600000000000000000000000.000000"),
	FLOAT_MAX_UNDEF("340282346638528860000000000000000000000.000000"),
	FLOAT_MAX("340282346638528860000000000000000000000.000000"),
	FLOAT_EMPTY(),
	FLOAT_MIN("-340282346638528860000000000000000000000.000000"),
	FLOAT_MIN_UNDEF("-340282346638528860000000000000000000000.000000"),
	FLOAT_UNDER("-3402823466385288600000000000000000000000.000000"),

	DOUBLE_OVER(
		"1797693134862315700000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.000000"),
	DOUBLE_MAX_UNDEF(
		"179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.000000"),
	DOUBLE_MAX(
		"179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.000000"),
	DOUBLE_EMPTY(),
	DOUBLE_MIN(
		"-179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.000000"),
	DOUBLE_MIN_UNDEF(
		"-179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.000000"),
	DOUBLE_UNDER(
		"-1797693134862315700000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.000000"),

	STRING_EMPTY(),
	STRING_UNDEF("kn>&V~s*.`_`s5?ngd7;bH :p` 4pmb]: )$~n;b?5?%)2QL3wX!F!M):LC)?(?R:9Kg2g[@589HK$t["),
	STRING_SAMPLE("}%>jrM8/hs2_ztFJ 5 SSt2p3(S))%R6swPkZ}WS[?,7(!m%hsf6WNG*LcmM<%(l 2Gc8d+)VMhR~2n'"),
	STRING_UNSET(),

	BINARY_EMPTY(),
	BINARY_UNDEF( // Checksum = f5c050a846647459de71cd7348f4c6987da8af0edf9af6e0cd0a3151a3a43c54
		"mgFLqv8Ljr6mAcqI8RcKfj9v0aMROcZP7MpXq2A6ZIPJmCLHmFP5niuEPu3swJikfHIhPr7e+czQVKsfc63KIsPnxz44BKRPTtoOvFE91mW+SoW5ep2U/IC+ytrRu88qyum+6BHqsnqMfeDf7bkbSgpHEuob9RFT9Ic/OLke6rpjxp/Kwq6+cbfK+nN0jVzV1WmuNJh3/S+H3f2L2uyteCXMHqxe710/n9s2Bu5+KuK607fNPt3jjFU2X1fnBSpofZK4izRMgTmp+SPMNt82+iMwRhuWodnByRrvcll5+qcj95Eeqkgtk0zA+BE1sCgBPHhO5jQn2JuLay+O4VOstLRnI6ZocXs+sS1T3GmSI6Yuvd7cvQQ9NFOA+eZcuphIdEVKB8ddAC2v3bg/AYq5pgYFQ+5x4N5zpivfzuPZcK84o7C9Uc/tTPXozTsWegLEsD14kL6M8Qu1OxnuDciNTEqI7TdMhb01fLny8ICdwv7gxH3IVmE64UqJh3Y7vq3+Xvj1As27/6ox3lQvM74CwOdaxlJSA1bGTtY3rTeb7dHN6BVwqM/vWjQ0q2T++MU6UU3zgRhrl4QCOYDFvjfcL22h5JpVem+hZgGthGi90DpVxraJFg2LPjDQQoWGKW84PU+ks4fX0MDjzpZgW8tgPD/6lh4+TTer2yCdON0ccgEvaCDQse4KGu7yQnQhySsepufuZ3dG5plFdI7dJYs8O1VIb2WOfPe01Kf6Kk06ylaHp71ZvxdTsNJesbXYzo0QETWNS+ECFY4Di/AnXRa56KQosNvyx7seaBsCIGdkLMLMUoB1HVTFe1POAiKB5BDQGyaYW9+WGB8MYG0mnpW3oZD2E5MKKm3iSHGwL2wBW1+OP4SaBQvtDpzcvHs1rM5z5pqmB97S8vNKgb4MQfPVwy/b8FmbYueV1H0am8CPZjCt0a1pSOR9LkmbrHAmwNf/WVYXDbbg4zg2V+rVBH8HAEnZGKe+ZbVMxnY1JCJd+JxdFaS1A2oNSDM91FlPmcQed2sWTV0ZfkarfyS5JZWTbRWwDciyzJ6YFXuMliwbXeOGrJolvDTi7x9t9BWbk9RaRwP9iOr2Wtq51ZWoA4yMo33yavWyqlRuC/v7O8xXlO7Avtug4mldKoldaFpYbCqfVliQiz8o5jO13cQuhkbfUpEwXKN+waBm+wd0jeBAbbu8rHkW+/0U+jSX75B9jXtdkMJivBf8sSCj6ED/iKiejZwnzLOPga3DgXOSJbk7mAeDTxeCwO5dIncOtQMxzsoq2UuRYct1V+DRGZSsJGeNTj3mwTbWCJpZB5PERpis1VDrkXcsDnZr4AHCKCwZShJEEBMipmkwzDFFJPGBrtMPiA=="),
	BINARY_SAMPLE( // Checksum = 933f16fa0a20881c369595c5ad02a6297287185526dccc78798cbe7c8539b88c
		"nl1rP+E4eL0JkNhkH1oGCnLp/ALDuW3lYJRqkp9iOm4r1sryNHn4DdXXOr+dSSWjImFLYMY/MZt/1XDnwNckrkXo/oAzcz5Adj8Bb66L7dj0uCxKegvHeSay+Z9uFBrraUj0XGwzK2zaXFZmu0r3sSJSN8WBxD6ODLEdh4dQGVh2Pp1ZY+CWXniM1KRTRpDibPr0rjI7gwCFqlRrU/I+6OGKPRC2AR4B6xGYTSVwalL9VLyLqXWFUvkzcdnLdWOkm1js1ptuojsVssiyWdF2Whg2QLSicSiAhq58fmyZzqMk3D5ZKxcPUmDIQjbx9qHz+CYof9nI3l9V5pukdU3tJoM/o1SKP2GRiKWHte4S/zdAnQoyT9F1R81cpcIRULmZEu3qV3fUglpOjOAAlO8BJaSIrS/JrnyMVrK+KbXrUxhFTwbJwotv76usQppdL/c8b6mLZbUqBeEJAf3Trxb4bGNLq8lvGVFOfknefxWroASEf3TddzTyVdn7jlQma3gvoCROk1reB5XAKaLeYQBc5l4zi/v2gC9dASzZbywQ0WFXxbO2AtO0yhiCEZj8pIdjtVf27E91hTjqMYgsBDTdHcZqwMcmUUxE9iAgaK53lqa+RIEuI5vE4OCa08D5CbM15SIDupupCRMJJq/TQ6AI4zkG7+LKzTHXkBvfLEG6db2bLHULDsp2ZrBJnH9ZIOdVJ7+RquNATOUbuwoOMoKt6IhdW+2Xsbb5UKnWNy4217EdR/QH02gN6TlxX4ohBjkU1T6+zD+NefYZxrGJsKoFJFyDnt9dSMHjGoiFhbi0OOXmuCQQlpGKHbDOkbWTiX53E9JQiHPBFA/VZt/mtGcTX4ydJO6gXP+m5PmHP8QKxb+whtvN7XkTbDQIjMRaTvIlaMPeT0ceOSEjZXvLrYUjjxR8F7vJkDf22R1hxixIuoybjojLlSx9/6H2bxpPOvHfBqoH1XTmblbz5fVTAAElE6c9hgsvK+Z7JYXLq8MmtFXPFws27R3cEnyPjFRhy7o9JQ6yWdtijcYkykV8vGy77Wtp1HRbizCJIc+Lf1KQ1FRMqCoTkkrrmG5xyBxGRVavsgWXznNdaTX7advLJWmxv8BGzeJLCMi30EBr/vz/ufH9ZXNuyM84pEF3G9rbva77VWAlzhGTkWs0K2UmsOI67bV3a1zJesa28y0aNj9otB7XTmRtggdhdVZHSM0KPQ7/9foy84WauDp7njyzUAq1Ay75jyPidsA8lUYQTgOT86kGENzbaYGukYdlMc9gBG7rVVrgoad4qgpgpCBd3VPvArq92n5MSPWefzoqSoQkeSH/WHlRiAFjvrXLUN/IUq0NvvRJbSzlik84dNrPbPvtEw==");

	private final String label;
	private final String defaultValue;

	private TestSettingString() {
		this(null);
	}

	private TestSettingString(String defaultValue) {
		this.label = name().trim().toLowerCase().replace('_', '.');
		this.defaultValue = Tools.toTrimmedString(defaultValue, true);
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