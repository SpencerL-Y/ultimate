/*
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
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
package de.uni_freiburg.informatik.ultimate.plugins.generator.cacsl2boogietranslator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.model.acsl.ACSLNode;
import de.uni_freiburg.informatik.ultimate.model.acsl.LTLPrettyPrinter;
import de.uni_freiburg.informatik.ultimate.model.acsl.ast.ACSLTransformer;
import de.uni_freiburg.informatik.ultimate.model.acsl.ast.ACSLVisitor;
import de.uni_freiburg.informatik.ultimate.model.acsl.ast.BinaryExpression;
import de.uni_freiburg.informatik.ultimate.model.acsl.ast.BinaryExpression.Operator;
import de.uni_freiburg.informatik.ultimate.model.acsl.ast.BooleanLiteral;
import de.uni_freiburg.informatik.ultimate.model.acsl.ast.Expression;
import de.uni_freiburg.informatik.ultimate.model.acsl.ast.IdentifierExpression;
import de.uni_freiburg.informatik.ultimate.model.acsl.ast.IntegerLiteral;
import de.uni_freiburg.informatik.ultimate.model.acsl.ast.RealLiteral;
import de.uni_freiburg.informatik.ultimate.model.acsl.ast.UnaryExpression;

/**
 * @author dietsch@informatik.uni-freiburg.de
 */
public class LTLExpressionExtractor {

	private String mLTLFormatString;
	private LinkedHashMap<String, Expression> mMap;

	// alphabet without X, U, F, G
	private static final String sAlpha = "ABCDEHIJKLMNOPQRSTVWYZ";

	/**
	 * @return true iff ACSLNode is a GlobalLTLInvariant and everything is done,
	 *         false otherwise
	 */
	public boolean run(ACSLNode node) {
		LTLPrettyPrinter printer = new LTLPrettyPrinter();
		mMap = null;
		node = removeWeakUntil(node);
		LTLExtractSubexpressions visitor = new LTLExtractSubexpressions();
		node.accept(visitor);

		mLTLFormatString = printer.print(node);

		if (visitor.getResult() != null) {
			// consolidate expression list, replace format string
			mMap = new LinkedHashMap<>();
			mLTLFormatString = new LTLFormatStringPrinter(new HashSet<>(visitor.getResult()), mMap).print(node);
			return true;
		}
		return false;
	}

	public ACSLNode removeWeakUntil(ACSLNode node) {
		return node.accept(new LTLReplaceWeakUntil());
	}

	public Map<String, Expression> getAP2SubExpressionMap() {
		return mMap;
	}

	public String getLTLFormatString() {
		return mLTLFormatString;
	}

	public static String getAPSymbol(int i) {
		if (i < sAlpha.length()) {
			return String.valueOf(sAlpha.charAt(i));
		}

		String rtr = "A";
		int idx = i;
		while (idx > sAlpha.length()) {
			idx = idx - sAlpha.length();
			rtr += String.valueOf(sAlpha.charAt(idx % sAlpha.length()));
		}
		return rtr;
	}

	private class LTLReplaceWeakUntil extends ACSLTransformer {

		@Override
		public BinaryExpression transform(BinaryExpression node) {

			if (node.getOperator().equals(Operator.LTLWEAKUNTIL)) {
				// a WU b == (a U b) || (G a)
				Expression left = node.getLeft().accept(this);
				Expression right = node.getRight().accept(this);

				BinaryExpression until = new BinaryExpression(Operator.LTLUNTIL, left, right);
				UnaryExpression globally = new UnaryExpression(UnaryExpression.Operator.LTLGLOBALLY, left);
				BinaryExpression or = new BinaryExpression(Operator.LOGICOR, until, globally);

				addAdditionalInfo(node, until);
				addAdditionalInfo(node, globally);
				addAdditionalInfo(node, or);

				return or;
			}

			return super.transform(node);
		}

		private void addAdditionalInfo(BinaryExpression node, Expression expr) {
			expr.setEndingLineNumber(node.getEndingLineNumber());
			expr.setStartingLineNumber(node.getStartingLineNumber());
			expr.setFileName(node.getFileName());
			expr.setType(node.getType());
		}

	}

	private class LTLFormatStringPrinter extends LTLPrettyPrinter {

		private final Map<String, Expression> mApString2Expr;
		private final Set<Expression> mSubExpressions;
		private final Map<String, String> mExprString2APString;
		private int mAPCounter;

		/**
		 * 
		 * @param subExpressions
		 *            Set of subexpressions that should be replaced by AP
		 * @param apString2Expr
		 *            Map that will be filled with a mapping from atomic
		 *            proposition symbols to actual expressions
		 */
		public LTLFormatStringPrinter(Set<Expression> subExpressions, Map<String, Expression> apString2Expr) {
			super();
			mApString2Expr = apString2Expr;
			mSubExpressions = subExpressions;
			mAPCounter = 0;
			mExprString2APString = new HashMap<String, String>();
		}

		@Override
		public boolean visit(Expression node) {
			if (mSubExpressions.contains(node)) {
				String symbol;
				String nodeString = new LTLPrettyPrinter().print(node);
				symbol = mExprString2APString.get(nodeString);
				if (symbol == null) {
					// we dont have a symbol for this AP yet, so we need a new
					// one.
					symbol = getAPSymbol(mAPCounter);
					mApString2Expr.put(symbol, node);
					mExprString2APString.put(nodeString, symbol);
					mAPCounter++;
				}
				mBuilder.append(symbol);
				return false;
			}
			return true;
		}
	}

	private class LTLExtractSubexpressions extends ACSLVisitor {

		private Expression mCurrentSubExpression;
		private HashSet<Expression> mExpressions;

		private LTLExtractSubexpressions() {
			mCurrentSubExpression = null;
			mExpressions = new HashSet<>();
		}

		public Collection<Expression> getResult() {
			return mExpressions;
		}

		@Override
		public boolean visit(BinaryExpression node) {
			switch (node.getOperator()) {
			case LTLUNTIL:
			case LTLWEAKUNTIL:
			case LTLRELEASE:
				mCurrentSubExpression = null;
				return super.visit(node);
			default:
				if (mCurrentSubExpression == null) {
					LTLExtractSubexpressions left = new LTLExtractSubexpressions();
					LTLExtractSubexpressions right = new LTLExtractSubexpressions();
					node.getLeft().accept(left);
					node.getRight().accept(right);

					if (left.getResult().isEmpty() && right.getResult().isEmpty()) {
						mCurrentSubExpression = node;
					} else {
						boolean allOfLeft = false;
						boolean allOfRight = false;
						HashSet<Expression> results = new HashSet<>();
						for (Expression expr : left.getResult()) {
							results.add(expr);
							if (expr == node.getLeft()) {
								allOfLeft = true;
							}
						}
						for (Expression expr : right.getResult()) {
							results.add(expr);
							if (expr == node.getRight()) {
								allOfRight = true;
							}
						}
						if ((allOfRight || right.getResult().isEmpty()) && (allOfLeft || left.getResult().isEmpty())) {
							mCurrentSubExpression = node;
						} else {
							mExpressions.addAll(results);
							return false;
						}
					}
				}
				return super.visit(node);
			}
		}

		@Override
		public boolean visit(UnaryExpression node) {
			switch (node.getOperator()) {
			case LTLFINALLY:
			case LTLGLOBALLY:
			case LTLNEXT:
				mCurrentSubExpression = null;
				return super.visit(node);
			default:
				if (mCurrentSubExpression == null) {
					LTLExtractSubexpressions right = new LTLExtractSubexpressions();
					node.getExpr().accept(right);

					if (right.getResult().isEmpty()) {
						mCurrentSubExpression = node;
					} else {
						for (Expression expr : right.getResult()) {
							if (expr == node.getExpr()) {
								mCurrentSubExpression = node;
							}
						}
						if (mCurrentSubExpression == null) {
							mExpressions.addAll(right.getResult());
							return false;
						}
					}
				}
				return super.visit(node);
			}
		}

		@Override
		public boolean visit(BooleanLiteral node) {
			literalReached();
			return super.visit(node);
		}

		@Override
		public boolean visit(IdentifierExpression node) {
			literalReached();
			return super.visit(node);
		}

		@Override
		public boolean visit(IntegerLiteral node) {
			literalReached();
			return super.visit(node);
		}

		@Override
		public boolean visit(RealLiteral node) {
			literalReached();
			return super.visit(node);
		}

		private void literalReached() {
			if (mCurrentSubExpression != null && mExpressions != null) {
				mExpressions.add(mCurrentSubExpression);
				mCurrentSubExpression = null;
			}
		}
		
		@Override
		public String toString() {
			if(mExpressions == null || mExpressions.isEmpty()){
				return "EMTPY";
			}
			return mExpressions.toString();
		}
	}
}
