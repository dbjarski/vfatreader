# vFatReader

A command-line utility that reads a FAT12 disk image and ouputs the contents to the console.

### Usage
java -jar vfatreader.jar [-pause] \<filename\>

### Project Background
This was an optional assignment from my operating systems class. The assignment was simply to use Java to read the contents of the simple FAT12 disk image, but I opted for a more general and thorough solution. And just for fun I included the hex output of the drive contents.

I also read the [Microsoft Extensible Firmware Initiative FAT32 File System Specification](http://download.microsoft.com/download/1/6/1/161ba512-40e2-4cc9-843a-923143f3456c/fatgen103.doc) in order to learn how to properly handle long file names (which I tested with my own FAT12 disk image, created in Linux).
