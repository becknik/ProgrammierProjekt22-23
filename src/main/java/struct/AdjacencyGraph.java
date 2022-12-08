package struct;

import java.io.PrintStream;
import java.util.Arrays;
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
	public void addEdgeAndCalculateDistance (final int edgeId, final int source, final int target) {
		sources[edgeId] = source;
		targets[edgeId] = target;

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
		// Adds distance value for this edge
		distances[edgeId] = this.calculateDistanceOf(source, target);
	}

	/**
	 * calculates the distance between 2 nodes
	 *
	 * @param source
	 * @param target
	 * @return - the distance between source and target as double value
	 */
	private double calculateDistanceOf (final int source, final int target) {
		double sourceLon, sourceLat, targetLon, targetLat, distance;

		sourceLon = longitudes[source];
		sourceLat = latitudes[source];
		targetLon = longitudes[target];
		targetLat = latitudes[target];

		distance = Math.sqrt(Math.pow((sourceLon - targetLon), 2) + Math.pow((sourceLat - targetLat), 2));

		return distance;
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
			if (o instanceof DijkstraNode dijkstraNode) {
				if (this.distance - dijkstraNode.distance > 0) {
					return 1;
				} else if (this.distance - dijkstraNode.distance == 0) {
					return 0;
				} else {
					return -1;
				}

			} else {
				throw new IllegalArgumentException("Compare to only for instances of DijkstraNode!");
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

		while (!priorityQ.isEmpty()) {

			DijkstraNode currentDijkstraNode = priorityQ.poll();
			int currentNodeId = currentDijkstraNode.nodeId;
			closedNodesDistances[currentNodeId] = currentDijkstraNode.distance;

			// Nodes are saved as arrays of node and edge Ids
			int[] adjacentNodes = this.getOutgoingTargetNodes(currentNodeId);
			int[] adjacentEdges = this.getOutgoingEdgesOf(currentNodeId);

			// Add neighbour nodes (called n) to priorityQ & skip nodes which are already included in
			// closedNodesDistances
			// TODO nodes which are already included in closedNodesDistances are not skipped so far
			for (int n = 0; n < adjacentNodes.length; n++) {
				final int nodeN = adjacentNodes[n];

				double oldDistanceToNodeN = openNodesDistances[adjacentNodes[n]];
				double updatedDistanceToNodeN = currentDijkstraNode.distance + distances[adjacentEdges[n]];

				// Updates distance for n in array & adds it to priorityQ
				if (oldDistanceToNodeN == -1) {
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
			// Sort PrioNodes - check this
		}
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
