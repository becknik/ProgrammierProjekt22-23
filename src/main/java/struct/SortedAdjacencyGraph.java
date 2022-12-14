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
	public record Node(double longitude, double latitude, int nodeId) implements Comparable {
		@Override
		public String toString () {
			return longitude + " " + latitude;
		}

		@Override
		public int compareTo (Object o) {

			if (o instanceof Node node) {
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

		/**
		 * This method returns the distance as double to the given targetNode to current targetNode using the euklid formula.
		 *
		 * @param targetNode - A second targetNode record to which the distance shall be calculated
		 * @return the distance from the targetNode on which this method is called to the given coordinates
		 */
		public double getDistanceTo (final Node targetNode) {
			return Math.sqrt(Math.pow((this.longitude - targetNode.longitude), 2) + Math.pow((this.latitude - targetNode.latitude), 2));
		}
	}

	private final Node[] sortedAdjacencyGraph;

	/**
	 * Constructor for sortedAdjacencyGraph.
	 * Adds every node from the original adjacencyGraph as node record to an array. After that, the array will be sorted
	 * by latitude.
	 *
	 * @param adjacencyGraph Array of node records, computed from the original adjacencyGraph, sorted by latitude.
	 */
	public SortedAdjacencyGraph (final AdjacencyGraph adjacencyGraph) {

		this.sortedAdjacencyGraph = new Node[adjacencyGraph.getNodeCount()];
		//adding every node from adjacecyGraph to the sortedAdjacencyGraph as node record.
		for (int i = 0; i < adjacencyGraph.getNodeCount(); i++) {
			this.sortedAdjacencyGraph[i] = new Node(adjacencyGraph.getLongitudeOf(i), adjacencyGraph.getLatitudeOf(i), i);
		}
		//sorting sortedAdjacencyGraph by latitudes.
		Arrays.sort(this.sortedAdjacencyGraph);
	}

	/**
	 * This method returns the closest node to the given coordinates
	 *
	 * @param longitude the x-value of the given coordinate
	 * @param latitude  the y-value of the given coordinate
	 * @return the closest node to the given coordinates which is contained by sortedAdjacencyGraph
	 */
	public Node getClosestNode (final double longitude, final double latitude) {
		Node imaginaryNode = new Node(longitude, latitude, -1);
		//find Index, where the imaginaryNode would be contained in sortedAdjacencyGraph, according to the latitude coordinate.
		int nearestNodeIndex = this.getPivotIndex(imaginaryNode);

		//find both candidates for closest node to imaginary node by traversing sortedAdjacencyGraph to the right and left.
		Node candidateOne = this.getCandidateTraversingRight(nearestNodeIndex, imaginaryNode);
		double candidateOneDistance = candidateOne.getDistanceTo(imaginaryNode);
		Node candidateTwo = this.getCandidateTraversingLeft(nearestNodeIndex, imaginaryNode);
		double candidateTwoDistance = candidateTwo.getDistanceTo(imaginaryNode);

		Node closestNode = (candidateOneDistance <= candidateTwoDistance) ? candidateOne : candidateTwo;

		return closestNode;
	}

	/**
	 * searching for closest node to the imaginary node by traversing sortedAdjacencyGraph to the right.
	 * Start looking for the closest node by traversing sortedAdjacencyGraph to the right (index increasing), starting at the pivot Index.
	 *
	 * @param pivotIndex    the pivot index where to start looking for the closest node in sortedAdjacencyGraph
	 * @param imaginaryNode The record object of longitude and latitude value
	 * @return the node included in sortedAdjacencyGraph, at pivotIndex or higher, located closest to the imaginaryNode
	 */
	private Node getCandidateTraversingRight (int pivotIndex, Node imaginaryNode) {
		Node currentClosestNode = sortedAdjacencyGraph[pivotIndex];
		double distanceToImaginaryNode = imaginaryNode.getDistanceTo(currentClosestNode);

		//Stop looking further to the right, when...
		// ...pivotIndex + i gets out of bound.
		// ...the difference of the latitude values of the imaginary node and the current watched node
		//is higher than the actual distance between the imaginary node and the current closest node.
		for (int i = 0; (pivotIndex + i <= sortedAdjacencyGraph.length) && (Math.abs(sortedAdjacencyGraph[pivotIndex + i].latitude - imaginaryNode.latitude) <= distanceToImaginaryNode); i++) {

			//Updating closestNodeToCoords and distanceToImaginaryNode when finding a closer node to the given coordinates.
			if (sortedAdjacencyGraph[pivotIndex + i].getDistanceTo(imaginaryNode) < distanceToImaginaryNode) {
				currentClosestNode = sortedAdjacencyGraph[pivotIndex + i];
				distanceToImaginaryNode = currentClosestNode.getDistanceTo(imaginaryNode);
			}
		}
		return currentClosestNode;
	}

	/**
	 * search for the closest node to the imaginary node by traversing sortedAdjacencyGraph to the left (index decreasing),
	 * start at the pivot index.
	 *
	 * @param pivotIndex    the pivot index where to start looking for the closest node in sortedAdjacencyGraph
	 * @param imaginaryNode The record object of longitude and latitude value
	 * @return the node included in sortedAdjacencyGraph, at pivotIndex or lower, located closest to the imaginaryNode
	 */
	private Node getCandidateTraversingLeft (int pivotIndex, Node imaginaryNode) {
		Node currentClosestNode = sortedAdjacencyGraph[pivotIndex];
		double distanceToImaginaryNode = imaginaryNode.getDistanceTo(currentClosestNode);
		boolean isNotOutOfBound = true;

		//stop looking further to the left, when...
		// ...the pivotIndex - i gets out of bound.
		// ...the difference of the latitude values of the imaginary node and the current watched node
		//  is higher than the actual distance between the imaginary node and the current closest node.
		for (int i = 0; isNotOutOfBound && Math.abs(sortedAdjacencyGraph[pivotIndex - i].latitude - imaginaryNode.latitude) <= distanceToImaginaryNode; i++) {

			//Updating closestNodeToCoords and distanceToImaginaryNode when finding a closer node to the given coordinates.
			if (sortedAdjacencyGraph[pivotIndex - i].getDistanceTo(imaginaryNode) < distanceToImaginaryNode) {
				currentClosestNode = sortedAdjacencyGraph[pivotIndex - i];
				distanceToImaginaryNode = currentClosestNode.getDistanceTo(imaginaryNode);
			}
			//check if next iteration will be out of bounds
			if (pivotIndex - (i + 1) <= 0) {
				isNotOutOfBound = false;
			}
		}
		return currentClosestNode;
	}


	/**
	 * This method returns the index of the node in sortedAdjacencyGraph array, which is has the nearest
	 * relative latitude to the given node
	 *
	 * @param node - The node with the lowest latitude will be searched for
	 * @return - Index of node in the sorted adj graph which has the lowest difference in latitude
	 */
	public int getPivotIndex (Node node) {
		//Returns: index of the node, if it is contained in sortedAdjacencyGraph
		//otherwise returns the negative index where should be
		int indexOfSearchPoint = Arrays.binarySearch(this.sortedAdjacencyGraph, node);

		indexOfSearchPoint = (indexOfSearchPoint < 0) ? -indexOfSearchPoint : indexOfSearchPoint;

		if (indexOfSearchPoint >= this.sortedAdjacencyGraph.length) {
			return this.sortedAdjacencyGraph.length - 1;
		}
		return indexOfSearchPoint;
	}
}
