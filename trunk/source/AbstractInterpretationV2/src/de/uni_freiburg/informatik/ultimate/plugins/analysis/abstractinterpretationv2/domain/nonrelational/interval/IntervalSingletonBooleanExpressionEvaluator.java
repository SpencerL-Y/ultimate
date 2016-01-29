/*
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE AbstractInterpretationV2 plug-in.
 * 
 * The ULTIMATE AbstractInterpretationV2 plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE AbstractInterpretationV2 plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE AbstractInterpretationV2 plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE AbstractInterpretationV2 plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE AbstractInterpretationV2 plug-in grant you additional permission 
 * to convey the resulting work.
 */

package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.interval;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.model.boogie.IBoogieVar;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.BooleanValue;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.evaluator.IEvaluationResult;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.evaluator.IEvaluator;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;

/**
 * Class for boolean singleton values in the {@link IntervalDomain}.
 * 
 * @author Marius Greitschus <greitsch@informatik.uni-freiburg.de>
 *
 */
public class IntervalSingletonBooleanExpressionEvaluator
        implements IEvaluator<IntervalDomainEvaluationResult, IntervalDomainState, CodeBlock, IBoogieVar> {

	private final BooleanValue mBooleanValue;

	/**
	 * Default constructor that creates a new instance of the {@link IntervalSingletonBooleanExpressionEvaluator} in the
	 * {@link IntervalDomain}.
	 * 
	 * @param value
	 *            The value to set.
	 */
	protected IntervalSingletonBooleanExpressionEvaluator(BooleanValue value) {
		mBooleanValue = value;
	}

	@Override
	public List<IEvaluationResult<IntervalDomainEvaluationResult>> evaluate(IntervalDomainState currentState) {
		final List<IEvaluationResult<IntervalDomainEvaluationResult>> returnList = new ArrayList<>();

		returnList.add(new IntervalDomainEvaluationResult(new IntervalDomainValue(), currentState, mBooleanValue));

		return returnList;
	}

	@Override
	public void addSubEvaluator(
	        IEvaluator<IntervalDomainEvaluationResult, IntervalDomainState, CodeBlock, IBoogieVar> evaluator) {
		throw new UnsupportedOperationException("Adding a subevaluator to this kind of evaluator is not permitted.");
	}

	@Override
	public Set<String> getVarIdentifiers() {
		return new HashSet<String>();
	}

	@Override
	public boolean hasFreeOperands() {
		return false;
	}

	@Override
	public boolean containsBool() {
		return true;
	}

	@Override
	public String toString() {
		return mBooleanValue.getValue().name();
	}

	@Override
	public List<IEvaluationResult<IntervalDomainEvaluationResult>> inverseEvaluate(
	        IEvaluationResult<IntervalDomainEvaluationResult> computedState) {
		return evaluate(computedState.getResult().getEvaluatedState());
	}
}
