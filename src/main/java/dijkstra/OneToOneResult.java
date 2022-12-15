package dijkstra;

import struct.AdjacencyGraph;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.function.Predicate;

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
}
