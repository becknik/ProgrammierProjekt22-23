package dijkstra;

import struct.AdjacencyGraph;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class DijkstraAlgorithm {

	// I now understand why they call java dynamically & bad playing with arrays.
	// Using int Arrays instead of this increases runtime from 8 -> 21 sec. Factor 2.5! (how is this even possible?)

	/**
	 * Holds the incremental, greedily set distance value to a specified node ID in the {@code dijkstra} operation &
	 * sets it in relation to the nodes ID. Used for picking the best node available from a priorityQ using a local comparator instance
	 *
	 * @param nodeId              Node ID
	 * @param incrementalDistance The steadily lowering distance value, which is updated by dereffering to the outdated record instance
	 */
	private record DijkstraNode(int nodeId, int incrementalDistance) {
		@Override
		public boolean equals (Object obj) {
			assert obj.getClass() == Integer.class;

			return ((int) obj) == this.nodeId;      // This one came straight out of hell.
		}
	}

	/**
	 * The method unites the calculation of the one to all and one to one dijkstra by making use of a vararg to allow multiple parameters.
	 * The default with one parameter is one to all dijkstra.
	 * This implementation uses the value -1 for nodes which were not inspected by the algorithm so far (=in WHITE),
	 * OPEN nodes are set if the path form the current node is better than the already set one & CLOSED nodes are not
	 * monitored due to the nature of the algorithm, that the distance can't get any better for them.
	 *
	 * @param nodeIds First node is the source node, second one is target. Second one can be neglected for on to all dijkstra execution
	 * @return {@code DijkstraResult} wrapper object, which holds information about the {@code AdjacencyGraph}, if 2All or 2One was executed, the predecessors array & the starting node/ path
	 *
	 * @throws IllegalArgumentException {@code this.dijkstraDefensiveProgrammingChecks}
	 */
	public static DijkstraResult dijkstra (final AdjacencyGraph adjacencyGraph, final int... nodeIds) {
		DijkstraAlgorithm.dijkstraDefensiveProgrammingChecks(adjacencyGraph, nodeIds);

		// Initialization of source node and target node & determination, if oneToOne should be executed
		final int sourceNodeId = nodeIds[0];
		final boolean oneToOneDijkstra = nodeIds.length == 2;
		final int targetNodeId = (oneToOneDijkstra) ? nodeIds[1] : -1;

		final int[] predecessorEdges = new int[adjacencyGraph.getNodeCount()];

		/*
		This is where the fun begins!
		 */

		// This one is by far a beauty, but 1-2 sec slower :(
		// private final Comparator<DijkstraNode> dijkstraNodeComparator = Comparator.comparingInt(DijkstraNode::incrementalDistance);
		final Comparator<DijkstraNode> dijkstraNodeComparator = (nodeOne, nodeTwo) -> {
			if (nodeOne.incrementalDistance - nodeTwo.incrementalDistance > 0) {    // This is faster than Integer.compare()!
				return 1;
			} else if (nodeOne.incrementalDistance - nodeTwo.incrementalDistance < 0) {
				return -1;
			} else {
				return 0;
			}
		};

		// TODO: Implement a priority queue which works with primitive ints (?)
		PriorityQueue<DijkstraNode> priorityQ = new PriorityQueue<>(73, dijkstraNodeComparator);
		// The favourite number of Sheldon Cooper for the win of our programming project! (Testing says better than 42, 69, 127 & 420!)

		// Must be initialized here to allow multiple runs of this method
		// Initializes all nodes as WHITE with distance -1, start node gets the distance 0
		final int[] dijkstraDistancesToSource = new int[adjacencyGraph.getNodeCount()];
		Arrays.fill(dijkstraDistancesToSource, -1);
		dijkstraDistancesToSource[sourceNodeId] = 0;

		// Setup for first loop iteration
		priorityQ.add(new DijkstraNode(sourceNodeId, 0));

		// Declaring variable for the current watched nodes and its adjacent nodes, obviously for performance reasons :|
		DijkstraNode currentDijkstraNode;
		int[] currentsAdjacentNodes;
		int[] currentsAdjacentEdges;

		while (!priorityQ.isEmpty()) {
			currentDijkstraNode = priorityQ.poll();

			if (oneToOneDijkstra && currentDijkstraNode.nodeId == targetNodeId) {
				ArrayDeque<Integer> path = adjacencyGraph.getPath(sourceNodeId, targetNodeId, predecessorEdges);
				return new DijkstraResult(adjacencyGraph, predecessorEdges, path);
			}

			// Adjacent neighbour nodes & edges are saved as arrays of node and edge Ids
			currentsAdjacentNodes = adjacencyGraph.getAdjacentNodeIdsFrom(currentDijkstraNode.nodeId);
			currentsAdjacentEdges = adjacencyGraph.getAdjacentEdgesIdsFrom(currentDijkstraNode.nodeId);

			// Adding adjacent nodes of current (called N for Neighbour) greedily to priorityQ
			for (int n = 0; n < currentsAdjacentNodes.length; n++) {
				int nodeN = currentsAdjacentNodes[n];

				final int oldDistanceToNodeN = dijkstraDistancesToSource[nodeN];
				final int updatedDistanceToNodeN = currentDijkstraNode.incrementalDistance + adjacencyGraph.getDistanceOf(currentsAdjacentEdges[n]);

				// If the current path to node N(eighbour) has a better distance than the previous one || node N is an open node =>
				// (update||set its distance (&predecessor) in priorityQ & array)
				if (oldDistanceToNodeN == -1 || updatedDistanceToNodeN < oldDistanceToNodeN) {
					dijkstraDistancesToSource[nodeN] = updatedDistanceToNodeN;

					predecessorEdges[nodeN] = currentsAdjacentEdges[n];

					// This works due to overwritten equals method in the record which explodes if the parameter is something else than int
					if (oldDistanceToNodeN != -1) priorityQ.remove(nodeN);

					priorityQ.add(new DijkstraNode(nodeN, updatedDistanceToNodeN));
				}
			}
		}
		return new DijkstraResult(adjacencyGraph, predecessorEdges, sourceNodeId);
	}

	/**
	 * Performs defensive checks on vararg parameters & values of the provided node IDs
	 *
	 * @param nodeIds The node ID vararg to be verified
	 * @throws IllegalArgumentException On check failure
	 */
	private static void dijkstraDefensiveProgrammingChecks (final AdjacencyGraph adjacencyGraph, final int... nodeIds ) {
		// Check for invalid amount of parameters
		if (nodeIds.length == 0 || 2 < nodeIds.length)
			throw new IllegalArgumentException("0 or more than 2 nodes are specified as arguments.");

		// Check for right integer range for source node
		if (nodeIds[0] < 0 || adjacencyGraph.getNodeCount() <= nodeIds[0])
			throw new IllegalArgumentException("Source node is negative or greater than the maximal node ID.");

		if (nodeIds.length == 2) {
			// Check for oneToOne dijkstra & target node integer range
			if (nodeIds[1] < 0 || adjacencyGraph.getNodeCount() <= nodeIds[1])
				throw new IllegalArgumentException("Target node is negative or greater than the maximum node ID");

			if (nodeIds[0] == nodeIds[1])
				throw new IllegalArgumentException("Target node ID is the same as source node ID");
		}
	}
}
