package dijkstra;

import struct.AdjacencyGraph;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.function.Predicate;

/**
 * TODO JavaDoc
 */
public class DijkstraResult {
	enum RunType {
		ONE_TO_ALL(),
		ONE_TO_ONE()
	}

	Predicate<ArrayDeque<Integer>> isPathValid = new Predicate<>() {
		@Override
		public boolean test (final ArrayDeque<Integer> path) {
			HashSet<Integer> seenEdgeIds = new HashSet<>();     // Used for checking for cycles in the path

			for (int edgeId : path) {
				if (edgeId < 0 || adjacencyGraph.getEdgeCount() <= edgeId) return false;

				if (seenEdgeIds.contains(edgeId)) return false;
				seenEdgeIds.add(edgeId);
			}
			return true;
		}
	};

	private final AdjacencyGraph adjacencyGraph;
	private RunType runType;
	private final int[] predecessorEdgeIds;
	private ArrayDeque<Integer> path;
	private int sourceNodeId;

	private DijkstraResult (final AdjacencyGraph adjacencyGraph, final int[] predecessorEdgeIds) {
		assert adjacencyGraph != null;
		assert predecessorEdgeIds != null;
		assert predecessorEdgeIds.length == adjacencyGraph.getNodeCount();

		this.adjacencyGraph = adjacencyGraph;
		this.predecessorEdgeIds = predecessorEdgeIds;
	}

	/**
	 * OneToOne Constructor
	 * @param adjacencyGraph
	 * @param predecessorEdgeIds
	 * @param path
	 */
	DijkstraResult (final AdjacencyGraph adjacencyGraph, final int[] predecessorEdgeIds, final ArrayDeque<Integer> path) {
		this(adjacencyGraph, predecessorEdgeIds);

		assert path != null;
		assert this.isPathValid.test(path);     // Some precondition checks on path: all edge IDs valid && no cycles

		this.runType = RunType.ONE_TO_ONE;
		this.path = path;
	}

	/**
	 * OneToAll Constructor
	 * @param adjacencyGraph
	 * @param predecessorEdgeIds
	 * @param sourceNodeId
	 */
	DijkstraResult (final AdjacencyGraph adjacencyGraph, final int[] predecessorEdgeIds, final int sourceNodeId) {
		this(adjacencyGraph, predecessorEdgeIds);

		assert 0 <= sourceNodeId;
		assert sourceNodeId <= this.adjacencyGraph.getNodeCount();
		assert predecessorEdgeIds[sourceNodeId] < this.adjacencyGraph.getNodeCount();

		this.runType = RunType.ONE_TO_ALL;
		this.sourceNodeId = sourceNodeId;

		assert this.isOneToAllCorrect();    // Precondition checks consistency of predecessorEdgeIds
	}

	private boolean isOneToAllCorrect () {
		assert this.runType == RunType.ONE_TO_ALL;

		// TODO Do unnecessary checks, if our algorithm is correct...
		return true;
	}

	/**
	 *
	 * @return
	 * @throws OperationNotSupportedException
	 */
	public int getDistanceFromPath () throws OperationNotSupportedException {
		if (this.runType == RunType.ONE_TO_ALL)
			throw new OperationNotSupportedException("Operation must not be called on an one to all DijkstraResult object.");

		return getDistanceFromPath(this.path);
	}

	public int getDistanceFromPath (final ArrayDeque<Integer> path) {
		if (this.path != path)
			throw new IllegalArgumentException("The specified path is the same as the one of the current object." +
					"Please run the overloaded operation without any parameters, due to objects of type already posses a path");

		int distance = 0;
		for (Integer edgeId : path) {
			distance += this.adjacencyGraph.getDistanceOf(edgeId);
		}
		return distance;
	}

	public ArrayDeque<Integer> getPathTo (final int targetNodeId) throws OperationNotSupportedException {
		if (this.runType == RunType.ONE_TO_ONE)
			throw new OperationNotSupportedException("Operation must not be called on an one to one DijkstraResult object.");
		if (this.sourceNodeId != targetNodeId)
			throw new IllegalArgumentException("Target node must not have the same index as source node.");
		if (this.adjacencyGraph.getNodeCount() <= targetNodeId)
			throw new IllegalArgumentException("Target node ID must not be higher than node count of adjacency graph.");

		return this.adjacencyGraph.getPath(this.sourceNodeId, targetNodeId, this.predecessorEdgeIds);   // Don't know how to do better...
	}

	public ArrayDeque<Integer> getPath () {
		return new ArrayDeque<>(this.path);
	}
}
