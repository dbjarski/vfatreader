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
 * <code>Bytes</code> defines static methods for converting contiguous bytes into
 * larger data types.
 * 
 * @author David Jarski
 *
 * (c) 2014-07-31
 * 
 */

public class Bytes
{
	/**
	 * Converts two little-endian bytes into a short.
	 * 
	 * @param b  the byte array
	 * @param offset  the index of the first byte to read from the array
	 * @return  the (big-endian) short
	 */
	public static short getShort(byte[] b, int offset) {
		return (short)((b[offset+1] << 8) | (b[offset] & 0xFF));
	}
	
	/**
	 * Converts two little-endian bytes into a char
	 * 
	 * @param b  the byte array
	 * @param offset  the index of the first byte to read from the array
	 * @return  the (big-endian) char
	 */
	public static char getChar(byte[] b, int offset) {
		return (char)((b[offset+1] << 8) | (b[offset] & 0xFF));
	}
	
	/**
	 * Converts four little-endian bytes into a char
	 * 
	 * @param b  the byte array
	 * @param offset  the index of the first byte to read from the array
	 * @return  the (big-endian) int
	 */
	public static int getInt(byte[] b, int offset) {
		return  (b[offset+3] << 24) | ((b[offset+2] & 0xFF) << 16) |
				((b[offset+1] & 0xFF) << 8) | (b[offset] & 0xFF);
	}
	
	/**
	 * Converts a byte sequence into an String of ASCII characters. If a byte
	 * does not contain a valid ASCII text character, the byte is translated to
	 * the Unicode replacement character U+FFFD.
	 * 
	 * @param b  the byte array
	 * @param offset  the index of the first byte to read from the array
	 * @param count  the number of bytes to read from the array
	 * @return  a String constructed from the specified byte sequence
	 */
	/* We'd probably be safe doing an "ANSI" version as well, by assuming an
	   8-bit encoding to be Windows-1252 (cp1252), but it'd be a bit more work to
	   convert from Windows-1252 to UTF-16. Charset could do it, though:
	   http://docs.oracle.com/javase/7/docs/api/java/nio/charset/package-summary.html
	 */
	public static String getAsciiString(byte[] b, int offset, int count) {
		char[] chars = new char[count];
		char nextChar;
		for (int i = 0; i < count; ++i) {
			nextChar = (char)(b[offset++] & 0xFF);
			// make sure nextChar is in the range of printable ASCII codes
			if( nextChar >= 0x20 && nextChar < 0x7F) {
				chars[i] = nextChar;
			} else {
				chars[i] = C.REPLACEMENT_CHARACTER;
			}
		}
		return String.valueOf(chars);
	}
}
