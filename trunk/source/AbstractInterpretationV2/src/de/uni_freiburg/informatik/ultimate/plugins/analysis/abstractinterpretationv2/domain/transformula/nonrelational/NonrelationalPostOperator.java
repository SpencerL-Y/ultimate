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

package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.transformula.nonrelational;

import java.util.ArrayList;
import java.util.List;

import de.uni_freiburg.informatik.ultimate.abstractinterpretation.model.IAbstractPostOperator;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.transitions.UnmodifiableTransFormula;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.variables.IProgramVarOrConst;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.INonrelationalValue;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Summary;

public abstract class NonrelationalPostOperator<STATE extends NonrelationalState<STATE, V>, V extends INonrelationalValue<V>>
		implements IAbstractPostOperator<STATE, CodeBlock, IProgramVarOrConst> {
	
	private final ILogger mLogger;
	
	protected NonrelationalPostOperator(final ILogger logger) {
		mLogger = logger;
	}
	
	@Override
	public List<STATE> apply(final STATE oldstate, final CodeBlock transition) {
		assert oldstate != null;
		assert !oldstate.isBottom() : "Trying to compute post for a bottom state.";
		assert transition != null;
		
		// TODO fix WORKAROUND unsoundness for summary code blocks without procedure implementation
		if (transition instanceof Summary && !((Summary) transition).calledProcedureHasImplementation()) {
			throw new UnsupportedOperationException("Summary for procedure without implementation");
		}
		
		final UnmodifiableTransFormula transformula = transition.getTransformula();
		final Term term = transformula.getFormula();
		
		final List<STATE> currentStates = new ArrayList<>();
		currentStates.add(oldstate);
		
		if (true) {
			throw new AssertionError("Not implemented");
		}

		// TODO
		
		return currentStates;
	}
	
	@Override
	public List<STATE> apply(final STATE stateBeforeLeaving, final STATE stateAfterLeaving,
			final CodeBlock transition) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
