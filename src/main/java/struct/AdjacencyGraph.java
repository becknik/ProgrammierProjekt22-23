package struct;

import java.awt.geom.Point2D;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Instances should be created by {@code GraphReader}.
 * Stores all essential data provided by the FMI plain text graph files in huge arrays: node long/ lat, edge source, target &
 * distance. Values of these arrays are internally accessed by IDs, which are equivalent to the arrays indices.
 * Provides operation for running the dijkstra algorithm
 */
public class AdjacencyGraph implements Graph {
	private static final Logger logger = Logger.getLogger(AdjacencyGraph.class.getName());
	public static boolean enableLogging;

	// Node stuff, referenced by node indices
	private final double[] longitudes; // Not private due to QuadTree using it directly :/
	private final double[] latitudes;

	// Edge stuff, referenced by edge indices
	public final int[] sources;
	private final int[] targets;
	private final int[] offset;
	private final int[] distances;
	// Stuff to be written by add operation:
	private int cachedSourceNodeID;


	/**
	 * Object creation by initializing the arrays. TODO: Merge GraphReader in here??
	 *
	 * @param nodeCount The size the node specific arrays will be initialized with
	 * @param edgeCount The size the edge specific arrays will be initialized with
	 */
	public AdjacencyGraph (int nodeCount, int edgeCount) {
		assert nodeCount > 0 && edgeCount > 0;

		this.longitudes = new double[nodeCount];
		this.latitudes = new double[nodeCount];
		this.offset = new int[nodeCount + 1];  // Gotcha!

		this.sources = new int[edgeCount];
		this.targets = new int[edgeCount];
		this.distances = new int[edgeCount];
	}

	/**
	 * Adds a new node to the corresponding arrays by the nodes ID to set its longitude & latitude
	 *
	 * @param nodeId #Number of occurrence in the graph file, starting counting from 0
	 * @param longitude Corresponding longitude
	 * @param latitude  Corresponding latitude
	 */
	public void addNode (final int nodeId, final double longitude, final double latitude) {
		if (nodeId < 0) throw new IllegalArgumentException("Provided node ID is < 0.");
		else if (this.longitudes.length <= nodeId)
			throw new IllegalArgumentException("Provided nodes ID is higher than the maximum index.");

		longitudes[nodeId] = longitude;
		latitudes[nodeId] = latitude;
	}

	/**
	 * Adds an edge to the sourceId & target ID array, using the source & target node ID and the corresponding distance
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

		sources[edgeId] = sourceId;
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
	 * Returns the outgoing node IDs of a node specified via source node ID alias the latitude/ longitudes corresponding index
	 * Optimization > code style due to the amount of calls by {@code this.dijkstra} for determination of the current nodes surrounding nodes
	 *
	 * @param sourceNodeId - The nodes ID/ index of which the outgoing nodes are requested
	 * @return - The outgoing nodes typed as int[]
	 */
	public int[] getAdjacentNodeIdsFrom (final int sourceNodeId) {
		return Arrays.copyOfRange(targets, offset[sourceNodeId], offset[sourceNodeId + 1]);
	}

	/**
	 * Returns an Array of the indices of form source node outgoing edges.
	 * Optimization > code style due to the amount of calls by {@code this.dijkstra} for determination of the surrounding nodes distances
	 *
	 * @param sourceNodeId the given source node
	 * @return array with the indices of those edges which are outgoing edges from source node
	 */
	public int[] getAdjacentEdgesIdsFrom (final int sourceNodeId) {
		// Initialize an array which holds the edges
		final int[] edgeIndices = new int[offset[sourceNodeId + 1] - offset[sourceNodeId]];

		// Adding the edge IDs which are saved in the offset array as difference between source node ID and source node ID + 1
		for (int i = 0; i < edgeIndices.length; i++) {
			edgeIndices[i] = offset[sourceNodeId] + i;
		}
		return edgeIndices;
	}

	/**
	 * Returns the path (edge IDs) from source to target by iteratively going through the predecessorEdgeIds array,
	 * starting from target node IDs. Uses dequeue to push the edge IDs into the right order
	 *
	 * @param sourceNodeId       The source node of the path
	 * @param targetNodeId       The target node of the path
	 * @param predecessorEdgeIds Contains edge connected with the predecessor node for every node ID (represented as indices)
	 * @return The path as edge IDs from source to target
	 */
	public ArrayDeque<Integer> getPath (final int sourceNodeId, final int targetNodeId, final int[] predecessorEdgeIds) {
		// Defensive programming: done in this.dijkstra and DijkstraRuns getDistanceTo methods

		int currentEdgeId = predecessorEdgeIds[targetNodeId];
		ArrayDeque<Integer> path = new ArrayDeque<>();

		// Building the path by following it in the inverted direction and pushing it to the linked list path
		int currentEdgesSourceNode = this.sources[currentEdgeId];
		while (currentEdgesSourceNode != sourceNodeId) {
			path.push(currentEdgeId);

			currentEdgeId = predecessorEdgeIds[currentEdgesSourceNode];
			currentEdgesSourceNode = this.sources[currentEdgeId];
		}

		// Adds the first edge to the path
		path.push(currentEdgeId);

		return path;
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
	 * Returns the source/ target node coordinates as object from the provided edge ID.
	 *
	 * @param edgeId The edge id the start/target node is returned from
	 * @param target If set to true, the coords of edges target node is returned, else the coords of source node is returned
	 * @return
	 */
	public Point2D.Double getEdgeIdsNode(final int edgeId, final boolean target) {
		int nodeId = (target) ? this.targets[edgeId] : this.sources[edgeId];
		double targetNodesLongitude = this.longitudes[nodeId];
		double targetNodesLatitude = this.latitudes[nodeId];

		return new Point2D.Double(targetNodesLongitude, targetNodesLatitude);
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

	public int getNodeCount () {
		return this.longitudes.length;
	}

	double getLongitudeOf (final int nodeId) {
		return longitudes[nodeId];
	}

	double getLatitudeOf (final int nodeId) {
		return latitudes[nodeId];
	}

	public int getDistanceOf (final int edgeId) {
		return this.distances[edgeId];
	}
}