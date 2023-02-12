package dijkstra;

import struct.AdjacencyGraph;

import java.util.Deque;

/**
 * This class stores the predecessor node array which holds the edge ids to the shortest paths next node,
 * were as all edges finally lead to the source node id. The source nodes edge in the array is set to 0.
 */
public final class OneToAllResult extends DijkstraResult {

	private final int[] predecessorEdgeIds;
	private final int sourceNodeId;

	OneToAllResult (final AdjacencyGraph adjacencyGraph, final int[] predecessorEdgeIds, final int sourceNodeId) {
		super(adjacencyGraph);

		assert 0 <= sourceNodeId;
		assert sourceNodeId <= adjacencyGraph.getNodeCount();

		this.sourceNodeId = sourceNodeId;

		assert predecessorEdgeIds != null;
		assert predecessorEdgeIds.length == adjacencyGraph.getNodeCount();
		assert predecessorEdgeIds[sourceNodeId] < adjacencyGraph.getNodeCount();

		this.predecessorEdgeIds = predecessorEdgeIds;
	}

	/**
	 * Returns the path (consisting out of edge ids) from the source node (class member) to the specified node id by calling the
	 * method in the {@code AdjacencyGraph} class.
	 *
	 * @param targetNodeId The target node
	 * @return A path of edge IDs from the source node to the target node
	 */
	@Override
	public Deque<Integer> getPathTo (final int targetNodeId) {
		assert  this.sourceNodeId == targetNodeId : "Target node must not have the same index as source node.";
		assert super.adjacencyGraph.getNodeCount() > targetNodeId : "Target node ID must not be higher than node count of adjacency graph.";

		return super.adjacencyGraph.getPath(this.sourceNodeId, targetNodeId, this.predecessorEdgeIds);   // Don't know how to do better...
	}
}
