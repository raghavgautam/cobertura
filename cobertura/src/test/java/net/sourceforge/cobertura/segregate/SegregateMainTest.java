package net.sourceforge.cobertura.segregate;

import junit.framework.TestCase;
import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.test.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Tests merging feature by launching Main class.
 */
public class SegregateMainTest extends TestCase {
	private static final Logger LOGGER = LoggerFactory.getLogger(SegregateMainTest.class);

	private void deleteDir(File dir) {
		final File[] dirContents = dir.listFiles();
		if (dirContents != null) {
			for (File dirContent : dirContents) {
				deleteDir(dirContent);
			}
		}
		dir.delete();
	}

	public void testNewDestinationFile() throws IOException {
		ProjectData uberProjectData = new ProjectData();
		// Create some coverage data
		for (int i = 1; i <= 2; i++) {
			final String packageName = "org.cobertura.test" + i;
			uberProjectData.addClassData(new ClassData(packageName + ".First"));
			uberProjectData.addClassData(new ClassData(packageName + ".Second"));
			uberProjectData.addClassData(new ClassData(packageName + ".deep.deep.Deeper"));
			uberProjectData.addClassData(new ClassData(packageName + ".deep.deep.Deeper2"));
		}
		uberProjectData.addClassData(new ClassData("org.cobertura.Seventh"));

		File tempDir = new File(TestUtils.getTempDir(), "testSegregation");
		File uberDataFile = new File(tempDir, "testSegregation.ser");
		deleteDir(tempDir);
		tempDir.mkdirs();

		// Run merge task
		assertEquals(tempDir.list().length,0);
		CoverageDataFileHandler.saveCoverageData(uberProjectData, uberDataFile);
		String[] args = {"--package", "org.cobertura", "--destination", tempDir.toString(),
				"--datafile", uberDataFile.toString()};
		SegregateMain.main(args);
		LOGGER.info("Found files: " + Arrays.toString(tempDir.list()));
		assertEquals(4, tempDir.list().length);
		deleteDir(tempDir);
	}
}
