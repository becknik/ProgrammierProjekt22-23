package test;

import loader.GraphReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import struct.AdjacencyGraph;
import struct.ClosestNodeDataStructure;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestClosestNodeDataStructure {

	AdjacencyGraph toyAdjGraph;

	@BeforeEach
	public void setUpAdjGraphs () {
		toyAdjGraph = GraphReader.createAdjacencyGraphOf(new File("toy.fmi"));
	}

	@Test
	public void simpleGetClosestNodeTest () {
		ClosestNodeDataStructure closestNode = new ClosestNodeDataStructure(this.toyAdjGraph);
		ClosestNodeDataStructure.Node node = closestNode.getClosestNode(0, 0);
		assertEquals(0, node.nodeId());
	}

	@Test
	public void copyPastFromMain () {
		/*long createClosestNodeStart = System.currentTimeMillis();
		ClosestNodeDataStructure closestNodeDataStructure = new ClosestNodeDataStructure(testAdjacencyGraph);
		long createClosestNodeEnd = System.currentTimeMillis();

		long getNearestNodeStart = System.currentTimeMillis();
		ClosestNodeDataStructure.Node closestNode = closestNodeDataStructure.getClosestNode(10.4, 49.52);
		long getNearestNodeEnd = System.currentTimeMillis();
		System.out.println("The closest Node is located at: " + closestNode);
		*/
	}
}
