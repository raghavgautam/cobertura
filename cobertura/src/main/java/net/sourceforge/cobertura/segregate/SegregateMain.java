package net.sourceforge.cobertura.segregate;

import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import net.sourceforge.cobertura.dsl.Cobertura;
import net.sourceforge.cobertura.util.CommandLineBuilder;
import net.sourceforge.cobertura.util.Header;

import java.io.File;

public class SegregateMain {

	public SegregateMain(String[] args) {
		ArgumentsBuilder builder = new ArgumentsBuilder();


		// Go through all the parameters
		boolean isDatafileSet = false;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--datafile")) {
				final String dataFile = args[++i];
				builder.setDataFile(dataFile);
				validateDataFile(dataFile);
				builder.setDataFile(dataFile);
				isDatafileSet = true;
			} else if (args[i].equals("--package")) {
				String packageName = args[++i];
				builder.setPackageName(packageName);
			} else if (args[i].equals("--destination")) {
				String destination = args[++i];
				builder.setDestinationDirectory(destination);
				validateAndCreateDestinationDirectory(destination);
			} else {
				System.err.println("Unknown option/argument: " + args[i]);
				System.exit(1);
			}
		}
		final Arguments arguments = builder.build();
		if (arguments.getPackageName() == null) {
			System.err.println("Error: --package option must be set");
			System.exit(1);
		}
		if (!isDatafileSet) {
			System.err.println("Error: --datafile option must be set");
			System.exit(1);
		}
		if (arguments.getDestinationDirectory() == null) {
			System.err.println("Error: --destination option must be set");
			System.exit(1);
		}

		new Cobertura(arguments).segregate();
	}

	private static void validateDataFile(String value) {
		File dataFile = new File(value);
		if (!dataFile.exists()) {
			System.err.println("Error: data file " + dataFile.getAbsolutePath()
					+ " does not exist");
			System.exit(1);
		}
		if (!dataFile.isFile()) {
			System.err.println("Error: data file " + dataFile.getAbsolutePath()
					+ " must be a regular file");
			System.exit(1);
		}
	}

	private static void validateAndCreateDestinationDirectory(String value) {
		File destinationDir = new File(value);
		if (destinationDir.exists() && !destinationDir.isDirectory()) {
			System.err.println("Error: destination directory " + destinationDir
					+ " already exists but is not a directory");
			System.exit(1);
		}
		destinationDir.mkdirs();
	}

	public static int segregate(String[] args) {
		Header.print(System.out);

		try {
			args = CommandLineBuilder.preprocessCommandLineArguments(args);
		} catch (Exception ex) {
			System.err.println("Error: Cannot process arguments: "
					+ ex.getMessage());
			return 1;
		}
		new SegregateMain(args);
		return 0;
	}

	public static void main(String[] args) {
		int returnValue = segregate(args);
		if ( returnValue != 0 ) {
			System.exit(returnValue);
		}
	}

}
