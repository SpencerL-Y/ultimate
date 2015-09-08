/*
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
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
/**
 * 
 */
package de.uni_freiburg.informatik.ultimatetest.suites.traceabstraction;

import java.util.Collection;

import de.uni_freiburg.informatik.ultimatetest.UltimateTestCase;

/**
 * Test for array interpolation
 * @author musab@informatik.uni-freiburg.de, heizmanninformatik.uni-freiburg.de
 *
 */

public class ArrayInterplationTest extends
		AbstractTraceAbstractionTestSuite {
	private static final String[] m_Directories = {
//		"examples/programs/regression",
//		"examples/programs/quantifier/",
//		"examples/programs/quantifier/regression",
//		"examples/programs/recursivePrograms",
//		"examples/programs/toy"
		"examples/svcomp/ldv-regression/"
	};
	
	private static final boolean m_TraceAbstractionBoogieWithBackwardPredicates = false;
	private static final boolean m_TraceAbstractionBoogieWithForwardPredicates = false;
	private static final boolean m_TraceAbstractionBoogieWithFPandBP = false;
	private static final boolean m_TraceAbstractionCWithBackwardPredicates = false;
	private static final boolean m_TraceAbstractionCWithForwardPredicates = true;		
	private static final boolean m_TraceAbstractionCWithFPandBP = false;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getTimeout() {
		return 120 * 1000;
	}
	
	@Override
	public Collection<UltimateTestCase> createTestCases() {
		if (m_TraceAbstractionBoogieWithForwardPredicates) {
			addTestCases(
					"AutomizerBpl.xml",
					"automizer/ForwardPredicates.epf",
				    m_Directories,
				    new String[] {".bpl"});
		} 
		if (m_TraceAbstractionBoogieWithBackwardPredicates) {
			addTestCases(
					"AutomizerBpl.xml",
					"automizer/BackwardPredicates.epf",
				    m_Directories,
				    new String[] {".bpl"});
		}
		if (m_TraceAbstractionBoogieWithFPandBP) {
			addTestCases(
					"AutomizerBpl.xml",
					"automizer/ForwardPredicatesAndBackwardPredicates.epf",
				    m_Directories,
				    new String[] {".bpl"});
		}
		if (m_TraceAbstractionCWithForwardPredicates) {
			addTestCases(
					"AutomizerC.xml",
					"automizer/arrayInterpolationTest/ForwardPredicates.epf",
				    m_Directories,
				    new String[] {".c", ".i"});
		}
		if (m_TraceAbstractionCWithBackwardPredicates) {
			addTestCases(
					"AutomizerC.xml",
					"automizer/BackwardPredicates.epf",
				    m_Directories,
				    new String[] {".c", ".i"});
		}
		if (m_TraceAbstractionCWithFPandBP) {
			addTestCases(
					"AutomizerC.xml",
					"automizer/ForwardPredicatesAndBackwardPredicates.epf",
				    m_Directories,
				    new String[] {".c", ".i"});
		}
		return super.createTestCases();
	}

	
}
