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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A <code>Disk</code> contains a reference to a FAT12 disk image and provides methods for
 * reading the image. To facilitate analysis of the FAT file system, the <code>read()</code>
 * methods have corresponding <code>readToString()</code> versions that return a hex dump of the
 * bytes read.<br><br>
 * 
 * Writing to the disk image is not a supported feature.
 * 
 * @author David Jarski
 *
 * (c) 2014-07-31
 * 
 */

public class Disk
{
	public final MBR mbr;
	public final FAT fat;
	private File file;
	
	// define some static constants used to output hex dumps
	/** This is simply the observed value of the <code>StringBuilder</code> used to create a<br>
	 *  dump. It is used to set the initial capacity of the <code>StringBuilder</code>. This<br>
	 *  value should be updated if the output format changes, although this is not<br>
	 *  strictly necessary.	 */
	private static final int HEX_DUMP_CHARS_PER_LINE = 78;
	private static final int BYTES_PER_LINE = 16;
	private static final int BYTES_PER_GROUP = 4;
	private static final int DIGITS_IN_LINE_NUMBER = 5;
	private static final String LINE_NUMBER_COLUMN_SEPARATOR = " | ";
	private static final String BYTE_SEPARATOR = " ";
	private static final String COLUMN_SEPARATOR = "  ";
	private static final String TRANSLATION_COLUMN_SEPARATOR = " | ";
	private static final String LINE_SEPARATOR = "\n";
	private static final char UNKNOWN_CHARACTER = '.';
	private static final String DASHES = "-------------------------------------------------------------------------------";
	
	private Disk(File file) throws FileNotFoundException {
		this.file = file;
		if (!file.exists()) {
			throw new FileNotFoundException("'" + file.getName() + "' does not exist.");
		}
		mbr = MBR.constructMasterBootRecord(this);
		fat = FAT.constructFileAllocationTable(this);
	}
	
	/**
	 * Factory method for creating a <code>Disk</code> from an image file
	 * 
	 * @param filename  the name of the disk image file
	 * @return  a new <code>Disk</code> instance
	 * @throws FileNotFoundException 
	 */
	public static Disk createDiskFromImage(String filename) throws FileNotFoundException {
		return new Disk(new File(filename));
	}
	
	/**
	 * Factory method for creating a <code>Disk</code> from an image file
	 * 
	 * @param file  a <code>File</code> representing the disk image
	 * @return  a new <code>Disk</code> instance
	 * @throws FileNotFoundException 
	 */
	public static Disk createDiskFromImage(File file) throws FileNotFoundException {
		return new Disk(file);
	}
	

/* ****************************************************************************
 *  get/readData() methods
 * 
 *****************************************************************************/
	
	/**
	 * Gets the data of a <code>DirectoryEntry</code> as a byte array.
	 * 
	 * @param entry  the <code>DirectoryEntry</code> to read from
	 * @return  an array containing all the data bytes of the <code>DirectoryEntry</code>
	 */
	public byte[] getData(DirectoryEntry entry) {
		byte[] buffer = new byte[(int)entry.getFileSize()];
		readData(buffer, entry);
		return buffer;
	}
	
	/**
	 * Reads the data of a <code>DirectoryEntry</code> into a byte array. The byte array
	 * is assumed to be of at least entry.getFileSize() in length.
	 * 
	 * @param buffer  the byte array in which to store the data
	 * @param entry  the <code>DirectoryEntry</code> to read from
	 */
	public void readData(byte[] buffer, DirectoryEntry entry) {
		int bytesPerCluster = mbr.getBytesPerSector() * mbr.getSectorsPerCluster();
		int sector = getDataSector(entry.getFirstLogicalCluster());
		int bytes = (int)entry.getFileSize();
		int bufferIndex = 0;
		int nextCluster = fat.getNextCluster(entry.getFirstLogicalCluster());
		while (nextCluster != C.EOC) {
			read(buffer, sector, bytesPerCluster, bufferIndex);
			sector = getDataSector(nextCluster);
			nextCluster = fat.getNextCluster(nextCluster);
			bufferIndex += bytesPerCluster;
			bytes -= bytesPerCluster;
		}
		read(buffer, sector, bytes, bufferIndex);
	}
	
	/**
	 * Reads the data of a <code>DirectoryEntry</code> into a byte array. The byte array
	 * is assumed to be of at least <code><nobr>entry.getFileSize()</nobr></code> in length. Returns a
	 * hex dump of the data that is read.
	 * 
	 * @param buffer  the byte array in which to store the data
	 * @param entry  the <code>DirectoryEntry</code> to read from
	 * @return  a hex dump of the data
	 */
	public String readDataToString(byte[] buffer, DirectoryEntry entry) {
		// initialize our variables
		int bytes = (int)entry.getFileSize();
		int bytesPerCluster = mbr.getBytesPerSector() * mbr.getSectorsPerCluster();
		int sector = getDataSector(entry.getFirstLogicalCluster());
		int bufferIndex = 0;
		int nextCluster = fat.getNextCluster(entry.getFirstLogicalCluster());
		
		// initialize the StringBuilder
		int lineCount = bytes / BYTES_PER_LINE;
		if (bytes % BYTES_PER_LINE > 1) {
			++lineCount;
		}
		StringBuilder builder = new StringBuilder(
				HEX_DUMP_CHARS_PER_LINE * lineCount);
		
	
		// follow the cluster chain
		while (nextCluster != C.EOC) {
			builder.append(readToString(buffer, sector, bytesPerCluster, bufferIndex));
			builder.append(DASHES);
			builder.append('\n');
			sector = getDataSector(nextCluster);
			nextCluster = fat.getNextCluster(nextCluster);
			bufferIndex += bytesPerCluster;
			bytes -= bytesPerCluster;
		}
		builder.append(readToString(buffer, sector, bytes, bufferIndex));
		return builder.toString();
	}
	
/* ****************************************************************************
 *  read() methods
 * 
 *****************************************************************************/
	
	/**
	 * Reads data into a byte array.<code>  numBytes</code> are read into the
	 * array, beginning at <code><nobr>buffer[bufferOffset]</nobr></code>.<br>
	 * Note that the array is assumed to be at least <code>(bufferOffset +
	 * numBytes)</code> in length.
	 * 
	 * @param buffer  the array in which to store the bytes
	 * @param startSector  the starting location of the read
	 * @param numBytes  the number of bytes to read
	 * @param bufferOffset  the starting position within <code>buffer</code>
	 */
	public void read(byte[] buffer, int startSector, int numBytes, int bufferOffset) {
		int skip;
		/* We handle the special case of reading the master boot record,
		 * which calls this method. Therefore, the first time this method is
		 * called, mbr will be null.  */
		try {
			skip = startSector * mbr.getBytesPerSector();
		} catch(NullPointerException e) {
			skip = startSector * C.BYTES_PER_SECTOR;	
		}
		
		try (DataInputStream inputStream =
        		new DataInputStream(new FileInputStream(file))) {
			inputStream.skipBytes(skip);
			for (int i = 0; i < numBytes; ++i) {
				buffer[bufferOffset++] = inputStream.readByte();
			}
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	/**
	 * Reads data into a byte array.<code>  numBytes</code> are read into the array.<br>
	 * Note that the array is assumed to be at least <code>numBytes</code> in length.
	 * 
	 * @param buffer  the array in which to store the bytes
	 * @param startSector  the starting location of the read
	 * @param numBytes  the number of bytes to read
	 */
	public void read(byte[] buffer, int startSector, int numBytes) {
		read(buffer, startSector, numBytes, 0);
	}
	
	/**
	 * Reads data into a byte array. Bytes are read until the array is filled.
	 * 
	 * @param buffer  the array in which to store the bytes
	 * @param startSector  the starting location of the read
	 */
	public void read(byte[] buffer, int startSector) {
		read(buffer, startSector, buffer.length, 0);
	}
	
	/**
	 * Reads a sector into a byte array. 
	 * 
	 * @param buffer  the array in which to store the sector bytes
	 * @param sector  the sector to read
	 */
	public void readSector(byte[] buffer, int sector) {
		read(buffer, sector, mbr.getBytesPerSector(), 0);
	}

	
/* ****************************************************************************
 *  readToString() methods
 * 
 *****************************************************************************/
	/**
	 * Reads data into a byte array.<code>  numBytes</code> are read into the
	 * array, beginning at <code<nobr>buffer[bufferOffset]</nobr></code>.<br>
	 * Note that the array is assumed to be at least <code>(bufferOffset +
	 * numBytes)</code> in length.<br>
	 * Returns a hex dump of the data that is read.
	 * 
	 * @param buffer  the array in which to store the bytes
	 * @param startSector  the starting location of the read
	 * @param numBytes  the number of bytes to read
	 * @param bufferOffset  the starting position within <code>buffer</code>
	 * @return  a hex dump of the data
	 */
	public String readToString(byte[] buffer, int startSector, int numBytes, int bufferOffset) {
		read(buffer, startSector, numBytes, bufferOffset);	
		return getHexString(buffer, startSector, numBytes, bufferOffset);
	}
	
	/**
	 * Reads data into a byte array.<code>  numBytes</code> are read into the array.<br>
	 * Note that the array is assumed to be at least <code>numBytes</code> in length.<br>
	 * Returns a hex dump of the data read.
	 * 
	 * @param buffer  the array in which to store the bytes
	 * @param startSector  the starting location of the read
	 * @param numBytes  the number of bytes to read
	 * @return  a hex dump of the data
	 */
	public String readToString(byte[] buffer, int startSector, int numBytes) {
		read(buffer, startSector, numBytes, 0);
		return getHexString(buffer, startSector, numBytes, 0);
	}
	
	/**
	 * Reads data into a byte array. Bytes are read until the array is filled.
	 * Returns a hex dump of the data that is read.
	 * 
	 * @param buffer  the array in which to store the bytes
	 * @param startSector  the starting location of the read
	 * @return  a hex dump of the data
	 */
	public String readToString(byte[] buffer, int startSector) {
		read(buffer, startSector, buffer.length, 0);
		return getHexString(buffer, startSector, buffer.length, 0);
	}
	
	/**
	 * Reads a sector into a byte array. Returns a hex dump of the data that is read.
	 * 
	 * @param buffer  the array in which to store the sector bytes
	 * @param startSector  the sector to read
	 * @return  a hex dump of the data
	 */
	public String readSectorToString(byte[] buffer, int startSector) {
		read(buffer, startSector, mbr.getBytesPerSector(), 0);
		return getHexString(buffer, startSector, mbr.getBytesPerSector(), 0);
	}
	
/* ****************************************************************************
 *  static String getHexString() methods
 * 
 *****************************************************************************/
	
	/**
	 * Generates a hex dump of a <code>byte</code> array.<br>
	 * The line number labels of the hex dump will start at 0x00.
	 * 
	 * @param buffer
	 * 		the <code>byte</code> array from which to create the hex dump
	 * @return  a hex dump of the data
	 */
	String getHexString(byte[] buffer) {
		return getHexString(buffer, 0, buffer.length, 0);
	}
	
	/**
	 * Generates a hex dump of a <code>byte</code> array.<br>
	 * The line number labels of the hex dump are calculated based on
	 * <code>startSector</code>.
	 * 
	 * @param buffer
	 * 		the <code>byte</code> array from which to create the hex dump
	 * @param startSector
	 * 		the disk sector that <code>buffer</code> is meant to represent
	 * @return  a hex dump of the data
	 */
	String getHexString(byte[] buffer, int startSector) {
		return getHexString(buffer, startSector, buffer.length, 0);
	}
	
	/**
	 * Generates a hex dump of a <code>byte</code> array. Reads the array from
	 * index 0 to index <code>numBytes</code>.<br>
	 * The line number labels of the hex dump are calculated based on <code>startSector</code>.
	 * 
	 * @param buffer
	 * 		the <code>byte</code> array from which to create the hex dump
	 * @param startSector
	 * 		the disk sector that <code>buffer</code> is meant to represent
	 * @param numBytes
	 * 		the number of bytes to read from the array
	 * @return  a hex dump of the data
	 */
	String getHexString(byte[] buffer, int startSector, int numBytes) {
		return getHexString(buffer, startSector, numBytes, 0);
	}
	
	/**
	 * Generates a hex dump of a <code>byte</code> array. Reads
	 * the array from index <code>bufferOffset</code> to index <code>numBytes</code>.<br>
	 * The line number labels of the hex dump are calculated based on <code>startSector</code>.
	 * 
	 * @param buffer
	 * 		the <code>byte</code> array from which to create the hex dump
	 * @param startSector
	 * 		the disk sector that <code>buffer</code> is meant to represent
	 * @param numBytes
	 * 		the number of bytes to read from the array
	 * @param bufferOffset  the starting position within <code>buffer</code>
	 * @return  a hex dump of the data
	 */
	String getHexString(byte[] buffer, int startSector, int numBytes, int bufferOffset) {
		int linesPerSector = mbr.getBytesPerSector() / BYTES_PER_LINE;
		int lineNumber = startSector * linesPerSector;
		int lineCount = numBytes / BYTES_PER_LINE;
		if (numBytes % BYTES_PER_LINE > 1) {
			++lineCount;
		}
		StringBuilder builder = new StringBuilder(
				HEX_DUMP_CHARS_PER_LINE * lineCount);
		for (int i = 0; i < lineCount; ++i) {
			PrettyPrint.lineNumber(lineNumber++, DIGITS_IN_LINE_NUMBER, builder);
			builder.append(LINE_NUMBER_COLUMN_SEPARATOR);
			appendLine(builder, buffer, bufferOffset, i);
			builder.append(LINE_SEPARATOR);
		}
		return builder.toString();
	}	
	
/* ****************************************************************************
 *  private helper methods
 * 
 *****************************************************************************/
	
	/**
	 * Computes the first sector of a cluster
	 * 
	 * @param cluster  the cluster number
	 * @return  the first sector of the cluster
	 */
	private int getDataSector(int cluster) {
		/* (cluster - 2) is required because the first two indices of the FAT
		 * are reserved  */
		return (cluster - 2) * mbr.getSectorsPerCluster() + mbr.getDataStart();
	}
	
	/**
	 * Appends a hex dump line to a <code>StringBuilder</code>.
	 * 
	 * @param builder the <code>StringBuilder</code>
	 * @param buffer
	 * 		the <code>byte</code> array from which to create the hex dump
	 * @param bufferOffset  the starting position within <code>buffer</code>
	 * @param lineNumber  the line number - used to label the line
	 */
	private static void appendLine(StringBuilder builder, byte[] buffer, int bufferOffset,
			int lineNumber) {
		int length = buffer.length;
		int offset = lineNumber * BYTES_PER_LINE + bufferOffset;
		for (int i = 0; i < BYTES_PER_LINE; ++i) {
			if (i > 0) {
				if (i % BYTES_PER_GROUP == 0) {
					builder.append(COLUMN_SEPARATOR);
				} else {
					builder.append(BYTE_SEPARATOR);
				}
			}
			if (offset < length) {
				PrettyPrint.hex(buffer[offset++], builder);
			} else {
				builder.append("XX");
			}
		}
	
		builder.append(TRANSLATION_COLUMN_SEPARATOR);
		
		offset = lineNumber * BYTES_PER_LINE + bufferOffset;
		char nextChar;
		for (int i = 0; i < BYTES_PER_LINE; ++i) {
			if ((offset < length) && 
					(((nextChar = (char)(buffer[offset] & 0xff)) >= 0x20 && nextChar < 0x7F)
							|| nextChar == 0xE5)) {
				builder.append(nextChar);
			} else {
				builder.append(UNKNOWN_CHARACTER);
			}
			++offset;
		}
	}
}
