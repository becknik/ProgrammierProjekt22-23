package loader;

import struct.AdjacencyGraph;

import java.io.*;
import java.util.logging.Logger;

/**
 * A helper class to create a {@code AdjacencyGraph} object from FMI plain text file
 */
public class GraphReader {
	private static final Logger logger = Logger.getLogger(GraphReader.class.getName());
	public static boolean enableLogging;

	/**
	 * Reads the FMI plain text graph file into a new {@code AdjacencyGraph} object by using the provided information of
	 * node and edge count in the files.
	 * A little hard coding, if you ask me TODO: use Janniks beautiful implementation from commit history here :)
	 *
	 * @param file - The file that contains the FMI "raw graph" contents
	 */
	public static AdjacencyGraph createAdjacencyGraphOf (final File file) {
		AdjacencyGraph adjGraph = null;

		try (
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader)
		) {
			// Skip the first irrelevant 5 lines
			for (int i = 0; i < 5; i++) bufferedReader.readLine();

			// Parse the amount of nodes and edges
			final int nodeCount = Integer.parseInt(bufferedReader.readLine());
			final int edgeCount = Integer.parseInt(bufferedReader.readLine());

			adjGraph = new AdjacencyGraph(nodeCount, edgeCount);

			String readLine;
			// Parses and adds every node readLine String and adds the node data to adjacency graph object
			for (int nodeId = 0; nodeId < nodeCount; nodeId++) {
				readLine = bufferedReader.readLine();
				final String[] rawValues = readLine.trim().split(" ");

				final double longitude = Double.parseDouble(rawValues[3]);
				final double latitude = Double.parseDouble(rawValues[2]);

				// Logging
				if (AdjacencyGraph.enableLogging)
					GraphReader.logger.info(String.format("Trying to add node no. %d\t with latitude %f,\t longitude %f\t to adjacency arrays.%n", nodeId, latitude, longitude));

				adjGraph.addNode(nodeId, longitude, latitude);
			}

			// Parses every edge line String and adds it to the adjacency graph object
			for (int edgeId = 0; edgeId < edgeCount; edgeId++) {
				readLine = bufferedReader.readLine();
				final String[] rawValues = readLine.trim().split(" ");

				final int sourceNode = Integer.parseInt(rawValues[0]);
				final int targetNode = Integer.parseInt(rawValues[1]);
				final int edgeDistance = Integer.parseInt(rawValues[2]);

				// Logging
				if (AdjacencyGraph.enableLogging)
					GraphReader.logger.info(String.format("Trying to add edge no. %d\t with source node no %d,\t target node no %d\t to adjacency arrays.%n", edgeId, sourceNode, targetNode));

				adjGraph.addEdge(edgeId, sourceNode, targetNode, edgeDistance);
			}
		} catch (FileNotFoundException e) {
			System.err.println("Reader could not find specified graph file location:" + file);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IO exception for accessing graph file:" + file);
			e.printStackTrace();
		}
		return adjGraph;
	}
}
