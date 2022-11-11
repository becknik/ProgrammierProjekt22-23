package ProgrammingProject.src.main.java.loader;

import ProgrammingProject.src.main.java.struct.AdjacencyGraph;

import java.io.*;
import java.util.Optional;
import java.util.logging.Logger;

public class GraphReader {
    private static final Logger logger = Logger.getLogger(GraphReader.class.getName());
    public static boolean enableLogging;


    private enum LineType {
        NODE,
        EDGE
    }

    /**
     * Simple struct for wrapping a {@code LineType} with values of a pgrah node or edge
     * @param type - (Optional) Used to determine if node or edge
     * @param values - A array of graph Numbers which follow no specific order...
     *               TODO due to records no supporting inheritance wrapping might be a bad idea
     */
    private record ParsedLine(LineType type, Number[] values) {
        ParsedLine(Number[] values) {   // Maybe useless
            this(values.length == 4 ? LineType.NODE : LineType.EDGE, values);
        }
        @Override
        public String toString() {
            if (type == LineType.NODE) {
                return String.format("%s:\tnode ID: %d,\tidk %d,\tlongitude: %f,\tlatitude: %f%n", type.toString(), values[0], values[1], values[2], values[3]);
            } else {
                return String.format("%s:\tstart node ID: %d,\ttarget node ID: %d,\t idk: %d%n", type.toString(), values[0], values[1], values[2]);
            }
        }
    }

    public static void main(String[] args) {
        enableLogging = true;
        GraphReader.read(new File("stgtregbz.fmi"));
    }

    /**
     * Does the reading of the raw graph file contents into a set of arrays
     * @param file - The file of the raw graph contents
     */
    public static AdjacencyGraph read(final File file) {

        int nodeCount = 0;
        int edgeCount = 0;
        AdjacencyGraph adjGraph;

        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {

            String line = "";

            // Skips the first 5 lines
            for(i=0; i<5; i++){
                bufferedReader.readLine();
            }

            // Reads the number of nodes and edges
            nodeCount = Integer.parseInt(bufferedReader.readLine());
            edgeCount = Integer.parseInt(bufferedReader.readLine());

            adjGraph = new AdjacencyGraph(nodeCount, edgeCount);

            // Loops through every node line and adds node to adjacency graph
            for(int nodeId = 0; nodeId<nodeCount; nodeId++){

                String[] rawValues = line.trim().split(" ");

                long idk = Long.parseLong(rawValues[1]);
                double  latitude = Double.parseDouble(rawValues[2]);
                double  longitude = Double.parseDouble(rawValues[3]);

                adjGraph.addNode(nodeId, longitude, latitude);
            }

            // Loops through every edge line and adds edge to adjacency graph
            for (int edgeId = 0; edgeId<edgeCount; edgeId++){

                String[] rawValues = line.trim().split(" ");

                int startNode = Integer.parseInt(rawValues[0]);
                int targetNode = Integer.parseInt(rawValues[1]);
                int distance = Integer.parseInt(rawValues[2]);

                adjGraph.addEdge(edgeId, startNode, targetNode);
            }


        } catch (FileNotFoundException e) {
            System.err.println("Reader could not find graph file location of " + file);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO exception for accessing graph file" + file);
            e.printStackTrace();
        }
    }

    /**
     * Extracts the input Strings of the raw graph files and parses them to data types. Also figures somehow out which
     * kind of line {@code LineType} the corresponding line is and wrapps all information in a {@code ParsedLine} object
     * @param line - The string to be parsed and typed
     * @return - The {@code ParsedLine} record object
     */
    private static Optional<ParsedLine> prepareBuffer(final String line) {

            String[] rawValues = line.trim().split(" "); // TODO: Maybe use RegEx here?

            if (rawValues.length < 5) {
                if (enableLogging) logger.info("No valid node or edge detected");
                return Optional.empty();
            } else if (rawValues[4].equals("0")) {   // TODO this may cause problems
                int nodeID = Integer.parseInt(rawValues[0]);    // Maybe chance this to long?
                long idk = Long.parseLong(rawValues[1]);
                double  longitude = Double.parseDouble(rawValues[2]);
                double  latitude = Double.parseDouble(rawValues[3]);

                return Optional.of(new ParsedLine(LineType.NODE, new Number[]{nodeID, idk, longitude, latitude}));
            } else {
                int startNode = Integer.parseInt(rawValues[0]);
                int targetNode = Integer.parseInt(rawValues[0]);
                int distance = Integer.parseInt(rawValues[0]);

                return Optional.of(new ParsedLine(LineType.EDGE, new Number[]{startNode, targetNode, distance}));
            }
    }

    /**
     * Outputs the formatted contents of the {@code ParsedLine} into the info log, when the {@code enableLogging}
     * variable is set.
     * @param parsedLine - The contents to be formatted & logged
     */
    private static void logParsedLine(final ParsedLine parsedLine) {
        if (GraphReader.enableLogging) {
            logger.info("Adding" + parsedLine.toString());
        }
    }
    private static  void parseNode(Number[] buffer) {

    }

    private static void parseEdge(Number[] buffer){

    }

}
