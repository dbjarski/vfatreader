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
 * A <code>FAT</code> is a data structure representing the file allocation table.
 * 
 * @author David Jarski
 *
 * (c) 2014-07-31
 * 
 */

public class FAT
{
	private byte[] buffer;
	private Disk disk;
	
	/** Private constructor
	 * @param disk the <code>Disk</code> from which to read the file allocation table
	 */
	private FAT(Disk disk) {
		this.disk = disk;
		buffer = new byte[disk.mbr.getBytesPerSector() * disk.mbr.getFatCount()
		                  * disk.mbr.getSectorsPerFat()];
		refreshFAT();
	}
	
	/**
	 * Factory method for constructing a <code>FAT</code> for a <code>Disk</code>.
	 * During creation, the file allocation table is read and its contents
	 * are stored in the returned <code>FAT</code> instance.
	 * 
	 * @param disk the <code>Disk</code> from which to read the file allocation table
	 * @return an new <code>FAT</code> instance
	 */
	public static FAT constructFileAllocationTable(Disk disk) {
		return new FAT(disk);
	}
	
	/**
	 * Reads and caches the file allocation table
	 */
	public void refreshFAT() {
		disk.read(buffer, disk.mbr.getFatStart());
	}
	
	/**
	 * Get the next cluster number in a cluster chain.
	 * 
	 * @param cluster  the current cluster number
	 * @return  the next cluster, or <code><nobr>C.EOC</nobr></code> if the end
	 * 			 of the cluster chain has been reached.
	 */
	public int getNextCluster(int cluster) {
		int index = cluster * 3 / 2;
		short word = Bytes.getShort(buffer, index);
		if (cluster % 2 == 0) {
			return word & 0x0FFF;  // get the lower 12 bits
		} else {
			return (word & 0xFFFF) >> 4;  // get the upper 12 bits
		}
	}
	
	@Override
	public String toString() {
		return disk.getHexString(buffer, disk.mbr.getFatStart(),
				disk.mbr.getSectorsPerFat() * disk.mbr.getBytesPerSector())
				+ "\n" +
				disk.getHexString(buffer, disk.mbr.getFatStart() + disk.mbr.getSectorsPerFat(),
				disk.mbr.getSectorsPerFat() * disk.mbr.getBytesPerSector());
	}
}
