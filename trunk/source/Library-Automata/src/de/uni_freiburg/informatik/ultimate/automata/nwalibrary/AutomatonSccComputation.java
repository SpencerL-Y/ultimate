/*
 * Copyright (C) 2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2009-2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE Automata Library.
 * 
 * The ULTIMATE Automata Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE Automata Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Automata Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Automata Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Automata Library grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.automata.nwalibrary;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryServices;
import de.uni_freiburg.informatik.ultimate.automata.LibraryIdentifiers;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.reachableStatesAutomaton.NestedWordAutomatonReachableStates;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.OutgoingCallTransition;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.OutgoingTransitionlet;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.StateBasedTransitionFilterPredicateProvider;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.SummaryReturnTransition;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.util.datastructures.FilteredIterable;
import de.uni_freiburg.informatik.ultimate.util.datastructures.IteratorConcatenation;
import de.uni_freiburg.informatik.ultimate.util.scc.DefaultSccComputation;
import de.uni_freiburg.informatik.ultimate.util.scc.SccComputation.ISuccessorProvider;
import de.uni_freiburg.informatik.ultimate.util.scc.StronglyConnectedComponent;

/**
 * Compute SCCs of an automaton. Allows to restrict computation to a subgraph
 * (subset of states with corresponding edges) of the automaton.
 * This computation should work for each INestedWordAutomaton, however it is
 * only sound if each return transition is reachable (i.e., each summary
 * transition can actually be taken). To enforce soundness we restricted the
 * input to NestedWordAutomatonReachableStates, we might relax this in the
 * future.
 * 
 * @author Matthias Heizmann
 *
 * @param <LETTER>
 * @param <STATE>
 */
public class AutomatonSccComputation<LETTER, STATE> {
	
	
	private final INestedWordAutomaton<LETTER, STATE> mNestedWordAutomaton;
	private final AutomataLibraryServices mServices;
	private final ILogger mLogger;
	private final DefaultSccComputation<STATE> mSccComputation;
	
	
	/**
	 * Computes SCCs of an automaton for a given subset of states.
	 * @param stateSubset subset of the automata's states
	 * @param startNodes states at which the computation of SSCs starts
	 * @return
	 */
	public AutomatonSccComputation(
			NestedWordAutomatonReachableStates<LETTER, STATE> nestedWordAutomatonReachableStates,
			AutomataLibraryServices services, Set<STATE> stateSubset, Set<STATE> startNodes) {
		super();
		mNestedWordAutomaton = nestedWordAutomatonReachableStates;
		mServices = services;
		mLogger = mServices.getLoggingService().getLogger(LibraryIdentifiers.PLUGIN_ID);
		mSccComputation = new DefaultSccComputation<STATE>(mLogger, 
				new InSumCaSuccessorProvider(mNestedWordAutomaton, stateSubset), 
				stateSubset.size(), startNodes);
	}


	/**
	 * Returns all balls of the computed SCC.
	 * @return
	 */
	public Collection<StronglyConnectedComponent<STATE>> getBalls() {
		return mSccComputation.getBalls();
	}



	/**
	 * Provides for a given STATE all STATEs that are 
	 * <ul>
	 * <li> successors of internal transitions, summaries and call transitions,
	 * and
	 * <li> contained in a given set of states.
	 * </ul>
	 * @author Matthias Heizmann
	 *
	 */
	private class InSumCaSuccessorProvider implements ISuccessorProvider<STATE> {
		
		private final StateBasedTransitionFilterPredicateProvider<LETTER, STATE> mTransitionFilter;

		public InSumCaSuccessorProvider(
				INestedWordAutomaton<LETTER, STATE> nestedWordAutomatonReachableStates,
				Set<STATE> stateSubset) {
			super();
			mTransitionFilter = new StateBasedTransitionFilterPredicateProvider<>(stateSubset);
		}
		private <E extends OutgoingTransitionlet<LETTER, STATE>> Iterator<STATE> getStateContainerIterator(final Iterator<E> it) {
			return new Iterator<STATE>() {

				@Override
				public boolean hasNext() {
					return it.hasNext();
				}

				@Override
				public STATE next() {
					return it.next().getSucc();
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException("not modifiable");
				}
				
			};
			
		}

		@Override
		public IteratorConcatenation<STATE> getSuccessors(final STATE state) {
			
			final Iterator<STATE> internalTransitionsIterator = 
					getStateContainerIterator(new FilteredIterable<OutgoingInternalTransition<LETTER, STATE>>(
							mNestedWordAutomaton.internalSuccessors(state), mTransitionFilter.getInternalSuccessorPredicate()).iterator());
			
			final Iterator<STATE> returnSummaryTransitionsIterator = 
					getStateContainerIterator(new FilteredIterable<SummaryReturnTransition<LETTER, STATE>>(
							mNestedWordAutomaton.returnSummarySuccessor(state), mTransitionFilter.getReturnSummaryPredicate()).iterator());
			
			
			final Iterator<STATE> callTransitionsIterator = 
					getStateContainerIterator(new FilteredIterable<OutgoingCallTransition<LETTER, STATE>>(
							mNestedWordAutomaton.callSuccessors(state), mTransitionFilter.getCallSuccessorPredicate()).iterator());

			
			final Iterator<STATE>[] iterators = (Iterator<STATE>[]) 
					new Iterator<?>[] { internalTransitionsIterator, returnSummaryTransitionsIterator, callTransitionsIterator };
			return new IteratorConcatenation<STATE>(Arrays.asList(iterators));
		}

	}

}
