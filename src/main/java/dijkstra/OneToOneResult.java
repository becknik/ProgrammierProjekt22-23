package dijkstra;

import org.w3c.dom.Node;
import struct.AdjacencyGraph;

import java.awt.geom.Point2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Predicate;

/**
 * Holds a path object from source to target node, which holds edge ids.
 */
public final class OneToOneResult extends DijkstraResult {

	private final Predicate<ArrayDeque<Integer>> isPathValid = (path) -> {
		HashSet<Integer> seenEdgeIds = new HashSet<>();     // Used for checking for cycles in the path

		for (int edgeId : path) {
			if (edgeId < 0 || adjacencyGraph.getEdgeCount() <= edgeId) return false;

			if (seenEdgeIds.contains(edgeId)) return false;
			seenEdgeIds.add(edgeId);
		}
		return true;
	};

	ArrayDeque<Integer> path;

	/**
	 * OneToOne Constructor
	 *
	 * @param adjacencyGraph
	 * @param path
	 */
	OneToOneResult (final AdjacencyGraph adjacencyGraph, final ArrayDeque<Integer> path) {
		super(adjacencyGraph);

		assert path != null;
		assert this.isPathValid.test(path);     // Some precondition checks on path: all edge IDs valid && no cycles

		this.path = path;
	}

	public int getDistanceFromPath () {
		return super.getDistanceFromPath(this.path);
	}

	@Override
	public int getDistanceFromPath (final ArrayDeque<Integer> path) throws IllegalArgumentException {
		throw new IllegalArgumentException("This object is an oneToToneResult, which already hold a path.");
	}

	public ArrayDeque<Integer> getPath () {
		return new ArrayDeque<>(this.path);
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
