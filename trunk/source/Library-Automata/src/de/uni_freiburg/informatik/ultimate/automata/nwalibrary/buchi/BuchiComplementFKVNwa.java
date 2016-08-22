/*
 * Copyright (C) 2013-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
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
package de.uni_freiburg.informatik.ultimate.automata.nwalibrary.buchi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryServices;
import de.uni_freiburg.informatik.ultimate.automata.AutomataOperationCanceledException;
import de.uni_freiburg.informatik.ultimate.automata.LibraryIdentifiers;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.INestedWordAutomatonSimple;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.NestedWordAutomatonCache;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.StateFactory;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.buchi.MultiOptimizationLevelRankingGenerator.FkvOptimization;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.IStateDeterminizer;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.oldapi.DeterminizedState;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.OutgoingCallTransition;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.OutgoingReturnTransition;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;

/**
 * Buchi Complementation based on
 * 2004ATVA - Friedgut,Kupferman,Vardi - Büchi Complementation Made Tighter.
 * TODO Christian 2016-08-19: Writes <tt>"bigX"</tt> to logger on <tt>WARN</tt> level in a loop, i.e., spams a lot.
 * 
 * @author heizmann@informatik.uni-freiburg.de
 * @param <LETTER>
 *            letter type
 * @param <STATE>
 *            state type
 */
public class BuchiComplementFKVNwa<LETTER, STATE>
		implements INestedWordAutomatonSimple<LETTER, STATE> {
		
	private final AutomataLibraryServices mServices;
	private final ILogger mLogger;
	
	/**
	 * TODO Allow definition of a maximal rank for cases where you know that
	 * this is sound. E.g. if the automaton is reverse deterministic a maximal
	 * rank of 2 is sufficient, see paper of Seth Forgaty.
	 */
	private final int mUserDefinedMaxRank;
	
	private final INestedWordAutomatonSimple<LETTER, STATE> mOperand;
	
	private final NestedWordAutomatonCache<LETTER, STATE> mCache;
	
	private final StateFactory<STATE> mStateFactory;
	
	/**
	 * Maps DeterminizedState to its representative in the resulting automaton.
	 */
	private final Map<DeterminizedState<LETTER, STATE>, STATE> mDet2res =
			new HashMap<DeterminizedState<LETTER, STATE>, STATE>();
			
	/**
	 * Maps a state in resulting automaton to the FkvSubsetComponentState for which it
	 * was created.
	 */
	private final Map<STATE, FkvSubsetComponentState<LETTER, STATE>> mRes2scs =
			new HashMap<STATE, FkvSubsetComponentState<LETTER, STATE>>();
			
	/**
	 * Maps a LevelRankingState to its representative in the resulting automaton.
	 */
	private final Map<LevelRankingState<LETTER, STATE>, STATE> mLrk2res =
			new HashMap<LevelRankingState<LETTER, STATE>, STATE>();
			
	/**
	 * Maps a state in resulting automaton to the LevelRankingState for which it
	 * was created.
	 */
	private final Map<STATE, LevelRankingState<LETTER, STATE>> mRes2lrk =
			new HashMap<STATE, LevelRankingState<LETTER, STATE>>();
			
	private final IStateDeterminizer<LETTER, STATE> mStateDeterminizer;
	
	/**
	 * Highest rank that occured during the construction. Used only for
	 * statistics.
	 */
	private int mHighestRank = -1;
	
	private final MultiOptimizationLevelRankingGenerator<LETTER, STATE, LevelRankingConstraint<LETTER, STATE>>
			mLevelRankingGenerator;
	
	private final STATE mSinkState;
	
	public BuchiComplementFKVNwa(final AutomataLibraryServices services,
			final INestedWordAutomatonSimple<LETTER, STATE> operand,
			final IStateDeterminizer<LETTER, STATE> stateDeterminizer,
			final StateFactory<STATE> stateFactory, final FkvOptimization optimization,
			final int userDefinedMaxRank) throws AutomataOperationCanceledException {
		mServices = services;
		mLogger = mServices.getLoggingService().getLogger(LibraryIdentifiers.PLUGIN_ID);
		mOperand = operand;
		mStateFactory = stateFactory;
		mCache = new NestedWordAutomatonCache<LETTER, STATE>(
				mServices,
				operand.getInternalAlphabet(), operand.getCallAlphabet(),
				operand.getReturnAlphabet(), mStateFactory);
		mStateDeterminizer = stateDeterminizer;
		mUserDefinedMaxRank = userDefinedMaxRank;
		mLevelRankingGenerator = new MultiOptimizationLevelRankingGenerator<>(
				mServices, mOperand, optimization, userDefinedMaxRank);
		mSinkState = constructSinkState();
	}
	
	private void constructInitialState() {
		final DeterminizedState<LETTER, STATE> detState = mStateDeterminizer.initialState();
		getOrAdd(detState, true);
	}
	
	private STATE constructSinkState() {
		final DeterminizedState<LETTER, STATE> detSinkState = new DeterminizedState<>(mOperand);
		final STATE resSinkState = mStateDeterminizer.getState(detSinkState);
		mCache.addState(false, true, resSinkState);
		mDet2res.put(detSinkState, resSinkState);
		mRes2scs.put(resSinkState, new FkvSubsetComponentState<>(detSinkState));
		return resSinkState;
	}
	
	/**
	 * Return state of result automaton that represents lrkState. If no such
	 * state was constructed yet, construct it.
	 */
	private STATE getOrAdd(final LevelRankingState<LETTER, STATE> lrkState) {
		if (lrkState.isEmpty()) {
			return mSinkState;
		} else {
			STATE resSucc = mLrk2res.get(lrkState);
			if (resSucc == null) {
				resSucc = mStateFactory.buchiComplementFKV(lrkState);
				assert resSucc != null;
				mCache.addState(false, lrkState.isOempty(), resSucc);
				mLrk2res.put(lrkState, resSucc);
				mRes2lrk.put(resSucc, lrkState);
				if (this.mHighestRank < lrkState.mHighestRank) {
					this.mHighestRank = lrkState.mHighestRank;
				}
			}
			return resSucc;
		}
	}
	
	/**
	 * Return state of result automaton that represents detState. If no such
	 * state was constructed yet, construct it.
	 */
	private STATE getOrAdd(final DeterminizedState<LETTER, STATE> detState, final boolean isInitial) {
		if (detState.isEmpty()) {
			assert !isInitial : "sink cannot be initial";
			return mSinkState;
		} else {
			STATE resSucc = mDet2res.get(detState);
			if (resSucc == null) {
				resSucc = mStateDeterminizer.getState(detState);
				assert resSucc != null;
				mCache.addState(isInitial, false, resSucc);
				mDet2res.put(detState, resSucc);
				mRes2scs.put(resSucc, new FkvSubsetComponentState<>(detState));
			}
			return resSucc;
		}
	}
	
	public int getHighesRank() {
		return mHighestRank;
	}
	
	public int getPowersetStates() {
		return mRes2scs.size();
	}
	
	public int getRankStates() {
		return mRes2lrk.size();
	}
	
	@Override
	public Iterable<STATE> getInitialStates() {
		constructInitialState();
		return mCache.getInitialStates();
	}
	
	@Override
	public Set<LETTER> getInternalAlphabet() {
		return mOperand.getInternalAlphabet();
	}
	
	@Override
	public Set<LETTER> getCallAlphabet() {
		return mOperand.getCallAlphabet();
	}
	
	@Override
	public Set<LETTER> getReturnAlphabet() {
		return mOperand.getReturnAlphabet();
	}
	
	@Override
	public StateFactory<STATE> getStateFactory() {
		return mStateFactory;
	}
	
	@Override
	public boolean isInitial(final STATE state) {
		return mCache.isInitial(state);
	}
	
	@Override
	public boolean isFinal(final STATE state) {
		return mCache.isFinal(state);
	}
	
	@Override
	public STATE getEmptyStackState() {
		return mCache.getEmptyStackState();
	}
	
	@Override
	public Set<LETTER> lettersInternal(final STATE state) {
		return mOperand.getInternalAlphabet();
	}
	
	@Override
	public Set<LETTER> lettersCall(final STATE state) {
		return mOperand.getCallAlphabet();
	}
	
	@Override
	public Set<LETTER> lettersReturn(final STATE state) {
		return mOperand.getReturnAlphabet();
	}
	
	@Override
	public Iterable<OutgoingInternalTransition<LETTER, STATE>> internalSuccessors(
			final STATE state, final LETTER letter) {
		final Collection<STATE> succs = mCache.succInternal(state, letter);
		if (succs == null) {
			final Collection<STATE> resSuccs = new ArrayList<STATE>();
			final FkvSubsetComponentState<LETTER, STATE> detUp = mRes2scs.get(state);
			if (detUp != null) {
				{
					final DeterminizedState<LETTER, STATE> detSucc = mStateDeterminizer.internalSuccessor(
							detUp.getDeterminizedState(), letter);
					final STATE resSucc = getOrAdd(detSucc, false);
					mCache.addInternalTransition(state, letter, resSucc);
					resSuccs.add(resSucc);
				}
				final boolean oIsEmpty = true; // considering O empty for subset component will safe some states
				final LevelRankingConstraint<LETTER, STATE> constraints =
						new LevelRankingConstraintDrdCheck<LETTER, STATE>(
								mOperand, oIsEmpty, mUserDefinedMaxRank, mStateDeterminizer.useDoubleDeckers());
				constraints.internalSuccessorConstraints(detUp, letter);
				final Collection<LevelRankingState<LETTER, STATE>> result =
						mLevelRankingGenerator.generateLevelRankings(constraints, true);
				if (result.size() > 2) {
					mLogger.warn("big" + result.size());
				}
				for (final LevelRankingState<LETTER, STATE> complSucc : result) {
					final STATE resSucc = getOrAdd(complSucc);
					mCache.addInternalTransition(state, letter, resSucc);
					resSuccs.add(resSucc);
				}
			}
			final LevelRankingState<LETTER, STATE> complUp = mRes2lrk.get(state);
			if (complUp != null) {
				final LevelRankingConstraint<LETTER, STATE> constraints =
						new LevelRankingConstraintDrdCheck<LETTER, STATE>(
								mOperand, complUp.isOempty(), mUserDefinedMaxRank,
								mStateDeterminizer.useDoubleDeckers());
				constraints.internalSuccessorConstraints(complUp, letter);
				final Collection<LevelRankingState<LETTER, STATE>> result =
						mLevelRankingGenerator.generateLevelRankings(constraints, false);
				if (result.size() > 4) {
					mLogger.warn("big" + result.size());
				}
				for (final LevelRankingState<LETTER, STATE> complSucc : result) {
					final STATE resSucc = getOrAdd(complSucc);
					mCache.addInternalTransition(state, letter, resSucc);
					resSuccs.add(resSucc);
				}
			}
		}
		return mCache.internalSuccessors(state, letter);
	}
	
	@Override
	public Iterable<OutgoingInternalTransition<LETTER, STATE>> internalSuccessors(
			final STATE state) {
		for (final LETTER letter : getInternalAlphabet()) {
			internalSuccessors(state, letter);
		}
		return mCache.internalSuccessors(state);
	}
	
	@Override
	public Iterable<OutgoingCallTransition<LETTER, STATE>> callSuccessors(
			final STATE state, final LETTER letter) {
		final Collection<STATE> succs = mCache.succCall(state, letter);
		if (succs == null) {
			final Collection<STATE> resSuccs = new ArrayList<STATE>();
			final FkvSubsetComponentState<LETTER, STATE> detUp = mRes2scs.get(state);
			if (detUp != null) {
				{
					final DeterminizedState<LETTER, STATE> detSucc = mStateDeterminizer.callSuccessor(
							detUp.getDeterminizedState(), letter);
					final STATE resSucc = getOrAdd(detSucc, false);
					mCache.addCallTransition(state, letter, resSucc);
					resSuccs.add(resSucc);
				}
				final boolean oIsEmpty = true; // considering O empty for subset component will safe some states
				final LevelRankingConstraint<LETTER, STATE> constraints =
						new LevelRankingConstraintDrdCheck<LETTER, STATE>(
								mOperand, oIsEmpty, mUserDefinedMaxRank, mStateDeterminizer.useDoubleDeckers());
				constraints.callSuccessorConstraints(detUp, letter);
				final Collection<LevelRankingState<LETTER, STATE>> result =
						mLevelRankingGenerator.generateLevelRankings(constraints, true);
				for (final LevelRankingState<LETTER, STATE> complSucc : result) {
					final STATE resSucc = getOrAdd(complSucc);
					mCache.addCallTransition(state, letter, resSucc);
					resSuccs.add(resSucc);
				}
			}
			final LevelRankingState<LETTER, STATE> complUp = mRes2lrk.get(state);
			if (complUp != null) {
				final LevelRankingConstraint<LETTER, STATE> constraints =
						new LevelRankingConstraintDrdCheck<LETTER, STATE>(
								mOperand, complUp.isOempty(), mUserDefinedMaxRank,
								mStateDeterminizer.useDoubleDeckers());
				constraints.callSuccessorConstraints(complUp, letter);
				final Collection<LevelRankingState<LETTER, STATE>> result =
						mLevelRankingGenerator.generateLevelRankings(constraints, false);
				for (final LevelRankingState<LETTER, STATE> complSucc : result) {
					final STATE resSucc = getOrAdd(complSucc);
					mCache.addCallTransition(state, letter, resSucc);
					resSuccs.add(resSucc);
				}
			}
		}
		return mCache.callSuccessors(state, letter);
	}
	
	@Override
	public Iterable<OutgoingCallTransition<LETTER, STATE>> callSuccessors(
			final STATE state) {
		for (final LETTER letter : getCallAlphabet()) {
			callSuccessors(state, letter);
		}
		return mCache.callSuccessors(state);
	}
	
	@Override
	public Iterable<OutgoingReturnTransition<LETTER, STATE>> returnSuccessors(
			final STATE state, final STATE hier, final LETTER letter) {
		final Collection<STATE> succs = mCache.succReturn(state, hier, letter);
		if (succs == null) {
			final Collection<STATE> resSuccs = new ArrayList<STATE>();
			final FkvSubsetComponentState<LETTER, STATE> detUp = mRes2scs.get(state);
			final FkvSubsetComponentState<LETTER, STATE> detDown = mRes2scs.get(hier);
			if (detUp != null) {
				{
					final DeterminizedState<LETTER, STATE> detSucc = mStateDeterminizer.returnSuccessor(
							detUp.getDeterminizedState(), detDown.getDeterminizedState(), letter);
					final STATE resSucc = getOrAdd(detSucc, false);
					mCache.addReturnTransition(state, hier, letter, resSucc);
					resSuccs.add(resSucc);
				}
				final boolean oIsEmpty = true; // considering O empty for subset component will safe some states
				final LevelRankingConstraint<LETTER, STATE> constraints =
						new LevelRankingConstraintDrdCheck<LETTER, STATE>(
								mOperand, oIsEmpty, mUserDefinedMaxRank, mStateDeterminizer.useDoubleDeckers());
				constraints.returnSuccessorConstraints(detUp, detDown, letter);
				final Collection<LevelRankingState<LETTER, STATE>> result =
						mLevelRankingGenerator.generateLevelRankings(constraints, true);
				for (final LevelRankingState<LETTER, STATE> complSucc : result) {
					final STATE resSucc = getOrAdd(complSucc);
					mCache.addReturnTransition(state, hier, letter, resSucc);
					resSuccs.add(resSucc);
				}
			}
			final LevelRankingState<LETTER, STATE> complUp = mRes2lrk.get(state);
			IFkvState<LETTER, STATE> complDown;
			if (mRes2scs.containsKey(hier)) {
				complDown = mRes2scs.get(hier);
			} else {
				assert mRes2lrk.containsKey(hier);
				complDown = mRes2lrk.get(hier);
			}
			if (complUp != null) {
				final LevelRankingConstraint<LETTER, STATE> constraints =
						new LevelRankingConstraintDrdCheck<LETTER, STATE>(
								mOperand, complUp.isOempty(), mUserDefinedMaxRank,
								mStateDeterminizer.useDoubleDeckers());
				constraints.returnSuccessorConstraints(complUp, complDown, letter);
				final Collection<LevelRankingState<LETTER, STATE>> result =
						mLevelRankingGenerator.generateLevelRankings(constraints, false);
				for (final LevelRankingState<LETTER, STATE> complSucc : result) {
					final STATE resSucc = getOrAdd(complSucc);
					mCache.addReturnTransition(state, hier, letter, resSucc);
					resSuccs.add(resSucc);
				}
			}
		}
		return mCache.returnSuccessors(state, hier, letter);
	}
	
	@Override
	public Iterable<OutgoingReturnTransition<LETTER, STATE>> returnSuccessorsGivenHier(
			final STATE state, final STATE hier) {
		for (final LETTER letter : getReturnAlphabet()) {
			returnSuccessors(state, hier, letter);
		}
		return mCache.returnSuccessorsGivenHier(state, hier);
	}
	
	@Override
	public int size() {
		return mCache.size();
	}
	
	@Override
	public Set<LETTER> getAlphabet() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String sizeInformation() {
		return "size Information not available";
	}
}
