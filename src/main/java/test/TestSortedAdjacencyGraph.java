package test;

import loader.GraphReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import struct.AdjacencyGraph;
import struct.SortedAdjacencyGraph;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSortedAdjacencyGraph {

	AdjacencyGraph stgtAdjGraph;

	@BeforeEach
	public void setUpAdjGraphs () {
		stgtAdjGraph = GraphReader.createAdjacencyGraphOf(new File("stgtregbz.fmi"));
	}

	@Test
	public void simpleGetClosestNodeTest () {
		SortedAdjacencyGraph closestNode = new SortedAdjacencyGraph(this.stgtAdjGraph);
		SortedAdjacencyGraph.IndexNode node = closestNode.getClosestNode(48.94207470000000627, 10.28);
		assertEquals(540, node.nodeId());
	}

	@Test
	public void copyPastFromMain () {
		/*long createClosestNodeStart = System.currentTimeMillis();
		SortedAdjacencyGraph closestNodeDataStructure = new SortedAdjacencyGraph(testAdjacencyGraph);
		long createClosestNodeEnd = System.currentTimeMillis();

		long getNearestNodeStart = System.currentTimeMillis();
		SortedAdjacencyGraph.Node closestNode = closestNodeDataStructure.getClosestNode(10.4, 49.52);
		long getNearestNodeEnd = System.currentTimeMillis();
		System.out.println("The closest Node is located at: " + closestNode);
		*/
	}
}
