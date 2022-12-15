package dijkstra;

import struct.AdjacencyGraph;

import java.util.ArrayDeque;

public final class OneToAllResult extends DijkstraResult {

	private final int[] predecessorEdgeIds;
	private final int sourceNodeId;


	/**
	 * OneToAll Constructor
	 *
	 * @param adjacencyGraph
	 * @param predecessorEdgeIds
	 * @param sourceNodeId
	 */
	OneToAllResult (final AdjacencyGraph adjacencyGraph, final int[] predecessorEdgeIds, final int sourceNodeId) {
		super(adjacencyGraph);

		assert 0 <= sourceNodeId;
		assert sourceNodeId <= this.adjacencyGraph.getNodeCount();

		this.sourceNodeId = sourceNodeId;

		assert predecessorEdgeIds != null;
		assert predecessorEdgeIds.length == adjacencyGraph.getNodeCount();
		assert predecessorEdgeIds[sourceNodeId] < this.adjacencyGraph.getNodeCount();   // TODO Maybe add more checks

		this.predecessorEdgeIds = predecessorEdgeIds;
	}

	public ArrayDeque<Integer> getPathTo (final int targetNodeId) {
		if (this.sourceNodeId == targetNodeId)
			throw new IllegalArgumentException("Target node must not have the same index as source node.");
		if (this.adjacencyGraph.getNodeCount() <= targetNodeId)
			throw new IllegalArgumentException("Target node ID must not be higher than node count of adjacency graph.");

		return this.adjacencyGraph.getPath(this.sourceNodeId, targetNodeId, this.predecessorEdgeIds);   // Don't know how to do better...
	}
}
