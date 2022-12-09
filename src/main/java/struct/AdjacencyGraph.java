package struct;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.logging.Logger;

public class AdjacencyGraph implements Graph {
	private static final Logger logger = Logger.getLogger(AdjacencyGraph.class.getName());
	public static boolean enableLogging;

	// Node stuff, referenced by node indices
	final double[] longitudes; // Not private due to QuadTree using it directly :/
	final double[] latitudes;

	// Edge stuff, referenced by edge indices
	private final int[] sources;
	private final int[] targets;
	private final int[] offset;
	// Stuff to be calculated:
	private final double[] distances;
	private int cachedSourceNodeID;
	private double[] distanceOneToAll;

	public AdjacencyGraph (int nodeCount, int edgeCount) {
		longitudes = new double[nodeCount];
		latitudes = new double[nodeCount];
		offset = new int[nodeCount + 1];  // Gotcha!

		sources = new int[edgeCount];
		targets = new int[edgeCount];
		distances = new double[edgeCount];

		distanceOneToAll = new double[nodeCount];
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
	public void addEdge (final int edgeId, final int source, final int target, final double distance) {
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

	public record DijkstraNode(int nodeId, double distance) implements Comparable {
		@Override
		public int compareTo (Object o) {
			if (o.getClass() != DijkstraNode.class) {
				throw new IllegalArgumentException("Compare to only for instances of DijkstraNode!");
			}
			DijkstraNode dijkstraNode = (DijkstraNode) o;
			if (this.distance - dijkstraNode.distance > 0) {
				return 1;
			} else if (this.distance - dijkstraNode.distance == 0) {
				return 0;
			} else {
				return -1;
			}
		}
	}

	/**
	 * calculates the oneToAllDijkstra
	 *
	 * @param fromNodeId the source node for the dijkstra algorithm
	 */
	public void oneToAllDijkstra (int fromNodeId) {
		PriorityQueue<DijkstraNode> priorityQ = new PriorityQueue<>();

		// Nodes which are already viewed at least one time & shortest path is not known so far
		double[] openNodesDistances = new double[this.longitudes.length];
		// Nodes of which the shortest path is know already and therefore should not be viewed again
		double[] closedNodesDistances = new double[this.longitudes.length];
		Arrays.fill(openNodesDistances, -1);
		Arrays.fill(closedNodesDistances, -1);

		// Setup for first loop iteration BANANA
		openNodesDistances[fromNodeId] = 0;
		priorityQ.add(new DijkstraNode(fromNodeId, 0));

		int[] adjacentNodes;
		int[] adjacentEdges;

		int currentNodeId;
		double oldDistanceToNodeN;
		double updatedDistanceToNodeN;

		while (!priorityQ.isEmpty()) {

			DijkstraNode currentDijkstraNode = priorityQ.poll();
			currentNodeId = currentDijkstraNode.nodeId;
			closedNodesDistances[currentNodeId] = currentDijkstraNode.distance;

			// Nodes are saved as arrays of node and edge Ids
			adjacentNodes = this.getOutgoingTargetNodes(currentNodeId);
			adjacentEdges = this.getOutgoingEdgesOf(currentNodeId);

			// Add neighbour nodes (called n) to priorityQ & skip nodes which are already included in
			// closedNodesDistances
			for (int n = 0; n < adjacentNodes.length; n++) {
				final int nodeN = adjacentNodes[n];

				oldDistanceToNodeN = openNodesDistances[adjacentNodes[n]];
				updatedDistanceToNodeN = currentDijkstraNode.distance + distances[adjacentEdges[n]];

				//skipping nodes which are already included in closedNodesDistances
				if (closedNodesDistances[nodeN] != -1) {
					continue;
				}
				// Updates distance for n in array & adds it to priorityQ
				else if (oldDistanceToNodeN == -1) {
					openNodesDistances[adjacentNodes[n]] = updatedDistanceToNodeN;
					priorityQ.add(new DijkstraNode(adjacentNodes[n], updatedDistanceToNodeN));
				}
				// If the current path in the graph has a better distance than the previous, update it in Q & array
				else if (updatedDistanceToNodeN < oldDistanceToNodeN) {
					openNodesDistances[adjacentNodes[n]] = updatedDistanceToNodeN;

					priorityQ.removeIf(openNode -> openNode.nodeId == nodeN);
					priorityQ.add(new DijkstraNode(adjacentNodes[n], updatedDistanceToNodeN));
				}
			}
		}
	}

	/**
	 * calculates the one to one dijkstra algorithm and returns the shortest path from sourceNodeID to targetNodeID
	 *
	 * @param sourceNodeID the start node for the dijkstra algorithm
	 * @param targetNodeID the target node for the dijkstra algorithm
	 * @return shortest path from sourceNodeID to targetNodeID as int[]
	 */
	public int[] oneToOneDijkstra (int sourceNodeID, int targetNodeID) {

		if (sourceNodeID < 0 || sourceNodeID >= longitudes.length) {
			throw new RuntimeException("sourceNode is not included in the graph!");
		}
		if (targetNodeID < 0 || targetNodeID >= longitudes.length) {
			throw new RuntimeException("targetNode is not included in the graph!");
		}

		PriorityQueue<DijkstraNode> priorityQ = new PriorityQueue<>();

		// Nodes which are already viewed at least one time & shortest path is not known so far
		double[] openNodesDistances = new double[this.longitudes.length];
		// Nodes of which the shortest path is know already and therefore should not be viewed again
		double[] closedNodesDistances = new double[this.longitudes.length];
		Arrays.fill(openNodesDistances, -1);
		Arrays.fill(closedNodesDistances, -1);

		int[] predecessors = new int[this.longitudes.length];
		Arrays.fill(predecessors, -1);    //TODO Remove this if the algorithm seems to work properly
		int currentNodesPredecessor = sourceNodeID;

		// Setup for first loop iteration
		openNodesDistances[sourceNodeID] = 0;
		priorityQ.add(new DijkstraNode(sourceNodeID, 0));

		while (!priorityQ.isEmpty()) {

			DijkstraNode currentDijkstraNode = priorityQ.poll();
			int currentNodeId = currentDijkstraNode.nodeId;
			closedNodesDistances[currentNodeId] = currentDijkstraNode.distance;

			if (currentNodeId == targetNodeID) {
				return getPath(sourceNodeID, targetNodeID, predecessors);
			}

			// Nodes are saved as arrays of node and edge Ids
			int[] adjacentNodes = this.getOutgoingTargetNodes(currentNodeId);
			int[] adjacentEdges = this.getOutgoingEdgesOf(currentNodeId);

			// Add neighbour nodes (called n) to priorityQ & skip nodes which are already included in
			// closedNodesDistances
			for (int n = 0; n < adjacentNodes.length; n++) {
				final int nodeN = adjacentNodes[n];

				double oldDistanceToNodeN = openNodesDistances[adjacentNodes[n]];
				double updatedDistanceToNodeN = currentDijkstraNode.distance + distances[adjacentEdges[n]];

				//skipping nodes which are already included in closedNodesDistances
				if (closedNodesDistances[nodeN] != -1) {
					continue;
				}
				// Updates distance for n in array & adds it to priorityQ
				else if (oldDistanceToNodeN == -1) {
					openNodesDistances[adjacentNodes[n]] = updatedDistanceToNodeN;
					priorityQ.add(new DijkstraNode(adjacentNodes[n], updatedDistanceToNodeN));

					predecessors[adjacentNodes[n]] = currentNodeId;
				}
				// If the current path in the graph has a better distance than the previous, update it in Q & array
				else if (updatedDistanceToNodeN < oldDistanceToNodeN) {
					openNodesDistances[adjacentNodes[n]] = updatedDistanceToNodeN;

					priorityQ.removeIf(openNode -> openNode.nodeId == nodeN);
					priorityQ.add(new DijkstraNode(adjacentNodes[n], updatedDistanceToNodeN));

					predecessors[adjacentNodes[n]] = currentNodeId;
				}
			}
		}
		throw new RuntimeException("You're so dumb, IntelliJ throws dumb user exception! (There exists no such path " +
				"in the graph!)");
	}

	private static int[] getPath (int sourceNodeID, int targetNodeID, int[] predecessors) {
		// get path from source to target via predecessors[]
		int pathIteratorBuffer = targetNodeID;
		LinkedList<Integer> path = new LinkedList<>();

		while (pathIteratorBuffer != sourceNodeID) {
			path.push(pathIteratorBuffer);
			pathIteratorBuffer = predecessors[pathIteratorBuffer];

		}
		path.push(sourceNodeID);    // Adds the source node to the path

		int[] pathAsArray = new int[path.size()];
		for (int i = 0; i < pathAsArray.length; i++) {
			pathAsArray[i] = path.get(i);
		}
		return pathAsArray;
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

	public double getDistanceOf (int index) {
		return distances[index];
	}
}
