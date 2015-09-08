/*
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2013-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
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



/**
 * Object for all results for which ULTIMATE does not offer a special result class.
 * @author heizmann@informatik.uni-freiburg.de
 *
 */
public class GenericResult extends AbstractResult implements IResultWithSeverity {

	private final String m_ShortDescription;
	private final String m_LongDescription;
	private final Severity m_Severity;

	public GenericResult(String plugin, String shortDescription, 
			String longDescription,	Severity severity) {
		super(plugin);
		m_ShortDescription = shortDescription;
		m_LongDescription = longDescription;
		m_Severity = severity;
	}

	@Override
	public String getShortDescription() {
		return m_ShortDescription;
	}

	@Override
	public String getLongDescription() {
		return m_LongDescription;
	}

	@Override
	public Severity getSeverity() {
		return m_Severity;
	}

}
