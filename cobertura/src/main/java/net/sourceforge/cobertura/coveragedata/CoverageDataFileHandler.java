/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2007 Joakim Erdfelt
 * Copyright (C) 2007 Ignat Zapolsky
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

package net.sourceforge.cobertura.coveragedata;

import net.sourceforge.cobertura.CoverageIgnore;
import net.sourceforge.cobertura.util.ConfigurationUtil;
import net.sourceforge.cobertura.util.FileLocker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;

/**
 * This contains methods used for reading and writing the
 * "cobertura.ser" file.
 */
@CoverageIgnore
public abstract class CoverageDataFileHandler {
	private static final Logger logger = LoggerFactory
			.getLogger(CoverageDataFileHandler.class);
	private static File defaultFile = null;

	public static File getDefaultDataFile() {
		// return cached defaultFile
		if (defaultFile != null) {
			return defaultFile;
		}

		// load and cache datafile configuration
		ConfigurationUtil config = new ConfigurationUtil();
		defaultFile = new File(config.getDatafile());

		return defaultFile;
	}

	public static ProjectData loadCoverageData(File dataFile) {
		InputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(dataFile), 16384);
			return loadCoverageData(is);
		} catch (IOException e) {
			logger.error("Cobertura: Error reading file "
					+ dataFile.getAbsolutePath() + ": "
					+ e.getLocalizedMessage(), e);
			return null;
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					logger.error("Cobertura: Error closing file "
							+ dataFile.getAbsolutePath() + ": "
							+ e.getLocalizedMessage(), e);
				}
		}
	}
	private static ProjectData loadCoverageData(InputStream dataFile)
			throws IOException {
		ObjectInputStream objects = null;

		try {
			objects = new ObjectInputStream(dataFile);
			ProjectData projectData = (ProjectData) objects.readObject();
			logger.info("Cobertura: Loaded information on "
					+ projectData.getNumberOfClasses() + " classes.");

			return projectData;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Cobertura: Error reading from object stream.", e);
			return null;
		} finally {
			if (objects != null) {
				try {
					objects.close();
				} catch (IOException e) {
					logger.error("Cobertura: Error closing object stream.");
				}
			}
		}
	}

	private static void unsafeSaveCoverageData(ProjectData projectData, File dataFile) {
		FileOutputStream os = null;

		try {
			File dataDir = dataFile.getParentFile();
			if ((dataDir != null) && !dataDir.exists()) {
				dataDir.mkdirs();
			}
			os = new FileOutputStream(dataFile);
			saveCoverageData(projectData, os);
		} catch (IOException e) {
			logger.error("Cobertura: Error writing file "
					+ dataFile.getAbsolutePath(), e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					logger.error("Cobertura: Error closing file "
							+ dataFile.getAbsolutePath(), e);
				}
			}
		}
	}

	private static void saveCoverageData(ProjectData projectData,
			OutputStream dataFile) {
		ObjectOutputStream objects = null;

		try {
			objects = new ObjectOutputStream(dataFile);
			objects.writeObject(projectData);
			logger.info("Cobertura: Saved information on "
					+ projectData.getNumberOfClasses() + " classes.");
		} catch (IOException e) {
			logger.error("Cobertura: Error writing to object stream.", e);
		} finally {
			if (objects != null) {
				try {
					objects.close();
				} catch (IOException e) {
					logger.error("Cobertura: Error closing object stream.", e);
				}
			}
		}
	}

	public static void saveCoverageData(ProjectData projectDataToSave, File dataFile) {
    /*
     * A note about the next synchronized block:  Cobertura uses static fields to
     * hold the data.   When there are multiple classloaders, each classloader
     * will keep track of the line counts for the classes that it loads.
     *
     * The static initializers for the Cobertura classes are also called for
     * each classloader.   So, there is one shutdown hook for each classloader.
     * So, when the JVM exits, each shutdown hook will try to write the
     * data it has kept to the datafile.   They will do this at the same
     * time.   Before Java 6, this seemed to work fine, but with Java 6, there
     * seems to have been a change with how file locks are implemented.   So,
     * care has to be taken to make sure only one thread locks a file at a time.
     *
     * So, we will synchronize on the string that represents the path to the
     * dataFile.  Apparently, there will be only one of these in the JVM
     * even if there are multiple classloaders.  I assume that is because
     * the String class is loaded by the JVM's root classloader.
     */
		synchronized (dataFile.getAbsolutePath().intern()) {
			FileLocker fileLocker = new FileLocker(dataFile);

			try {
				// Read the old data, merge our current data into it, then
				// write a new ser file.
				if (fileLocker.lock()) {
					ProjectData datafileProjectData = loadCoverageDataFromDatafile(dataFile);
					if (datafileProjectData == null) {
						datafileProjectData = projectDataToSave;
					} else {
						datafileProjectData.merge(projectDataToSave);
					}
					unsafeSaveCoverageData(
							datafileProjectData, dataFile);
				}
			} finally {
				// Release the file lock
				fileLocker.release();
			}
		}
	}

	private static ProjectData loadCoverageDataFromDatafile(File dataFile) {
		ProjectData projectData = null;

		// Read projectData from the serialized file.
		if (dataFile.isFile()) {
			projectData = CoverageDataFileHandler.loadCoverageData(dataFile);
		}

		if (projectData == null) {
			// We could not read from the serialized file, so use a new object.
			logger
					.info("Cobertura: Coverage data file "
							+ dataFile.getAbsolutePath()
							+ " either does not exist or is not readable.  Creating a new data file.");
		}

		return projectData;
	}


}
