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

import java.io.IOException;

/**
 * A <code>DirectoryEntry</code> is a data structure representing a single directory entry.
 * 
 * @author David Jarski
 *
 * (c) 2014-07-31
 * 
 */

public class DirectoryEntry
{
	private String filename;
	private String extension;
	private byte attributes;
	private short creationTime;
	private short creationDate;
	private short lastAccessDate;
	private short lastWriteTime;
	private short lastWriteDate;
	private short firstLogicalCluster;
	private int fileSize;
	
	private static final String NUMBER_FORMAT = "%-24s%d\n";
	private static final String STRING_FORMAT = "%-24s%s\n";
	
	/** Private constructor
	 * @param filename  the long filename - <code>null</code> references are not accepted -
	 * 					use an empty <code>String</code> instead.
	 * @param buffer  an array containing the short entry of the directory entry
	 * @param offset  points to the first <code>byte</code> of the short entry
	 * @throws IOException if an error is encountered while parsing the entry
	 */
	private DirectoryEntry(String filename, byte[] buffer, int offset) throws IOException {
		if (isDeleted(buffer, offset) || isEmpty(buffer, offset)
				|| isLongEntry(buffer, offset)) {
			throw new IOException("error reading directory entry");
		}
		if (filename.isEmpty()) {
			this.filename = Bytes.getAsciiString(buffer, offset, 8).trim();
		} else {
			this.filename = filename;
		}
		extension = Bytes.getAsciiString(buffer, offset + C.FILE_EXTENSION_OFFSET, 3).trim();
		attributes = buffer[offset + C.FILE_ATTRIBUTE_OFFSET]; 
		creationTime = Bytes.getShort(buffer, offset + 14);
		creationDate = Bytes.getShort(buffer, offset + 16);
		lastAccessDate = Bytes.getShort(buffer, offset + 18);
		lastWriteTime = Bytes.getShort(buffer, offset + 22);
		lastWriteDate = Bytes.getShort(buffer, offset + 24);
		firstLogicalCluster = Bytes.getShort(buffer, offset + 26);
		fileSize = Bytes.getInt(buffer, offset + 28);
	}
	
	/** 
	 * Factory method for creating a <code>DirectoryEntry</code>.
	 * 
	 * @param filename  the long filename - <code>null</code> references are not accepted -
	 * 					use an empty <code>String</code> instead.
	 * @param buffer  an array containing the short entry of the directory entry
	 * @param offset  points to the first <code>byte</code> of the short entry
	 * @return  a new instance of <code>DirectoryEntry</code>
	 * @throws IOException if an error is encountered while parsing the entry
	 */
	public static DirectoryEntry createEntry(String filename, byte[] buffer, int offset) throws IOException {
		return new DirectoryEntry(filename, buffer, offset);
	}

	/**
	 * Returns <code>true</code> if the directory entry at <code><nobr>buffer[offset]</nobr></code>
	 * is marked as deleted.<br><br>
	 * 
	 * No attempt is made to verify that <code><nobr>buffer[offset]</nobr></code>
	 * actually points to the first byte of a directory entry.
	 * 
	 * @param buffer  an array containing the the directory entry
	 * @param offset  points to the first <code>byte</code> of the entry
	 * @return  <code>true</code> if the entry is marked deleted, <code>false</code> otherwise
	 */
	public static boolean isDeleted(byte[] buffer, int offset) {
		return (buffer[offset] & 0xff) == 0xE5;
	}
	
	/**
	 * Returns <code>true</code> if the directory entry at <code><nobr>buffer[offset]</nobr></code>
	 * is marked as empty.<br>
	 * If it is marked empty, then the remainder of the directory will also be empty.<br><br>
	 * 
	 * No attempt is made to verify that <code><nobr>buffer[offset]</nobr></code>
	 * actually points to the first byte of a directory entry.
	 * 
	 * @param buffer  an array containing the the directory entry
	 * @param offset  points to the first <code>byte</code> of the entry
	 * @return  <code>true</code> if the entry is empty, <code>false</code> otherwise
	 */
	public static boolean isEmpty(byte[] buffer, int offset) {
		return buffer[offset] == 0x00;
	}
	
	/**
	 * Returns <code>true</code> if the directory entry at <code><nobr>buffer[offset]</nobr></code>
	 * is marked as being part of a long filename.<br><br>
	 * 
	 * No attempt is made to verify that <code><nobr>buffer[offset]</nobr></code>
	 * actually points to the first byte of a directory entry.
	 * 
	 * @param buffer  an array containing the the directory entry
	 * @param offset  points to the first <code>byte</code> of the entry
	 * @return  <code>true</code> if the entry is marked as being part of a long
	 * 			filename, <code>false</code> otherwise
	 */
	public static boolean isLongEntry(byte[] buffer, int offset) {
		return buffer[offset + C.FILE_ATTRIBUTE_OFFSET] == C.LONG_ENTRY;
	}
	
	/**
	 * Returns <code>true</code> if the directory entry at <code><nobr>buffer[offset]</nobr></code>
	 * is marked as being both part of a long filename and as the last entry of
	 * the long filename.<br><br>
	 * 
	 * No attempt is made to verify that <code><nobr>buffer[offset]</nobr></code>
	 * actually points to the first byte of a directory entry.
	 *  
	 * @param buffer  an array containing the the directory entry
	 * @param offset  points to the first <code>byte</code> of the entry
	 * @return  <code>true</code> if the entry is marked as the last entry of a long filename,
	 * 			<code>false</code> otherwise
	 */
	public static boolean isLastLongEntry(byte[] buffer, int offset) {
		return (buffer[offset] & C.LAST_LONG_ENTRY_MASK) == C.LAST_LONG_ENTRY_MASK
				&& isLongEntry(buffer, offset);
	}
	
	/**
	 * Returns the number of the number of directory entry slots allocated to an
	 * entry.<br><br>
	 * 
	 * No attempt is made to verify that <code><nobr>buffer[offset]</nobr></code>
	 * actually points to the first byte of a directory entry.
	 * 
	 * @param buffer  an array containing the the directory entry
	 * @param offset  points to the first <code>byte</code> of the entry
	 * @return  the number of directory entry slots allocated to the entry
	 */
	public static int entrySize(byte[] buffer, int offset) { 
		return (buffer[offset] & C.LONG_ENTRY_INDEX_MASK) + 1;
	}
	
	@Override
	public String toString() {
		return String.format("%s  %s  %5s  %6d  %s",
				PrettyPrint.fatDate(creationDate),
				PrettyPrint.fatTime(creationTime),
				isDirectory() ? "<DIR>" : "",
				fileSize,
				filename);
	}

	/**
	 * @return a <code>String</code> containing all of the metadata for this
	 * 			<code>DirectoryEntry</code>. Each item is listed on a line of
	 * 			its own.
	 */
	public String toFullString() {
		String output = String.format(STRING_FORMAT, "Name", filename);
		output += String.format(STRING_FORMAT, "Extension", extension);
		output += String.format(STRING_FORMAT, "Attributes", PrettyPrint.attributes(attributes));
		output += String.format(STRING_FORMAT, "Creation Time", PrettyPrint.fatTime(creationTime));
		output += String.format(STRING_FORMAT, "Creation Date",  PrettyPrint.fatDate(creationDate));
		output += String.format(STRING_FORMAT, "Last Access Date", PrettyPrint.fatDate(lastAccessDate));
		output += String.format(STRING_FORMAT, "Last Write Time",  PrettyPrint.fatTime(lastWriteTime));
		output += String.format(STRING_FORMAT, "Last Write Date", PrettyPrint.fatDate(lastWriteDate));
		output += String.format(NUMBER_FORMAT, "First Logical Cluster", firstLogicalCluster);
		output += String.format(NUMBER_FORMAT, "File Size", fileSize);
		return output;
	}
	
	/** Returns <code>true</code> if and only if this entry is a directory.
	 * @return  <code>true</code> if this entry is a directory, otherwise <code>false</code>
	 */
	public boolean isDirectory() {
		return (attributes & C.DIRECTORY_MASK) != 0;
	}

	/** Returns the filename of this entry, including the extension (if any).
	 * @return the filename
	 */
	public String getFilename()
	{
		return filename;
	}

	/** Returns the three-character extension of this entry. May return an empty
	 * <code>String</code> if this entry does not have an extension.
	 * @return the extension
	 */
	public String getExtension()
	{
		return extension;
	}

	/** 
	 * Returns the bit-packed attribute of this entry.<br>
	 * The bits are packed as follows:<pre>
	 * READ_ONLY_MASK  0x01
	 * HIDDEN_MASK     0x02
	 * SYSTEM_MASK     0x04
	 * VOLUME_MASK     0x08
	 * DIRECTORY_MASK  0x10
	 * ARCHIVE_MASK    0x20</pre>
	 * 
	 * @return the bit-packed attributes of this entry
	 */
	public byte getAttributes()
	{
		return attributes;
	}

	/**
	 * Returns the bit-packed creation time of this entry.<br>
	 * The bits are packed as follows:<pre>
	 * [0-4]   seconds (in 2-second intervals)
	 * [5-10]  minutes (0-60)
	 * [11-15] hour of the day (0-24)</pre>
	 * 
	 * @return the creation time
	 */
	public short getCreationTime()
	{
		return creationTime;
	}

	/**
	 * Returns the bit-packed creation date of this entry.<br>
	 * The bits are packed as follows:<pre>
	 * [0-4]  day of month (1-31)
	 * [5-8]  month of year (1-12)
	 * [9-15] years since 1980 (0-127)</pre>
	 * 
	 * @return the creation date
	 */
	public short getCreationDate()
	{
		return creationDate;
	}

	/**
	 * Returns the bit-packed date of the last access for this entry.<br>
	 * The bits are packed as follows:<pre>
	 * [0-4]  day of month (1-31)
	 * [5-8]  month of year (1-12)
	 * [9-15] years since 1980 (0-127)</pre>
	 * 
	 * @return the last access date
	 */
	public short getLastAccessDate()
	{
		return lastAccessDate;
	}

	/**
	 * Returns the bit-packed time of the last write for this entry.<br>
	 * The bits are packed as follows:<pre>
	 * [0-4]   seconds (in 2-second intervals)
	 * [5-10]  minutes (0-60)
	 * [11-15] hour of the day (0-24)</pre>
	 * 
	 * @return the last write time
	 */
	public short getLastWriteTime()
	{
		return lastWriteTime;
	}

	/**
	 * Returns the bit-packed date of the last write for this entry.<br>
	 * The bits are packed as follows:<pre>
	 * [0-4]  day of month (1-31)
	 * [5-8]  month of year (1-12)
	 * [9-15] years since 1980 (0-127)</pre>
	 * 
	 * @return the last write date
	 */
	public short getLastWriteDate()
	{
		return lastWriteDate;
	}

	/** Returns the first logical cluster of data for this entry.
	 * @return the first logical cluster
	 */
	public short getFirstLogicalCluster()
	{
		return firstLogicalCluster;
	}

	/** Return the size (in bytes) of this entry.
	 * @return the fileSize
	 */
	public int getFileSize()
	{
		return fileSize;
	}


}
