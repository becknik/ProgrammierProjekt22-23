package loader;

import struct.AdjacencyGraph;

import java.io.*;
import java.util.logging.Logger;

public class GraphReader {
    private static final Logger logger = Logger.getLogger(GraphReader.class.getName());
    public static boolean enableLogging;

    /**
     * Does the reading of the raw graph file contents into a set of arrays
     * @param file - The file of the raw graph contents
     */
    public static AdjacencyGraph createAdjacencyGraphOf (final File file) {
        int nodeCount;
        int edgeCount;
        AdjacencyGraph adjGraph = null;

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;

            // Skips the first irrelevant 5 lines
            for (int i = 0; i < 5; i++) bufferedReader.readLine();

            // Reads number of nodes and edges
            nodeCount = Integer.parseInt(bufferedReader.readLine());
            edgeCount = Integer.parseInt(bufferedReader.readLine());

            adjGraph = new AdjacencyGraph(nodeCount, edgeCount);

            // Parses and adds every node line String and adds node to adjacency graph object
            for (int nodeId = 0; nodeId < nodeCount; nodeId++) {
                line = bufferedReader.readLine();
                String[] rawValues = line.trim().split(" ");

                double  latitude = Double.parseDouble(rawValues[2]);
                double  longitude = Double.parseDouble(rawValues[3]);

                // Logging
                if (AdjacencyGraph.enableLogging) GraphReader.logger.info(String.format("Trying to add node no. %d\t with latitude %f,\t longitude %f\t to adjacency arrays.%n", nodeId, latitude, longitude));
                
                adjGraph.addNode(nodeId, longitude, latitude);
            }

            // Parses and adds every edge line String and adds edge to adjacency graph
            for (int edgeId = 0; edgeId < edgeCount; edgeId++){
                line = bufferedReader.readLine();
                String[] rawValues = line.trim().split(" ");

                int sourceNode = Integer.parseInt(rawValues[0]);
                int targetNode = Integer.parseInt(rawValues[1]);

                // Logging
                if (AdjacencyGraph.enableLogging) GraphReader.logger.info(String.format("Trying to add edge no. %d\t with source node no %d,\t target node no %d\t to adjacency arrays.%n", edgeId, sourceNode, targetNode));
                
                adjGraph.addEdgeAndCalculateDistance(edgeId, sourceNode, targetNode);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Reader could not find graph file location of " + file);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO exception for accessing graph file" + file);
            e.printStackTrace();
        }
        return adjGraph;
    }
}
