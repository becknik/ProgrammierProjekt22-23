package dijkstra;

import struct.AdjacencyGraph;

import java.util.ArrayDeque;
import java.util.Arrays;

public class DijkstraAlgorithm {

	/**
	 * The method unites the calculation of the one to all and one to one dijkstra by making use of a vararg to allow multiple parameters.
	 * The default with one parameter is one to all dijkstra.
	 * This implementation uses the value -1 for nodes which were not inspected by the algorithm so far (=in WHITE),
	 * OPEN nodes are set if the path form the current node is better than the already set one & CLOSED nodes are not
	 * monitored due to the nature of the algorithm, that the distance can't get any better for them.
	 *
	 * @param nodeIds First node is the source node, second one is target. Second one can be neglected for on to all dijkstra execution
	 * @return {@code DijkstraResult} wrapper object, which holds information about the {@code AdjacencyGraph}, if 2All or 2One was executed, the predecessors array & the starting node/ path
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

		// Using the datatype long to store (int nodeID, int relativeDistance) by unsigned bit shifts & a scalar optimized collection
		DijkstraLongPriorityQueue priorityQLong = new DijkstraLongPriorityQueue(73, (k1, k2) -> {
			// The favourite number of Sheldon Cooper for the win of our programming project! (Testing says better than 42, 69, 127 & 420!)
			// Saving (nodeID, incrementalDistance) in a long primitive -> because of this (int) k1 == relativeDistance(k1)
				if ((int) k1 > (int) k2) {
					return 1;
				} else if ((int) k1 < (int) k2) {
					return -1;
				} else {
					return 0;
				}
			});

		// Initializes all nodes as WHITE with distance -1, start node gets the distance 0
		// Must be initialized here to allow multiple runs of this method
		final int[] dijkstraDistancesToSource = new int[adjacencyGraph.getNodeCount()];
		Arrays.fill(dijkstraDistancesToSource, -1);
		dijkstraDistancesToSource[sourceNodeId] = 0;

		//final boolean[] closed = new boolean[adjacencyGraph.getNodeCount()];
		// Seems to slow down onToAll/ have a low slowdown on oneToOne (?)

		// Setup priorityQ for first loop iteration
		final long shiftedSourceNode = ((long) sourceNodeId)<<32;
		priorityQLong.enqueue(shiftedSourceNode);

		// Declaring variable for the current watched nodes and its adjacent nodes, obviously for performance reasons :|
		long currentDijkstraNodeOnSpeed;
		int[] currentsAdjacentNodes;
		int[] currentsAdjacentEdges;

		while (!priorityQLong.isEmpty()) {
			currentDijkstraNodeOnSpeed = priorityQLong.dequeueLong();
			int currentDijkstraNode = (int) (currentDijkstraNodeOnSpeed>>>32);


			if (oneToOneDijkstra && currentDijkstraNode == targetNodeId) {
				ArrayDeque<Integer> path = adjacencyGraph.getPath(sourceNodeId, targetNodeId, predecessorEdges);
				return new OneToOneResult(adjacencyGraph, path);
			}

			// Adjacent neighbour nodes & edges are saved as arrays of node and edge Ids
			currentsAdjacentNodes = adjacencyGraph.getAdjacentNodeIdsFrom(currentDijkstraNode);
			currentsAdjacentEdges = adjacencyGraph.getAdjacentEdgesIdsFrom(currentDijkstraNode);

			// Adding adjacent nodes of current (called N for Neighbour) greedily to priorityQ
			for (int n = 0; n < currentsAdjacentNodes.length; n++) {
				int nodeN = currentsAdjacentNodes[n];

				final int oldDistanceToNodeN = dijkstraDistancesToSource[nodeN];
				final int updatedDistanceToNodeN = (int) currentDijkstraNodeOnSpeed + adjacencyGraph.getDistanceOf(currentsAdjacentEdges[n]);

				long updatedNodeN =  ((long) nodeN<<32); // nodeID0...(32)...0
				// (((long) updatedDistanceToNodeN) & 0xFFFFFFFF);      // 0...(32)...0updateDistanceToNodeN
				updatedNodeN |= updatedDistanceToNodeN; // nodeIDupdateDistanceToNodeN

				// If the current path to node N(eighbour) has a better distance than the previous one || node N is an open node =>
				// (update||set its distance (&predecessor) in priorityQ & array)
				if (oldDistanceToNodeN == -1 || updatedDistanceToNodeN < oldDistanceToNodeN) {
					dijkstraDistancesToSource[nodeN] = updatedDistanceToNodeN;

					predecessorEdges[nodeN] = currentsAdjacentEdges[n];     // Sets last node to current node, when distance is better

					// This works due to overwritten equals method in the record which explodes if the parameter is something else than int
					if (oldDistanceToNodeN != -1) {
						priorityQLong.removeAndAddWithUpdatedDistance(updatedNodeN);
						priorityQLong.changed();
					} else {
						priorityQLong.enqueue(updatedNodeN);
					}
				}
			}
		}
		return new OneToAllResult(adjacencyGraph, predecessorEdges, sourceNodeId);
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
