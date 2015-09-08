/*
 * Copyright (C) 2012-2015 Oday Jubran
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE SMTSolverBridge.
 * 
 * The ULTIMATE SMTSolverBridge is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE SMTSolverBridge is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE SMTSolverBridge. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE SMTSolverBridge, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE SMTSolverBridge grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.smtsolver.external;

import java_cup.runtime.Symbol;
import java_cup.runtime.SymbolFactory;

public class MySymbolFactory implements SymbolFactory {
	class LineColumnSymbol extends Symbol {
		private final String name;
		private final int lcolumn;
		private final int rcolumn;
 
 		public LineColumnSymbol(String name, int id, int state) {
 			super(id, state);
 			this.name = name;
 			this.lcolumn = -1;
 			this.rcolumn = -1;
 		}
 		
 		public LineColumnSymbol(String name, int id, 
	            int left, int lcolumn, int right, int rcolumn, 
	            Object o) {
 			super(id, left, right, o);
 			this.name = name;
 			this.lcolumn = lcolumn;
 			this.rcolumn = rcolumn;
 		}
		
		public LineColumnSymbol(String name, int id, Symbol left, Symbol right, Object o) {
			super(id, left, right, o);
			this.name = name;
			if (left instanceof LineColumnSymbol)
				lcolumn = ((LineColumnSymbol) left).lcolumn;
			else
				lcolumn = 0;
			if (right instanceof LineColumnSymbol)
				rcolumn = ((LineColumnSymbol) left).rcolumn;
			else
				rcolumn = 0;
		}
		
		public String getLocation() {
			if (lcolumn >= 0)
				return ""+left+":"+lcolumn;
			else
				return ""+left;
		}

		public String getName() {
			return name;
		}
		
		public String toString() {
			return "("+name+" "+left+":"+lcolumn+"-"+right+":"+rcolumn+")";
		}
	}
	
    // Factory methods
    public Symbol newSymbol(String name, int id, int lline, int lcol, int rline, int rcol, Object value){
        return new LineColumnSymbol(name,id,lline,lcol,rline,rcol,value);
    }
    public Symbol newSymbol(String name, int id, int lline, int lcol, int rline, int rcol){
        return new LineColumnSymbol(name,id,lline,lcol,rline,rcol, null);
    }
    public Symbol newSymbol(String name, int id, Symbol left, Symbol right, Object value){
        return new LineColumnSymbol(name,id,left,right,value);
    }
    public Symbol newSymbol(String name, int id, Symbol left, Symbol right){
        return new LineColumnSymbol(name,id,left,right,null);
    }
    public Symbol newSymbol(String name, int id){
        return new LineColumnSymbol(name,id,-1,-1,-1,-1,null);
    }
    public Symbol newSymbol(String name, int id, Object value){
        return new LineColumnSymbol(name,id,-1,-1,-1,-1,value);
    }
    public Symbol startSymbol(String name, int id, int state){
        LineColumnSymbol s = new LineColumnSymbol(name,id, state);
        return s;
    }
}
