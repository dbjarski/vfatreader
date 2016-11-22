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
import java.util.Scanner;

/**
 * <code>Main</code> is the entry point for the <em>Programming Assignment 3</em> project.
 * 
 * @author David Jarski
 *
 * (c) 2014-07-31
 * 
 */

public class Main
{
	private static final String PAUSE = "-pause";
	private static final String USAGE = "Usage: vfatreader [" + PAUSE + "] <filename>";
	private static boolean pauseOutput = false;	// wait for carriage return after each method call
	private static Scanner scanner = null;
	
	private static void pause() {
		if (pauseOutput) {
			System.out.print("Press the enter key to continue ");
			scanner.nextLine();
			System.out.println();
		}
	}
	
	private static void exitWithUsageMessage() {
		System.out.println(USAGE);
		System.exit(0);
	}
	
	public static void main(String[] args) {
		
		if (args.length == 0 ||	args.length > 2) {
			exitWithUsageMessage();
		}
		
		int filenameIndex = 0;
		if (args.length == 2) {
			filenameIndex = 1;
			if (!args[0].equalsIgnoreCase(PAUSE)) {
				exitWithUsageMessage();
			}
			pauseOutput = true;
		}
		
		Fat12Reader reader;
		try {
			reader = new Fat12Reader(args[filenameIndex]);
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
			return;
		}
		
		if (pauseOutput) {
			scanner = new Scanner(System.in);
		}
		
		reader.printMasterBootRecord(); pause();
		reader.printRawRootDirectory(); pause();
		reader.printVerboseRootDirectory(); pause();
		reader.printFAT(); pause();
		reader.printFiles();
		
		if (pauseOutput) {
			scanner.close();
		}
	}
}
