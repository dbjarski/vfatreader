/* 
 * The MIT License
 *
 * Copyright 2014 David Jarski.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.davidjarski.vfatreader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

/**
 * <code>PrettyPrint</code> defines static methods for getting a formatted
 * <code>String</code> and/or appending a formatted <code>String</code> to
 * a <code>StringBuilder</code>.
 * 
 * @author David Jarski
 *
 * (c) 2014-07-31
 * 
 */

public class PrettyPrint
{
	
	/** Converts a <code>byte</code> into a zero-padded,
	 *  two-digit hex <code>String</code>
	 * @param b  the <code>byte</code> to convert
	 * @return a  zero-padded, two-digit hex <code>String</code>
	 */
	public static String hex(byte b) {
		return Integer.toHexString((b & 0xFF) >> 4)
				+ Integer.toHexString(b & 0x0F);
	}
	
	/** Appends a zero-padded, two-digit hex string to a <code>StringBuilder</code>.
	 * 
	 * @param b  the <code>byte</code> to convert into hex
	 * @param builder  the <code>StringBuilder</code> that the hex digits will be
	 * 					appended to
	 */
	public static void hex(byte b, StringBuilder builder) {
		builder.append(Integer.toHexString((b & 0xFF) >> 4));
		builder.append(Integer.toHexString(b & 0x0F));
	}
	
	/** Appends a zero-padded line number to a <code>StringBuilder</code>.
	 * @param lineNumber  the line number
	 * @param digits  the number of digits to use for the line number
	 * 					(for zero-padding)
	 * @param builder  the <code>StringBuilder</code> that the line number will be
	 * 					appended to
	 */
	public static void lineNumber(int lineNumber, int digits, StringBuilder builder) {
		String hex = Integer.toHexString(lineNumber).toUpperCase();
		int zeros = digits - hex.length();
		for (int j = 0; j < zeros; ++j) {
			builder.append(0);
		}
		builder.append(hex);
	}
	
	/** Parses a bit-packed date <code>short</code> and returns a formatted
	 *  <code>String</code>.
	 * @param date  the bit-packed date
	 * @return  a formatted <code>String</code>.
	 */
	public static String fatDate(short date) {
		int year = 1980 + ((date & 0xFFFF) >>> C.YEAR_SHIFT & C.YEAR_SINCE_1980_MASK);
		int month = (date >>> C.MONTH_SHIFT & C.MONTH_MASK) - 1;
		int dayOfMonth = date & C.DAY_OF_MONTH_MASK;
		GregorianCalendar calendar = new GregorianCalendar(year, month, dayOfMonth);
		DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM);
		return format.format(calendar.getTime());
	}
	
	/** Parses a bit-packed time <code>short</code> and returns a formatted
	 *  <code>String</code>.
	 * @param time  the bit-packed time
	 * @return  a formatted <code>String</code>.
	 */
	public static String fatTime(short time) {
		int hourOfDay = (time & 0xFFFF) >> C.HOUR_SHIFT & C.HOUR_OF_DAY_MASK;
		int minute = time >> C.MINUTES_SHIFT & C.MINUTES_MASK;
		int second = time & C.SECONDS_MASK;
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		GregorianCalendar calendar = new GregorianCalendar(1970, 0, 1, hourOfDay, minute, second);
		return format.format(calendar.getTime());
	}
	
	/** Parses a bit-packed attribute <code>byte</code> and returns a formatted
	 * <code>String</code>.
	 * @param attributes  the bit-packed attribute
	 * @return  a formatted <code>String</code>.
	 */
	public static String attributes(short attributes) {
		String output = "";
		if ((attributes & C.DIRECTORY_MASK) > 0) output += "D";
		if ((attributes & C.READ_ONLY_MASK) > 0) output += "R";
		if ((attributes & C.HIDDEN_MASK) > 0) output += "H";
		if ((attributes & C.SYSTEM_MASK) > 0) output += "S";
		if ((attributes & C.VOLUME_MASK) > 0) output += "V";
		if ((attributes & C.ARCHIVE_MASK) > 0) output += "A";
		return output;
	}
}
