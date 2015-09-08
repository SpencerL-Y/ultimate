/*
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE Test Library.
 * 
 * The ULTIMATE Test Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE Test Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Test Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Test Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Test Library grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimatetest.suites.translation;

import java.io.File;
import java.util.Collection;

import de.uni_freiburg.informatik.ultimatetest.util.TestUtil;

public class SVCOMPCTranslationTestSuite extends AbstractCTranslationTestSuite {

//	private static File sInputDirectory = new File(Util.getFromMavenVariableSVCOMPRoot("../../svcomp/"));
//	private static File sInputDirectory = new File(TestUtil.getPathFromTrunk("examples/svcomp/ldv-regression"));
	private static File sInputDirectory = new File(TestUtil.getPathFromTrunk("examples/svcomp/ldv-linux-3.0"));
	private static File sSettings = new File(TestUtil.getPathFromTrunk("examples/settings/automizer/ForwardPredicates_SvcompReachPreciseMM.epf"));
	

	@Override
	public Collection<File> getInputFiles() {
		return TestUtil.getFiles(sInputDirectory, new String[] { ".c", ".i" });
	}
	
	@Override 
	public File getSettings() {
		return sSettings;
	}

}
