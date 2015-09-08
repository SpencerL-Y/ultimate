/*
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE GUIGeneratedPreferencePages plug-in.
 * 
 * The ULTIMATE GUIGeneratedPreferencePages plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE GUIGeneratedPreferencePages plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE GUIGeneratedPreferencePages plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE GUIGeneratedPreferencePages plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE GUIGeneratedPreferencePages plug-in grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.gui.preferencepages;

import org.eclipse.jface.preference.PreferenceNode;

public class UltimatePreferenceNode extends PreferenceNode {

	
	private UltimateGeneratedPreferencePage mCachedPage;
	
	public UltimatePreferenceNode(String id, UltimateGeneratedPreferencePage preferencePage) {
		super(id, preferencePage);
		mCachedPage = preferencePage;
		setPage(mCachedPage);
	}

	@Override
	public void createPage() {
		UltimateGeneratedPreferencePage p = mCachedPage.copy();
		setPage(p);
	}
	
	@Override
	public String getLabelText() {
		return mCachedPage.getTitle();
	}
	
	
	
	

}
