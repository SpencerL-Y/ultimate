package de.uni_freiburg.informatik.ultimate.pea2boogie.testgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.lib.pea.BoogieBooleanExpressionDecision;
import de.uni_freiburg.informatik.ultimate.lib.pea.BooleanDecision;
import de.uni_freiburg.informatik.ultimate.lib.pea.CDD;
import de.uni_freiburg.informatik.ultimate.lib.pea.CounterTrace.DCPhase;
import de.uni_freiburg.informatik.ultimate.lib.pea.Decision;
import de.uni_freiburg.informatik.ultimate.lib.pea.EventDecision;
import de.uni_freiburg.informatik.ultimate.lib.pea.RangeDecision;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;

public class Req2CauseTrackingCDD {

	private final ILogger mLogger;
	private final Map<String, String> mTrackingVars;
	private final int CONST_MIN_DELAY = 1;

	public Req2CauseTrackingCDD(final ILogger logger) {
		mLogger = logger;
		mTrackingVars = new HashMap<>();
	}

	public CDD transformInvariant(final CDD cdd, final Set<String> effectVars, final Set<String> inputVars,
			final boolean isEffectPhase) {
		final Set<String> vars = getCddVariables(cdd);
		vars.removeAll(inputVars);
		if (isEffectPhase) {
			vars.removeAll(effectVars);
		}
		final CDD newGuard = addTrackingGuards(cdd, vars);
		return newGuard;
	}

	public CDD transformGurad(final CDD cdd, final Set<String> effectVars, final Set<String> inputVars,
			final boolean isEffectEdge) {
		final Set<String> vars = getCddVariables(cdd);
		vars.removeAll(inputVars);
		if (isEffectEdge) {
			vars.removeAll(effectVars);
		}
		// TODO remove primed effect variables in a nicer way
		vars.removeAll(effectVars.stream().map(var -> var + "'").collect(Collectors.toSet()));
		vars.removeAll(inputVars.stream().map(var -> var + "'").collect(Collectors.toSet()));
		final CDD newGuard = addTrackingGuards(cdd, vars);
		final CDD wClocks = transformGuardClock(newGuard, isEffectEdge);
		return wClocks;
	}

	private CDD addTrackingGuards(CDD cdd, final Set<String> trackedVars) {
		if (cdd == CDD.TRUE) {
			return cdd;
		}
		if (cdd == CDD.FALSE) {
			return cdd;
		}

		final List<CDD> newChildren = new ArrayList<>();
		if (cdd.getChilds() != null) {
			for (final CDD child : cdd.getChilds()) {
				newChildren.add(addTrackingGuards(child, trackedVars));
			}
		}

		cdd = CDD.create(cdd.getDecision(), newChildren.toArray(new CDD[newChildren.size()]));
		for (final String v : getVarsFromDecision(cdd.getDecision())) {
			if (trackedVars.contains(v)) {
				final String varName = "u_" + v;
				// TODO more elegant way to check if its a primed var
				if (!v.endsWith("'")) {
					mTrackingVars.put(varName, "bool");
				}
				final CDD trackGurad = CDD.create(new BooleanDecision(varName), CDD.TRUE_CHILDS);
				cdd = trackGurad.and(cdd);
			}
		}
		return cdd;
	}


	public CDD transformClockInvariant(CDD cdd, boolean effectState) {
		if (cdd == CDD.TRUE) {
			return cdd;
		}
		if (cdd == CDD.FALSE) {
			return cdd;
		}
		if (!effectState) {
			return cdd;
		}

		final List<CDD> newChildren = new ArrayList<>();
		if (cdd.getChilds() != null) {
			for (final CDD child : cdd.getChilds()) {
				newChildren.add(transformClockInvariant(child, effectState));
			}
		}
		final CDD[] children = newChildren.toArray(new CDD[newChildren.size()]);
		final Decision<?> decision = cdd.getDecision();
		if (decision instanceof RangeDecision) {
			final RangeDecision d = (RangeDecision) decision;
			transformClockDecisionInvariant(d, children);
		}
		return CDD.create(cdd.getDecision(), children);
	}

	private CDD transformClockDecisionInvariant(RangeDecision d, CDD[] children) {
		CDD returnDecision = CDD.TRUE;
		for (int i = 0; i < children.length; i++) {
			if (children[i] == CDD.FALSE) {
				continue;
			}
			returnDecision = returnDecision.and(transformPrefixClockDecisionInvariant(d, i));
		}
		return returnDecision;
	}

	private CDD transformPrefixClockDecisionInvariant(RangeDecision d, int trueChild) {
		switch (d.getOp(trueChild)) {
		//TODO care about <>_{<= x} E things only, rest of clocks in peas are already ok
		default:
			return RangeDecision.create(d.getVar(), d.getOp(trueChild) , d.getVal(trueChild));
		}
	}

	public CDD transformGuardClock(final CDD cdd, boolean effectEdge) {
		if (cdd == CDD.TRUE) {
			return cdd;
		}
		if (cdd == CDD.FALSE) {
			return cdd;
		}
		if (!effectEdge) {
			return cdd;
		}

		final List<CDD> newChildren = new ArrayList<>();
		if (cdd.getChilds() != null) {
			for (final CDD child : cdd.getChilds()) {
				newChildren.add(transformClockInvariant(child, effectEdge));
			}
		}
		final CDD[] children = newChildren.toArray(new CDD[newChildren.size()]);
		final Decision<?> decision = cdd.getDecision();
		if (decision instanceof RangeDecision) {
			final RangeDecision d = (RangeDecision) decision;
			return transformClockDecisionGuard(d, children);
		}
		return CDD.create(cdd.getDecision(), children);
	}

	private CDD transformClockDecisionGuard(final RangeDecision d, final CDD[] children) {
		CDD returnDecision = CDD.TRUE;
		for (int i = 0; i < children.length; i++) {
			if (children[i] == CDD.FALSE) {
				continue;
			}
			returnDecision = returnDecision.and(transformPrefixClockDecisionGuard(d, i));
		}
		return returnDecision;
	}

	private CDD transformPrefixClockDecisionGuard(RangeDecision d, int trueChild) {
		switch (d.getOp(trueChild)) {
		//TODO care about <>_{<= x} E things only, rest of clocks in peas are already ok
		default:
			return RangeDecision.create(d.getVar(), d.getOp(trueChild) , d.getVal(trueChild));
		}
	}

	public static Set<String> getAllVariables(PatternType pattern,  Map<String, Integer> id2bounds){
		final DCPhase[] tc = pattern.constructCounterTrace(id2bounds).getPhases();
		// find max phase and second max phase, compare
		final Set<String> variables = new HashSet<>();
		for (final DCPhase p : tc) {
			variables.addAll(getCddVariables(p.getInvariant()));
		}
		return variables;
	}

	public static CDD getEffectCDD(final PatternType pattern) {
		final List<CDD> cdds = pattern.getCdds();
		// lets just assume that the effect of the requirement is always mentioned at the end of the pattern (i.e. last
		// CDD)
		// e.g. it is always the case that if _condition_ then _effect_ holds for at least 5 (scope does not matter)
		// TODO: do not rely on this ordering and mark the effect in some way during parsing
		return cdds.get(0);
	}

	public static Set<String> getEffectVariables(final PatternType pattern, final Map<String, Integer> id2bounds) {
		final DCPhase[] tc = pattern.constructCounterTrace(id2bounds).getPhases();
		//find max phase and second max phase, compare
		final CDD finalStateInvar = tc[tc.length - 2].getInvariant();
		if(tc.length >= 3) {
			final CDD beforeStateInvar = tc[tc.length - 3].getInvariant();
			return getDifferences(beforeStateInvar, finalStateInvar);
		}
		return getCddVariables(finalStateInvar);
	}

	private static Set<String> getDifferences(final CDD beforeStateInvar, final CDD finalStateInvar) {
		final Set<String> differences = getCddVariables(finalStateInvar);
		// collect the atomics from both cdds
		final Set<CDD> beforeAtomics = getCDDAtoms(beforeStateInvar);
		for (final CDD cdd : finalStateInvar.toDNF()) {
			final Set<String> localDifferences = new HashSet<>();
			final Set<CDD> afterAtomics = getCDDAtoms(cdd);
			for (final CDD a : afterAtomics) {
				for (final CDD b : beforeAtomics) {
					if (a.isEqual(b)) {
						break;
					}
				}
				localDifferences.addAll(getVarsFromDecision(a.getDecision()));
			}
			differences.retainAll(localDifferences);
		}
		return differences;
	}

	public static Set<CDD> getCDDAtoms(final CDD cdd) {
		final Set<CDD> atomics = new HashSet<>();
		extractAtomics(cdd, atomics);
		return atomics;
	}

	private static void extractAtomics(final CDD cdd, final Set<CDD> atomics) {
		if (cdd == CDD.TRUE) {
			return;
		}
		if (cdd == CDD.FALSE) {
			return;
		}
		if (cdd.isAtomic()) {
			atomics.add(cdd);
			return;
		}

		if (cdd.getChilds() != null) {
			for (final CDD child : cdd.getChilds()) {
				extractAtomics(child, atomics);
			}
		}
	}

	public static Set<String> getCddVariables(final CDD cdd) {
		final Set<String> variables = new HashSet<>();
		extractVars(cdd, variables);
		return variables;
	}

	private static void extractVars(final CDD cdd, final Set<String> variables) {
		if (cdd == CDD.TRUE) {
			return;
		}
		if (cdd == CDD.FALSE) {
			return;
		}

		variables.addAll(getVarsFromDecision(cdd.getDecision()));
		if (cdd.getChilds() != null) {
			for (final CDD child : cdd.getChilds()) {
				extractVars(child, variables);
			}
		}
	}

	private static Set<String> getVarsFromDecision(final Decision<?> dec) {
		final Set<String> variables = new HashSet<>();
		if (dec == null) {
			// may happen for true/false phases
		} else if (dec instanceof EventDecision) {
			// requirements ignore events so far
		} else if (dec instanceof RangeDecision) {
			// range decisions are currently only used for clocks thus we ignore them here
		} else if (dec instanceof BooleanDecision) {
			variables.add(((BooleanDecision) dec).getVar());
		} else if (dec instanceof BoogieBooleanExpressionDecision) {
			final BoogieBooleanExpressionDecision bbedec = (BoogieBooleanExpressionDecision) dec;
			for (final Entry<String, String> entry : bbedec.getVars().entrySet()) {
				variables.add(entry.getKey());
			}
		} else {
			throw new UnsupportedOperationException("Unknown decision type: " + dec.getClass());
		}
		return variables;
	}

	public Map<String, String> getTrackingVars() {
		return mTrackingVars;
	}


}
