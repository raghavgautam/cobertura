/* Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2006 John Lewis
 * Copyright (C) 2006 Mark Doliner
 * Copyright (C) 2009 Chris van Es
 *
 * Note: This file is dual licensed under the GPL and the Apache
 * Source License 1.1 (so that it can be used from both the main
 * Cobertura classes and the ant tasks).
 *
 * Cobertura is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * Cobertura is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cobertura; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package net.sourceforge.cobertura.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

/**
 * This class controls access to any file so that multiple JVMs will
 * not be able to write to the file at the same time.
 * <p/>
 * A file called "filename.lock" is created and Java's FileLock class
 * is used to lock the file.
 * <p/>
 * The java.nio classes were introduced in Java 1.4, so this class
 * does a no-op when used with Java 1.3.  The class maintains
 * compatability with Java 1.3 by accessing the java.nio classes
 * using reflection.
 *
 * @author John Lewis
 * @author Mark Doliner
 */
public class FileLocker {

	private FileLock lock = null;

	/**
	 * A file called "filename.lock" that resides in the same directory
	 * as "filename"
	 */
	private File lockFile;
	private RandomAccessFile randomAccessFile;

	public FileLocker(File file) {
		String lockFileName = file.getAbsolutePath() + ".lock";
		lockFile = new File(lockFileName);
	}

	/**
	 * Obtains a lock on the file.  This blocks until the lock is obtained.
	 */
	public boolean lock() {
		String useNioProperty = System.getProperty("cobertura.use.java.nio");
		if (useNioProperty != null && useNioProperty.equalsIgnoreCase("false")) {
			return true;
		}

		try {
			randomAccessFile = new RandomAccessFile(lockFile, "rw");
			lock = randomAccessFile.getChannel().lock();
		} catch (IOException e) {
			System.err.println("Unable to get lock channel for " + lockFile.getAbsolutePath() + ": " + e.getLocalizedMessage());
			e.printStackTrace();
			return false;
		} catch (Throwable t) {
			System.err.println("Unable to acquire lock: " + t.getLocalizedMessage());
			t.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Releases the lock on the file.
	 */
	public void release() {
		try {
			if (lock.isValid()) {
				lock.release();
				lock = null;
			}
		} catch (Throwable t) {
			System.err.println("Unable to release locked file: " + t.getLocalizedMessage());
		}
		try {
			randomAccessFile.close();
		} catch (Exception e) {
			System.err.println("Unable to close stream: " + e.getLocalizedMessage());
			e.printStackTrace();
		}
		if (!lockFile.delete()) {
			System.err.println("Unable to delete lockFile " + lockFile);
		}
	}
}
