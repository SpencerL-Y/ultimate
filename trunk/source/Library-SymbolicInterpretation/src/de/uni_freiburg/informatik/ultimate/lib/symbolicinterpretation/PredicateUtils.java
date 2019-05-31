/*
 * Copyright (C) 2019 Claus Schätzle (schaetzc@tf.uni-freiburg.de)
 * Copyright (C) 2019 University of Freiburg
 *
 * This file is part of the ULTIMATE Library-SymbolicInterpretation plug-in.
 *
 * The ULTIMATE Library-SymbolicInterpretation plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ULTIMATE Library-SymbolicInterpretation plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Library-SymbolicInterpretation plug-in. If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Library-SymbolicInterpretation plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE Library-SymbolicInterpretation plug-in grant you additional permission
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.lib.symbolicinterpretation;

import java.util.Collection;

import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Script.LBool;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.CfgSmtToolkit;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.IIcfg;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.IIcfgCallTransition;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.IIcfgTransition;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.IcfgLocation;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.transitions.TransFormula;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils.SimplificationTechnique;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils.XnfConversionTechnique;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.managedscript.ManagedScript;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.IPredicate;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.PredicateTransformer;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.TermDomainOperationProvider;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.PredicateFactory;

public class PredicateUtils {

	private final IIcfg<IcfgLocation> mIcfg;
	private final ManagedScript mScript;
	private final PredicateFactory mFactory;
	private final PredicateTransformer<Term, IPredicate, TransFormula> mTransformer;
	private final IPredicate mTop;
	private final IPredicate mBottom;

	public PredicateUtils(final IUltimateServiceProvider services, final IIcfg<IcfgLocation> icfg) {
		mIcfg = icfg;
		mScript = icfg.getCfgSmtToolkit().getManagedScript();
		// TODO decide which techniques to use or use a setting
		mFactory = new PredicateFactory(services, mScript, icfg.getCfgSmtToolkit().getSymbolTable(),
				SimplificationTechnique.SIMPLIFY_DDA, XnfConversionTechnique.BOTTOM_UP_WITH_LOCAL_SIMPLIFICATION);
		mTransformer = new PredicateTransformer<>(mScript, new TermDomainOperationProvider(services, mScript));
		mTop = mFactory.newPredicate(mScript.term(this, "true"));
		mBottom = mFactory.newPredicate(mScript.term(this, "false"));
	}

	public ManagedScript getScript() {
		return mScript;
	}

	public PredicateFactory getFactory() {
		return mFactory;
	}

	public IPredicate post(final IPredicate input, final IIcfgTransition<IcfgLocation> transition) {
		return mFactory.newPredicate(mTransformer.strongestPostcondition(input, transition.getTransformula()));
	}

	public IPredicate postCall(final IPredicate input, final IIcfgCallTransition<IcfgLocation> transition) {
		final CfgSmtToolkit toolkit = mIcfg.getCfgSmtToolkit();
		final String calledProcedure = transition.getSucceedingProcedure();
		return mFactory.newPredicate(mTransformer.strongestPostconditionCall(input, transition.getLocalVarsAssignment(),
				toolkit.getOldVarsAssignmentCache().getGlobalVarsAssignment(calledProcedure),
				toolkit.getOldVarsAssignmentCache().getOldVarsAssignment(calledProcedure),
				toolkit.getModifiableGlobalsTable().getModifiedBoogieVars(calledProcedure)));
	}

	public IPredicate merge(final IPredicate first, final IPredicate second) {
		// TODO decide whether or not to use simplification or use a setting
		final boolean simplifyDDA = true;
		return mFactory.or(simplifyDDA, first, second);
	}

	public IPredicate top() {
		return mTop;
	}

	public IPredicate bottom() {
		return mBottom;
	}

	public IPredicate or(final IPredicate... operands) {
		return mFactory.or(true, operands);
	}

	public IPredicate or(final Term... operands) {
		return mFactory.newPredicate(SmtUtils.or(mScript.getScript(), operands));
	}

	public IPredicate or(final Collection<Term> operands) {
		return mFactory.newPredicate(SmtUtils.or(mScript.getScript(), operands));
	}

	public boolean isFalse(final IPredicate pred) {
		// TODO: Use unifier instead of costly SMT check
		if (mBottom.equals(pred)) {
			return true;
		}
		final LBool result = SmtUtils.checkSatTerm(mScript.getScript(), pred.getClosedFormula());
		return !satAsBool(result);
	}

	public boolean implies(final IPredicate p1, final IPredicate p2) {
		// TODO: Use unifier instead of costly SMT check
		if (p1.equals(p2)) {
			return true;
		}
		final Script script = mScript.getScript();
		final Term negImplTerm =
				SmtUtils.neg(script, SmtUtils.implies(script, p1.getClosedFormula(), p2.getClosedFormula()));
		final LBool result = SmtUtils.checkSatTerm(script, negImplTerm);
		return !satAsBool(result);
	}

	private boolean satAsBool(final LBool solverResult) {
		switch (solverResult) {
		case SAT:
			return true;
		case UNSAT:
			return false;
		case UNKNOWN:
			throw new UnsupportedOperationException("Abstraction of intricate predicate not yet implemented");
		default:
			throw new UnsupportedOperationException("Missing case: " + solverResult);
		}
	}

}