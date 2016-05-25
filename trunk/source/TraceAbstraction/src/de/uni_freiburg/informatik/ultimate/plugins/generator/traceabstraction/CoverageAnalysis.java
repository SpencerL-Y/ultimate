/*
 * Copyright (C) 2014-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
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
package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import de.uni_freiburg.informatik.ultimate.automata.IRun;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedRun;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedWord;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.IAction;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.hoaretriple.IHoareTripleChecker.Validity;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.IPredicate;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ProgramPoint;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.ISLPredicate;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singleTraceCheck.IInterpolantGenerator;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singleTraceCheck.PredicateUnifier;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singleTraceCheck.TraceCheckerUtils.InterpolantsPreconditionPostcondition;

/**
 * Object that will analyze a trace with respect to a sequence of ProgramPoints 
 * and a sequence of interpolants.
 * The analysis starts at the beginning of the trace. For each ProgramPoint
 * that has already appeared while traversing the trace we check if the
 * current interpolant implies the interpolant at the position of the recurring
 * ProgramPoint.
 * @author heizmann@informatik.uni-freiburg.de
 */
public class CoverageAnalysis {
	
	protected final IUltimateServiceProvider mServices;

	protected final ILogger mLogger ;
	
	protected final NestedWord<? extends IAction> mNestedWord;
	private final List<ProgramPoint> mProgramPointSequence;
	private final IPredicate[] mInterpolants;
	private final PredicateUnifier mPredicateUnifier;
	
	private final Map<ProgramPoint, List<Integer>> mProgramPoint2Occurence = 
		new HashMap<ProgramPoint, List<Integer>>();
	
	private int mUnsat;
	private int mSat;
	private int mUnknown;
	private int mTrivial;
	private int mNotchecked;
	
	protected final IInterpolantGenerator mInterpolantGenerator;
	protected final InterpolantsPreconditionPostcondition mIPP;

	public CoverageAnalysis(IUltimateServiceProvider services, 
			IInterpolantGenerator interpolantGenerator,
			List<ProgramPoint> programPointSequence, ILogger logger) {
		mServices = services;
		mLogger = logger;
		mInterpolants = interpolantGenerator.getInterpolants();
		mNestedWord = NestedWord.nestedWord(interpolantGenerator.getTrace());
		mProgramPointSequence = programPointSequence;
		mPredicateUnifier = interpolantGenerator.getPredicateUnifier();
		mInterpolantGenerator = interpolantGenerator;
		mIPP = new InterpolantsPreconditionPostcondition(interpolantGenerator);
	}
	
	public void analyze() {
		assert(mNestedWord.length()-1 == mInterpolants.length);
		preprocess();
		
		for (int i=0; i<mNestedWord.length(); i++) {

			processCodeBlock(i);

			final ProgramPoint pp = mProgramPointSequence.get(i);
			List<Integer> previousOccurrences = mProgramPoint2Occurence.get(pp);
			if (previousOccurrences == null) {
				previousOccurrences = new ArrayList<Integer>();
				mProgramPoint2Occurence.put(pp, previousOccurrences);
			} else {
				for (final int previousOccurrence : previousOccurrences) {
					assert i > previousOccurrence;
					final IPredicate currentPredicate = mIPP.getInterpolant(i);
					final IPredicate previousPredicate = mIPP.getInterpolant(previousOccurrence);
					if (currentPredicate == previousPredicate) {
						// trivially covered and backedges already contained
						mTrivial++;
					} else {
						final Validity lbool = mPredicateUnifier.getCoverageRelation().isCovered(
								currentPredicate, previousPredicate);
						processCoveringResult(i, previousOccurrence, lbool);
						switch (lbool) {
						case VALID:
							mUnsat++;
							break;
						case INVALID:
							mSat++;
							break;
						case UNKNOWN:
							mUnknown++;
							break;
						case NOT_CHECKED:
							mNotchecked++;
							break;
						default:
							throw new AssertionError();
						}
					}
				}
			}
			previousOccurrences.add(i);
		}
		assert sumCountedOccurrences() == mProgramPointSequence.size() - 1;

		postprocess();
		
		mLogger.info("Checked inductivity of " +
				(mUnsat+mSat+mUnknown+mTrivial+mNotchecked) +	" backedges. " + 
				mUnsat + " proven. " + 
				mSat + " refuted. " + 
				mUnknown + " times theorem prover too weak." +
				mTrivial + " trivial." +
				mNotchecked + " not checked.");

	}

	private int sumCountedOccurrences() {
		int occurrenceSum = 0;
		for (final Entry<ProgramPoint, List<Integer>> entry : mProgramPoint2Occurence.entrySet()) {
			occurrenceSum += entry.getValue().size();
		}
		return occurrenceSum;
	}

	protected void processCodeBlock(int i) {
		// do nothing
	}

	protected void processCoveringResult(int currentPosition,
			int previousOccurrence, Validity lbool) {
		// do nothing
	}

	protected void postprocess() {
		// do nothing
	}

	protected void preprocess() {
		// do nothing
	}
	
	
	public static List<ProgramPoint> extractProgramPoints(IRun<CodeBlock, IPredicate> irun) {
		final ArrayList<IPredicate> predicateSequence = 
				((NestedRun<CodeBlock, IPredicate>) irun).getStateSequence();
		final ArrayList<ProgramPoint> result = new ArrayList<>();
		for (final IPredicate p : predicateSequence) {
			result.add(((ISLPredicate) p).getProgramPoint());
		}
		return result;
	}
	
	
	public BackwardCoveringInformation getBackwardCoveringInformation() {
		final int potentialBackwardCoverings = (mUnsat+mSat+mUnknown+mTrivial+mNotchecked);
		final int successfullBackwardCoverings = mUnsat+mTrivial;
		return new BackwardCoveringInformation(potentialBackwardCoverings, successfullBackwardCoverings);
	}
	
	
	public static Function<Object, Function<Object,Object>> s_DefaultAggregation = 
			x -> y -> { return new BackwardCoveringInformation((BackwardCoveringInformation)x, (BackwardCoveringInformation)y); };
	
	
	public static class BackwardCoveringInformation {
		private final int mPotentialBackwardCoverings;
		private final int mSuccessfullBackwardCoverings;
		
		public BackwardCoveringInformation(int potentialBackwardCoverings,
				int successfullBackwardCoverings) {
			super();
			mPotentialBackwardCoverings = potentialBackwardCoverings;
			mSuccessfullBackwardCoverings = successfullBackwardCoverings;
		}
		
		public BackwardCoveringInformation(BackwardCoveringInformation bci1, BackwardCoveringInformation bci2) {
			mPotentialBackwardCoverings = bci1.getPotentialBackwardCoverings() + bci2.getPotentialBackwardCoverings();
			mSuccessfullBackwardCoverings = bci1.getSuccessfullBackwardCoverings() + bci2.getSuccessfullBackwardCoverings();
		}
		public int getPotentialBackwardCoverings() {
			return mPotentialBackwardCoverings;
		}
		public int getSuccessfullBackwardCoverings() {
			return mSuccessfullBackwardCoverings;
		}

		@Override
		public String toString() {
			return mSuccessfullBackwardCoverings + "/" + mPotentialBackwardCoverings;
//			if (mPotentialBackwardCoverings == 0) {
//				return "not available";
//			} else {
//				long result = Math.round((((double) mSuccessfullBackwardCoverings) / mPotentialBackwardCoverings) * 100);
//				return result + "%";
//			}
		}
		
	}

}
