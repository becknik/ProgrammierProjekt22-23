package dijkstra;

import struct.AdjacencyGraph;

import java.util.ArrayDeque;

/**
 * This class represents the result of a {@code DijkstraAlgorithm}.
 * Due to the nature of the algorithm being able to execute the one to all or one to one,
 * there are two child classes for each execution type of the algorithm, which are thought to be the dynamic type on runtime.
 */
public abstract sealed class DijkstraResult permits OneToAllResult, OneToOneResult {
	protected final AdjacencyGraph adjacencyGraph;

	protected DijkstraResult (final AdjacencyGraph adjacencyGraph)
	{
		assert adjacencyGraph != null;

		this.adjacencyGraph = adjacencyGraph;
	}

	/**
	 * Used for the calculation if the path length from a given path
	 *
	 * @param path
	 * @return
	 */
	public int getDistanceFromPath (final ArrayDeque<Integer> path)
	{
		int distance = 0;
		for (Integer edgeId : path) {
			distance += this.adjacencyGraph.getDistanceOf(edgeId);
		}

		return distance;
	}
}
