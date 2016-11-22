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

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A <code>Fat12Reader</code> holds a reference to a <code>Disk</code> and provides methods for outputting
 * the contents of the disk as required by <em>Programming Assignment 3</em>.
 * 
 * @author David Jarski
 *
 * (c) 2014-07-31
 * 
 */

public class Fat12Reader
{
	
	private Disk disk;
	
	private static final String DASHES = "-------------------------------------------------------------------------------";
	
	/**
	 * @throws FileNotFoundException 
	 */
	public Fat12Reader(String filename) throws FileNotFoundException {
		disk = Disk.createDiskFromImage(filename);
	}
	
	/**
	 * Prints the master boot record
	 */
	public void printMasterBootRecord() {
		System.out.println("Master Boot Record:\n" + DASHES);
		System.out.println(disk.readToString(new byte[disk.mbr.getBytesPerSector()], C.MBR_START));
		System.out.println(disk.mbr);
	}
	
	/**
	 * Prints the hex dump of the sectors of the root directory
	 */
	public void printRawRootDirectory() {
		System.out.println("\nRoot Directory Hex Dump:\n" + DASHES);
		System.out.println(Directory.rootDirectoryToHexString(disk));
	}
	
	/**
	 * Iterates through the <code>Directory</code> object that represents the root directory,
	 * printing a single line for each <code>DirectoryEntry</code>.
	 */
	public void printCompactRootDirectory() {
        try {
	        Directory dir = Directory.readRootDirectory(disk);
			System.out.println("\nRoot Directory:\n" + DASHES);
			for (DirectoryEntry entry : dir) {
				System.out.println(entry.toString());
			}
        }
        catch (IOException e) {
        	System.err.println(e.getMessage());
	        e.printStackTrace();
        }
	}
	
	/**
	 * Iterates through the <code>Directory</code> object that represents the root directory,
	 * printing all fields stored in each <code>DirectoryEntry</code>.
	 */
	public void printVerboseRootDirectory() {
        try {
        	Directory dir = Directory.readRootDirectory(disk);
    		System.out.println("\nRoot Directory:\n" + DASHES);
    		for (DirectoryEntry entry : dir) {
    			System.out.println(entry.toFullString());
    		}
        }
        catch (IOException e) {
        	System.err.println(e.getMessage());
	        e.printStackTrace();
        }

	}
	
	/**
	 * Prints the file allocation table, including the duplicate table.
	 */
	public void printFAT() {
		System.out.println("\nFAT:\n" + DASHES);
		System.out.println(disk.fat);
	}
	
	/**
	 * Prints each file in the root directory. The hex dump of a file's
	 * sectors are printed first, followed by the file's contents as text.
	 */
	public void printFiles() {
		System.out.println("\nPrinting all files in root directory");
        try {
	        Directory dir = Directory.readRootDirectory(disk);
			for (DirectoryEntry entry : dir) {
				if (entry.getFileSize() > 0) {
					byte[] buffer = new byte[(int)entry.getFileSize()];
					System.out.println("\nHex output of " + entry.getFilename() +
							"\n" + DASHES);
					System.out.println(disk.readDataToString(buffer, entry));
					System.out.println("\nText output of " + entry.getFilename() +
							"\n" + DASHES);
					printText(buffer);
				}
			}
        }
        catch (IOException e) {
        	System.err.println(e.getMessage());
	        e.printStackTrace();
        }
	}
	
	/**
	 * Prints a hex dump of a sector on the disk.
	 * @param sector  the sector to read
	 */
	public void printSector(int sector) {
		if (sector > disk.mbr.getSectorCount()) {
			System.out.println("There aren't that many sectors on the disk!");
		} else {
			byte[] buffer = new byte[disk.mbr.getBytesPerSector()];
			System.out.println(disk.readToString(buffer, sector));
		}
	}
	
	/**
	 * Creates a hex dump for the entire disk and saves it to the current
	 * directory.
	 */
	public void saveAllSectorsToDisk() {
		try (FileWriter fw = new FileWriter("hex_dump.txt")) {
			fw.write(disk.readToString(
					new byte[C.BYTES_PER_SECTOR * disk.mbr.getSectorCount()], 0));
		}
        catch (FileNotFoundException e) {
	        e.printStackTrace();
        }
        catch (IOException e) {
	        e.printStackTrace();
        }
	}
	
	/** Convenience method for printing a byte array as characters.
	 * @param array  the array to print
	 */
	private void printText(byte[] array) {
		int length = array.length;
		for (int i = 0; i < length; ++i) {
			System.out.print((char)array[i]);
		}
	}

	
}
 