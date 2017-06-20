package net.sourceforge.cobertura.segregate;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.PackageData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.dsl.Arguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

public class SegregateProjectDataFilesTask {
	private static final Logger logger = LoggerFactory
			.getLogger(SegregateProjectDataFilesTask.class);

	public void segregateProjectDataFiles(Arguments arguments,
										  ProjectData projectData) {
		File destinationDirectory = arguments.getDestinationDirectory();

		if (!destinationDirectory.exists()) {
			logger.error("Destination directory does not exist.");
		}

		final String packageName = arguments.getPackageName();
		final SortedSet recursiveSubPackagesData = projectData.getSubPackages(packageName);
		final HashMap subProjectDataMap = new HashMap();
		for (Object subPackageCoverage : recursiveSubPackagesData) {
			PackageData oneCoverage = (PackageData) subPackageCoverage;
			final String subPackageName = getSubPackageName(packageName, oneCoverage.getName());
			ProjectData subPackageProjectData = (ProjectData) subProjectDataMap.get(subPackageName);
			if (subPackageProjectData == null) {
				subPackageProjectData = new ProjectData();
				subProjectDataMap.put(subPackageName, subPackageProjectData);
			}
			final SortedSet classes = oneCoverage.getClasses();
			for (Object aClass : classes) {
				final ClassData coverageData = (ClassData) aClass;
				subPackageProjectData.addClassData(coverageData);
			}
		}
		for (Object o : subProjectDataMap.entrySet()) {
			Map.Entry entry = (Map.Entry) o;
			String subPackageName = (String) entry.getKey();
			ProjectData subProjectData = (ProjectData) entry.getValue();
			final File dataFile = new File(destinationDirectory, subPackageName.replace('.', '_')+".ser");
			CoverageDataFileHandler.saveCoverageData(subProjectData, dataFile);
		}
	}

	private String getSubPackageName(String packageName, String subPackageName) {
		final String[] split = subPackageName.substring(packageName.length()).split("\\.", 3);
		if (split.length <= 1) {
			return packageName;
        } else {
            //splits look like ["", "subpackage", ""]
			return packageName + "." + split[1];
        }
	}
}
