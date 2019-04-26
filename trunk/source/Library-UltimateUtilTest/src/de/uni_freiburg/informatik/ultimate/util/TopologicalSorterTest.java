/*
 * Copyright (C) 2019 Claus Schätlze (schaetzc@tf.uni-freiburg.de)
 * Copyright (C) 2019 University of Freiburg
 *
 * This file is part of the ULTIMATE Util Library.
 *
 * The ULTIMATE Util Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ULTIMATE Util Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Util Library. If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Util Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE Util Library grant you additional permission
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import de.uni_freiburg.informatik.ultimate.util.datastructures.poset.TopologicalSorter;

public class TopologicalSorterTest {

	// TODO Use existing graph structures instead of this class. Write String to Graph parser based on this class.
	public static class Graph {

		private final Map<String, Set<String>> mSuccRel = new LinkedHashMap<>();

		/** Constructs an empty graph. */
		public Graph() {
			// nothing to do
		}

		/**
		 * Constructs a graph from a a list of nodes and edges.
		 * @param nodesAndEdges  Nodes of the form "word" and edges of the form "word1→word2" separated by whitespace
		 */
		public Graph(final String nodesAndEdges) {
			Arrays.stream(entries(nodesAndEdges)).forEach(this::addNodeOrEdge);
		}

		private void addNodeOrEdge(final String nodeOrEdge) {
			final String[] operands = nodeOrEdge.split("→");
			if (operands.length == 1) {
				addNode(operands[0]);
			} else if (operands.length == 2) {
				addEdge(operands[0], operands[1]);
			} else {
				throw new IllegalArgumentException("Cannot parse entry: " + nodeOrEdge);
			}
		}

		private void addNode(final String node) {
			successors(node);
		}

		private void addEdge(final String source, final String sink) {
			successors(source).add(sink);
			successors(sink);
		}

		/**
		 * Retrieves the successors of a given node. The node is created if it did not already exist.
		 * @param node Any node from the graph or a new one to be created
		 * @return Successors of the node
		 */
		public Set<String> successors(final String node) {
			return mSuccRel.computeIfAbsent(node, newNode -> new LinkedHashSet<>());
		}

		public Set<String> nodes() {
			return mSuccRel.keySet();
		}
	}

	@Test
	public void empty(){
		assertTopSortEqualsAny("", "");
	}

	@Test
	public void disconnected(){
		assertTopSortEqualsAny("a→b x→y", "a b x y", "a x b y", "x y a b");
	}

	@Test
	public void cycle(){
		assertUnsortable("a→a");
		assertUnsortable("a→b b→c c→d d→a");
		assertUnsortable("a→b a→end b→c c→end c→d d→a");
	}


	@Test
	public void twoNodesPermutations(){
		final String expected = "source sink";
		assertTopSortEqualsAny("source→sink", expected);
		assertTopSortEqualsAny("sink source→sink", expected);
	}

	@Test
	public void cherryPermutations(){
		for (String nodePermutation : new String[]{"a b c", "b a c", "c a b", "a c b", "b c a", "c b a"}) {
			assertTopSortEqualsAny(nodePermutation + " a→b a→c", "a b c", "a c b");
		}
	}

	@Test
	public void generated() {
		assertTopSort("o→x p→z c→e g→z g→v d→z b→h n→o f→o d→j x→z h→m l→t d→y f→p v→y r→t "
				+ "o→u a→v f→w f→k q→s e→h g→i h→q m→y l→v x→y k→t a→f o→w e→q a→m");
		// Same graph permuted
		assertTopSort("e→q a→m d→j x→z o→x p→z c→e g→z g→v o→u a→v f→w f→k q→s e→h g→i d→z "
				+ "h→m b→h n→o f→o h→q m→y l→v x→y k→t l→t d→y f→p v→y r→t a→f o→w");
	}

	private static final String GENERATED_2 = "16 27 29 14→20 11→30 7→6 12→6 2→19 5→6 9→12 15→4 "
			+ "17→7 13→12 21→9 5→18 20→1 12→7 24→14 4→24 4→28 19→25 23→14 22→8 25→5 8→6 15→25 "
			+ "3→2 2→15 24→21 28→26 18→1 9→10 10→12 1→10 20→28 24→1 15→26 18→12 1→17 8→25 22→6";

	@Test
	public void generated2() {
		assertTopSort(GENERATED_2);
		assertUnsortable("28→14 " + GENERATED_2);
		assertUnsortable(GENERATED_2 + " 17→5");
		// Same graph permuted and re-labeled
		assertTopSort("e→l Xa o→Xb m s→d w d→c j→a p→a Xd g→w g→j l→w c→x t→x f→z t→z b→Xd r Xc→y i→z p→n "
				+ "b→d u→a y→u n→b a→k b→x y→p c→h j→w f→i z→l Xd→f q n→h x→i p→Xa x→e k→t v→z k→w z→w h→Xa ");
	}

	@Test
	public void testCheckingMechanismPositive() {
		checkTopOrder("a b c", "a→b a→c");
		checkTopOrder("dolor amet", "dolor→amet");
		checkTopOrder("dolor amet", "amet dolor→amet");
		checkTopOrder("", "");
		checkTopOrder("I_am_the_one_and_only", "I_am_the_one_and_only");
		checkTopOrder("⚳ ⚴", "⚳ ⚴");
		checkTopOrder("⚴ ⚳", "⚳ ⚴");
	}

	@Test(expected = AssertionError.class)
	public void testCheckingMechanismMissingNode() {
		checkTopOrder("a b","a→b a→c");
	}

	@Test(expected = AssertionError.class)
	public void testCheckingMechanismUnknownNode() {
		checkTopOrder("a b c d", "a→b a→c");
	}

	@Test(expected = AssertionError.class)
	public void testCheckingMechanismDuplicate() {
		checkTopOrder("a b c a", "a→b a→c");
	}

	@Test(expected = AssertionError.class)
	public void testCheckingMechanismUnsorted() {
		checkTopOrder("c a b", "a→b a→c");
	}
	
	@Test(expected = AssertionError.class)
	public void testCheckingMechanismUnsorted2() {
		checkTopOrder("a d b c", "a→b b→c c→d");
	}

	@Test(expected = AssertionError.class)
	public void testCheckingMechanismUnsortable() {
		assertUnsortable("a→b");
	}


	private static void assertTopSort(final String inputGraph) {
		final Graph input = new Graph(inputGraph);
		checkTopOrder(topSort(input), input);
	}

	private static void assertUnsortable(final String inputGraph) {
		final List<String> actual = topSort(inputGraph);
		if (actual != null) {
			Assert.fail("Expected graph to be cyclic but got result " + actual);
		}
	}

	private static void assertTopSortEqualsAny(final String inputGraph, final String... expectedTopOrders) {
		final List<String> actual = topSort(inputGraph);
		for (final String expected : expectedTopOrders) {
			if (Arrays.asList(entries(expected)).equals(actual)) {
				return;
			}
		}
		Assert.fail("Result did not match any expected order: " + actual);
	}

	private static List<String> topSort(final String input) {
		return topSort(new Graph(input));
	}

	private static List<String> topSort(final Graph input) {
		final TopologicalSorter<String, ?> sorter = TopologicalSorter.create(input::successors);
		return sorter.topologicalOrdering(input.nodes());
	}

	private static String[] entries(final String whitespaceSeparatedEntries) {
		if (whitespaceSeparatedEntries.isEmpty()) {
			return new String[]{};
		}
		return whitespaceSeparatedEntries.trim().split("\\s+");
	}

	private static void checkTopOrder(final String actualTopOrder, final String graph) {
		checkTopOrder(Arrays.asList(entries(actualTopOrder)), new Graph(graph));
	}

	private static void checkTopOrder(final List<String> actualTopOrder, final Graph graph) {
		Assert.assertNotNull(actualTopOrder);
		checkCompleteness(actualTopOrder, graph);
		checkOrdering(actualTopOrder, graph);
	}

	private static void checkCompleteness(final List<String> actualTopOrder, final Graph graph) {
		final Set<String> actualNodes = new HashSet<>(actualTopOrder);
		final int duplicates = actualTopOrder.size() - actualNodes.size();
		if (duplicates > 0) {
			Assert.fail(String.format("Result had %d duplicate(s): %s", duplicates, actualTopOrder));
		}
		if (!actualNodes.equals(graph.nodes())) {
			Assert.fail("Result listed unknown nodes or not all nodes from the graph:" + actualTopOrder);
		}
	}

	private static void checkOrdering(final List<String> actualTopOrder, final Graph graph) {
		for (final String source : graph.nodes()) {
			for (final String target : graph.successors(source)) {
				if (actualTopOrder.indexOf(source) >= actualTopOrder.indexOf(target)) {
					Assert.fail(String.format("Dependency %s→%s violated by result %s",
							source, target, actualTopOrder));
				}
			}
		}
	}
}
