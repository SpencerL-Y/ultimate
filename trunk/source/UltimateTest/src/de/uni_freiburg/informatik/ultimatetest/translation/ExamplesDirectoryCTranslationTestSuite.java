package de.uni_freiburg.informatik.ultimatetest.translation;

import java.io.File;
import java.util.Collection;

import de.uni_freiburg.informatik.ultimatetest.summary.ITestSummary;
import de.uni_freiburg.informatik.ultimatetest.util.Util;

public class ExamplesDirectoryCTranslationTestSuite extends AbstractCTranslationTestSuite {

	private static File sInputDirectory = new File(Util.getPathFromTrunk("examples/"));

	@Override
	public Collection<File> getInputFiles() {
		return Util.getFiles(sInputDirectory, new String[] { ".c", ".i" });
	}

	@Override
	public ITestSummary getSummary() {
		return new TranslationTestSummary("ExamplesDirectoryCTranslationTestSuite", sInputDirectory.getAbsolutePath());
	}

}
