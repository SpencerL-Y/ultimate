/*
 * Copyright (C) 2015 Marius Greitschus (greitsch@informatik.uni-freiburg.de)
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

package de.uni_freiburg.informatik.ultimatetest.suites.evals;

import java.util.Collection;

import de.uni_freiburg.informatik.ultimate.test.DirectoryFileEndingsPair;
import de.uni_freiburg.informatik.ultimate.test.UltimateRunDefinition;
import de.uni_freiburg.informatik.ultimate.test.UltimateTestCase;
import de.uni_freiburg.informatik.ultimate.test.decider.ITestResultDecider;
import de.uni_freiburg.informatik.ultimate.test.decider.SafetyCheckTestResultDecider;
import de.uni_freiburg.informatik.ultimate.util.datastructures.relation.Triple;
import de.uni_freiburg.informatik.ultimatetest.suites.AbstractEvalTestSuite;
import de.uni_freiburg.informatik.ultimatetest.summaries.ColumnDefinition;
import de.uni_freiburg.informatik.ultimatetest.summaries.ColumnDefinition.Aggregate;
import de.uni_freiburg.informatik.ultimatetest.summaries.ConversionContext;

/**
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * @author Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 *
 */
public class AbstractInterpretationV2TestSuite extends AbstractEvalTestSuite {

	private static final int DEFAULT_LIMIT = Integer.MAX_VALUE;
	
	@SuppressWarnings("unchecked")
	private static final Triple<String, String, String>[] TOOLCHAINS = new Triple[] {
//	        new Triple<>("AutomizerC.xml", ".i", "svcomp2016/svcomp-Reach-64bit-Automizer_Default.epf"),
//	        new Triple<>("AbstractInterpretationv2C.xml", ".i", "ai/AIv2_INT.epf"),
//	        new Triple<>("AbstractInterpretationv2.xml", ".bpl", "ai/AIv2_INT.epf"),
	        new Triple<>("AbstractInterpretationv2C.xml", ".c", "ai/AIv2_OCT.epf"),
	        new Triple<>("AbstractInterpretationv2.xml", ".bpl", "ai/AIv2_OCT.epf"),
	};

	private static final String[] INPUT = new String[] {
			/* failed tests */
//			"examples/programs/abstractInterpretation/EvenOdd.bpl",  // doesn't terminate (FXPE cannot handle recursion)
//			// ... more recursive programs
//			"examples/programs/regression/c/InParamRenaming.c",      // cacsl2boogie UnsupportedOperationException
//	        "examples/svcomp/loops/bubble_sort_true-unreach-call.c", // RCFG AssertionError non-linear
			/* RCFG does not seem to terminate */
//	        "examples/svcomp/loops/compact_false-unreach-call.c",
//	        "examples/svcomp/loops/heavy_false-unreach-call.c",
//	        "examples/svcomp/loops/heavy_true-unreach-call.c",
//			"examples/programs/abstractInterpretation/EasyRecursive_incorrect.bpl",

//	        "varDiffOrder/",

			/* closure test set */
//			"examples/svcomp/locks/",
//			"examples/svcomp/loop-acceleration/",
//			"examples/svcomp/loop-invgen/",
//			"examples/svcomp/loop-lit/",
//			"examples/svcomp/loop-new/",
//			"examples/svcomp/loops/",
//			"examples/svcomp/ntdrivers/",
//			"examples/svcomp/ntdrivers-simplified/",
//			"examples/svcomp/ssh/",
//			"examples/svcomp/ssh-simplified/",
//			"examples/svcomp/systemc/",

	        "examples/programs/abstractInterpretation/",
	        /* ULTIMATE repo */
	         "examples/programs/regression/bpl/",
	         "examples/programs/regression/c/",
	         "examples/programs/recursivePrograms",
	        /* SV-COMP repo */
	        "examples/svcomp/loops/", // SPLIT
			// "examples/svcomp/ntdrivers-simplified/",
	   		// "examples/svcomp/ssh-simplified/", 
			// "examples/svcomp/locks/",
			// "examples/svcomp/recursive/",
			// "examples/svcomp/systemc/",
			// "examples/svcomp/loopsSelection/",
			// "examples/svcomp/eca/", // SPLIT
			// "examples/svcomp/ecaSelection/",
			// "examples/svcomp/systemc/", // SPLIT
			// "examples/svcomp/systemc1/",
			// "examples/svcomp/systemc2/",
			// "examples/svcomp/eca-rers2012/",
//			 "examples/svcomp/recursive/",
			// "examples/svcomp/ssh-simplified/",
			// "examples/svcomp/ssh/",
//			"examples/programs/toy/",
//			"examples/programs/toy/4BitCounterPointer-safe.c",

			// problems with loop detector
			// "examples/svcomp/loops/eureka_01_false-unreach-call.c",
			// "examples/svcomp/loops/matrix_false-unreach-call_true-termination.c",

			// unsoundness

			// problems with unsupportedops
			// "examples/svcomp/loops/sum01_true-unreach-call_true-termination.c",
			// "examples/svcomp/loops/string_false-unreach-call.c",
	};

	@Override
	protected ColumnDefinition[] getColumnDefinitions() {
		// @formatter:off
		return new ColumnDefinition[] {
		        new ColumnDefinition("Runtime (ns)", "Total time", ConversionContext.Divide(1000000000, 2, " s"),
		                Aggregate.Sum, Aggregate.Average),
		        new ColumnDefinition("Allocated memory end (bytes)", "Alloc. Memory",
		                ConversionContext.Divide(1048576, 2, " MB"), Aggregate.Max, Aggregate.Average),
		        new ColumnDefinition("Peak memory consumption (bytes)", "Peak Memory",
		                ConversionContext.Divide(1048576, 2, " MB"), Aggregate.Max, Aggregate.Average), };
		// @formatter:on
	}

	@Override
	protected long getTimeout() {
		return 10 * 1000; // origin/dev uses 60 * 1000
	}
	
	@Override
	public ITestResultDecider constructITestResultDecider(UltimateRunDefinition urd) {
		return new SafetyCheckTestResultDecider(urd, true);
		//		return new NoTimeoutTestResultDecider(urd);
	}

	@Override
	public Collection<UltimateTestCase> createTestCases() {
		for (final Triple<String, String, String> triple : TOOLCHAINS) {
			final DirectoryFileEndingsPair[] pairs = new DirectoryFileEndingsPair[INPUT.length];
			for (int i = 0; i < INPUT.length; ++i) {
				pairs[i] = new DirectoryFileEndingsPair(INPUT[i], new String[] { triple.getSecond() }, DEFAULT_LIMIT);
			}
			addTestCase(triple.getFirst(), triple.getThird(), pairs);
		}
		return super.createTestCases();
	}
}
