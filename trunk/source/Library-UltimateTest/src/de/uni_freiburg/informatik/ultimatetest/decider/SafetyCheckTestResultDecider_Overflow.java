/*
 * Copyright (C) 2014-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE UnitTest Library.
 * 
 * The ULTIMATE UnitTest Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE UnitTest Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE UnitTest Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE UnitTest Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE UnitTest Library grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimatetest.decider;

import java.util.Collections;
import java.util.Map;

import de.uni_freiburg.informatik.ultimatetest.UltimateRunDefinition;
import de.uni_freiburg.informatik.ultimatetest.decider.expectedResult.IExpectedResultFinder;
import de.uni_freiburg.informatik.ultimatetest.decider.expectedResult.KeywordBasedExpectedResultFinder;
import de.uni_freiburg.informatik.ultimatetest.decider.overallResult.SafetyCheckerOverallResult;

/**
 * Use keywords in filename and first line to decide correctness of overflow
 * checks. Since so far we do not use keywords for this we use an empty map.
 * 
 * @author heizmann@informatik.uni-freiburg.de
 * 
 */
public class SafetyCheckTestResultDecider_Overflow extends SafetyCheckTestResultDecider {

	public SafetyCheckTestResultDecider_Overflow(
			UltimateRunDefinition ultimateRunDefinition,
			boolean unknownIsJUnitSuccess) {
		super(ultimateRunDefinition, unknownIsJUnitSuccess);
	}

	@Override
	public IExpectedResultFinder<SafetyCheckerOverallResult> constructExpectedResultFinder() {
		Map<String, SafetyCheckerOverallResult> emptyMap = Collections.emptyMap();
		return new KeywordBasedExpectedResultFinder<SafetyCheckerOverallResult>(
				emptyMap, null,
				emptyMap);
	}


}
