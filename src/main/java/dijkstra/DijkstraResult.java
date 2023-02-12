package dijkstra;

import struct.AdjacencyGraph;

import javax.naming.OperationNotSupportedException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Deque;

/**
 * Classes implementing this interface represents the result of a {@code DijkstraAlgorithm}.
 * Due to the nature of the algorithm being able to execute the one to all or one to one, dynamic binding ist used for behavior.
 */
public abstract class DijkstraResult {
	protected final AdjacencyGraph adjacencyGraph;

	protected DijkstraResult (final AdjacencyGraph adjacencyGraph) {
		assert adjacencyGraph != null;

		this.adjacencyGraph = adjacencyGraph;
	}

	/**
	 * If the dynamic type of the object this method is called on is a {@link OneToAllResult}, the path from the starting
	 * node the oneToAll algorithm was executed on is returned.
	 * Else the exception is thrown.
	 *
	 * @param targetNodeId The node ID the shortest path is calculated to from the source node of the {@link OneToAllResult}
	 * @return The path to the specified node ID, if this is a {@link OneToAllResult}
	 * @throws OperationNotSupportedException Thrown if the (dynamic) type of this object is not a {@link OneToAllResult}
	 */
	public Deque<Integer> getPathTo (final int targetNodeId) throws OperationNotSupportedException
	{throw new OperationNotSupportedException("This operation is only executable for oneToAll typed DijkstraResults");}

	/**
	 * If the (dynamic) type of the called object is a {@link OneToOnePath} this method returns the length of the path
	 * by summing up all edge IDs lengths. Else an exception is thrown.
	 * The method calls the getLengthOf method internally.
	 *
	 * @return The {@link OneToOnePath} length
	 * @throws OperationNotSupportedException If the (dynamic) type of the object is not {@link OneToOnePath}
	 */
	public int getLength () throws OperationNotSupportedException
	{throw new OperationNotSupportedException("This operation is only executable for oneToOne typed DijkstraResults");}

	/**
	 * Used for the calculation of the path length from a given path by using edge lengths saved in an adjacency graph &
	 * a path containing edge IDs.
	 *
	 * @param path The path consisting of edge IDs
	 * @return The length of all edge IDs summed up
	 */
	public int getLengthOf(final Deque<Integer> path)
	{
		int distance = 0;
		for (Integer edgeId : path) {
			distance += this.adjacencyGraph.getDistanceOf(edgeId);
		}

		return distance;
	}

	public ArrayList<Point2D.Double> getPathInCoordinates (final Deque<Integer> path) throws OperationNotSupportedException
	{
		ArrayList<Point2D.Double> result = new ArrayList<>(path.size() + 1);

		int firstEdge = path.peek();
		Point2D.Double sourceNodesCoords = this.adjacencyGraph.getEdgeIdsNode(firstEdge, false);
		result.add(sourceNodesCoords);

		// Add the remaining edgesIds target nodes to get a list of edge node coordinates
		for (int edgeId : path) {
			result.add(this.adjacencyGraph.getEdgeIdsNode(edgeId, true));
		}

		return result;
	}
}
