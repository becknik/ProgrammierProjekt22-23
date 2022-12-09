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
	public void test1 () {
		System.out.println("Test!");
		assertEquals(true, true);
	}

	@Test
	public void simpleGetClosestNodeTest () {
		ClosestNodeDataStructure closestNode = new ClosestNodeDataStructure(this.toyAdjGraph);
		ClosestNodeDataStructure.Node node = closestNode.getClosestNode(0, 0);
		assertEquals(0, node.nodeId());
	}
}
