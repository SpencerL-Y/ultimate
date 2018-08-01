/*
 * TODO: Copyright.
 */

package de.uni_freiburg.informatik.ultimate.mso;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.logic.Term;

/*
 * TODO: Comment.
 * Questions:
 * 1) How to deal with empty symbol?
 */
public class MoNatDiffAlphabetSymbol {
	private Map<Term, Boolean> mMap;
	
	/*
	 * TODO: Comment.
	 */
	public MoNatDiffAlphabetSymbol(Term term, int value) {
		mMap = new HashMap<Term, Boolean>();
		add(term, value);
	}
	
	/*
	 * TODO: Comment.
	 */
	public MoNatDiffAlphabetSymbol(Term term1, Term term2, int value1, int value2) {
		mMap = new HashMap<Term, Boolean>();
		add(term1, value1);
		add(term2, value2);
	}

	/*
	 * TODO: Comment.
	 */
	public MoNatDiffAlphabetSymbol(Term[] terms, int[] values) {
		if (terms.length != values.length)
			throw new InvalidParameterException("Different lengths of terms and values.");

		mMap = new HashMap<Term, Boolean>();
		for (int i = 0; i < terms.length; i++)
			add(terms[i], values[i]);
	}
	
	/*
	 * TODO: Comment.
	 */
	public void add(Term term, int value) {
		if (!MoNatDiffUtils.isVariable(term))
			throw new InvalidParameterException("Term must be a variable of sort Int or SetOfInt.");
		
		if (value < 0 || value > 1)
			throw new InvalidParameterException("Value must be either 0 or 1.");
		
		mMap.put(term, value != 0);
	}

	/*
	 * TODO: Comment.
	 */
	public String toString() {
		String str = new String("(");
		
		Iterator<Map.Entry<Term, Boolean>> it = mMap.entrySet().iterator();
		while (it.hasNext()) {
		    Map.Entry<Term, Boolean> entry = it.next();
		    str += entry.getKey().toString() + "=" + (entry.getValue() ? "1" : "0");
		    str += it.hasNext() ? ", " : "";
		}
		str += ")";
		
		return str;
	}
}