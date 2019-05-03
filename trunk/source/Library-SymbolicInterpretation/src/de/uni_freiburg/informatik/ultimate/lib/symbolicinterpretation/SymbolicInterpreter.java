/*
 * Copyright (C) 2019 Claus Schätzle (schaetzc@tf.uni-freiburg.de)
 * Copyright (C) 2019 University of Freiburg
 *
 * This file is part of the ULTIMATE Library-SymbolicInterpretation plug-in.
 *
 * The ULTIMATE Library-SymbolicInterpretation plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ULTIMATE Library-SymbolicInterpretation plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Library-SymbolicInterpretation plug-in. If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Library-SymbolicInterpretation plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE Library-SymbolicInterpretation plug-in grant you additional permission
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.lib.symbolicinterpretation;

import java.util.Collection;

import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.IIcfg;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.IcfgLocation;

/**
 * Annotates given program locations with predicates over-approximating the actually reachable concrete states at that
 * locations.
 *
 * @author Claus Schätzle (schaetzc@tf.uni-freiburg.de)
 */
public class SymbolicInterpreter {

	private final IIcfg<IcfgLocation> mIcfg;
	private final Collection<IcfgLocation> mLocationsOfInterest;
	private final CallGraph mCallGraph;

	public SymbolicInterpreter(final IIcfg<IcfgLocation> icfg, final Collection<IcfgLocation> locationsOfInterest) {
		mIcfg = icfg;
		mLocationsOfInterest = locationsOfInterest;
		mCallGraph = new CallGraph(icfg, locationsOfInterest);
	}
	
	public void interpret() {
		for (final String procedure : mCallGraph.initialProceduresOfInterest()) {
			// TODO compute procedure graph with enter calls for ...
			// (could require method callGraph.callsToBeEntered() with return type Collection<IIcfgCallTransition>)
			mCallGraph.successorsOfInterest(procedure);

			// TODO Compute path expressions for
			mCallGraph.locationsOfInterest(procedure);
			mCallGraph.successorsOfInterest(procedure);

			// TODO compress path expressions into RegexDag

			// TODO Interpret RegexDag
			// First search backward which nodes to interpret
			// Then interpret found nodes
			mCallGraph.locationsOfInterest(procedure);
			mCallGraph.successorsOfInterest(procedure);
		}
	}
}
