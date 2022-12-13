package struct;

import java.util.Arrays;

public class SortedAdjacencyGraph {

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
		 * This method returns the distance as double to the given coordinates
		 *
		 * @param node - A second node record to which the distance shall be calculated
		 * @return the distance from the node on which this method is called to the given coordinates
		 */
		public double getDistanceTo (Node node) {
			return Math.sqrt(Math.pow((this.longitude - node.longitude), 2) + Math.pow((this.latitude - node.latitude), 2));
		}
	}

	private final Node[] sortedAdjacencyGraph;

	public SortedAdjacencyGraph (AdjacencyGraph adjacencyGraph) {

		this.sortedAdjacencyGraph = new Node[adjacencyGraph.getNodeCount()];
		for (int i = 0; i < adjacencyGraph.getNodeCount(); i++) {
			this.sortedAdjacencyGraph[i] = new Node(adjacencyGraph.getLongitudeOf(i), adjacencyGraph.getLatitudeOf(i), i);
		}
		Arrays.sort(this.sortedAdjacencyGraph);
	}

	/**
	 * This method returns the closest node to the given coordinates
	 *
	 * @param longitude the x-value of the given coordinate
	 * @param latitude  the y-value of the given coordinate
	 * @return the closest node to the given coordinates which is contained by sortedAdjacencyGraph
	 */
	public Node getClosestNode (double longitude, double latitude) {
		Node imaginaryNode = new Node(longitude, latitude, -1);    // TODO Dont create new record instance right here
		int nearestNodeIndex = this.getPivotIndex(imaginaryNode);

		Node closestNodeToCoords = sortedAdjacencyGraph[nearestNodeIndex];
		double distanceToImaginaryNode = imaginaryNode.getDistanceTo(closestNodeToCoords);

		// TODO: Commentary needed for understanding!!!
		for (int i = 0; Math.abs(closestNodeToCoords.latitude - imaginaryNode.latitude) <= distanceToImaginaryNode; i++) {
			if (nearestNodeIndex + i >= sortedAdjacencyGraph.length - 1) {
				break;
			}
			if (sortedAdjacencyGraph[nearestNodeIndex + i].getDistanceTo(imaginaryNode) < distanceToImaginaryNode) {
				closestNodeToCoords = sortedAdjacencyGraph[nearestNodeIndex + i];
				distanceToImaginaryNode = closestNodeToCoords.getDistanceTo(imaginaryNode);
			}
		}
		for (int i = 1; distanceToImaginaryNode >= Math.abs(sortedAdjacencyGraph[nearestNodeIndex - i].latitude - imaginaryNode.latitude); i++) {
			if (nearestNodeIndex - i <= 0) {
				break;
			}
			if (sortedAdjacencyGraph[nearestNodeIndex - i].getDistanceTo(imaginaryNode) < distanceToImaginaryNode) {
				closestNodeToCoords = sortedAdjacencyGraph[nearestNodeIndex - i];
				distanceToImaginaryNode = closestNodeToCoords.getDistanceTo(imaginaryNode);
			}
		}
		return closestNodeToCoords;
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
