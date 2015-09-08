/*
 * Copyright (C) 2012-2015 Stefan Wissert
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE BlockEncoding plug-in.
 * 
 * The ULTIMATE BlockEncoding plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE BlockEncoding plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE BlockEncoding plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE BlockEncoding plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE BlockEncoding plug-in grant you additional permission 
 * to convey the resulting work.
 */
/**
 * 
 */
package de.uni_freiburg.informatik.ultimate.blockencoding.model;

import java.util.HashSet;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.blockencoding.model.interfaces.IBasicEdge;
import de.uni_freiburg.informatik.ultimate.blockencoding.model.interfaces.IMinimizedEdge;
import de.uni_freiburg.informatik.ultimate.blockencoding.rating.interfaces.IRating;
import de.uni_freiburg.informatik.ultimate.blockencoding.rating.metrics.RatingFactory;
import de.uni_freiburg.informatik.ultimate.blockencoding.rating.util.EncodingStatistics;
import de.uni_freiburg.informatik.ultimate.model.boogie.BoogieVar;
import de.uni_freiburg.informatik.ultimate.model.structure.ModifiableMultigraphEdge;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.TransFormula;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.GotoEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Summary;

/**
 * Wrapper class for a normal edge, which exists in the RCFG.
 * 
 * @author Stefan Wissert
 * 
 */
public class BasicEdge extends
		ModifiableMultigraphEdge<MinimizedNode, IMinimizedEdge> implements
		IBasicEdge {

	/**
	 * Serial number, do not really use it.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * the underlying original edge (of type "CodeBlock")
	 */
	private CodeBlock originalEdge;

	/**
	 * the rating of this edge
	 */
	private IRating rating;

	/**
	 * 
	 */
	private HashSet<BoogieVar> usedVariables;

	/**
	 * @param originalEdge
	 * @param source
	 * @param target
	 */
	public BasicEdge(CodeBlock originalEdge, MinimizedNode source,
			MinimizedNode target) {
		super(source, target);
		this.originalEdge = originalEdge;
		if (originalEdge.getTransitionFormula() != null
				&& !(originalEdge instanceof Summary)) {
			this.usedVariables = new HashSet<BoogieVar>();
			this.usedVariables.addAll(originalEdge.getTransitionFormula()
					.getAssignedVars());
			this.usedVariables.addAll(originalEdge.getTransitionFormula()
					.getInVars().keySet());
			this.usedVariables.addAll(originalEdge.getTransitionFormula()
					.getOutVars().keySet());
		} else {
			this.usedVariables = new HashSet<BoogieVar>();
		}
		this.rating = RatingFactory.getInstance().createRating(this);
		EncodingStatistics.incCountOfBasicEdges();
		EncodingStatistics.setMaxMinDiffVariablesInOneEdge(this.usedVariables
				.size());
	}

	@Override
	public boolean isBasicEdge() {
		return true;
	}

	@Override
	public CodeBlock getOriginalEdge() {
		return originalEdge;
	}

	@Override
	public String toString() {
		if (originalEdge instanceof GotoEdge) {
			return "goto";
		}
		return originalEdge.getPrettyPrintedStatements();
	}

	@Override
	public int getElementCount() {
		return 1;
	}

	@Override
	public boolean isOldVarInvolved() {
		if (originalEdge.getTransitionFormula() == null) {
			return false;
		}
		TransFormula tFormula = originalEdge.getTransitionFormula();
		return checkBoogieVarSetForOldVar(tFormula.getAssignedVars())
				|| checkBoogieVarSetForOldVar(tFormula.getInVars().keySet())
				|| checkBoogieVarSetForOldVar(tFormula.getOutVars().keySet());
	}

	/**
	 * This method checks a Set of BoogieVars, if there is a OldVar contained.
	 * 
	 * @param vars
	 *            a Set of BoogieVars
	 * @return true if there is an OldVar
	 */
	private boolean checkBoogieVarSetForOldVar(Set<BoogieVar> vars) {
		for (BoogieVar boogieVar : vars) {
			if (boogieVar.isOldvar()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public IRating getRating() {
		return rating;
	}

	@Override
	public Set<BoogieVar> getDifferentVariables() {
		return this.usedVariables;
	}

}
