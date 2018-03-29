/*
 * Copyright (C) 2016 Alexander Nutz (nutz@informatik.uni-freiburg.de)
 * Copyright (C) 2016 University of Freiburg
 *
 * This file is part of the ULTIMATE HeapSeparator plug-in.
 *
 * The ULTIMATE HeapSeparator plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ULTIMATE HeapSeparator plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE HeapSeparator plug-in. If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE HeapSeparator plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE HeapSeparator plug-in grant you additional permission
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.icfgtransformer.heapseparator;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.uni_freiburg.informatik.ultimate.logic.ApplicationTerm;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.BoogieConst;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.BoogieNonOldVar;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.BoogieOldVar;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.LocalBoogieVar;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.CfgSmtToolkit;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.transformations.IntraproceduralReplacementVar;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.variables.IProgramVarOrConst;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.variables.ProgramVarUtils;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.managedscript.ManagedScript;
import de.uni_freiburg.informatik.ultimate.util.datastructures.relation.NestedMap2;

/**
 *
 * @author Alexander Nutz (nutz@informatik.uni-freiburg.de)
 *
 */
public class SubArrayManager {


	/**
	 * used for caching the sub arrays that this class manages
	 */
	private final NestedMap2<IProgramVarOrConst, List<LocationBlock>, IProgramVarOrConst>
		mArrayToLocationBlockListToSubArray;

	private final Map<IProgramVarOrConst, ArrayGroup> mArrayIdToArrayGroup;

	private final ManagedScript mManagedScript;

	private final HeapSeparatorBenchmark mStatistics;

	public SubArrayManager(final CfgSmtToolkit csToolkit, final HeapSeparatorBenchmark statistics,
			final Map<IProgramVarOrConst, ArrayGroup> arrayIdToArrayGroup) {

		mManagedScript = csToolkit.getManagedScript();

		mStatistics = statistics;

		mArrayIdToArrayGroup = arrayIdToArrayGroup;

		mArrayToLocationBlockListToSubArray = new NestedMap2<>();
	}



	@Override
	public String toString() {
		return "NewArrayIdProvider";// + mArrayToPartitionInformation.toString();
	}

	public IProgramVarOrConst getSubArray(final IProgramVarOrConst programVar, final List<LocationBlock> projectList) {
		final ArrayGroup arrayGroup = mArrayIdToArrayGroup.get(programVar);
		assert Objects.nonNull(arrayGroup);
		if (projectList.size() != arrayGroup.getDimensionality()) {
			throw new AssertionError("list of location blocks does not have the right length for the given array!");
		}

		IProgramVarOrConst subArray = mArrayToLocationBlockListToSubArray.get(programVar, projectList);
		if (subArray == null) {
			subArray = constructFreshProgramVarsForIndexPartition(programVar, projectList);
			mArrayToLocationBlockListToSubArray.put(programVar, projectList, subArray);

			mStatistics.incrementNewArrayVarCounter(arrayGroup);
		}

		return subArray;
	}

	/**
	 * Given an IndexPartition constructs fresh Terms and ProgramVars for all the arrays in this ParititionInformation's
	 * array group.
	 * Updates the mappings that holds these fresh Terms.
	 *
	 * @param oldArrayId
	 * @param indexPartition
	 * @return
	 */
	private IProgramVarOrConst constructFreshProgramVarsForIndexPartition(final IProgramVarOrConst arrayPv,
			final List<LocationBlock> projectList) {

		IProgramVarOrConst freshVar = null;
		if (arrayPv instanceof LocalBoogieVar) {
			final LocalBoogieVar lbv = (LocalBoogieVar) arrayPv;
			final String newId = lbv.getIdentifier() + "_part_" + constructIndexName(projectList);
			final TermVariable newTv = mManagedScript.constructFreshCopy(lbv.getTermVariable());

			mManagedScript.lock(this);
			final String constString = newId + "_const";
			mManagedScript.getScript().declareFun(constString, new Sort[0], newTv.getSort());
			final ApplicationTerm newConst = (ApplicationTerm) mManagedScript.term(this, constString);

			final String constPrimedString = newId + "_const_primed";
			mManagedScript.getScript().declareFun(constPrimedString, new Sort[0], newTv.getSort());
			final ApplicationTerm newPrimedConst = (ApplicationTerm) mManagedScript.term(this, constPrimedString);

			freshVar = new LocalBoogieVar(
					newId,
					lbv.getProcedure(),
					null,
					newTv,
					newConst,
					newPrimedConst);
			mManagedScript.unlock(this);
			return freshVar;
		} else if (arrayPv instanceof BoogieNonOldVar) {
			final BoogieNonOldVar bnovOld = (BoogieNonOldVar) arrayPv;

			final String newId = bnovOld.getIdentifier() + "_part_" + constructIndexName(projectList);

			mManagedScript.lock(this);
			final BoogieNonOldVar bnovNew =
					ProgramVarUtils.constructGlobalProgramVarPair(newId, bnovOld.getSort(), mManagedScript, this);

			freshVar = bnovNew;
			mManagedScript.unlock(this);
			return freshVar;
		} else if (arrayPv instanceof BoogieOldVar) {
			final BoogieOldVar bovOld = (BoogieOldVar) arrayPv;

			final String newId = bovOld.getGloballyUniqueId() + "_part_" + constructIndexName(projectList);

			mManagedScript.lock(this);
			final BoogieNonOldVar bnovNew =
					ProgramVarUtils.constructGlobalProgramVarPair(newId, bovOld.getSort(), mManagedScript, this);

			freshVar = bnovNew.getOldVar();
			assert freshVar != null;
			mManagedScript.unlock(this);
			return freshVar;
		} else if (arrayPv instanceof IntraproceduralReplacementVar) {
			throw new AssertionError("TODO: implement");
		} else if (arrayPv instanceof BoogieConst) {
			throw new AssertionError("TODO: implement");
		} else {
			throw new AssertionError("case missing --> add it?");
		}
	}

	private String constructIndexName(final List<LocationBlock> projectList) {
		final StringBuilder sb = new StringBuilder();
		String sep = "";
		for (final LocationBlock lb : projectList) {
			sb.append(sep);
			sb.append(lb.toString());
			sep = "_";
		}
		return sb.toString();
	}
}

