package dijkstra;

import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongHeapPriorityQueue;

/**
 * This class is used to encode a (int nodeID,int relativeDistance) tupel into a long.
 * It provides an operation used in the {@link DijkstraAlgorithm} class which semantic can't be satisfied by {@link LongHeapPriorityQueue}.
 */
public class DijkstraLongPriorityQueue extends LongHeapPriorityQueue {

	DijkstraLongPriorityQueue(final int size, LongComparator c)
	{
		super(size, c);
	}

	/**
	 * This method takes a long = (int nodeID,int relativeDistance) parameter which relativeDistance is modified from the one
	 * present in this heap. Therefore, the long in the heap is updated & to fulfill the heap semantics the updated value is
	 * swapped to the first position of the heap array.
	 * @param lUpdatedDistance
	 */
	public void removeAndAddWithUpdatedDistance(long lUpdatedDistance)
	{
		for (int i = 0; i < this.heap.length; i++) {
			if ((this.heap[i] >> 32) == (lUpdatedDistance >>> 32)) {

				// Swapping the first element with the updated one
				long swappedFirstElement = this.heap[0];
				this.heap[0] = lUpdatedDistance;
				this.heap[i] = swappedFirstElement;

				return;
			}
		}
	}
}
