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

		System.gc();    // WFT. You just had ohne job, JVM?!

		this.adjacencyGraph = adjacencyGraph;
	}

	/**
	 * @return
	 *
	 * @throws OperationNotSupportedException
	 */
	public int getDistanceFromPath (final ArrayDeque<Integer> path) {
		this.checkDijkstraResultInvariant();

		int distance = 0;
		for (Integer edgeId : path) {
			distance += this.adjacencyGraph.getDistanceOf(edgeId);
		}

		return distance;
	}

	protected void checkDijkstraResultInvariant () {
		assert this.adjacencyGraph != null;
	}
}
