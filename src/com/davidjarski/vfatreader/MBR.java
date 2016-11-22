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
 * An <code>MBR</code> is a data structure representing the master boot record.
 * 
 * 
 * @author David Jarski
 *
 * (c) 2014-07-31
 * 
 */

public class MBR
{
	// define the values stored in the master boot record
	private int bytesPerSector;  // we ignore this, since it isn't guaranteed to be accurate
	                             // e.g. disk is formatted on one OS but written to by another
	private int sectorsPerCluster;
	private int reservedSectors;
	private int fatCount;
	private int maxRootDirctories;
	private int sectorCount;
	private int sectorsPerFat;
	private int sectorsPerTrack;
	private int headCount;
	private int bootSignature;
	private long volumeId;
	private String volumeLabel = "";
	private String fileSystemType = "";
	
	// define some derived values
	private int rootDirectorySectorCount;
	private int rootDirectoryStart;
	private int dataStart;
	
	/** Private constructor
	 * @param disk the <code>Disk</code> from which to read the master boot record
	 */
	private MBR(Disk disk) {
		byte[] buffer = new byte[C.BYTES_PER_SECTOR];
		// read the first sector, using the default
		disk.read(buffer, C.MBR_START, C.BYTES_PER_SECTOR, 0);
		
		// get the values stored in the master boot record
		bytesPerSector = Bytes.getShort(buffer, 11);
		sectorsPerCluster = buffer[13];
		reservedSectors = Bytes.getShort(buffer, 14);
		fatCount = buffer[16];
		maxRootDirctories = Bytes.getShort(buffer, 17);
		sectorCount = Bytes.getShort(buffer, 19);
		sectorsPerFat = Bytes.getShort(buffer, 22);
		sectorsPerTrack = Bytes.getShort(buffer, 24);
		headCount = Bytes.getShort(buffer, 26);
		bootSignature = buffer[38];
		if (bootSignature == C.HAS_SIGNATURE) {
			volumeId = Bytes.getInt(buffer,39);
			volumeLabel = Bytes.getAsciiString(buffer, 43, 11);
			fileSystemType = Bytes.getAsciiString(buffer, 54, 8);
		}
		
		// calculate the derived values
		rootDirectoryStart = fatCount * sectorsPerFat + reservedSectors;
		rootDirectorySectorCount = maxRootDirctories * C.BYTES_PER_ENTRY / bytesPerSector;
		dataStart = rootDirectoryStart + rootDirectorySectorCount;
	}
	
	/**
	 * Factory method for constructing an <code>MBR</code> for a <code>Disk</code>.
	 * During creation, the master boot record is read and parsed and its values
	 * are stored in the returned <code>MBR</code> instance.
	 * 
	 * @param disk the <code>Disk</code> from which to read the master boot record
	 * @return an new <code>MBR</code> instance
	 */
	public static MBR constructMasterBootRecord(Disk disk) {
		return new MBR(disk);
	}
	
	@Override
	public String toString() {
		String numberFormat = "%-30s%d\n";
		String stringFormat = "%-30s%s\n";
		String output = "";
		output += String.format(numberFormat, "Bytes per sector", bytesPerSector);
		output += String.format(numberFormat, "Sectors per cluster", sectorsPerCluster);
		output += String.format(numberFormat, "Number of reserved sectors", reservedSectors);
		output += String.format(numberFormat, "Number of FATs", fatCount);
		output += String.format(numberFormat, "Max root directory entries", maxRootDirctories);
		output += String.format(numberFormat, "Total sector count", sectorCount);
		output += String.format(numberFormat, "Sectors per FAT", sectorsPerFat);
		output += String.format(numberFormat, "Sectors per track", sectorsPerTrack);
		output += String.format(numberFormat, "Number of heads", headCount);
		output += String.format(numberFormat, "Boot signature", bootSignature);
		output += String.format(numberFormat, "Volume id", volumeId);
		output += String.format(stringFormat, "Volume label", volumeLabel);
		output += String.format(stringFormat, "File system type", fileSystemType);
		output += String.format(numberFormat, "Root directory sector count", rootDirectorySectorCount);
		output += String.format(numberFormat, "Root directory start sector", rootDirectoryStart);
		output += String.format(numberFormat, "Data start sector", dataStart);

		return output;
	}
	
	/** 
	 * @return the first sector of the FAT
	 */
	public int getFatStart() {
		return reservedSectors;  // the FAT comes right after the reserved sectors
	}

	/**
	 * @return the number of bytes per sector
	 */
	public int getBytesPerSector()
	{
		return bytesPerSector;
	}

	/**
	 * @return the number of sectors per cluster
	 */
	public int getSectorsPerCluster()
	{
		return sectorsPerCluster;
	}

	/**
	 * @return the number of reserved sectors
	 */
	public int getReservedSectors()
	{
		return reservedSectors;
	}

	/**
	 * Get the number of File Allocation Tables. This count includes the
	 * duplicate table(s).
	 * 
	 * @return the number of FATs
	 */
	public int getFatCount()
	{
		return fatCount;
	}

	/**
	 * @return the maximum number of entries in the root directory
	 */
	public int getMaxRootDirctories()
	{
		return maxRootDirctories;
	}

	/**
	 * @return the total number of sectors on the <code>Disk</code>
	 */
	public int getSectorCount()
	{
		return sectorCount;
	}

	/**
	 * @return the number of sectors allocated to each File Allocation Table
	 */
	public int getSectorsPerFat()
	{
		return sectorsPerFat;
	}

	/**
	 * @return the number of sectors per track
	 */
	public int getSectorsPerTrack()
	{
		return sectorsPerTrack;
	}

	/**
	 * @return the number of heads on this <code>Disk</code>
	 */
	public int getHeadCount()
	{
		return headCount;
	}

	/**
	 * @return the boot signature
	 */
	public int getBootSignature()
	{
		return bootSignature;
	}

	/**
	 * @return the volume ID - may be 0 if no ID is set for the device
	 */
	public long getVolumeId()
	{
		return volumeId;
	}

	/**
	 * @return the volume label - never <code>null</code>, but may be an empty <code>String</code>
	 */
	public String getVolumeLabel()
	{
		return volumeLabel;
	}

	/**
	 * Get a hint indicating the declared file system of the <code>Disk</code>.<br>
	 * This is <b>NOT</b> guaranteed to be accurate, and should not be used as
	 * the sole means of verifying file system type.
	 * @return the file system type - 
	 * 			never <code>null</code>, but may be an empty <code>String</code>
	 */
	public String getFileSystemType()
	{
		return fileSystemType;
	}

	/**
	 * @return the number of sectors allocated to the root directory
	 */
	public int getRootDirectorySectorCount()
	{
		return rootDirectorySectorCount;
	}

	/**
	 * @return the first sector of the root directory
	 */
	public int getRootDirectoryStart()
	{
		return rootDirectoryStart;
	}

	/**
	 * @return the the first sector of the data section of the <code>Disk</code>
	 */
	public int getDataStart()
	{
		return dataStart;
	}

}
