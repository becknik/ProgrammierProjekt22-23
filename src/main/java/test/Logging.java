package test;

import struct.AdjacencyGraph;
import struct.QuadTree;

import java.io.*;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class Logging {

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

	static void logBenchmark (final File graphFile, final long adjacencyGraphCreation,
	                          final long creatGraphDataStructure, final long getNearestNode,
	                          final long oneToAllDijkstra, final long oneToOneDijkstra) {

		String formattedOutput = String.format(
				"%s@%s - %tc%n\tCreation of adjArray:\t%f sec%n" +
						"\tCreation of graphStruct:\t%f sec%n" +
						"\tGetting nearest node from coords:\t%f sec%n" +
						"\tExecution of One2All Dijkstra Algorithm:\t%f sec%n" +
						"\tExecution of One2One Dijkstra Algorithm:\t%f sec%n%n",
				System.getProperty("user.name"), System.getProperty("os.name"), new Date(),
				adjacencyGraphCreation * 10E-4, creatGraphDataStructure * 10E-4, getNearestNode * 10E-4,
				oneToAllDijkstra * 10E-4, oneToOneDijkstra * 10E-4);

		try (
				FileWriter writer = new FileWriter(graphFile.getName() + "-benchmark.log", true)
		) {
			writer.write(formattedOutput);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void logTestBenchmark (GraphFileType graphFileType, String testname, long result) {
		String formattedOutput = String.format("%tc:\tTest %s took about %f sec%n", new Date(), testname, result * 10E-4);

		try (
				FileWriter writer = new FileWriter(graphFileType.fileName + ".test-benchmark.log", true)
		) {
			writer.write(formattedOutput);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

