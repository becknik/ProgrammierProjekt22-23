package struct;

import java.io.PrintStream;
import java.util.*;
import java.util.logging.Logger;

public class AdjacencyGraph implements Graph {
	private static final Logger logger = Logger.getLogger(AdjacencyGraph.class.getName());
	public static boolean enableLogging;

	// Node stuff, referenced by node indices
	final double[] longitudes; // Not private due to QuadTree using it directly :/
	final double[] latitudes;

	// Edge stuff, referenced by edge indices
	private final int[] targets;
	private final int[] offset;
	private final int[] distances;
	private final int[] dijkstraDistancesToSource; // Moved over here to waste as less time as possible in dijkstra :/
	// Stuff to be written by add & dijkstra operations:
	private int cachedSourceNodeID;

	public AdjacencyGraph (int nodeCount, int edgeCount) {
		assert nodeCount > 0 && edgeCount > 0;

		longitudes = new double[nodeCount];
		latitudes = new double[nodeCount];
		offset = new int[nodeCount + 1];  // Gotcha!

		targets = new int[edgeCount];
		distances = new int[edgeCount];

		// Moved from dijkstra method - oneToAll
		this.dijkstraDistancesToSource = new int[this.longitudes.length];
		Arrays.fill(this.dijkstraDistancesToSource, -1);
	}

	/**
	 * Adds a new node by specifying the nodes ID, longitude & latitude
	 *
	 * @param nodeId    Number of occurrence in the graph file
	 * @param longitude Corresponding longitude
	 * @param latitude  Corresponding latitude
	 */
	public void addNode (final int nodeId, final double longitude, final double latitude) {
		if (nodeId < 0) throw new IllegalArgumentException("Provided node ID is < 0.");
		else if (this.longitudes.length <= nodeId)
			throw new IllegalArgumentException("Provided nodes ID is higher than the count of nodes.");

		longitudes[nodeId] = longitude;
		latitudes[nodeId] = latitude;
	}

	/**
	 * Adds an edge to the sourceId & target ID array, using the sourceId & target ID node and the corresponding distance
	 *
	 * @param edgeId   - The ID of the edge (one probably habe more edges)
	 * @param sourceId - The sourceId node the edge is outgoing
	 * @param targetId - The targetId node the edge aims to
	 * @param distance - The edges distance
	 */
	public void addEdge (final int edgeId, final int sourceId, final int targetId, final int distance) {
		if (edgeId < 0) throw new IllegalArgumentException("Provided edge ID is < 0.");
		else if (this.targets.length <= edgeId)
			throw new IllegalArgumentException("Provided edge ID is higher than the count of edges.");
		else if (this.longitudes.length <= sourceId || this.longitudes.length <= targetId)
			throw new IllegalArgumentException("Node ID for edge is higher than the overall node count.");

		targets[edgeId] = targetId;
		distances[edgeId] = distance;

        /* When there is a sequence of nodes without outgoing edges in between the last observed node and the current,
         the value of the offset[last observed node+1] is copied inductively into the offset value gap until offset[current node]
         is set to the value of the last observed node
        */
		while (cachedSourceNodeID < sourceId) {   // TODO There may be an more efficient realization
			cachedSourceNodeID++;
			offset[cachedSourceNodeID + 1] = offset[cachedSourceNodeID];
		}
		// Offset value of the next row increases
		offset[sourceId + 1]++;
	}

	/**
	 * The method unites the calculation of the one to all and one to one dijkstra by making use of a vararg to allow multiple parameters.
	 * The default with one parameter is one to all dijkstra.
	 * This implementation uses the value -1 for nodes which were not inspected by the algorithm so far (=in WHITE),
	 * OPEN nodes are set if the path form the current node is better than the already set one & CLOSED nodes are not
	 * monitored due to the nature of the algorithm, that the distance can't get any better for them.
	 *
	 * @param nodeIds First node is the source node, second one is target. Second one can be neglected for on to all dijkstra exeecution
	 * @return Optional.empty(), if the one to all is executed, else the path as linked list wrapped in Optional object
	 *
	 * @throws IllegalArgumentException {@code this.dijkstraDefensiveProgrammingChecks}
	 */
	public Optional<Queue<Integer>> dijkstra (final int... nodeIds) {
		this.dijkstraDefensiveProgrammingChecks(nodeIds);

		// Initialization of source node and target node & determination, if oneToOne should be executed
		final int sourceNodeId = nodeIds[0];
		final boolean oneToOneDijkstra = nodeIds.length == 2;
		final int targetNodeId = (oneToOneDijkstra) ? nodeIds[1] : -1;

		final int[] predecessorNodes = (oneToOneDijkstra) ? new int[this.longitudes.length] : null;

		/*
		This is where the fun beginns!
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

		// Initialization of whole array with -1 values was moved to constructor for performance reasons
		this.dijkstraDistancesToSource[sourceNodeId] = 0;

		// Setup for first loop iteration
		priorityQ.add(new DijkstraNode(sourceNodeId, 0));

		// Declaring variable for the current watched nodes and its adjacent nodes, obviously for performance reasons :|
		DijkstraNode currentDijkstraNode;
		int[] currentsAdjacentNodes;
		int[] currentsAdjacentEdges;

		while (!priorityQ.isEmpty()) {
			currentDijkstraNode = priorityQ.poll();

			if (oneToOneDijkstra && currentDijkstraNode.nodeId == targetNodeId)
				return this.getPath(sourceNodeId, targetNodeId, predecessorNodes);

			// Adjacent neighbour nodes & edges are saved as arrays of node and edge Ids
			currentsAdjacentNodes = this.getAdjacentNodeIdsFrom(currentDijkstraNode.nodeId);
			currentsAdjacentEdges = this.getAdjacentEdgesIdsFrom(currentDijkstraNode.nodeId);

			// Adding adjacent nodes of current (called N for Neighbour) greedily to priorityQ
			for (int n = 0; n < currentsAdjacentNodes.length; n++) {
				int nodeN = currentsAdjacentNodes[n];

				final int oldDistanceToNodeN = this.dijkstraDistancesToSource[nodeN];
				final int updatedDistanceToNodeN = currentDijkstraNode.incrementalDistance + distances[currentsAdjacentEdges[n]];

				// If the current path to node N(eighbour) has a better distance than the previous one || node N is an open node =>
				// (update||set its distance (&predecessor) in priorityQ & array)
				if (oldDistanceToNodeN == -1 || updatedDistanceToNodeN < oldDistanceToNodeN) {
					this.dijkstraDistancesToSource[nodeN] = updatedDistanceToNodeN;

					if (oneToOneDijkstra) predecessorNodes[currentsAdjacentNodes[n]] = currentDijkstraNode.nodeId;

					// This works due to overwritten equals method in the record which explodes if the parameter is something else than int
					if (oldDistanceToNodeN != -1) priorityQ.remove(nodeN);

					priorityQ.add(new DijkstraNode(nodeN, updatedDistanceToNodeN));
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Performs defensive checks on vararg parameters & values of the provided node IDs
	 *
	 * @param nodeIds The node ID vararg to be verified
	 * @throws IllegalArgumentException On check failure
	 */
	private void dijkstraDefensiveProgrammingChecks (final int... nodeIds) {
		// Check for invalid amount of parameters
		if (nodeIds.length == 0 || 2 < nodeIds.length)
			throw new IllegalArgumentException("0 or more than 2 nodes are specified as arguments.");

		// Check for right integer range for source node
		if (nodeIds[0] < 0 || longitudes.length <= nodeIds[0])
			throw new IllegalArgumentException("Source node is negative or greater than the maximal node ID.");

		if (nodeIds.length == 2) {
			// Check for oneToOne dijkstra & target node integer range
			if (nodeIds[1] < 0 || longitudes.length <= nodeIds[1])
				throw new IllegalArgumentException("Target node is negative or greater than the maximum node ID");

			if (nodeIds[0] == nodeIds[1])
				throw new IllegalArgumentException("Target node ID is the same as source node ID");
		}
	}

	/**
	 * Returns the outgoing node IDs of a node specified via source node ID alias the latitude/ longitudes corresponding index
	 * Optimization > code style due to the amount of calls by {@code this.dijkstra} for determination of the current nodes surrounding nodes
	 *
	 * @param sourceNodeId - The nodes ID/ index of which the outgoing nodes are requested
	 * @return - The outgoing nodes typed as int[]
	 */
	private int[] getAdjacentNodeIdsFrom (final int sourceNodeId) {
		return Arrays.copyOfRange(targets, offset[sourceNodeId], offset[sourceNodeId + 1]);
	}

	/**
	 * Returns an Array of the indices of form source node outgoing edges.
	 * Optimization > code style due to the amount of calls by {@code this.dijkstra} for determination of the surrounding nodes distances
	 *
	 * @param sourceNodeId the given source node
	 * @return array with the indices of those edges which are outgoing edges from source node
	 */
	private int[] getAdjacentEdgesIdsFrom (final int sourceNodeId) {
		// Initialize an array which holds the edges
		final int[] edgeIndices = new int[offset[sourceNodeId + 1] - offset[sourceNodeId]];

		// Adding the edge IDs which are saved in the offset array as difference between source node ID and source node ID + 1
		for (int i = 0; i < edgeIndices.length; i++) {
			edgeIndices[i] = offset[sourceNodeId] + i;
		}
		return edgeIndices;
	}

	/**
	 * Returns the path from source to target by iteratively going through the predecessors array, starting from target node id.
	 * Uses dequeue to push  the node ids to
	 *
	 * @param sourceNodeID The source node of the path
	 * @param targetNodeID The target node of the path
	 * @param predecessors The array in which for every node id of the path (represented as indices) has a predecessor
	 * @return The path from source to target by using the integer node ids
	 */
	private Optional<Queue<Integer>> getPath (final int sourceNodeID, final int targetNodeID, final int[] predecessors) {
		assert predecessors != null;
		assert sourceNodeID != targetNodeID;

		int pathIteratorBuffer = targetNodeID;
		ArrayDeque<Integer> path = new ArrayDeque<>();

		// Building the path by following it in the inverted direction and pushing it to the linked list path
		while (pathIteratorBuffer != sourceNodeID) {
			path.push(pathIteratorBuffer);
			pathIteratorBuffer = predecessors[pathIteratorBuffer];

		}
		path.push(sourceNodeID);    // Adds the source node to the path

		return Optional.of(path);
	}

	/**
	 * Returns the lowest latitude & longitude, used by the {@code QuadTree} class to determine the placement of the greatest "tile"
	 *
	 * @return - A quadrupel (longitude highest, latitude, Longitude lowest, latitude)
	 */
	double[] getOutestCoordinates () {
		double highestLatitude = 0d, lowestLatitude = 0d, highestLongitude = 0d, lowestLongitude = 0d;

		// Saving the highest & lowest longitude & latitude from the arrays into the initialized variables
		for (double longitude : this.longitudes) {
			if (longitude > highestLongitude) highestLongitude = longitude;
			else if (longitude < lowestLongitude) lowestLongitude = longitude;
		}
		for (double latitude : this.latitudes) {
			if (latitude > highestLatitude) highestLongitude = latitude;
			else if (latitude < lowestLatitude) lowestLongitude = latitude;
		}

		return new double[]{highestLongitude, highestLatitude, lowestLongitude, lowestLatitude};
	}

	/**
	 * Prints out the current objects structure with all interesting values in a formatted tabular
	 *
	 * @param out - PrintStream to be printed to
	 */
	public void printOutStructs (final PrintStream out) {
		out.print(" Node ID/Index:\t| Latitude:\t| Longitude:\t| Offset: \t| Targets: \n");
		for (int i = 0; i < this.longitudes.length; i++) {
			out.printf("  %d\t\t|  %f\t|  %f\t| %d\t|  %s%n", i, latitudes[i], longitudes[i], offset[i], Arrays.toString(getAdjacentNodeIdsFrom(i)));
		}
	}

	int getNodeCount () {
		return this.longitudes.length;
	}

	double getLongitudeOf (int index) {
		return longitudes[index];
	}

	double getLatitudeOf (int index) {
		return latitudes[index];
	}

	// I now understand why they call java dynamically & bad playing with arrays.
	// Using int Arrays instead of this increases runtime from 8 -> 21 sec. Factor 2.5!
	private record DijkstraNode(int nodeId, int incrementalDistance) {
		@Override
		public boolean equals (Object obj) {
			assert obj.getClass() == Integer.class;

			return ((int) obj) == this.nodeId;      // This one came straight out of hell.
		}
	}
}