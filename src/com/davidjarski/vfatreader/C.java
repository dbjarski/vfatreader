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

/**
 * <code>C</code> defines static final constants.
 * 
 * @author David Jarski
 *
 * (c) 2014-07-31
 * 
 */

public class C
{
	public static final int BYTES_PER_SECTOR = 512;
	public static final int BYTES_PER_ENTRY = 32;
	public static final int MBR_START = 0;
	
	/** FAT value representing the end of a cluster chain */
	public static final int EOC = 0x0FFF;
	
	// general directory entry constants
	public static final int HAS_SIGNATURE = 0x29;
	
	/** Offset of file extension - short entry only */
	public static final int FILE_EXTENSION_OFFSET = 8;
	
	/** Offset of the byte that contains the file attributes */
	public static final int FILE_ATTRIBUTE_OFFSET = 11;
	
	/** Attribute value if the entry is a long entry */
	public static final int LONG_ENTRY = 0x0F;
	
	/** Bit 6 of the first byte */
	public static final int LAST_LONG_ENTRY_MASK = 0x40;	
	
	/** Bits 0-5 of the first byte<br>
	 *  Gets the long entry index number. The first index encountered is the<br>
	 *  last entry of the long filename. This index also represents the total<br>
	 *  number of long entries for the directory entry. */
	public static final int LONG_ENTRY_INDEX_MASK = 0x3F;
	
	public static final int UNICODE_CHARS_PER_ENTRY = 13;
	
	// date bit masks/shifts for directory entries
	/** Bits 0-4 <br> First day of month = 1 */
	public static final int DAY_OF_MONTH_MASK = 0x1F;
	
	/** Bits 5-8 <br> January = 1 */
	public static final int MONTH_MASK = 0x0F;
	
	/** Right bit shift */
	public static final int MONTH_SHIFT = 5;
	
	/** Bits 9-15 <br> Number of years since 1980 */
	public static final int YEAR_SINCE_1980_MASK = 0xFF;	// bits 9-15
	
	/** Right bit shift */
	public static final int YEAR_SHIFT = 9;
	
	// time bit masks/shifts for directory entries	
	/** Bits 0-4 <br> 2-second intervals */
	public static final int SECONDS_MASK = 0x1F;
	
	/** Bits 5-10 */
	public static final int MINUTES_MASK = 0x3F;
	
	/** Right bit shift */
	public static final int MINUTES_SHIFT = 5;
	
	/** Bits 11-15 */
	public static final int HOUR_OF_DAY_MASK = 0x1F;
	
	/** Right bit shift */
	public static final int HOUR_SHIFT = 11;
	
	// attribute bit masks for directory entries
	public static final int READ_ONLY_MASK = 0x01;
	public static final int HIDDEN_MASK = 0x02;
	public static final int SYSTEM_MASK = 0x04;
	public static final int VOLUME_MASK = 0x08;
	public static final int DIRECTORY_MASK = 0x10;
	public static final int ARCHIVE_MASK = 0x20;
	
	public static final char REPLACEMENT_CHARACTER = '\uFFFD';

}
