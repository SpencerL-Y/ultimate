/*
 * Copyright (C) 2013-2015 Betim Musa (musab@informatik.uni-freiburg.de)
 * Copyright (C) 2014-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE AutomataScriptParser plug-in.
 * 
 * The ULTIMATE AutomataScriptParser plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE AutomataScriptParser plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE AutomataScriptParser plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE AutomataScriptParser plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE AutomataScriptParser plug-in grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.plugins.source.automatascriptparser.AST;


import de.uni_freiburg.informatik.ultimate.model.location.ILocation;
import de.uni_freiburg.informatik.ultimate.plugins.source.automatascriptparser.AtsASTNode;

/**
 * @author musab@informatik.uni-freiburg.de
 */
public class ConstantExpressionAST extends AtsASTNode {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9065975410268575852L;
	private Object value;
	
	public ConstantExpressionAST(ILocation loc, Integer val) {
		super(loc);
		setType(Integer.class);
		value = val;
	}
	
	public ConstantExpressionAST(ILocation loc, String val) {
		super(loc);
		setType(String.class);
		this.value = val;
	}
	
	public ConstantExpressionAST(ILocation loc, boolean val) {
		super(loc);
		setType(Boolean.class);
		value = val;
	}
	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "ConstantExpression [Value : " + value + "]";
	}

	@Override
	public String getAsString() {
		if (value instanceof String) {
			return "\"" + value.toString() + "\"";
		}
		return value.toString();
	}
	
	

}
