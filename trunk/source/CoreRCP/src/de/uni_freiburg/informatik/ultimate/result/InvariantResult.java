/*
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2012-2015 Markus Lindenmann (lindenmm@informatik.uni-freiburg.de)
 * Copyright (C) 2012-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE Core.
 * 
 * The ULTIMATE Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Core. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Core, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Core grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.result;

import de.uni_freiburg.informatik.ultimate.core.services.IBacktranslationService;
import de.uni_freiburg.informatik.ultimate.model.IElement;

/**
 * Report an invariant that holds at ELEM which is a node in an Ultimate model.
 * The invariant is given as an expression of type E.
 * 
 * @author Matthias Heizmann
 */
public class InvariantResult<ELEM extends IElement, E> extends AbstractResultAtElement<ELEM> implements
		IResultWithLocation {

	private final E m_Invariant;

	public InvariantResult(String plugin, ELEM element, IBacktranslationService translatorSequence, E invariant) {
		super(element, plugin, translatorSequence);
		this.m_Invariant = invariant;
	}

	public E getInvariant() {
		return m_Invariant;
	}

	@Override
	public String getShortDescription() {
		return "Loop Invariant";
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getLongDescription() {
		StringBuffer sb = new StringBuffer();
		sb.append("Derived loop invariant: ");
		sb.append(mTranslatorSequence.translateExpressionToString(m_Invariant, (Class<E>) m_Invariant.getClass()));
		return sb.toString();
	}
}
