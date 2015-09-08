/*
 * Copyright (C) 2010-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2009-2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE Automata Library.
 * 
 * The ULTIMATE Automata Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE Automata Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Automata Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Automata Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Automata Library grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.automata.petrinet.visualization;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.automata.petrinet.IPetriNet;
import de.uni_freiburg.informatik.ultimate.automata.petrinet.ITransition;
import de.uni_freiburg.informatik.ultimate.automata.petrinet.Marking;
import de.uni_freiburg.informatik.ultimate.automata.petrinet.Place;
import de.uni_freiburg.informatik.ultimate.automata.petrinet.julian.Transition;

public class PetriNetToUltimateModel<S, C> {
	
	@SuppressWarnings("unchecked")
	public PetriNetInitialNode getUltimateModelOfPetriNet(IPetriNet<S, C> net) {
		Collection<Collection<Place<S, C>>> acceptingMarkings = 
			net.getAcceptingMarkings();
		PetriNetInitialNode graphroot = 
			new PetriNetInitialNode(printAcceptingMarkings(acceptingMarkings));
		
		Marking<S,C> initialStates = net.getInitialMarking();

		
		Map<Place<S, C>,PlaceNode> place2placeNode =
			new HashMap<Place<S, C>,PlaceNode>();
		Map<ITransition<S, C>,TransitionNode> transition2transitionNode =
			new HashMap<ITransition<S, C>,TransitionNode>();

		LinkedList<Object> queue = new LinkedList<Object>();
	
		// add all initial states to model - all are successors of the graphroot
		for (Place<S, C> place : initialStates) {
			queue.add(place);
			PlaceNode placeNode = new PlaceNode(place,
					participatedAcceptingMarkings(place, acceptingMarkings));
			place2placeNode.put(place,placeNode);
			graphroot.connectOutgoing(placeNode);
		}
		
		while (!queue.isEmpty()) {
			Object node = queue.removeFirst();
			
			if (node instanceof Place) {
				Place<S,C> place = (Place<S,C>) node;
				PlaceNode placeNode = place2placeNode.get(place);
				for (ITransition<S, C> transition : place.getSuccessors()) {
					TransitionNode transNode = 
						transition2transitionNode.get(transition);
					if (transNode == null) {
						transNode = new TransitionNode((Transition) transition);
						transition2transitionNode.put(transition, transNode);
						queue.add(transition);
					}
					placeNode.connectOutgoing(transNode);
				}
			}
			else if (node instanceof ITransition) {
				ITransition<S,C> transition = (ITransition<S,C>) node;
				TransitionNode transitionNode = 
					transition2transitionNode.get(transition);
				for (Place<S, C> place : transition.getSuccessors()) {
					PlaceNode placeNode = place2placeNode.get(place);
					if (placeNode == null) {
						
						placeNode = new PlaceNode(place,
										participatedAcceptingMarkings(place,
															acceptingMarkings));
						place2placeNode.put(place, placeNode);
						queue.add(place);
					}
					transitionNode.connectOutgoing(placeNode);
				}
			}
		}
		return graphroot;
	}
	
	
	private Collection<String> participatedAcceptingMarkings(Place<S,C> place,
					Collection<Collection<Place<S, C>>> acceptingMarkings) {
		LinkedList<String> participatedAcceptingMarkings = 
													new LinkedList<String>();
		for (Collection<Place<S, C>> acceptingMarking : acceptingMarkings) {
			if (acceptingMarking.contains(place)) {
				String acceptingMarkingString = "{ ";
				for (Place<S,C> placeInMarking : acceptingMarking) {
					acceptingMarkingString += 
										placeInMarking.getContent().toString();
					acceptingMarkingString += " , ";
				}
				acceptingMarkingString = acceptingMarkingString.substring(0,
											acceptingMarkingString.length()-3);
				acceptingMarkingString += "}";
				participatedAcceptingMarkings.add(acceptingMarkingString);
			}
		}
		return participatedAcceptingMarkings;
	}
	
	private Collection<String> printAcceptingMarkings(
			Collection<Collection<Place<S, C>>> acceptingMarkings) {
		LinkedList<String> acceptingMarkingsList = new LinkedList<String>();
		for (Collection<Place<S, C>> acceptingMarking : acceptingMarkings) {
			if (acceptingMarking.isEmpty()) {
				acceptingMarkingsList.add("{ }");
			}
			else {
				String acceptingMarkingString = "{ ";
				for (Place<S,C> placeInMarking : acceptingMarking) {
					acceptingMarkingString += 
										placeInMarking.getContent().toString();
					acceptingMarkingString += " , ";
				}
				acceptingMarkingString = acceptingMarkingString.substring(0, 
											acceptingMarkingString.length()-3);
				acceptingMarkingString += "}";
				acceptingMarkingsList.add(acceptingMarkingString);
			}
		}
		return acceptingMarkingsList;
}

}
