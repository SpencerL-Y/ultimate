/*
 * Copyright (C) 2015 Dirk Steinmetz
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE TraceAbstraction plug-in.
 * 
 * The ULTIMATE TraceAbstraction plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE TraceAbstraction plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE TraceAbstraction plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE TraceAbstraction plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE TraceAbstraction plug-in grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.pathinvariants.internal;

import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.TransFormula;

/**
 * A predicate formula representing a transition between two program points with
 * given invariant patterns.
 * 
 * If all InvariantTransitionPredicates of a program are fulfilled by an
 * assignment on variables within the invariant pattern, a valid invariant map
 * can be generated by inserting that particular assignment into the invariant
 * map.
 * 
 * @param <IPT>
 *            Invariant Pattern Type: Type used for invariant patterns
 */
public class InvariantTransitionPredicate<IPT> {
	private final IPT invStart;
	private final IPT invEnd;
	private final TransFormula transition;

	/**
	 * Creates a invariant transition predicate from two given invariant
	 * patterns and a connecting {@link TransFormula}.
	 * 
	 * @param invStart
	 *            the invariant at the transition's start location
	 * @param invEnd
	 *            the invariant at the transition's end location
	 * @param transition
	 *            the TransFormula describing the transition's behavior
	 */
	public InvariantTransitionPredicate(final IPT invStart, final IPT invEnd,
			final TransFormula transition) {
		this.invStart = invStart;
		this.invEnd = invEnd;
		this.transition = transition;
	}

	/**
	 * Returns the invariant at the transition's start location.
	 * 
	 * @return invariant at the transition's start location.
	 */
	public final IPT getInvStart() {
		return invStart;
	}
	
	/**
	 * Returns the invariant at the transition's end location.
	 * 
	 * @return invariant at the transition's end location.
	 */
	public final IPT getInvEnd() {
		return invEnd;
	}

	/**
	 * Returns the {@link TransFormula} describing the transition's behavior.
	 * 
	 * @return TransFormula describing the transition's behavior
	 */
	public final TransFormula getTransition() {
		return transition;
	}
}
