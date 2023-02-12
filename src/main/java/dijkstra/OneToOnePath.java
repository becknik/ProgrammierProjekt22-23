package dijkstra;

import struct.AdjacencyGraph;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Deque;

/**
 * Holds a path object from source to target node, which holds edge ids.
 */
public final class OneToOnePath extends DijkstraResult {
	Deque<Integer> path;

	OneToOnePath(final AdjacencyGraph adjacencyGraph, final Deque<Integer> path) {
		super(adjacencyGraph);
		this.path = path;
	}

	@Override
	public int getLength() {
		return super.getLengthOf(this.path);
	}

	/**
	 * This method returns the path of the one to one dijkstra result as coordinates
	 * @return path of the dijkstra result in coordinates
	 */
	public ArrayList<Point2D.Double> getPathInCoordinates () {
		ArrayList<Point2D.Double> result = new ArrayList<>(this.path.size() + 1);

		int firstEdge = this.path.peek();
		Point2D.Double sourceNodesCoords = this.adjacencyGraph.getEdgeIdsNode(firstEdge, false);
		result.add(sourceNodesCoords);

		// Add the remaining edgesIds target nodes to get a list of edge node coordinates
		for (int edgeId : this.path) {
			result.add(this.adjacencyGraph.getEdgeIdsNode(edgeId, true));
		}

		return result;
	}
}
