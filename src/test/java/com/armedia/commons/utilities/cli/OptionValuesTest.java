/*-
 * #%L
 * Armedia Commons Utilities
 * %%
 * Copyright (C) 2013 - 2025 Armedia, LLC
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
 */
package com.armedia.commons.utilities.cli;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

public class OptionValuesTest {

	private static final Random RANDOM = new Random(System.nanoTime());

	/*
	private static List<Option> OPTIONS;
	static {
		List<Option> l = new ArrayList<>();
		l.add(new OptionImpl() //
			.setShortOpt('a') //
			.setLongOpt("alpha") //
			.setArgumentLimits(0) //
			.setDescription("Alpha option - no arguments") //
			.setDefault("any") //
			.setValueFilter(new StringValueFilter( //
				false, // Case-insensitive
				"true", "yes", "on", "1", "false", "off", "no", "0", "any" //
			)) //
		);
	
		l.add(new OptionImpl() //
			.setShortOpt('b') //
			.setLongOpt("bravo") //
			.setArgumentLimits(1) //
			.setDescription("Bravo option - one argument") //
			.setDefault("bravo") //
			.setValueFilter(new StringValueFilter( //
				false, // Case-insensitive
				"bravo", "baker", "beta", "bully" //
			)) //
		);
	
		OptionValuesTest.OPTIONS = Tools.freezeList(l);
	}
	*/

	private List<Pair<Option, List<List<String>>>> renderOptions() {
		List<Pair<Option, List<List<String>>>> ret = new LinkedList<>();

		// How many options to render?
		final int options = OptionValuesTest.RANDOM.nextInt(10) + 1;
		for (int o = 0; o < options; o++) {
			// How many instances of the option to include
			final int instances = OptionValuesTest.RANDOM.nextInt(5) + 1;
			for (int i = 0; i < instances; i++) {
				// How many parameters will this instance have?
				final int parameters = OptionValuesTest.RANDOM.nextInt(10);

			}
		}

		return ret;
	}

	@Test
	public void testOptionValues() {
		new OptionValues();
	}

	@Test
	public void testClone() {
		OptionValues a = new OptionValues();
		OptionValues b = null;

		// Add options to a

		// Clone
		b = a.clone();

		// Compare a and b, and make sure they're IDENTICAL
	}

	@Test
	public void testAddOption() {

	}

	@Test
	public void testAddOptionCollectionOfString() {

	}

	@Test
	public void testIterator() {

	}

	@Test
	public void testShortOptions() {

	}

	@Test
	public void testGetOptionChar() {

	}

	@Test
	public void testHasOptionChar() {

	}

	@Test
	public void testLongOptions() {

	}

	@Test
	public void testGetOptionString() {

	}

	@Test
	public void testHasOptionString() {

	}

	@Test
	public void testIsDefinedOption() {

	}

	@Test
	public void testGetOptionOption() {

	}

	@Test
	public void testGetOptionValueByKey() {

	}

	@Test
	public void testGetStringOption() {

	}

	@Test
	public void testGetStringOptionString() {

	}

	@Test
	public void testGetStringsOption() {

	}

	@Test
	public void testGetStringsOptionListOfString() {

	}

	@Test
	public void testGetMappedOptionFunctionOfStringT() {

	}

	@Test
	public void testGetMappedOptionFunctionOfStringTT() {

	}

	@Test
	public void testGetAllMapped() {

	}

	@Test
	public void testGetBooleanOption() {

	}

	@Test
	public void testGetBooleanOptionBoolean() {

	}

	@Test
	public void testGetBooleansOption() {

	}

	@Test
	public void testGetIntegerOption() {

	}

	@Test
	public void testGetIntegerOptionInteger() {

	}

	@Test
	public void testGetIntegersOption() {

	}

	@Test
	public void testGetLongOption() {

	}

	@Test
	public void testGetLongOptionLong() {

	}

	@Test
	public void testGetLongsOption() {

	}

	@Test
	public void testGetFloatOption() {

	}

	@Test
	public void testGetFloatOptionFloat() {

	}

	@Test
	public void testGetFloatsOption() {

	}

	@Test
	public void testGetDoubleOption() {

	}

	@Test
	public void testGetDoubleOptionDouble() {

	}

	@Test
	public void testGetDoublesOption() {

	}

	@Test
	public void testGetBigIntegerOption() {

	}

	@Test
	public void testGetBigIntegerOptionBigInteger() {

	}

	@Test
	public void testGetBigIntegersOption() {

	}

	@Test
	public void testGetBigDecimalOption() {

	}

	@Test
	public void testGetBigDecimalOptionBigDecimal() {

	}

	@Test
	public void testGetBigDecimalsOption() {

	}

	@Test
	public void testGetEnumClassOfEOption() {

	}

	@Test
	public void testGetEnumClassOfEBiFunctionOfObjectExceptionEOption() {

	}

	@Test
	public void testGetEnumClassOfEOptionE() {

	}

	@Test
	public void testGetEnumClassOfEBiFunctionOfObjectExceptionEOptionE() {

	}

	@Test
	public void testGetEnumsClassOfEOption() {

	}

	@Test
	public void testGetEnumsClassOfEBiFunctionOfObjectExceptionEOption() {

	}

	@Test
	public void testGetEnumsClassOfEStringBiFunctionOfObjectExceptionEOption() {

	}

	@Test
	public void testGetEnumsClassOfEOptionSetOfE() {

	}

	@Test
	public void testGetEnumsClassOfEBiFunctionOfObjectExceptionEOptionSetOfE() {

	}

	@Test
	public void testGetEnumsClassOfEStringBiFunctionOfObjectExceptionEOptionSetOfE() {

	}

	@Test
	public void testIsPresentOption() {

	}

	@Test
	public void testGetOccurrencesOption() {

	}

	@Test
	public void testGetOccurrenceValuesOptionInt() {

	}

	@Test
	public void testGetValueCountOption() {

	}

	@Test
	public void testHasValuesOption() {

	}

	@Test
	public void testIsDefinedSupplierOfOption() {

	}

	@Test
	public void testGetOptionSupplierOfOption() {

	}

	@Test
	public void testGetBooleanSupplierOfOption() {

	}

	@Test
	public void testGetBooleanSupplierOfOptionBoolean() {

	}

	@Test
	public void testGetBooleansSupplierOfOption() {

	}

	@Test
	public void testGetIntegerSupplierOfOption() {

	}

	@Test
	public void testGetIntegerSupplierOfOptionInteger() {

	}

	@Test
	public void testGetIntegersSupplierOfOption() {

	}

	@Test
	public void testGetLongSupplierOfOption() {

	}

	@Test
	public void testGetLongSupplierOfOptionLong() {

	}

	@Test
	public void testGetLongsSupplierOfOption() {

	}

	@Test
	public void testGetFloatSupplierOfOption() {

	}

	@Test
	public void testGetFloatSupplierOfOptionFloat() {

	}

	@Test
	public void testGetFloatsSupplierOfOption() {

	}

	@Test
	public void testGetDoubleSupplierOfOption() {

	}

	@Test
	public void testGetDoubleSupplierOfOptionDouble() {

	}

	@Test
	public void testGetDoublesSupplierOfOption() {

	}

	@Test
	public void testGetBigIntegerSupplierOfOption() {

	}

	@Test
	public void testGetBigIntegerSupplierOfOptionBigInteger() {

	}

	@Test
	public void testGetBigIntegersSupplierOfOption() {

	}

	@Test
	public void testGetBigDecimalSupplierOfOption() {

	}

	@Test
	public void testGetBigDecimalSupplierOfOptionBigDecimal() {

	}

	@Test
	public void testGetBigDecimalsSupplierOfOption() {

	}

	@Test
	public void testGetStringSupplierOfOption() {

	}

	@Test
	public void testGetStringSupplierOfOptionString() {

	}

	@Test
	public void testGetStringsSupplierOfOption() {

	}

	@Test
	public void testGetStringsSupplierOfOptionListOfString() {

	}

	@Test
	public void testGetEnumClassOfESupplierOfOption() {

	}

	@Test
	public void testGetEnumClassOfESupplierOfOptionE() {

	}

	@Test
	public void testGetEnumClassOfEBiFunctionOfObjectExceptionESupplierOfOption() {

	}

	@Test
	public void testGetEnumClassOfEBiFunctionOfObjectExceptionESupplierOfOptionE() {

	}

	@Test
	public void testGetEnumsClassOfESupplierOfOption() {

	}

	@Test
	public void testGetEnumsClassOfESupplierOfOptionSetOfE() {

	}

	@Test
	public void testGetEnumsClassOfEBiFunctionOfObjectExceptionESupplierOfOption() {

	}

	@Test
	public void testGetEnumsClassOfEStringBiFunctionOfObjectExceptionESupplierOfOption() {

	}

	@Test
	public void testGetEnumsClassOfEBiFunctionOfObjectExceptionESupplierOfOptionSetOfE() {

	}

	@Test
	public void testGetEnumsClassOfEStringBiFunctionOfObjectExceptionESupplierOfOptionSetOfE() {

	}

	@Test
	public void testIsPresentSupplierOfOption() {

	}

	@Test
	public void testGetOccurrencesSupplierOfOption() {

	}

	@Test
	public void testGetOccurrenceValuesSupplierOfOptionInt() {

	}

	@Test
	public void testGetValueCountSupplierOfOption() {

	}

	@Test
	public void testHasValuesSupplierOfOption() {

	}

}
