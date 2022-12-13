import loader.GraphReader;
import struct.AdjacencyGraph;
import struct.DijkstraResult;
import struct.SortedAdjacencyGraph;

import javax.naming.OperationNotSupportedException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.Scanner;

public class Benchmark {

	public static void main(String[] args) {
		// read parameters (parameters are expected in exactly this order)
		String graphPath = args[1];
		double lon = Double.parseDouble(args[3]);
		double lat = Double.parseDouble(args[5]);
		String quePath = args[7];
		int sourceNodeId = Integer.parseInt(args[9]);

		// run benchmarks
		System.out.println("Reading graph file and creating graph data structure (" + graphPath + ")");
		long graphReadStart = System.currentTimeMillis();
		// TODO: read graph here
		File fmiGraphFile = new File(graphPath);
		AdjacencyGraph adjacencyGraph = GraphReader.createAdjacencyGraphOf(fmiGraphFile);
		long graphReadEnd = System.currentTimeMillis();
		System.out.println("\tgraph read took " + (graphReadEnd - graphReadStart) + "ms");

		System.out.println("Setting up closest node data structure...");
		// TODO: set up closest node data structure here
		SortedAdjacencyGraph sortedAdjacencyGraph = new SortedAdjacencyGraph(adjacencyGraph);

		System.out.println("Finding closest node to coordinates " + lon + " " + lat);
		long nodeFindStart = System.currentTimeMillis();
		double[] coords = {0.0, 0.0};
		// TODO: find closest node here and write coordinates into coords
		SortedAdjacencyGraph.Node closestNode = sortedAdjacencyGraph.getClosestNode(lon, lat);
		coords[0] = closestNode.longitude();
		coords[1] = closestNode.latitude();
		long nodeFindEnd = System.currentTimeMillis();
		System.out.println("\tfinding node took " + (nodeFindEnd - nodeFindStart) + "ms: " + coords[0] + ", " + coords[1]);

		System.out.println("Running one-to-one Dijkstras for queries in .que file " + quePath);
		long queStart = System.currentTimeMillis();
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(quePath))) {
			String currLine;
			while ((currLine = bufferedReader.readLine()) != null) {
				int oneToOneSourceNodeId = Integer.parseInt(currLine.substring(0, currLine.indexOf(" ")));
				int oneToOneTargetNodeId = Integer.parseInt(currLine.substring(currLine.indexOf(" ") + 1));
				int oneToOneDistance = -42;
				// TODO set oneToOneDistance to the distance from
				// oneToOneSourceNodeId to oneToOneSourceNodeId as computed by
				// the one-to-one Dijkstra
				DijkstraResult dijkstraResultToOne = adjacencyGraph.dijkstra(oneToOneSourceNodeId, oneToOneTargetNodeId);
				oneToOneDistance = dijkstraResultToOne.getDistanceFromPath();
				System.out.println(oneToOneDistance);
			}
		} catch (Exception e) {
			System.out.println("Exception...");
			e.printStackTrace();
		}
		long queEnd = System.currentTimeMillis();
		System.out.println("\tprocessing .que file took " + (queEnd - queStart) + "ms");

		System.out.println("Computing one-to-all Dijkstra from node id " + sourceNodeId);
		long oneToAllStart = System.currentTimeMillis();
		// TODO: run one-to-all Dijkstra here
		DijkstraResult dijkstraResultToAll = adjacencyGraph.dijkstra(sourceNodeId);
		long oneToAllEnd = System.currentTimeMillis();
		System.out.println("\tone-to-all Dijkstra took " + (oneToAllEnd - oneToAllStart) + "ms");

		// ask user for a target node id
		System.out.print("Enter target node id... ");
		int targetNodeId = (new Scanner(System.in)).nextInt();
		int oneToAllDistance = -42;
		// TODO set oneToAllDistance to the distance from sourceNodeId to
		// targetNodeId as computed by the one-to-all Dijkstra
		ArrayDeque<Integer> pathToInput = new ArrayDeque<>();
		try {
			pathToInput = dijkstraResultToAll.getPathTo(targetNodeId);
		} catch (OperationNotSupportedException e) {
			System.err.println("Something clearly seems to be wrong with your entered node ID. :)");
			e.printStackTrace();
		}
		oneToAllDistance = dijkstraResultToAll.getDistanceFromPath(pathToInput);
		System.out.println("Distance from " + sourceNodeId + " to " + targetNodeId + " is " + oneToAllDistance);
	}

}
