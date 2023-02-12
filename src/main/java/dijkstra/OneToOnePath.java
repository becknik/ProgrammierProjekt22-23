package dijkstra;

import struct.AdjacencyGraph;

import javax.naming.OperationNotSupportedException;
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

	@Override
	public ArrayList<Point2D.Double> getPathInCoordinates(final Deque<Integer> path) throws OperationNotSupportedException
	{
		throw new OperationNotSupportedException("Calling this operation on a OneToOnePath is logically not consistent." +
				"This object holds it's own path already. Use the overloaded operation instead.");
	}

	/**
	 * This method returns the path of the one to one dijkstra result as coordinates
	 * @return path of the dijkstra result in coordinates
	 */
	public ArrayList<Point2D.Double> getPathInCoordinates () throws OperationNotSupportedException
	{
		return super.getPathInCoordinates(this.path);
	}
}
