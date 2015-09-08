/*
 * Copyright (C) 2013-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2012-2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE ModelCheckerUtils Library.
 * 
 * The ULTIMATE ModelCheckerUtils Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE ModelCheckerUtils Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE ModelCheckerUtils Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE ModelCheckerUtils Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE ModelCheckerUtils Library grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.linearTerms;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermTransformer;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.linearTerms.BinaryRelation.NoRelationOfThisKindException;

/**
 * Transform all subterms that are an affine relation to positive normal form.
 * 
 * @author Matthias Heizmann
 */
public class AffineSubtermNormalizer extends TermTransformer {

	private final Script m_Script;
	private final Logger mLogger;

	public AffineSubtermNormalizer(Script script, Logger logger) {
		super();
		m_Script = script;
		mLogger = logger;
	}

	private static boolean isBinaryNumericRelation(Term term) {
		boolean result = true;
		try {
			new BinaryNumericRelation(term);
		} catch (NoRelationOfThisKindException e) {
			result = false;
		}
		return result;
	}

	@Override
	protected void convert(Term term) {
		if (!term.getSort().getName().equals("Bool")) {
			// do not descend further
			super.setResult(term);
			return;
		}
		if (isBinaryNumericRelation(term)) {
			AffineRelation affRel = null;
			try {
				affRel = new AffineRelation(term);
			} catch (NotAffineException e) {
				setResult(term);
				return;
			}
			Term pnf = affRel.positiveNormalForm(m_Script);
			setResult(pnf);
			return;
		}

		super.convert(term);
	}

}
