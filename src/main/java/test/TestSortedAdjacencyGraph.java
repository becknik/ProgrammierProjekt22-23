package test;

import loader.GraphReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import struct.AdjacencyGraph;
import struct.SortedAdjacencyGraph;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSortedAdjacencyGraph {

	AdjacencyGraph toyAdjGraph;

	@BeforeEach
	public void setUpAdjGraphs () {
		toyAdjGraph = GraphReader.createAdjacencyGraphOf(new File("toy.fmi"));
	}

	@Test
	public void simpleGetClosestNodeTest () {
		SortedAdjacencyGraph closestNode = new SortedAdjacencyGraph(this.toyAdjGraph);
		SortedAdjacencyGraph.Node node = closestNode.getClosestNode(0, 0);
		assertEquals(0, node.nodeId());
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
