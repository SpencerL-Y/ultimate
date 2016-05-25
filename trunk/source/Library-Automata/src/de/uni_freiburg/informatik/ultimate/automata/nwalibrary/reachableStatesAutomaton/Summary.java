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
package de.uni_freiburg.informatik.ultimate.automata.nwalibrary.reachableStatesAutomaton;

import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.IncomingReturnTransition;

class Summary<LETTER, STATE> {
	private final StateContainer<LETTER, STATE> mHierPred;
	private final StateContainer<LETTER, STATE> mLinPred;
	private final LETTER mLetter;
	private final StateContainer<LETTER, STATE> mSucc;
	public Summary(StateContainer<LETTER, STATE> hierPred, 
			StateContainer<LETTER, STATE> linPred,
			LETTER letter,
			StateContainer<LETTER, STATE> succ) {
		super();
		mHierPred = hierPred;
		mLinPred = linPred;
		mLetter = letter;
		mSucc = succ;
	}
	public StateContainer<LETTER, STATE> getHierPred() {
		return mHierPred;
	}
	public StateContainer<LETTER, STATE> getLinPred() {
		return mLinPred;
	}
	public LETTER getLetter() {
		return mLetter;
	}
	public StateContainer<LETTER, STATE> getSucc() {
		return mSucc;
	}
	

	
	public IncomingReturnTransition<LETTER, STATE> obtainIncomingReturnTransition(
			NestedWordAutomatonReachableStates<LETTER, STATE> nwars) {
		for (final IncomingReturnTransition<LETTER, STATE> inTrans  : 
			nwars.returnPredecessors(getHierPred().getState(), getLetter(), getSucc().getState())) {
			if (getLinPred().getState().equals(inTrans.getLinPred())) {
				return inTrans;
			}
		}
		throw new AssertionError("no such transition");
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mHierPred == null) ? 0 : mHierPred.hashCode());
		result = prime * result
				+ ((mLinPred == null) ? 0 : mLinPred.hashCode());
		result = prime * result
				+ ((mSucc == null) ? 0 : mSucc.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Summary other = (Summary) obj;
		if (mHierPred == null) {
			if (other.mHierPred != null) {
				return false;
			}
		} else if (!mHierPred.equals(other.mHierPred)) {
			return false;
		}
		if (mLinPred == null) {
			if (other.mLinPred != null) {
				return false;
			}
		} else if (!mLinPred.equals(other.mLinPred)) {
			return false;
		}
		if (mSucc == null) {
			if (other.mSucc != null) {
				return false;
			}
		} else if (!mSucc.equals(other.mSucc)) {
			return false;
		}
		return true;
	}
	@Override
	public String toString() {
		return "(" + mHierPred + ", " + mLinPred + ", " + mSucc + ")";
	}
	
	
}
