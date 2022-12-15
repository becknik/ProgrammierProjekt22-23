package struct;

import java.util.Arrays;

/**
 * Stores a sorted array of nodes from the graph (sorted by latitudes).
 * Provides operations to find the closest node located to a given position.
 */
public class SortedAdjacencyGraph {

	/**
	 * Object for a node from the graph. Record nodes are contained in the sorted adjacency graph.
	 *
	 * @param longitude the longitude coordinate of a node.
	 * @param latitude  the latitude coordinate of a node.
	 * @param nodeId    the Index of the node in the adjacency graph, which was made by reading the textfile.
	 */
	public record IndexNode(double longitude, double latitude, int nodeId) implements Comparable {
		@Override
		public String toString () {
			return longitude + " " + latitude;
		}

		@Override
		public int compareTo (Object o) {
			if (o instanceof IndexNode node) {
				if (this.latitude - node.latitude < 0) {
					return -1;
				} else if (this.latitude - node.latitude > 0) {
					return 1;
				} else {
					return 0;
				}
			} else {
				throw new IllegalArgumentException("Compare to only for instances of Node!");
			}
		}
	}

	/**
	 * This is the enum is the collection for the possible direction to traverse an array.
	 */
	private enum MovementDirection {
		RIGHT,
		LEFT
	}

	private final IndexNode[] nodeIdsSortedByLatitude;

	/**
	 * Constructor for sortedAdjacencyGraph.
	 * Adds every node from the original adjacencyGraph as node record to an array. After that, the array will be sorted
	 * by latitude.
	 *
	 * @param adjacencyGraph Array of node records, computed from the original adjacencyGraph, sorted by latitude.
	 */
	public SortedAdjacencyGraph (final AdjacencyGraph adjacencyGraph) {

		this.nodeIdsSortedByLatitude = new IndexNode[adjacencyGraph.getNodeCount()];
		//adding every node from adjacecyGraph to the sortedAdjacencyGraph as node record.
		for (int i = 0; i < adjacencyGraph.getNodeCount(); i++) {
			this.nodeIdsSortedByLatitude[i] = new IndexNode(adjacencyGraph.getLongitudeOf(i), adjacencyGraph.getLatitudeOf(i), i);
		}
		//sorting sortedAdjacencyGraph by latitudes.
		Arrays.sort(this.nodeIdsSortedByLatitude);
	}

	/**
	 * This method returns the closest node to the given coordinates
	 *
	 * @param longitude the x-value of the given coordinate
	 * @param latitude  the y-value of the given coordinate
	 * @return the closest node to the given coordinates which is contained by sortedAdjacencyGraph
	 */
	public IndexNode getClosestNode (final double longitude, final double latitude) {
		IndexNode imaginaryNode = new IndexNode(longitude, latitude, -1);
		//find Index, where the imaginaryNode would be contained in sortedAdjacencyGraph, according to the latitude coordinate.
		int colestNodeIndex = this.getNodeWithNearestLatitude(imaginaryNode);

		colestNodeIndex = this.getNearestNodeId(longitude, latitude, colestNodeIndex, MovementDirection.RIGHT);
		colestNodeIndex = this.getNearestNodeId(longitude, latitude, colestNodeIndex, MovementDirection.LEFT);

		return this.nodeIdsSortedByLatitude[colestNodeIndex];
	}

	/**
	 *
	 * @param longitude the longitude value of the given position
	 * @param latitude the latitude value of the given position
	 * @param initialNearestNodeIndex the index of the Node, which has the lowest difference in latitude values to the given position
	 * @param direction the direction in which nodeIdsSortedByLatitude will be looked at for the closest node to the given position
	 * @return the index of the closest node to the given position in the "direction" side of sorted array of nodeIdsSortedByLatitude
	 */
	private int getNearestNodeId (final double longitude, final double latitude, final int initialNearestNodeIndex, final MovementDirection direction) {
		final int crementer = (direction == MovementDirection.RIGHT) ? 1 : -1;

		int currentBestIndex = initialNearestNodeIndex;
		double currentBestDistance = this.calculateDistancesOf(longitude, latitude, initialNearestNodeIndex);

		// For loop searches the left side of sorted array of initial node index for nodes with better distance to specified coords
		for (int currentNodesIndex = initialNearestNodeIndex + crementer;
		     !this.isOutOfBounds(currentNodesIndex) && !this.isLatitudeDiffGreater(currentBestDistance, latitude, currentNodesIndex);
			 currentNodesIndex += crementer) {

			double currentNodesDistance = this.calculateDistancesOf(longitude, latitude, currentNodesIndex);

			if (currentNodesDistance < currentBestDistance) {
				currentBestIndex = currentNodesIndex;
				currentBestDistance = currentNodesDistance;
			}
		}

		return currentBestIndex;
	}

	/**
	 * This method calculates the distance between a given position and the node at a certain index in nodeIdsSortedByLatitude
	 * @param fromLongitude the longitude value of the given position
	 * @param fromLatitude the latitude value of the given position
	 * @param toNodeIndex the index of the node to calculate the distance to
	 * @return the distance between the position and the node at the index in nodeIdsSortedByLatitude
	 */
	private double calculateDistancesOf(final double fromLongitude, final double fromLatitude, final int toNodeIndex) {
		return Math.sqrt(Math.pow((fromLongitude - nodeIdsSortedByLatitude[toNodeIndex].longitude), 2) + Math.pow((fromLatitude - this.nodeIdsSortedByLatitude[toNodeIndex].latitude), 2));
	}

	/**
	 * This method calculates the determination condition for the for loop in getNearestNodeId method.
	 * This method returns true if the latitude difference between fromLatitude and the latitude value of the node at toNodesIndex
	 * is greater than the value of bestDistance
	 * @param bestDistance the distance value which will be compared to the latitude difference
	 * @param fromLatitude the latitude value of a given postition
	 * @param toNodesIndex the index of the node which is looked at
	 * @return true if the latitude difference is greater than the current best distance value
	 */
	private boolean isLatitudeDiffGreater(final double bestDistance, final double fromLatitude, final int toNodesIndex) {
		double latitudeDif = Math.abs(fromLatitude - this.nodeIdsSortedByLatitude[toNodesIndex].latitude);

		return (latitudeDif > bestDistance);
	}

	/**
	 * This method calculates the determination condition for the for loop in getNearestNodeId method.
	 * This method returns true if the given index is out of bounds for nodeIdsSortedByLatitude.
	 * @param index the given index
	 * @return true if index is out of bounds for nodeIdsSortedByLatitude, else false
	 */
	private boolean isOutOfBounds (final int index) {
		if (index < 0 || this.nodeIdsSortedByLatitude.length <= index) return true;
		else return false;
	}

	/**
	 * This method returns the index of the node in sortedAdjacencyGraph array, which is has the nearest
	 * relative latitude to the given node
	 *
	 * @param node - The node with the lowest latitude will be searched for
	 * @return - Index of node in the sorted adj graph which has the lowest difference in latitude
	 */
	public int getNodeWithNearestLatitude (IndexNode node) {
		//Returns: index of the node, if it is contained in sortedAdjacencyGraph
		//otherwise returns the negative index where should be
		int indexOfSearchPoint = Arrays.binarySearch(this.nodeIdsSortedByLatitude, node);

		indexOfSearchPoint = (indexOfSearchPoint < 0) ? -indexOfSearchPoint : indexOfSearchPoint;

		if (indexOfSearchPoint >= this.nodeIdsSortedByLatitude.length) {
			return this.nodeIdsSortedByLatitude.length - 1;
		}
		return indexOfSearchPoint;
	}
}
