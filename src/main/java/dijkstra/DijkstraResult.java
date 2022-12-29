package dijkstra;

import struct.AdjacencyGraph;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayDeque;

/**
 * TODO JavaDoc
 */
public abstract sealed class DijkstraResult permits OneToAllResult, OneToOneResult {
	protected final AdjacencyGraph adjacencyGraph;

	protected DijkstraResult (final AdjacencyGraph adjacencyGraph) {
		assert adjacencyGraph != null;

		this.adjacencyGraph = adjacencyGraph;
	}

	/**
	 * @return
	 *
	 * @throws OperationNotSupportedException
	 */
	public int getDistanceFromPath (final ArrayDeque<Integer> path) {
		int distance = 0;
		for (Integer edgeId : path) {
			distance += this.adjacencyGraph.getDistanceOf(edgeId);
		}

		return distance;
	}
}
