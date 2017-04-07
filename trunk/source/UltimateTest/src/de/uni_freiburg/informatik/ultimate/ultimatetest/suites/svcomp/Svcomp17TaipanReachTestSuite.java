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
package de.uni_freiburg.informatik.ultimate.ultimatetest.suites.svcomp;

import java.util.ArrayList;
import java.util.List;

import de.uni_freiburg.informatik.ultimate.test.UltimateRunDefinition;
import de.uni_freiburg.informatik.ultimate.test.decider.ITestResultDecider;
import de.uni_freiburg.informatik.ultimate.test.decider.SvcompReachTestResultDecider;

/**
 *
 * @author dietsch@informatik.uni-freiburg.de
 *
 */
public class Svcomp17TaipanReachTestSuite extends AbstractSVCOMPTestSuite {

	@Override
	protected ITestResultDecider getTestResultDecider(final UltimateRunDefinition urd) {
		return new SvcompReachTestResultDecider(urd, false);
	}

	@Override
	protected long getTimeout() {
		// Timeout for each test case in milliseconds
		return 90 * 1000;
	}

	@Override
	protected int getFilesPerCategory() {
		// -1 or value larger than 0
		return 100;
	}

	@Override
	protected List<SVCOMPTestDefinition> getTestDefinitions() {
		final List<SVCOMPTestDefinition> rtr = new ArrayList<>();
		// contains 135 examples
		rtr.addAll(getForAll("ReachSafety-Arrays", true));

		// contains 94 examples
		rtr.addAll(getForAll("ReachSafety-ControlFlow", true));

		// contains 1149 examples
		rtr.addAll(getForAll("ReachSafety-ECA", true));

		// contains 173 examples
		rtr.addAll(getForAll("ReachSafety-Heap", true));

		// contains 159 examples
		rtr.addAll(getForAll("ReachSafety-Loops", true));

		// contains 159 examples
		rtr.addAll(getForAll("ReachSafety-ProductLines", true));

		// contains 98 examples
		rtr.addAll(getForAll("ReachSafety-Recursive", true));

		// contains 273 examples
		rtr.addAll(getForAll("ReachSafety-Sequentialized", true));

		// contains 2795 examples
		rtr.addAll(getForAll("Systems_DeviceDriversLinux64_ReachSafety", false));

		return rtr;
	}

	private List<SVCOMPTestDefinition> getForAll(final String set, final int limit, final boolean arch32) {
		return getForAll(set, getTimeout(), limit, arch32);
	}

	private List<SVCOMPTestDefinition> getForAll(final String set, final boolean arch32) {
		return getForAll(set, getTimeout(), getFilesPerCategory(), arch32);
	}

	private List<SVCOMPTestDefinition> getForAll(final String set, final long timeout, final int limit,
			final boolean arch32) {
		final List<SVCOMPTestDefinition> rtr = new ArrayList<>();

		if (arch32) {
			rtr.add(getTestDefinitionFromExamples(set, "AutomizerCInline.xml",
					"default/taipan/svcomp-Reach-32bit-Taipan_Default.epf", timeout, limit));
			rtr.add(getTestDefinitionFromExamples(set, "AutomizerCInline.xml",
					"ai/svcomp-Reach-32bit-RubberTaipan_Default.epf", timeout, limit));
		} else {
			rtr.add(getTestDefinitionFromExamples(set, "AutomizerCInline.xml",
					"default/taipan/svcomp-Reach-64bit-Taipan_Default.epf", timeout, limit));
			rtr.add(getTestDefinitionFromExamples(set, "AutomizerCInline.xml",
					"ai/svcomp-Reach-64bit-RubberTaipan_Default.epf", timeout, limit));
		}

		return rtr;
	}
}
