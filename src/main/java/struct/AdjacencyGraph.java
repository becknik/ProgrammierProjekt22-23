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
	private final int[] sources;    // TODO Maybe delete this?
	private final int[] targets;
	private final int[] offset;
	private final int[] distances;

	// Stuff to be calculated:
	private int cachedSourceNodeID;
	private final int[] distancesToNode; // Moved over here to waste as less time as possible in oneToAllDijkstra :/

	record DijkstraNode(int nodeId, int incrementalDistance) {
		@Override
		public boolean equals (Object obj) {    // TODO Maybe add type checks if efficient?
			return ((int) obj) == this.nodeId;      // This one came straight out of hell.
		}
	}

	// This one is by far a beauty, but 1-2 sec slower :(
	// private final Comparator<DijkstraNode> dijkstraNodeComparator = Comparator.comparingInt(DijkstraNode::incrementalDistance);
	private final Comparator<DijkstraNode> dijkstraNodeComparator = (nodeOne, nodeTwo) -> {
		if (nodeOne.incrementalDistance - nodeTwo.incrementalDistance > 0) {    // TODO check if Integer.compare is faster here
			return 1;
		} else if (nodeOne.incrementalDistance - nodeTwo.incrementalDistance < 0) {
			return -1;
		} else {
			return 0;
		}
	};

	public AdjacencyGraph (int nodeCount, int edgeCount) {
		longitudes = new double[nodeCount];
		latitudes = new double[nodeCount];
		offset = new int[nodeCount + 1];  // Gotcha!

		sources = new int[edgeCount];
		targets = new int[edgeCount];
		distances = new int[edgeCount];

		// Moved from oneToAllDijkstra
		this.distancesToNode = new int[this.longitudes.length];
		Arrays.fill(distancesToNode, -1);
	}

	/**
	 * Simply adds some node by specifying the nodes ID, longitude & latitude
	 *
	 * @param nodeId    - Number of occurrence in the graph file
	 * @param longitude
	 * @param latitude
	 */
	public void addNode (int nodeId, double longitude, double latitude) {
		longitudes[nodeId] = longitude;
		latitudes[nodeId] = latitude;
	}

	/**
	 * Adds an edge to the source & target array and calculates the distance by calling the {@code calculateDistances}
	 * and saving the result to the distances array.
	 *
	 * @param edgeId - The id of the edge (one probably habe more edges)
	 * @param source - The source node the edge is outgoing
	 * @param target - The target node the edge aims to
	 */
	public void addEdge (final int edgeId, final int source, final int target, final int distance) {
		sources[edgeId] = source;
		targets[edgeId] = target;
		distances[edgeId] = distance;

        /* When there is a sequence of nodes without outgoing edges in between the last observed node and the current,
         the value of the offset[last observed node+1] is copied inductively into the offset value gap until offset[current node]
         is set to the calue of the last observed node
        */
		while (cachedSourceNodeID < source) {
			cachedSourceNodeID++;
			offset[cachedSourceNodeID + 1] = offset[cachedSourceNodeID];
		}
		// Offset value of the next row increases
		offset[source + 1]++;
	}

	/**
	 * Returns the lowest latitude & longitude, used by the {@code QuadTree} class to determine the placement of the greatest "tile"
	 *
	 * @return - A quadrupel (longitude highest, latitude, Longitude lowest, latitude)
	 */
	public double[] getOutestCoordinates () {
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
			// TODO Add distance value to outgoing nodes
			out.printf("  %d\t\t|  %f\t|  %f\t| %d\t|  %s%n", i, latitudes[i], longitudes[i], offset[i], Arrays.toString(getOutgoingTargetNodes(i)));
		}
	}

	/**
	 * Returns the outgoing nodes of a node specified via node ID alias the latitude/ longitudes corresponding index
	 *
	 * @param node - The nodes ID/ index of which the outgoing nodes are requested
	 * @return - The outgoing nodes typed as int[]
	 */
	private int[] getOutgoingTargetNodes (final int node) {
		int startOutgoingNodeID = offset[node];
		int exclusiveOutgoingNodeID = offset[node + 1];
		return Arrays.copyOfRange(targets, startOutgoingNodeID, exclusiveOutgoingNodeID);
	}

	/**
	 * Returns an Array of the indexes for the outgoing edges from sourceNode.
	 *
	 * @param sourceNode the given source node
	 * @return array with the indexes of those edges which are outgoing edges from sourceNode
	 */
	private int[] getOutgoingEdgesOf (final int sourceNode) {
		int startOutgoingNodeID = offset[sourceNode];
		int exclusiveOutgoingNodeID = offset[sourceNode + 1];
		int indexRange = exclusiveOutgoingNodeID - startOutgoingNodeID;

		int[] indizes = new int[indexRange];

		for (int i = 0; i < indexRange; i++) {
			indizes[i] = startOutgoingNodeID + i;
		}
		return indizes;
	}

	/**
	 * TODO
	 * TODO Refactor
	 *
	 * @param nodeIds
	 * @return
	 */
	public Optional<LinkedList<Integer>> dijkstra (final int... nodeIds) {
		/*
		 Initialization of source node and target node, offensive programming & determination, if oneToOne should be executed
		 */
		if (nodeIds.length == 0 || nodeIds.length > 2)
			throw new IllegalArgumentException("0 or more than 2 nodes are specified as arguments.");

		final int sourceNodeId = nodeIds[0];
		if (sourceNodeId < 0 || longitudes.length <= sourceNodeId)
			throw new IllegalArgumentException("Source node is negative or greater than the maximal node ID.");

		final boolean oneToOneDijkstra = nodeIds.length == 2;
		final int targetNodeId = (oneToOneDijkstra) ? nodeIds[1] : 0;

		if (oneToOneDijkstra && targetNodeId < 0 || longitudes.length <= targetNodeId)
			throw new IllegalArgumentException("Target node is negative or greater than the maximum node ID");

		final int[] predecessorNodes = (oneToOneDijkstra) ? new int[this.longitudes.length] : null;
		int currentsPredecessor = -1;   // TODO Maybe remove this


		/*
		This is where the fun beginns!
		 */
		// TODO: Implement a priority queue which works with primitive ints (?) :(
		PriorityQueue<DijkstraNode> priorityQ = new PriorityQueue<>(73, this.dijkstraNodeComparator);
		// The favourite number of Sheldon Cooper for the win of our programming project! (Testing says better than 42, 69, 127 & 420!)

		// Initialization of whole array with -1 values was moved to constructor for performance reasons
		this.distancesToNode[sourceNodeId] = 0;

		// Setup for first loop iteration
		priorityQ.add(new DijkstraNode(sourceNodeId, 0));

		// Declaring variable for the current watched nodes and its adjacent nodes, obviously for performance reasons :|
		DijkstraNode currentDijkstraNode;
		int[] currentsAdjacentNodes;
		int[] currentsAdjacentEdges;

		while (!priorityQ.isEmpty()) {
			currentDijkstraNode = priorityQ.poll();

			// TODO See second TODO
			// Sets the value of current node in predecessorNodes array to the last closed nodes id & then sets the value of currentsPredecessor
			// to its own ID for the next closed node
			/*if (oneToOneDijkstra) {
				predecessorNodes[currentDijkstraNode.nodeId] = currentsPredecessor;
				currentsPredecessor = currentDijkstraNode.nodeId;
			}*/

			if (oneToOneDijkstra && currentDijkstraNode.nodeId == targetNodeId)
				return this.getPath(sourceNodeId, targetNodeId, predecessorNodes);

			// Nodes are saved as arrays of node and edge Ids
			currentsAdjacentNodes = this.getOutgoingTargetNodes(currentDijkstraNode.nodeId);
			currentsAdjacentEdges = this.getOutgoingEdgesOf(currentDijkstraNode.nodeId);

			// Adding adjacent nodes of current (called N for Neighbour) to priorityQ
			//for (int n = 0; n < currentsAdjacentNodes.length; n++) {  // We tried to do HotSpots work right here
			int n = 0;
			while (n < currentsAdjacentNodes.length) {
				int nodeN = currentsAdjacentNodes[n]; // Neighbour node to the current node

				final int oldDistanceToNodeN = this.distancesToNode[nodeN];
				final int updatedDistanceToNodeN =
						currentDijkstraNode.incrementalDistance + distances[currentsAdjacentEdges[n]];

				// If the current path to node N(eighbour) has a better distance than the previous one || node N is an open node, update||set it in priorityQ & array
				if (oldDistanceToNodeN == -1 || updatedDistanceToNodeN < oldDistanceToNodeN) {
					this.distancesToNode[nodeN] = updatedDistanceToNodeN;

					if (oldDistanceToNodeN != -1)
						priorityQ.remove(nodeN);    // This works due to overwritten equals method in the record which explodes if the parameter is something else than int

					// n++ is here due to the optimization stuff HotSpot should actually do D:
					if (oneToOneDijkstra)
						predecessorNodes[currentsAdjacentNodes[n++]] = currentDijkstraNode.nodeId;  // TODO This might be wrong, right?

					priorityQ.add(new DijkstraNode(nodeN, updatedDistanceToNodeN));
				}
				n++;    // May HotSpot be with you!
			}
		}
		return Optional.empty();
	}

	/**
	 * TODO
	 *
	 * @param sourceNodeID
	 * @param targetNodeID
	 * @param predecessors
	 * @return
	 */
	private Optional<LinkedList<Integer>> getPath (int sourceNodeID, int targetNodeID, int[] predecessors) {
		int pathIteratorBuffer = targetNodeID;
		LinkedList<Integer> path = new LinkedList<>();  // TODO Find more efficient type for this

		// Building the path by following it in the inverted direction and pushing it to the linked list path
		while (pathIteratorBuffer != sourceNodeID) {
			path.push(pathIteratorBuffer);
			pathIteratorBuffer = predecessors[pathIteratorBuffer];

		}
		path.push(sourceNodeID);    // Adds the source node to the path

		return Optional.of(path);
	}


	public int getNodeCount () {
		return this.longitudes.length;
	}

	public double getLongitudeOf (int index) {
		return longitudes[index];
	}

	public double getLatitudeOf (int index) {
		return latitudes[index];
	}
}