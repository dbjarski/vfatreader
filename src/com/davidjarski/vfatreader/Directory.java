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
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A <code>Directory</code> is a iterable list of <code>DirectoryEntries</code>.<br><br>
 * 
 * A factory method is provided to get the root directory of a <code>Disk</code>.<br>
 * The root directory is currently the only directory supported by this class.<br>
 * 
 * @author David Jarski
 *
 * (c) 2014-07-31
 * 
 */

public class Directory implements Iterable<DirectoryEntry>
{
	/** <b>&nbsp&nbsp&nbsp
	 * READ ONLY! DO NOT MODIFY!</b> */
	private static final int[] LONG_ENTRY_CHAR_OFFSETS =  // DO NOT MODIFY!
		{1,3,5,7,9,14,16,18,20,22,24,28,30};
	private ArrayList<DirectoryEntry> entries;
	
	@Override
	public Iterator<DirectoryEntry> iterator() {
		return entries.iterator();
	}
	
	private Directory(ArrayList<DirectoryEntry> entries) {
		this.entries = entries;
	}

	/**
	 * Factory method for generating a <code>Directory</code> that represents the root
	 * directory of a <code>Disk</code>.
	 * 
	 * @param disk  the <code>Disk</code> from which to read the root directory
	 * @return  a <code>Directory</code> instance representing the root directory
	 * @throws IOException  if an error is encountered while parsing the root
	 * 						directory
	 */
	/* Two buffers are used to read the root directory one sector at a time.
	 * There isn't any particular reason for this implementation, other than
	 * that I was curious how I would solve a problem like this. Due to the
	 * long filename entries, a single directory entry may not be contained
	 * in a single sector. Also, since the first long entry that we read is 
	 * actually the end of the filename string, we may need to cross the sector
	 * border a couple of times to read an entry. 
	 */
	public static Directory readRootDirectory(Disk disk) throws IOException {
		int bytesPerSector = disk.mbr.getBytesPerSector();
		ArrayList<DirectoryEntry> entries = new ArrayList<>();
		int currentSector = disk.mbr.getRootDirectoryStart();
		byte[] buffer1 = new byte[bytesPerSector];
		byte[] buffer2 = new byte[bytesPerSector];
		byte[] buffer;  // will reference one of the other buffers
		disk.readSector(buffer1, currentSector);
		disk.readSector(buffer2, currentSector + 1);
		
		int maxSector = disk.mbr.getRootDirectorySectorCount() + currentSector - 1;
		boolean finishedReadingDirectory = false;
		boolean split = false;  // does the entry span two sectors?
		int index = 0;  // references the first byte in the directory entry
		
		while (currentSector <= maxSector && !finishedReadingDirectory) {
			if (currentSector > disk.mbr.getRootDirectoryStart()) {  
				// do some swapping due to the two-buffer implementation
				buffer = buffer1;  // save the reference for a swap
				buffer1 = buffer2;
				if (currentSector != maxSector) {
					buffer2 = buffer;  // do the swap
					disk.readSector(buffer2, currentSector + 1);
				}
			}
			buffer = buffer1;
			
			/* * /
			System.out.println("Sector " + currentSector + "\n-------------------------");
			System.out.println(Sector.toString(buffer1));
			System.out.println("Sector " + (currentSector + 1) + "\n-------------------------");
			System.out.println(Sector.toString(buffer2));
			/* */
			
			if (split) {
				index %= bytesPerSector;
				split = false;
			} else {
				index = 0;
			}
			// loop through the entries in the current sector
			while (index < bytesPerSector && !finishedReadingDirectory) {
				/* the offset represents the offset from index, which in turn
				   points to the first byte in a directory entry */
				int offset = index + C.FILE_ATTRIBUTE_OFFSET;
				if (DirectoryEntry.isEmpty(buffer, index)) {
						finishedReadingDirectory = true;
				} else if (DirectoryEntry.isDeleted(buffer, index)) {
					/* there isn't any way to know how many deleted entries we
					 * might find in a row, so we simply need to keep looping
					 * through the directory one entry at a time. */
					index += C.BYTES_PER_ENTRY;
				} else if (!DirectoryEntry.isLastLongEntry(buffer, index)) {
					// we should always encounter the last entry first
					throw new IOException("Sector " + currentSector + ","
							+ " Offset " + Integer.toString(buffer[index])
							+ "was expecting final LFN entry");
				} else {
					/* get the number of directory entries that are being used
					   to store the long filename */
					int longEntryCount = DirectoryEntry.entrySize(buffer, index) - 1;
					char[] name = new char[longEntryCount * C.UNICODE_CHARS_PER_ENTRY];
					int nameIndex = 0;
					boolean usingNextSector = false;  // does a split entry need to use 2nd sector?
					// loop through the long file name entries
					for (int i = longEntryCount - 1; i >= 0; --i) {
						offset = index + (C.BYTES_PER_ENTRY * i);
						
						/* do a little busy-work setting the correct buffer
						 * reference and offset. this is only necessary due to
						 * the decision to use two buffers instead of one.
						 */
						if (offset >= bytesPerSector) {
							split = true;
							usingNextSector = true;
							offset %= bytesPerSector;
							buffer = buffer2;
						} else if (usingNextSector && offset < bytesPerSector) {
							usingNextSector = false;
							buffer = buffer1;
						}
						
						// verify that each entry is what we expect it to be
						if (!DirectoryEntry.isLongEntry(buffer, offset)) {
							throw new IOException("Sector " + currentSector + ","
									+ " Offset " + Integer.toString(i)
									+ " does not contain an LFN");
						}
						
						// get the unicode characters from the entry
						char nextChar;	
						for (int j = 0; j < C.UNICODE_CHARS_PER_ENTRY; ++j) {
							nextChar = Bytes.getChar(
									buffer, offset + LONG_ENTRY_CHAR_OFFSETS[j]);
							if (nextChar == 0) {
								break;
							}
							name[nameIndex++] = nextChar;
						}
						
						// TODO process the checksum
						
					}  // end loop through long entries  <-----------------------------------------
					
					String filename = String.valueOf(name).trim();
					/* now that we finally have the long file name, we can set
					 * the offset to point to the short file name entry (which
					 * also holds all the other file data).	 */
					offset = (longEntryCount * C.BYTES_PER_ENTRY + index);
					
					// do a little more busy-work for the two-buffer implementation
					if (offset >= bytesPerSector) {
						split = true;
						offset %= bytesPerSector;
					}
					if (split) {
						buffer = buffer2;
					}
					
					// we can finally create a new directory entry!
					DirectoryEntry entry = DirectoryEntry.createEntry(
						filename, buffer, offset);
					entries.add(entry);
					index += (longEntryCount + 1) * C.BYTES_PER_ENTRY;
				}  // end else block that processes a valid entry  <-------------------------------
			}  // end while loop through the current sector  <-------------------------------------
			++currentSector;
		}  // end while loop through all the sectors of the root directory  <----------------------
		
		Directory directory = new Directory(entries);
		return directory;
	}
	
	
	/**
	 * Gets a hex dump of the root directory of a disk. Empty sectors are
	 * excluded from the output.
	 * 
	 * @param disk  the <code>Disk</code> from which to read the root directory
	 * @return  a hex dump of the root directory
	 */
	public static String rootDirectoryToHexString(Disk disk) {
		byte[] buffer = new byte[disk.mbr.getBytesPerSector()];
		int currentSector = disk.mbr.getRootDirectoryStart();
		int maxSector = disk.mbr.getRootDirectorySectorCount() + currentSector - 1;
		StringBuilder builder = null;
		boolean hasNext = true;
		while (hasNext && currentSector <= maxSector) {
			String sectorString = 
					disk.readSectorToString(buffer, currentSector++);
			if (builder == null) {
				builder = new StringBuilder(sectorString.length()
						* disk.mbr.getRootDirectorySectorCount());
			}
			builder.append(sectorString);
			if (buffer[disk.mbr.getBytesPerSector() - 32] == 0) {
				hasNext = false;
			}
		}
		return builder.toString();
	}
}
