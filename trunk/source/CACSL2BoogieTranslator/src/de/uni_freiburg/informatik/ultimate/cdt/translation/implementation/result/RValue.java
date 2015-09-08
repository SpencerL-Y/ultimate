/*
 * Copyright (C) 2013-2015 Alexander Nutz (nutz@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE CACSL2BoogieTranslator plug-in.
 * 
 * The ULTIMATE CACSL2BoogieTranslator plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE CACSL2BoogieTranslator plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE CACSL2BoogieTranslator plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE CACSL2BoogieTranslator plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE CACSL2BoogieTranslator plug-in grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.result;

import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.container.c.CType;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Expression;

public class RValue extends LRValue {

	public Expression value;
	
	/**
	 * The Value in a ResultExpression that may only be used on the 
	 * right-hand side of an assignment, i.e. its corresponding
	 * memory cell may only be read.
	 * @param value
	 */
	public RValue(Expression value, CType cType) {
		this(value, cType, false);
//		this(value, cType, false, false, false);
	}
	
	/**
	 * The Value in a ResultExpression that may only be used on the 
	 * right-hand side of an assignment, i.e. its corresponding
	 * memory cell may only be read.
	 * @param value
	 */
	public RValue(Expression value, CType cType, boolean boogieBool) {
		this(value, cType, boogieBool, false);
//	public RValue(Expression value, CType cType, boolean wrappedBool, boolean isPointer, boolean isOnHeap) {
//		this.value = value;
//		this.cType = cType;
//		this.isBoogieBool = boogieBool;
		//this.isPointer = isPointer;
	}
	
	public RValue(RValue rval) {
		this(rval.value, rval.getCType(), rval.isBoogieBool(), rval.isIntFromPointer());
//		this(rval.value, rval.cType, rval.isWrappedBool, rval.isPointer, rval.isOnHeap);
	}

	public RValue(Expression value, CType cType,
			boolean isBoogieBool, boolean isIntFromPointer) {
		super(cType, isBoogieBool, isIntFromPointer);
		this.value = value;
	}

	public Expression getValue() {
		return this.value;
	}
}
