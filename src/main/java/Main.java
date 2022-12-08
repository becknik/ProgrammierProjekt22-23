import loader.GraphReader;
import struct.AdjacencyGraph;
import struct.ClosestNodeDataStructure;
import struct.QuadTree;

import java.io.*;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class Main {
	public static void main (String[] args) {
		// Logging file parsing & adjacency graph creation to System.out:
		//GraphReader.enableLogging = true;
		//AdjacencyGraph.enableLogging = true;

		long createAdjacencyGraphStart = System.currentTimeMillis();
		File file = new File("germany.fmi");    // "germany.fmi"
		AdjacencyGraph testAdjacencyGraph = GraphReader.createAdjacencyGraphOf(file);
		long createAdjacencyGraphEnd = System.currentTimeMillis();

		//Main.writeAdjacencyGraph(testAdjacencyGraph, file);

		//Main.quadTreeStuff(file, testAdjacencyGraph);

		long createClosestNodeStart = System.currentTimeMillis();
		ClosestNodeDataStructure closestNodeDataStructure = new ClosestNodeDataStructure(testAdjacencyGraph);
		long createClosestNodeEnd = System.currentTimeMillis();


		long getNearestNodeStart = System.currentTimeMillis();
		ClosestNodeDataStructure.Node closestNode = closestNodeDataStructure.getClosestNode(10.4, 49.52);
		long getNearestNodeEnd = System.currentTimeMillis();
		System.out.println("The closest Node is located at:" + closestNode);

		// Benchmarking:
		long adjacencyGraphCreationTime = createAdjacencyGraphEnd - createAdjacencyGraphStart;
		long creatClosestNodeTime = createClosestNodeEnd - createClosestNodeStart;
		long getNearestNodeTime = getNearestNodeEnd - getNearestNodeStart;
		Main.logBenchmark(file, adjacencyGraphCreationTime, creatClosestNodeTime, getNearestNodeTime);

		long onetoAllDijkstraStart = System.currentTimeMillis();
		testAdjacencyGraph.oneToAllDijkstra(0);
		long oneToAllDijkstraEnd = System.currentTimeMillis();
		System.out.println(oneToAllDijkstraEnd - onetoAllDijkstraStart);
	}

	/**
	 * Everything QuadTree specific including logging, refactored for testing this experimental feature
	 *
	 * @param file           graph file
	 * @param adjacencyGraph needed by QuadTree
	 */
	private static void quadTreeStuff (final File file, final AdjacencyGraph adjacencyGraph) {
		// Logging:
		QuadTree.enableLogging = true;
		try {
			FileHandler fileHandler = new FileHandler(file + "-quad-tree-creation.log");
			fileHandler.setFormatter(new SimpleFormatter());
			QuadTree.logger.addHandler(fileHandler);
		} catch (IOException e) {
			e.printStackTrace();
		}

		QuadTree testQuadTree = new QuadTree(adjacencyGraph);
	}

	private static void writeAdjacencyGraph (AdjacencyGraph adjacencyGraph, File graphFile) {

		try {
			adjacencyGraph.printOutStructs(new PrintStream(new PrintStream(graphFile.getName() + "-adjacency-graph.txt")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void logBenchmark (final File graphFile, final long adjacencyGraphCreation,
	                                  final long creatGraphDataStructure, final long getNearestNode) {

		String formattedOutput = String.format("%s@%s - %tc%n\tCreation of adjArray:\t%f sec%n\tCreation of " +
						"graphStruct:\t%f sec%n\tGetting nearest node from coords:\t%f sec%n%n",
				System.getProperty("user.name"), System.getProperty("os.name"), new Date(),
				adjacencyGraphCreation * 10E-4, creatGraphDataStructure * 10E-4, getNearestNode * 10E-4);

		try (
				FileWriter writer = new FileWriter(graphFile.getName() + "-benchmark.log", true)
		) {
			writer.write(formattedOutput);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
