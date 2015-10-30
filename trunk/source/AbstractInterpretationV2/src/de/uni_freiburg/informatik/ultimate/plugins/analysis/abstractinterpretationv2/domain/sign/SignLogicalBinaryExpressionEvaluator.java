/*
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE AbstractInterpretationV2 plug-in.
 * 
 * The ULTIMATE AbstractInterpretationV2 plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE AbstractInterpretationV2 plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE AbstractInterpretationV2 plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE AbstractInterpretationV2 plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE AbstractInterpretationV2 plug-in grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.sign;

import de.uni_freiburg.informatik.ultimate.core.services.model.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.model.boogie.BoogieVar;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.evaluator.IEvaluationResult;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.evaluator.ILogicalEvaluator;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractState;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.sign.SignDomainValue.Values;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;

public class SignLogicalBinaryExpressionEvaluator extends SignBinaryExpressionEvaluator implements
        ILogicalEvaluator<Values, CodeBlock, BoogieVar> {

	public SignLogicalBinaryExpressionEvaluator(IUltimateServiceProvider services) {
		super(services);
	}

	@Override
	public IEvaluationResult<Values> evaluate(IAbstractState<CodeBlock, BoogieVar> currentState) {
		for (String var : mLeftSubEvaluator.getVarIdentifiers()) {
			mVariableSet.add(var);
		}
		for (String var : mRightSubEvaluator.getVarIdentifiers()) {
			mVariableSet.add(var);
		}

		final SignDomainValue firstResult = (SignDomainValue) mLeftSubEvaluator.evaluate(currentState);
		final SignDomainValue secondResult = (SignDomainValue) mRightSubEvaluator.evaluate(currentState);

		switch (mOperator) {
		// case LOGICIFF:
		// break;
		// case LOGICIMPLIES:
		// break;
		// case LOGICAND:
		// break;
		// case LOGICOR:
		// break;
		// case COMPLT:
		// break;
		// case COMPGT:
		// break;
		// case COMPLEQ:
		// break;
		// case COMPGEQ:
		// break;
		case COMPEQ:
		case COMPNEQ:
			// return evaluateComparisonOperators(firstResult, secondResult);
			// case COMPPO:
			// break;
			// case BITVECCONCAT:
			// break;
			// case ARITHMUL:
			// break;
			// case ARITHDIV:
			// break;
			// case ARITHMOD:
			// break;
			// case ARITHPLUS:
			// break;
			// case ARITHMINUS:
			// break;
		default:
			throw new UnsupportedOperationException("The operator " + mOperator.toString() + " is not implemented.");
		}
	}

	@Override
	public IAbstractState<CodeBlock, BoogieVar> logicallyInterpret(IAbstractState<CodeBlock, BoogieVar> currentState) {

		final SignDomainValue firstResult = (SignDomainValue) mLeftSubEvaluator.evaluate(currentState);
		final SignDomainValue secondResult = (SignDomainValue) mRightSubEvaluator.evaluate(currentState);

		if (firstResult.getResult().equals(Values.BOTTOM) || secondResult.getResult().equals(Values.BOTTOM)) {
			SignDomainState<CodeBlock, BoogieVar> newState = (SignDomainState<CodeBlock, BoogieVar>) currentState
			        .copy();
			newState.setToBottom();
			return newState;
		}

		final IAbstractState<CodeBlock, BoogieVar> firstLogicalInterpretation = ((ILogicalEvaluator<Values, CodeBlock, BoogieVar>) mLeftSubEvaluator)
		        .logicallyInterpret(currentState);
		final IAbstractState<CodeBlock, BoogieVar> secondLogicalInterpretation = ((ILogicalEvaluator<Values, CodeBlock, BoogieVar>) mRightSubEvaluator)
		        .logicallyInterpret(currentState);

		SignDomainState<CodeBlock, BoogieVar> newState = (SignDomainState<CodeBlock, BoogieVar>) currentState.copy();

		boolean compResult = evaluateComparisonOperators(firstResult, secondResult);

		switch (mOperator) {
		case LOGICIFF:
		case LOGICIMPLIES:
		case LOGICAND:
		case LOGICOR:
		case COMPLT:
		case COMPGT:
		case COMPLEQ:
		case COMPGEQ:
			throw new UnsupportedOperationException("The operator " + mOperator.toString() + " is not implemented.");
		case COMPNEQ:
			if (firstResult.getResult().equals(Values.TOP) || secondResult.getResult().equals(Values.TOP)) {
				return newState;
			}

			// TODO How to deal with inequalities in the sign domain?
			if (!compResult) {
				newState.setToBottom();
			}

			return newState;
		case COMPEQ:
			if (firstResult.getResult().equals(Values.TOP) || secondResult.getResult().equals(Values.TOP)) {
				return newState;
			}

			if (compResult) {
				// Compute new state, only of the form x == 3 or 3 == x for now.
				if (mLeftSubEvaluator instanceof SignLogicalSingletonVariableExpressionEvaluator
				        && !(mRightSubEvaluator instanceof SignLogicalSingletonVariableExpressionEvaluator)) {
					SignLogicalSingletonVariableExpressionEvaluator leftie = (SignLogicalSingletonVariableExpressionEvaluator) mLeftSubEvaluator;
					SignDomainState<CodeBlock, BoogieVar> intersecterino = (SignDomainState<CodeBlock, BoogieVar>) currentState
					        .copy();
					SignDomainState<CodeBlock, BoogieVar> rightState = (SignDomainState<CodeBlock, BoogieVar>) secondLogicalInterpretation;
					intersecterino.setValue(leftie.mVariableName, rightState.getValues().get(leftie.mVariableName));

					newState = newState.intersect(intersecterino);
				} else if (!(mLeftSubEvaluator instanceof SignLogicalSingletonVariableExpressionEvaluator)
				        && mRightSubEvaluator instanceof SignLogicalSingletonVariableExpressionEvaluator) {
					SignLogicalSingletonVariableExpressionEvaluator rightie = (SignLogicalSingletonVariableExpressionEvaluator) mRightSubEvaluator;
					SignDomainState<CodeBlock, BoogieVar> intersecterino = (SignDomainState<CodeBlock, BoogieVar>) currentState
					        .copy();
					SignDomainState<CodeBlock, BoogieVar> leftState = (SignDomainState<CodeBlock, BoogieVar>) firstLogicalInterpretation;
					intersecterino.setValue(rightie.mVariableName, leftState.getValues().get(rightie.mVariableName));

					newState = newState.intersect(intersecterino);
				}
			}
		case COMPPO:
			// return evaluateLogicalOperator(currentState, firstResult, secondResult);
		case BITVECCONCAT:
		case ARITHMUL:
		case ARITHDIV:
		case ARITHMOD:
		case ARITHPLUS:
		case ARITHMINUS:
			throw new UnsupportedOperationException("The operator " + mOperator.toString() + " is not implemented.");
		default:
			mLogger.warn("Loss of precision while interpreting the logical expression " + this.toString());
			return currentState.copy();
			// throw new UnsupportedOperationException("The operator " + mOperator.toString() + " is not implemented.");
		}
	}

	private boolean evaluateComparisonOperators(SignDomainValue firstResult, SignDomainValue secondResult) {

		switch (mOperator) {
		case COMPLT:
			return evaluateLTComparison(firstResult, secondResult);
		case COMPGT:
			return evaluateGTComparison(firstResult, secondResult);
		case COMPLEQ:
		case COMPGEQ:
		case COMPNEQ:
			return evaluateNEComparison(firstResult, secondResult);
		case COMPEQ:
			return evaluateEQComparison(firstResult, secondResult);
		default:
			throw new UnsupportedOperationException("The operator " + mOperator.toString() + " is not implemented.");
		}
	}

	private boolean evaluateEQComparison(SignDomainValue firstResult, SignDomainValue secondResult) {
		if (firstResult.equals(secondResult)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean evaluateNEComparison(SignDomainValue firstResult, SignDomainValue secondResult) {
		if (firstResult.equals(Values.ZERO) && secondResult.equals(Values.ZERO)) {
			return false;
		}

		return true;
	}

	private boolean evaluateGTComparison(SignDomainValue firstResult, SignDomainValue secondResult) {
		if (firstResult.equals(secondResult) || firstResult.equals(Values.BOTTOM) || secondResult.equals(Values.BOTTOM)
		        || firstResult.equals(Values.TOP) || secondResult.equals(Values.TOP)) {
			return false;
		}

		if (firstResult.equals(Values.POSITIVE) && !secondResult.equals(Values.POSITIVE)) {
			return true;
		}

		if (firstResult.equals(Values.ZERO) && secondResult.equals(Values.NEGATIVE)) {
			return true;
		}

		return false;
	}

	private boolean evaluateLTComparison(SignDomainValue firstResult, SignDomainValue secondResult) {
		if (firstResult.equals(secondResult)) {
			return false;
		}

		if (firstResult.equals(Values.NEGATIVE) && !secondResult.equals(Values.NEGATIVE)) {
			return true;
		}

		if (firstResult.equals(Values.ZERO) && secondResult.equals(Values.POSITIVE)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean logicalEvaluation(IAbstractState<CodeBlock, BoogieVar> currentState) {

		final ILogicalEvaluator<Values, CodeBlock, BoogieVar> left = (ILogicalEvaluator<SignDomainValue.Values, CodeBlock, BoogieVar>) mLeftSubEvaluator;
		final ILogicalEvaluator<Values, CodeBlock, BoogieVar> right = (ILogicalEvaluator<SignDomainValue.Values, CodeBlock, BoogieVar>) mRightSubEvaluator;

		switch (mOperator) {
		case COMPEQ:
			return left.logicalEvaluation(currentState) == right.logicalEvaluation(currentState);
		case COMPNEQ:
			return left.logicalEvaluation(currentState) != right.logicalEvaluation(currentState);
		case LOGICIMPLIES:
			return !left.logicalEvaluation(currentState) || right.logicalEvaluation(currentState);
		case LOGICIFF:
			return (left.logicalEvaluation(currentState) && right.logicalEvaluation(currentState))
			        || (!left.logicalEvaluation(currentState) && !right.logicalEvaluation(currentState));
		case LOGICOR:
			return left.logicalEvaluation(currentState) || right.logicalEvaluation(currentState);
		default:
			// TODO: implement other cases
		}

		// TODO Auto-generated method stub
		return false;
	}
}
