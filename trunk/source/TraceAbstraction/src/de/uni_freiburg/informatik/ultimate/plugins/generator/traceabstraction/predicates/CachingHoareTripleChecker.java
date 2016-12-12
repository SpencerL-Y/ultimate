/*
 * Copyright (C) 2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
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
package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates;

import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.IAction;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.ICallAction;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.IInternalAction;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.IReturnAction;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.hoaretriple.HoareTripleCheckerStatisticsGenerator;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.hoaretriple.IHoareTripleChecker;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.IPredicate;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.Activator;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singletracecheck.PredicateUnifier;
import de.uni_freiburg.informatik.ultimate.util.datastructures.relation.NestedMap3;
import de.uni_freiburg.informatik.ultimate.util.datastructures.relation.NestedMap4;

/**
 * IHoareTripleChecker that caches already computed results.
 * Also tries to use these results for more intelligent checks.
 * @author Matthias Heizmann
 *
 */
public abstract class CachingHoareTripleChecker implements IHoareTripleChecker {

	protected final IUltimateServiceProvider mServices;
	protected final ILogger mLogger;
	protected final IHoareTripleChecker mComputingHoareTripleChecker;
	protected final PredicateUnifier mPredicateUnifer;
	protected final boolean mUnknownIfSomeExtendedCacheCheckIsUnknown = true;
	
	private final NestedMap3<IAction, IPredicate, IPredicate, Validity> mInternalCache =
			new NestedMap3<>();
	private final NestedMap3<IAction, IPredicate, IPredicate, Validity> mCallCache =
			new NestedMap3<>();
	private final NestedMap4<IPredicate, IAction, IPredicate, IPredicate, Validity> mReturnCache =
			new NestedMap4<>();

	
	public CachingHoareTripleChecker(
			final IUltimateServiceProvider services, final IHoareTripleChecker protectedHoareTripleChecker,
			final PredicateUnifier predicateUnifer) {
		super();
		mServices = services;
		mLogger = mServices.getLoggingService().getLogger(Activator.PLUGIN_ID);
		mComputingHoareTripleChecker = protectedHoareTripleChecker;
		mPredicateUnifer = predicateUnifer;
	}

	@Override
	public Validity checkInternal(final IPredicate pre, final IInternalAction act, final IPredicate succ) {
		Validity result = getFromInternalCache(pre, act, succ);
		if (result == null) {
			result = extendedBinaryCacheCheck(pre, act, succ, mInternalCache);
			if (result == null) {
				result = mComputingHoareTripleChecker.checkInternal(pre, act, succ);
			}
			addToInternalCache(pre, act, succ, result);
		}
		return result;
	}

	private Validity getFromInternalCache(final IPredicate pre, final IInternalAction act, final IPredicate succ) {
		return mInternalCache.get(act, pre, succ);
	}

	private final void addToInternalCache(final IPredicate pre, final IInternalAction act, final IPredicate succ,
			final Validity result) {
		mInternalCache.put(act, pre, succ, result);
	}


	@Override
	public Validity checkCall(final IPredicate pre, final ICallAction act, final IPredicate succ) {
		return mComputingHoareTripleChecker.checkCall(pre, act, succ);
	}

	@Override
	public Validity checkReturn(final IPredicate preLin, final IPredicate preHier,
			final IReturnAction act, final IPredicate succ) {
		return mComputingHoareTripleChecker.checkReturn(preLin, preHier, act, succ);
	}
	

	
	
	
	protected abstract Validity extendedBinaryCacheCheck(final IPredicate pre, final IAction act, final IPredicate succ, 
			NestedMap3<IAction, IPredicate, IPredicate, Validity> binaryCache);

	@Override
	public HoareTripleCheckerStatisticsGenerator getEdgeCheckerBenchmark() {
		return mComputingHoareTripleChecker.getEdgeCheckerBenchmark();
	}

	public IHoareTripleChecker getProtectedHoareTripleChecker() {
		return mComputingHoareTripleChecker;
	}

	@Override
	public void releaseLock() {
		mComputingHoareTripleChecker.releaseLock();
	}
	
	

}
